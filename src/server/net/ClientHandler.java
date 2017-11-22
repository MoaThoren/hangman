package server.net;

import server.controller.Controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

/**
 * Handles all communication with one particular chat client.
 */
class ClientHandler implements Runnable {

    private int MAX_MSG_LENGTH = 12;

    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(MAX_MSG_LENGTH);
    private Controller controller  = new Controller();
    private Net server;
    private String answer;

    /**
     * Creates a new instance, which will handle communication with one specific client connected to
     * the specified channel.
     *
     * @param clientChannel The socket to which this handler's client is connected.
     */
    ClientHandler(Net net, SocketChannel clientChannel) {
        server = net;
        this.clientChannel = clientChannel;
    }

    /**
     * Receives and handles one message from the connected client.
     */
    @Override
    public void run() {
        try {
            server.broadcast(controller.checkString(answer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the specified message to the connected client.
     *
     * @param msg The message to send.
     * @throws IOException If failed to send message.
     */
    void sendMsg(ByteBuffer msg) throws MessageException, IOException {
        clientChannel.write(msg);
        if (msg.hasRemaining()) {
            throw new MessageException("Could not send message");
        }
    }

    /**
     * Reads a message from the connected client, then submits a task to the default
     * <code>ForkJoinPool</code>. That task which will handle the received message.
     *
     * @throws IOException If failed to read message
     */
    void recieveMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1)
            throw new IOException("Client has closed connection.");
        answer = extractMessageFromBuffer();
        //msgSplitter.appendRecvdString(answer);
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    /**
     * Closes this instance's client connection.
     *
     * @throws IOException If failed to close connection.
     */
    void disconnectClient() throws IOException {
        clientChannel.close();
    }

    /*
    private static class Message {
        private String msgBody;
        private String receivedString;

        private Message(String receivedString) {
            parse(receivedString);
            this.receivedString = receivedString;
        }

        private void parse(String strToParse) {
            try {
                String[] msgTokens = strToParse.split(Constants.MSG_TYPE_DELIMETER);
                if (hasBody(msgTokens)) {
                    msgBody = msgTokens[Constants.MSG_BODY_INDEX].trim();
                }
            } catch (Throwable throwable) {
                throw new MessageException(throwable);
            }
        }

        private boolean hasBody(String[] msgTokens) {
            return msgTokens.length > 1;
        }
    }
    */
}
