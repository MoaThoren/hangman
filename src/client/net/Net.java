package client.net;


import common.Constants;
import common.MessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
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
    private CommunicationListener communicationListener;
    private final Queue<ByteBuffer> messagesWaitingToBeSent = new ArrayDeque<>();
    private volatile boolean messageReady = false;
    private final ByteBuffer receivedFromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private String ERROR_IN_COMMUNICATION = "Connection has been lost, please try again later";
    private String DISCONNECT_MESSAGE = "EXIT_GAME";

    @Override
    public void run() {
        try {
            startConnection();
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
                        messageFromServer();
                    } else if (key.isWritable()) {
                        sendMessageToServer(key);
                    }
                }
            }
            disconnectFromServer();
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println(ERROR_IN_COMMUNICATION);
        }
    }

    public void newConnection(String host, CommunicationListener communicationListener) {
        this.communicationListener = communicationListener;
        serverAddress = new InetSocketAddress(host, PORT_NUMBER);
        new Thread(this).start();

    }

    public void sendDisconnectMessage() throws IOException {
        connected = false;
        sendMessage(DISCONNECT_MESSAGE);
    }

    private void disconnectFromServer() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
        notifyDisconnectionDone();
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    private void startConnection() throws IOException {
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

    public void sendMessage(String message) {
        String messageWithLengthHeader = MessageHandler.addHeaderLength(message);
        synchronized (messagesWaitingToBeSent) {
            messagesWaitingToBeSent.add(ByteBuffer.wrap(messageWithLengthHeader.getBytes()));
        }
        messageReady = true;
        selector.wakeup();
    }

    private void sendMessageToServer (SelectionKey key) throws IOException {
        ByteBuffer message;
        synchronized (messagesWaitingToBeSent) {
            while((message = messagesWaitingToBeSent.peek()) != null) {
                socketChannel.write(message);
                if(message.hasRemaining()){
                    return;
                }
                messagesWaitingToBeSent.remove(message);
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void messageFromServer() throws IOException {
        receivedFromServer.clear();
        int readBytes = socketChannel.read(receivedFromServer);
        if (readBytes == -1) {
            throw new IOException(ERROR_IN_COMMUNICATION);
        }
        String stringFromServer = extractMessageFromBuffer();
        String extractedMessage = messageHandler.appendReceivedString(stringFromServer);
        notifyMessageReceived(extractedMessage);
    }


    private String extractMessageFromBuffer() {
        receivedFromServer.flip();
        byte[] bytes = new byte[receivedFromServer.remaining()];
        receivedFromServer.get(bytes);
        return new String(bytes);
    }

    private void notifyConnectionDone(InetSocketAddress connectedAddress) {
        Executor pool = ForkJoinPool.commonPool();
        pool.execute(() -> communicationListener.connected(connectedAddress));
    }

    private void notifyDisconnectionDone() {
        Executor pool = ForkJoinPool.commonPool();
        pool.execute(communicationListener::disconnected);
    }

    private void notifyMessageReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        pool.execute(() -> communicationListener.receivedMsg(msg));
    }

}