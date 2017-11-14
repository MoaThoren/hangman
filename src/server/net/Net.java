package server.net;

import server.controller.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class for running the net layer of the server.
 * Handles the sockets for the client and server on the server side, and then lies waiting for connections.
 * Each transmission is served in its own thread and is handled using the <code>checkString</code> method in
 * the controller.
 */
public class Net {
    private final int PORT_NUMBER = 5555;
    private final int LINGER_TIME = 5000;
    private Controller controller = new Controller();
    private ServerSocket serverSocket;
    private PrintWriter output;
    private BufferedReader input;

    /**
     * Creates/assigns a socket for the server and then opens a new client socket for each incoming transmission.
     * @param args Optional arguments.
     */
    public static void main(String[] args) {
        Net net = new Net();
        try {
            net.serverSocket = new ServerSocket(net.PORT_NUMBER);
        } catch (IOException e) {
            System.exit(1);
        }
        net.newClientSocket();
    }

    private void newClientSocket() {
        new NetThread().run();
    }

    private class NetThread extends Thread {
        private Socket clientSocket;

        public void run() {
            try {
                clientSocket = serverSocket.accept();
                newClientSocket();
                clientSocket.setSoLinger(true, LINGER_TIME);
                output = new PrintWriter(clientSocket.getOutputStream(),true);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                receive();
            } catch (IOException e) {
                System.exit(1);
            } finally {
                try {
                    clientSocket.close();
                } catch(IOException ioEx) {
                    System.exit(1);
                }
            }
        }

        private void receive() {
            while(true) {
                try {
                    String reply = controller.checkString(input.readLine());
                    if(reply.equalsIgnoreCase("exit-game")) break;
                    send(reply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void send(String reply) {
            output.println(reply);
        }
    }
}