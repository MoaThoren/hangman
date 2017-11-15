package server.net;

import server.controller.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Class for running the net layer of the server.
 * Handles the sockets for the client and server on the server side, and then lies waiting for connections.
 * Each transmission is served in its own thread and is handled using the <code>checkString</code> method in
 * the controller.
 */
class Net {
    private final int PORT_NUMBER = 5555;
    private final int LINGER_TIME = 0;
    private final String EXIT_MESSAGE = "exit game";
    private final String FORCE_EXIT_MESSAGE = "force close game";
    private boolean connected = false;
    private Controller controller = null;
    private ServerSocket serverSocket;
    private PrintWriter output;
    private BufferedReader input;

    private Net() {
        try {
            controller = new Controller();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Couldn't create controller. System shutting down.");
            System.exit(1);
        }
    }

    /**
     * Creates/assigns a socket for the server and then opens a new client socket for each incoming transmission.
     *
     * @param args Optional arguments.
     */
    public static void main(String[] args) {
        Net net = new Net();
        net.newServerSocket();
        net.newClientSocket();
    }

    private void newServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.PORT_NUMBER);
            this.serverSocket.setSoTimeout(LINGER_TIME);
        } catch (IOException e) {
            System.out.println("Couldn't create a new server socket, exiting...");
            System.exit(1);
        }
        System.out.println("Server socket created.");
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
            } catch (SocketTimeoutException e) {
                System.out.println("Connection timed out, no client connected within " + LINGER_TIME/1000 + " s.");
                System.exit(0);
            } catch (IOException e1) {
                System.out.println("Something went wrong during connection startup, please restart the server.");
                System.exit(1);
            } finally {
                closeClientSocket();
            }
        }

        private void waitForConnection() throws IOException {
            System.out.println("Server waiting on client socket acceptance");
            clientSocket = serverSocket.accept();
        }

        private void connected() throws SocketException {
            connected = true;
            System.out.println("Client connection found.");
            clientSocket.setSoLinger(true, LINGER_TIME);
            newClientSocket();
        }

        private void setupCommunication() throws IOException {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        private void receive() {
            send("Please enter your name to begin:\n");
            try {
                String reply = input.readLine();
                if (!checkForExit(reply)) {
                    send(controller.newGame(reply));
                }
            } catch (IOException e) {
                System.out.println("Couldn't get a reply, client probably disconnected...\nRestarting connection...");
                checkForExit(FORCE_EXIT_MESSAGE);
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't load leaderboard, try deleting the \"leaderboard.ser\" file and try again.");
                System.exit(1);
            }
            System.out.println("Starting to wait for messages...");
            while (connected) {
                System.out.println("Checking for new messages...");
                String reply;
                try {
                    reply = input.readLine();
                } catch (IOException e) {
                    System.out.println("Couldn't get a reply, client probably disconnected...\nRestarting connection...");
                    checkForExit(FORCE_EXIT_MESSAGE);
                    break;
                }
                if (checkForExit(reply))
                    break;
                try {
                    send(controller.checkString(reply));
                } catch (IOException e) {
                    System.out.println("Didn't manage to read from or write to leaderboard.");
                    System.exit(1);
                }
            }
        }

        private void send(String reply) {
            System.out.println("Sending reply...");
            output.println(reply);
            System.out.println("The reply \"" + reply + "\" is sent!");
        }

        private boolean checkForExit(String reply) {
            if (reply.equalsIgnoreCase(EXIT_MESSAGE) || reply.equalsIgnoreCase(FORCE_EXIT_MESSAGE)) {
                System.out.println("Client requested to " + reply);
                connected = false;
                try {
                    controller.didUserEscapeWord();
                } catch (IOException e) {
                    System.out.println("Didn't manage to save the cheating users score, consider yourself lucky...");
                }
                closeClientSocket();
                newClientSocket();
                return true;
            }
            return false;
        }

        private void closeClientSocket() {
            try {
                clientSocket.close();
            } catch (IOException ioEx) {
                System.out.println("Couldn't close the client socket, shutting down.");
                System.exit(1);
            }
        }
    }
}