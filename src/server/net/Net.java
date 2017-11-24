package server.net;

import common.MessageException;
import common.MessageHandler;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import static common.Constants.PORT_NUMBER;

class Net {
    private final int LINGER_TIME = 0;
    private final String EXIT_MESSAGE = "exit game";
    private final String FORCE_EXIT_MESSAGE = "force close game";
    private final String NEW_USER_MESSAGE = "Please enter your name:\n";
    private ServerSocketChannel listeningSocketChannel;
    private Boolean sendAll = false;
    private Selector selector;

    public static void main(String[] args) {
        new Net().run();
    }

    public void run() {
        try {
            selector = Selector.open();
            initRecieve();

            while (true) {
                if (sendAll) {
                    sendAll();
                    sendAll = false;
                }
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid())
                        continue;
                    if (key.isAcceptable()) {
                        acceptClient(key);
                    } else if (key.isReadable()) {
                        recieveMsg(key);
                    } else if (key.isWritable()) {
                        sendMsg(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessageException e) {
            System.out.println(e.getMessage());
        }
    }

    void initRecieve() {
        try {
            listeningSocketChannel = ServerSocketChannel.open();
            listeningSocketChannel.configureBlocking(false);
            listeningSocketChannel.bind(new InetSocketAddress(PORT_NUMBER));
            listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendAll() {
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    void acceptClient(SelectionKey key) throws MessageException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            ClientHandler handler = new ClientHandler(this, clientChannel);
            clientChannel.register(selector, SelectionKey.OP_WRITE, new Client(handler));
            clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
            askForName(handler);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void askForName(ClientHandler handler) throws IOException, MessageException {
        handler.sendMsg(ByteBuffer.wrap(MessageHandler.addHeaderLength(NEW_USER_MESSAGE).getBytes()));
    }

    void recieveMsg(SelectionKey key) throws IOException, MessageException {
        Client client = (Client) key.attachment();
        try {
            client.handler.receiveMsg();
        } catch (IOException clientHasClosedConnection) {
            client.handler.disconnectClient();
            key.cancel();
        }
    }

    void sendMsg(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.sendAll();
            key.interestOps(SelectionKey.OP_READ);
        } catch (MessageException couldNotSendAllMessages) {
        } catch (IOException clientHasClosedConnection) {
                            client.handler.disconnectClient();
                            key.cancel();
                        }
    }

    void queueMsgToSend(ClientHandler clientHandler, String msg) {
        ByteBuffer bufferedMsg = ByteBuffer.wrap(MessageHandler.addHeaderLength(msg).getBytes());
        synchronized (clientHandler.messagesToSend) {
            clientHandler.messagesToSend.add(bufferedMsg);
        }
        sendAll = true;
        selector.wakeup();
    }
    

    private class Client {
        private final ClientHandler handler;

        private Client(ClientHandler handler) {
            this.handler = handler;
        }

        private void sendAll() throws IOException, MessageException {
            ByteBuffer msg;
            synchronized (handler.messagesToSend) {
                while ((msg = handler.messagesToSend.peek()) != null) {
                    handler.sendMsg(msg);
                    handler.messagesToSend.remove();
                }
            }
        }
    }

}


