package server.net;

import server.controller.Controller;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Net {
    private int PORT_NUMBER = 8888;
    private int LINGER_TIME = 5000;
    private volatile boolean connected;
    private File rootDir = new File("/");
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Controller controller;

    public Net() {
        controller = new Controller();
    }

    private void serve() {
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.exit(1);
        }
        handleClient();
    }

    private void handleClient() {
        try {
            socket = serverSocket.accept();
            socket.setSoLinger(true, LINGER_TIME);
            output = new PrintWriter(socket.getOutputStream(),true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receive();
        } catch (IOException e) {
            System.exit(1);
        } finally {
            try {
                socket.close();
            } catch(IOException ioEx) {
                System.exit(1);
            }
        }
    }

    private void receive() {
        while(connected) {
            try {
                controller.checkString(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
