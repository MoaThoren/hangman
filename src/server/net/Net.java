package server.net;

import server.controller.Controller;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Net {
    private int PORT_NUMBER = 5555;
    private int LINGER_TIME = 5000;
    private File rootDir = new File("/");
    private ServerSocket serverSocket;
    private PrintWriter output;
    private BufferedReader input;
    private Controller controller = new Controller();

    public void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.exit(1);
        }
        newClientSocket();
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
