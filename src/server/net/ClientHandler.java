package server.net;

import common.MessageException;
import common.MessageHandler;
import server.controller.Controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

import static common.Constants.MAX_MSG_LENGTH;

class ClientHandler implements Runnable {

        private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(MAX_MSG_LENGTH);
    private Controller controller  = new Controller();
    private Net server;
    private String answer;

    ClientHandler(Net net, SocketChannel clientChannel) {
        server = net;
        this.clientChannel = clientChannel;
        initGame();
    }

    @Override
    public void run() {
        try {
            System.out.println();
            server.queueMsgToSend(controller.checkString(answer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        try {
            server.queueMsgToSend(controller.newGame("anon"));
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

    void receiveMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1)
            throw new IOException("Client has closed connection.");
        answer = extractMessageFromBuffer();
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }
}
