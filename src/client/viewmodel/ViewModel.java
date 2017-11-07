package client.viewmodel;

import client.TempServer;

import java.util.Scanner;

public class ViewModel {
    public static void input(){
        boolean exit = false;
        Scanner sc = new Scanner(System.in);

        //TEMP
        TempServer ts = new TempServer();

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
                        System.out.println(ts.guessWord(in));

                    }
            }
        }

    }
}
