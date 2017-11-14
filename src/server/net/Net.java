package server.net;

import server.controller.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class for running the net layer of the server.
 * Handles the sockets for the client and server on the server side, and then lies waiting for connections.
 * Each transmission is served in its own thread and is handled using the <code>checkString</code> method in
 * the controller.
 */
public class Net {
    private final int PORT_NUMBER = 5555;
    private final int LINGER_TIME = 5000;
    private boolean connected = false;
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
        System.out.println("Server socket created.");
        net.newClientSocket();
    }

    private void newClientSocket() {
        new NetThread().run();
    }

    private class NetThread extends Thread {
        private Socket clientSocket;

        public void run() {
            try {
                waitForConnection();
                connected();
                setupCommunication();
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

        private void waitForConnection() throws IOException {
            System.out.println("Server going into waiting on client socket acceptance");
            clientSocket = serverSocket.accept();
        }

        private void connected() throws SocketException {
            connected = true;
            System.out.println("Client connection found.");
            clientSocket.setSoLinger(true, LINGER_TIME);
        }

        private void setupCommunication() throws IOException {
            output = new PrintWriter(clientSocket.getOutputStream(),true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        private void receive() {
            send("Please enter your name to begin:\n");
            try {
                String reply = controller.checkString(input.readLine());
                if(!checkForExit(reply)) {
                    send(controller.newGame(reply));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Starting to wait for messages...");
            while(connected) {
                System.out.println("Checking for new messages...");
                String reply = "";
                try {
                    reply = controller.checkString(input.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(checkForExit(reply))
                    break;
                send(reply);
            }
        }

        private boolean checkForExit(String reply) {
            if(reply.equalsIgnoreCase("exit-game")) {
                System.out.println("Client requested to exit the game.");
                connected = false;
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                newClientSocket();
                return true;
            }
            return false;
        }

        private void send(String reply) {
            System.out.println("Sending reply...");
            output.println(reply);
            System.out.println("The reply \"" + reply + "\" is sent!");
        }
    }
}