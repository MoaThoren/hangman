package client.net;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Net {
    private final int PORT_NUMBER = 5555;
    private final int TIME_OUT = 10000;
    private final String DISCONNECT_MESSAGE = "exit game";
    private Socket socket;
    private PrintWriter sentMessage;
    private BufferedReader receivedMessage;
    private String prevHost;
    private MessageHandler prevMessageHandler;
    private boolean connected = false;

    public void newConnection(String host, MessageHandler messageHandler) throws IOException {
        prevHost = host;
        prevMessageHandler = messageHandler;
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, PORT_NUMBER), TIME_OUT);
        connected = true;
        sentMessage = new PrintWriter(socket.getOutputStream(), true);
        receivedMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Listener listener = new Listener(messageHandler);
        new Thread(listener).start();
    }

    public void disconnect() throws IOException {
        connected = false;
        sendMessage(DISCONNECT_MESSAGE);
        receivedMessage.close();
        sentMessage.close();
        socket.close();
        socket = null;

    }

    public void sendMessage(String message) {
        sentMessage.println(message);
    }

    private class Listener implements Runnable {
        private final MessageHandler messageHandler;

        private Listener(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }


        @Override
        public void run() {
            while (connected) {
                try {
                    String received = receivedMessage.readLine();
                    if (!(received == null))
                        messageHandler.handleMessage(received);
                } catch (IOException e) {
                    messageHandler.handleMessage("No connection to server, server probably went down.\n" +
                            "Will try to reconnect in " + TIME_OUT / 1000 + " s.");
                    try {
                        Thread.sleep(TIME_OUT);
                        messageHandler.handleMessage("TRYING TO RECONNECT...");
                        newConnection(prevHost, prevMessageHandler);
                        messageHandler.handleMessage("RECONNECTED!");
                    } catch (IOException e1) {
                        messageHandler.handleMessage("Couldn't establish new connection, shutting down...");
                        System.exit(1);
                    } catch (InterruptedException e1) {
                        messageHandler.handleMessage("Couldn't sleep before attempting new connection, shutting down...");
                        System.exit(1);
                    }
                }
            }
        }
    }
}