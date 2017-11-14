package client.view;
import client.controller.Controller;
import client.net.Messagehandler;

import java.io.IOException;
import java.util.Scanner;

public class View {
    private Controller controller = new Controller();
    private String WELCOME_MESSAGE = "*** Welcome to hangman ***";
    private String NO_IP_MESSAGE = "No host IP given, please launch with IP argument. E.g. > java hangman 127.0.0.1";
    private String SERVER_IP = "127.0.0.1";
    private Messagehandler messagehandler = this::printOut;

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

    private void gameCommunication(String host){
        Scanner sc = new Scanner(System.in);
        try {
            controller.newConnection(host, messagehandler);
            System.out.println("Connected to: " + host);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gameLoop: while(true){
            String input = sc.next().toLowerCase();
            switch (input){
                default:
                    controller.guessWord(input);
                    break;
                case "exit-game":
                    try {
                        controller.disconnect();
                    } catch (IOException e) {
                        System.out.println("Failed to disconnect.");
                        e.printStackTrace();
                    }
                    System.out.println("Good bye!");
                    break gameLoop;
            }
        }
    }

    private void welcome() {
        if(SERVER_IP.isEmpty()) {
            System.out.println(NO_IP_MESSAGE);
            System.exit(1);
        }
        System.out.println(WELCOME_MESSAGE);
    }

    private void printOut(String message) {
        System.out.println(message);
    }
}
