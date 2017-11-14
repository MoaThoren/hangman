package client.net;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Net {
    private Socket socket;
    private PrintWriter sentMessage;
    private BufferedReader receivedMessage;
    private boolean connected = false;
    private final int PORT_NUMBER = 5555;
    private final int TIME_OUT_ONE_MINUTE = 60000;
    private final String DISCONNECT_MESSAGE = "exit-game";


    public void newConnection(String host, Messagehandler messageHandler) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, PORT_NUMBER), TIME_OUT_ONE_MINUTE);
        connected = true;
        boolean autoflush = true;
        sentMessage = new PrintWriter(socket.getOutputStream(), autoflush);
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
        private Messagehandler messageHandler;

        private Listener(Messagehandler messageHandler) {
            this.messageHandler = messageHandler;
        }


        @Override
        public void run() {
                while(connected) {
                    try {
                        String recieved = receivedMessage.readLine();
                        if(!(recieved == null))
                            messageHandler.handleMessage(recieved);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}