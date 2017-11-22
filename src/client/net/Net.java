package client.net;


import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;


public class Net {
    private boolean connected;
    private SocketChannel socketChannel;
    private Selector selector;
    private InetSocketAddress serverAddress;
    private MessageHandler messageSplitter = new MessageHandler();
    private int port = 5555;
    private CommunicationHandler outputhandler;
    private final Queue<String> messagesWaitingToBeSent = new ArrayDeque<>();
    private volatile boolean messageReady = true;
    private final ByteBuffer receivedFromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private String ERROR_IN_COMMUNICATION = "Connection has been lost, please try again later"
    private String DISCONNECT_MESSAGE = "EXIT_GAME";

    @Override
    public void run() {
        try {
            newConnection();
            startSelector();

            while (connected || !messagesWaitingToBeSent.isEmpty()) {
                if (messageReady) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    messageReady = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        finishConnection();
                    } else if (key.isReadable()) {
                        messageFromServer(key);
                    } else if (key.isWritable()) {
                        sendMessageToServer(key);
                    }
                }
            }

        }
        catch(Exception e){
            System.err.prinln(ERROR_IN_COMMUNICATION);
        }
        try {
            disconnectFromServer();
        }
        catch (IOException ex) {
            System.err.println(ERROR_IN_COMMUNICATION);
        }
    }

    private void newConnection(String host) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();

    }

    public void sendDisconnectMessage() throws IOException {
        connected = false;
        sendMessage(DISCONNECT_MESSAGE);
    }

    public void disconnectFromServer() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }

    public void startSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void startConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }

    private void finishConnection (SelectionKey ) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                notifyConnectionDone(remoteAddress);
        }
        catch(IOException usingDefaultinsteadofRemote) {
            notifyConnectionDone(serverAddress);
        }
    }

    private void sendMessage(String message) {
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
            while((message = messagesWaitingToBeSent.peek()) != null ) {
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
        messageSplitter.appendReceivedString(stringFromServer);
        while (messageSplitter.hasNext()) {
            String msg = messageSplitter.nextMessage();
            notifyMessageReceived(messageSplitter.bodyOf(msg));

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
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.connected(connectedAddress);
                }
            });
        }
    }

    private void notifyDisconnectionDone() {
        Executor pool = ForkJoinPool.commonPool();
        for (CommunicationListener listener : listeners) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.disconnected();
                }
            });
        }
    }

    private void notifyMsgReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        for (CommunicationListener listener : listeners) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.recvdMsg(msg);
                }
            });
        }
    }

}