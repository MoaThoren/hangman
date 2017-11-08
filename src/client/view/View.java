package client.view;
import server.model.GameHandler;

import java.util.Scanner;

public class View {
    public static void main(String[] args) {
        gameCommunication();
    }

    public static void gameCommunication(){
        boolean exit = false;
        Scanner sc = new Scanner(System.in);

        //TEMP
        GameHandler gh = new GameHandler();

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
                        System.out.println(gh.guessWord(in));

                    }
            }
        }

    }
}
