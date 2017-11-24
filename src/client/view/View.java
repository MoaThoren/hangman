package client.view;

import client.controller.Controller;
import client.net.CommunicationListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

class View {
    private final Controller controller = new Controller();
    private final String WELCOME_MESSAGE = "*** Welcome to hangman ***";
    private final String NO_IP_MESSAGE = "No host IP given, please launch with IP argument. E.g. > java hangman 127.0.0.1";
    private final String EXIT_MESSAGE = "exit game";
    private String SERVER_IP = "127.0.0.1";
    private final CommunicationListener communicationListener = new CommunicationListener() {
        @Override
        public void receivedMsg(String msg) {
            printOut(msg);
        }

        @Override
        public void connected(InetSocketAddress serverAddress) {
            printOut("Connected to: " + serverAddress.toString());
        }

        @Override
        public void disconnected() {
            printOut("Disconnected");
        }
    };

    public static void main(String[] args) {
        View view = new View();
        try {
            view.SERVER_IP = args[0].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No host IP argument, reverting to localhost...");
        }
        view.welcome();
        view.gameCommunication(view.SERVER_IP);
    }

    private void gameCommunication(String host) {
        Scanner sc = new Scanner(System.in);
        try {
            controller.newConnection(host, communicationListener);
        } catch (IOException e) {
            System.out.println("Couldn't create a new connection...\nPlease restart client and try again.");
            System.exit(1);
        }

        gameLoop:
        while (true) {
            String input = sc.nextLine().toLowerCase();
            switch (input) {
                case EXIT_MESSAGE:
                    try {
                        controller.disconnect();
                        System.exit(0);
                    } catch (IOException e) {
                        System.out.println("Failed to disconnect. Shutting down.");
                        System.exit(1);
                    }
                    System.out.println("Exiting game, goodbye!");
                    break gameLoop;
                default:
                    controller.guessWord(input);
                    break;
            }
        }
    }

    private void welcome() {
        if (SERVER_IP.isEmpty()) {
            System.out.println(NO_IP_MESSAGE);
            System.exit(1);
        }
        System.out.println(WELCOME_MESSAGE);
    }

    private void printOut(String message) {
        System.out.println(message);
    }
}
