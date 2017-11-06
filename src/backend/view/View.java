package backend.view;

import backend.controller.Controller;

public class View {
    public static void main(String[] args) {
        Controller controller = new Controller();
        System.out.println(controller.randomWord());
    }
}
