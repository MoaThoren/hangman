package client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by enfet on 2017-11-21.
 */
public class OldNet {
    private final int PORT_NUMBER = 5555;
    private final int TIME_OUT = 10000;
    private final String DISCONNECT_MESSAGE = "exit game";
    private Socket socket;
    private PrintWriter sentMessage;
    private BufferedReader receivedMessage;
    private String prevHost;
    private MessagePrinter prevMessagePrinter;
    private boolean connected = false;


    public void newConnection(String host, MessagePrinter messagePrinter) throws IOException {
        prevHost = host;
        prevMessagePrinter = messagePrinter;
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, PORT_NUMBER), TIME_OUT);
        connected = true;
        sentMessage = new PrintWriter(socket.getOutputStream(), true);
        receivedMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Listener listener = new Listener(messagePrinter);
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
        private final MessagePrinter messagePrinter;

        private Listener(MessagePrinter messagePrinter) {
            this.messagePrinter = messagePrinter;
        }


        @Override
        public void run() {
            while (connected) {
                try {
                    String received = receivedMessage.readLine();
                    if (!(received == null))
                        messagePrinter.handleMessage(received);
                } catch (IOException e) {
                    messagePrinter.handleMessage("No connection to server, server probably went down.\n" +
                            "Will try to reconnect in " + TIME_OUT / 1000 + " s.");
                    try {
                        Thread.sleep(TIME_OUT);
                        messagePrinter.handleMessage("TRYING TO RECONNECT...");
                        newConnection(prevHost, prevMessagePrinter);
                        messagePrinter.handleMessage("RECONNECTED!");
                    } catch (IOException e1) {
                        messagePrinter.handleMessage("Couldn't establish new connection, shutting down...");
                        System.exit(1);
                    } catch (InterruptedException e1) {
                        messagePrinter.handleMessage("Couldn't sleep before attempting new connection, shutting down...");
                        System.exit(1);
                    }
                }
            }
        }
    }
}
