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
private int port = 5555;
private static final int TIME_OUT_ONE_MINUTE = 60000;
private String disconnectMessage = "exit-game"


 public void newConnection(String host) throws IOException {
     socket = new Socket();
     socket.connect(new InetSocketAddress(host, port), TIME_OUT_ONE_MINUTE);
     boolean autoflush = true;
     sentMessage = new PrintWriter(socket.getOutputStream(), autoflush);
     receivedMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     new Thread(new Listener)
 }

 public void disconnect() throws IOException {
     sendMessage(disconnectMessage);
     socket.close();
     socket = null;

 }

 public void sendMessage(String message) {
     sentMessage.println(message);
 }
    private class Listener()implements Runnable {

        private Listener() {

        }


    }



}
