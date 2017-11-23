package server.net;

import common.MessageException;
import server.controller.Controller;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

class Net {
    private final int PORT_NUMBER = 5555;
    private final int LINGER_TIME = 0;
    private final String EXIT_MESSAGE = "exit game";
    private final String FORCE_EXIT_MESSAGE = "force close game";
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private ServerSocketChannel listeningSocketChannel;
    private Controller controller = new Controller();
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
                if (sendAll)
                    sendAll();
                selector.select();
                System.out.println("Shoutout from line 38, selector.select();");
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid())
                        continue;
                    if (key.isAcceptable()) {
                        System.out.println("Shoutout from line 46, if (key.isAcceptable()) {");
                        acceptClient(key);
                    } else if (key.isReadable()) {
                        System.out.println("Shoutout from line 49, } else if (key.isReadable()) {");
                        recieveMsg(key);
                    } else if (key.isWritable()) {
                        System.out.println("Shoutout from line 52, } else if (key.isWritable()) {");
                        sendMsg(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    void acceptClient(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            ClientHandler handler = new ClientHandler(this, clientChannel);
            clientChannel.register(selector, SelectionKey.OP_WRITE, new Client(handler));
            clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void recieveMsg(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.handler.receiveMsg();
        } catch (IOException clientHasClosedConnection) {
            client.handler.disconnectClient();
            key.cancel();
        }
    }

    void sendMsg(SelectionKey key) throws IOException {
        System.out.println("Shoutout from line 109, void sendMsg(SelectionKey key) throws IOException {");
        Client client = (Client) key.attachment();
        try {
            System.out.println("Shoutout from line 112, Client client = (Client) key.attachment();\n" +
                    "        try {");
            client.sendAll();
            System.out.println("Shoutout from line 114, client.sendAll();");
            key.interestOps(SelectionKey.OP_READ);
            System.out.println("Shoutout from line 116, key.interestOps(SelectionKey.OP_READ);");
        } catch (MessageException couldNotSendAllMessages) {
        } catch (IOException clientHasClosedConnection) {
                            client.handler.disconnectClient();
                            key.cancel();
                        }
    }

    void queueMsgToSend(String msg) {
        ByteBuffer bufferedMsg = ByteBuffer.wrap(msg.getBytes());
        synchronized (messagesToSend) {
            messagesToSend.add(bufferedMsg);
            System.out.println("MESSAGES TO SEND: " + messagesToSend.peek().toString());
        }
        selector.wakeup();
    }

    private class Client {
        private final ClientHandler handler;
        private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();

        private Client(ClientHandler handler) {
            this.handler = handler;
        }

        private void queueMsgToSend(ByteBuffer msg) {
            synchronized (messagesToSend) {
                messagesToSend.add(msg.duplicate());
            }
        }

        private void sendAll() throws IOException, MessageException {
            ByteBuffer msg;
            System.out.println("Shoutout from line 148, private void sendAll() throws IOException, MessageException {");
            synchronized (messagesToSend) {
                System.out.println("Shoutout from line 150, synchronized (messagesToSend) {");
                while ((msg = messagesToSend.peek()) != null) {
                    System.out.println("Shoutout from line 152, while ((msg = messagesToSend.peek()) != null) {");
                    handler.sendMsg(msg);
                    messagesToSend.remove();
                }
            }
        }
    }
}