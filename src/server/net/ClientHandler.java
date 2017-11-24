package server.net;

import common.MessageException;
import common.MessageHandler;
import server.controller.Controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

import static common.Constants.MAX_MSG_LENGTH;

class ClientHandler implements Runnable {

    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(MAX_MSG_LENGTH);
    private Controller controller  = new Controller();
    private Net server;
    private String answer;
    private boolean first_run = true;
    final Queue<ByteBuffer> messagesToSend;

    ClientHandler(Net net, SocketChannel clientChannel) {
        messagesToSend  = new ArrayDeque<>();
        server = net;
        this.clientChannel = clientChannel;
    }

    @Override
    public void run() {
        try {
            if(first_run) {
                server.queueMsgToSend(this, controller.newGame(answer));
                first_run = false;
            } else
            server.queueMsgToSend(this, controller.checkString(answer));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void sendMsg(ByteBuffer msg) throws MessageException, IOException {
        clientChannel.write(msg);
        if (msg.hasRemaining()) {
            throw new MessageException("Could not send message");
        }
    }

    void receiveMsg() throws IOException, MessageException {
        msgFromClient.clear();
        int numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1)
            throw new IOException("Client has closed connection.");
        answer = extractMessageFromBuffer();
        System.out.println(answer);
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer() throws MessageException {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return MessageHandler.extractMsg(new String(bytes));
    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }
}
