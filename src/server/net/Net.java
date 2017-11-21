package server.net;

import server.controller.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Class for running the net layer of the server.
 * Handles the sockets for the client and server on the server side, and then lies waiting for connections.
 * Each transmission is served in its own thread and is handled using the <code>checkString</code> method in
 * the controller.
 */
class Net {
    private final int PORT_NUMBER = 5555;
    private final int LINGER_TIME = 0;
    private final String EXIT_MESSAGE = "exit game";
    private final String FORCE_EXIT_MESSAGE = "force close game";
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;
    private Controller controller;

    Net() {
        try {
            controller = new Controller();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            selector = Selector.open();
            listeningSocketChannel = ServerSocketChannel.open();
            listeningSocketChannel.configureBlocking(false);
            listeningSocketChannel.bind(new InetSocketAddress(PORT_NUMBER));
            listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
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
            client.handler.recvMsg();
        } catch (IOException clientHasClosedConnection) {
            client.handler.disconnectClient();
            key.cancel();
        }
    }

    void sendMsg(SelectionKey key) {
        Client client = (Client) key.attachment();
        try {
            client.sendAll();
            key.interestOps(SelectionKey.OP_READ);
        } catch (/*Message*/Exception couldNotSendAllMessages) {
        }/* catch (IOException clientHasClosedConnection) {
                            client.handler.disconnectClient();
                            key.cancel();
                        }*/
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

        private void sendAll() throws IOException {
            ByteBuffer msg;
            synchronized (messagesToSend) {
                while ((msg = messagesToSend.peek()) != null) {
                    handler.sendMsg(msg);
                    messagesToSend.remove();
                }
            }
        }
    }
}