package backend.view;

import backend.controller.Controller;
import backend.model.Person;

public class View {

    private Controller controller;

    private View() {
        controller = new Controller();
    }

    public static void main(String[] args) {
        View view = new View();
        view.runTest();
    }

    private void runTest() {
        System.out.println("Word of this run: " + controller.randomWord());
        System.out.println("* Test empty search:");
        System.out.println(controller.search("Pelle"));
        controller.addScore("Pelle");
        System.out.println("* Test first search after add:");
        System.out.println(controller.search("Pelle"));
        controller.updateScore("Pelle", true);
        System.out.println("* Test search after victory:");
        System.out.println(controller.search("Pelle"));
        controller.updateScore("Pelle", false);
        System.out.println("* Test search after loss:");
        System.out.println(controller.search("Pelle"));
        controller.updateScore("Pelle", false);
        System.out.println("* Test search after one more loss:");
        System.out.println(controller.search("Pelle"));
        controller.updateScore("Svenne", false);
        System.out.println("* Test search for first time loss search:");
        System.out.println(controller.search("Svenne"));
        System.out.println("* Leaderboard:");
        for (Person person:controller.returnLeaderboard()) {
            System.out.println(person.getName() + " has " + person.getScore() + " points.");
        }
    }
}
