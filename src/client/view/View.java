package client.view;
import client.controller.Controller;
import server.model.GameHandler;

import java.util.Scanner;

public class View {
    private Controller controller = new Controller();
    public static void main(String[] args) {
        new View().gameCommunication();
    }

    private void gameCommunication(){
        boolean exit = false;
        Scanner sc = new Scanner(System.in);

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
