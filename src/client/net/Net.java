package client.net;


import common.MessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;


public class Net implements Runnable {
    private boolean connected;
    private SocketChannel socketChannel;
    private Selector selector;
    private InetSocketAddress serverAddress;
    private MessageHandler messageHandler = new MessageHandler();
    private int PORT_NUMBER = 5555;
    //private CommunicationHandler outputhandler;
    private final List<CommunicationListener> listeners = new ArrayList<>();
    private final Queue<String> messagesWaitingToBeSent = new ArrayDeque<>();
    private volatile boolean messageReady = true;
    private final ByteBuffer receivedFromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private String ERROR_IN_COMMUNICATION = "Connection has been lost, please try again later";
    private String DISCONNECT_MESSAGE = "EXIT_GAME";
    private String HOST_IP = "127.0.0.1";

    @Override
    public void run() {
        try {
            newConnection(HOST_IP);
            initSelector();

            while (connected || !messagesWaitingToBeSent.isEmpty()) {
                if (messageReady) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    messageReady = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        finishConnection(key);
                    } else if (key.isReadable()) {
                        messageFromServer(key);
                    } else if (key.isWritable()) {
                        sendMessageToServer(key);
                    }
                }
            }
            disconnectFromServer();
        }
        catch(Exception e){
            System.err.println(ERROR_IN_COMMUNICATION);
        }
    }

    private void newConnection(String host) {
        serverAddress = new InetSocketAddress(host, PORT_NUMBER);
        ForkJoinPool.commonPool().execute(this);

    }

    // Denna vet jag inte om ni tänkt skulle vara i disconnectFromServer() rad 77 eller inte så lämnar den så här.
    public void sendDisconnectMessage() throws IOException {
        connected = false;
        sendMessage(DISCONNECT_MESSAGE);
    }

    private void disconnectFromServer() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    //Används inte
    public void startConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }

    private void finishConnection (SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                notifyConnectionDone(remoteAddress);
        }
        catch(IOException usingDefaultInsteadOfRemote) {
            notifyConnectionDone(serverAddress);
        }
    }

    private void sendMessage(String message) {
        String messageWithLengthHeader = MessageHandler.addHeaderLength(message);
        synchronized (messagesWaitingToBeSent) {
            messagesWaitingToBeSent.add(messageWithLengthHeader);
        }
        messageReady = true;
        selector.wakeup();
    }

    private void sendMessageToServer (SelectionKey key) throws IOException {
        ByteBuffer message;
        synchronized (messagesWaitingToBeSent) {
            message = ByteBuffer.wrap(messagesWaitingToBeSent.peek().getBytes());
            while(message != null) {
                socketChannel.write(message);
                if(message.hasRemaining()){
                    return;
                }
                messagesWaitingToBeSent.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void messageFromServer(SelectionKey key) throws IOException {
        receivedFromServer.clear();
        int readBytes = socketChannel.read(receivedFromServer);
        if (readBytes == -1) {
            throw new IOException(ERROR_IN_COMMUNICATION);
        }
        String stringFromServer = extractMessageFromBuffer();
        //MessageHandler stuff som behövs eller som ni får göra om.
        messageHandler.appendReceivedString(stringFromServer);
        while (messageHandler.hasNext()) {
            String msg = messageHandler.nextMessage();
            notifyMessageReceived(messageHandler.bodyOf(msg));

        }
    }


    private String extractMessageFromBuffer() {
        receivedFromServer.flip();
        byte[] bytes = new byte[receivedFromServer.remaining()];
        receivedFromServer.get(bytes);
        return new String(bytes);
    }

    private void notifyConnectionDone(InetSocketAddress connectedAddress) {
        Executor pool = ForkJoinPool.commonPool();
        for (CommunicationListener listener : listeners) {
            pool.execute(() -> listener.connected(connectedAddress));
        }
    }

    private void notifyDisconnectionDone() {
        Executor pool = ForkJoinPool.commonPool();
        for (CommunicationListener listener : listeners) {
            pool.execute(listener::disconnected);
        }
    }

    private void notifyMessageReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        for (CommunicationListener listener : listeners) {
            pool.execute(() -> listener.recvdMsg(msg));
        }
    }

}