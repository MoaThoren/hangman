package client.view;
import client.controller.Controller;
import client.net.Messagehandler;

import java.util.Scanner;

public class View {
    private Controller controller = new Controller();
    private Messagehandler messagehandler = new Messagehandler() {
        @Override
        public void handleMessage(String message) {

        }
    };
    private String host = "";
    public static void main(String[] args) {
        View view = new View();
        System.out.println("*** Welcome to hangman ***");
        if(args[0].trim().isEmpty()) {
            System.out.println("No host IP given, please launch with IP argument.");
            System.exit(1);
        }
        view.gameCommunication(view.host = args[0]);
    }

    private void gameCommunication(String host){
        boolean exit = false;
        Scanner sc = new Scanner(System.in);
        controller.newConnection(host, messagehandler);

        while(!exit){
            String in = sc.next().toLowerCase();
            switch (in){
                case "exit-game":{
                    System.out.println("Good bye!");
                    exit = true;
                }
                default:
                    if(!exit){
                        //PASS ON TO SERVER AND GET RESPONSE
                        controller.guessWord(in);

                    }
            }
        }

    }
}
