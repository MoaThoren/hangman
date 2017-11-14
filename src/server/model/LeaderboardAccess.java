package server.model;

import java.io.*;
import java.util.ArrayList;

/**
 * Class for handling the leaderboard.
 */
public class LeaderboardAccess {

    // ArrayList containing all leaderboard entries.
    private ArrayList<Person> leaderboard;

    /**
     * Constructor which in case of no previous leaderboard creates a new one,
     * if there already existed a previous one it loads that one instead.
     */
    public LeaderboardAccess() {
        File f = new File("leaderboard.ser");
        if (f.isFile()) {
            long size = f.length();
            if (size != 0) {
                load();
            }
        } else  leaderboard = new ArrayList<>();
    }

    /**
     * Call to add a new person to the leaderboard. If person already has played the game
     * and has either won or lost, call <code>updateScore</code> instead.
     * If person already exists, nothing happens.
     * @param name The name of the person to be added to the leaderboard
     */
    public void addScore(String name) {
        int PersonInt = search(name);
        if(PersonInt == -1) leaderboard.add(new Person(name));
        save();
    }

    /**
     * Call to update or add the score of a person who has played the game.
     * @param name Name of the person.
     * @param won <code>Boolean</code> of win status, <code>true</code> for victory and <code>false</code> for loss.
     */
    public void updateScore(String name, boolean won) {
        int personInt = search(name);
        if(personInt != -1)   updateEntry(personInt, won);
        else {
            Person person = new Person(name);
            if(won)    person.won();
            else    person.lost();
            leaderboard.add(person);
        }
        save();
    }

    private void updateEntry(int personInt, boolean won) {
        Person person = new Person(leaderboard.get(personInt).name);
        if(won) person.score = leaderboard.get(personInt).won();
        else person.score = leaderboard.get(personInt).lost();
        leaderboard.remove(personInt);
        leaderboard.add(person);
    }

    private int search(String name) {
        int pos = 0;
        for (Person person:leaderboard) {
            if(person.name.equalsIgnoreCase(name)) {
                return pos;
            }
            ++pos;
        }
        return -1;
    }

    /**
     * Search for a person in the leaderboard and get a <code>String</code> with the current status in return.
     * @param name The name of the person, case-insensitive.
     * @return A <code>String</code> either stating the score in the format "<code>name</code> has <code>score</code> points."
     * Or "<code>name</code> has no score yet :(" if name isn't found.
     */
    public String searchName(String name) {
        for (Person person:leaderboard) {
            if(person.name.equalsIgnoreCase(name)) {
                return name + " has " + person.score + " points.";
            }
        }
        return name + " has no score yet :(";
    }

    /**
     * Call to get the entire leaderboard returned.
     * @return An <code>Array</code> containing objects of the <code>Person</code> class.
     */
    public Person[] returnAll() {
        return leaderboard.toArray(new Person[0]);
    }

    private void save() {
        try {
            FileOutputStream fos = new FileOutputStream("leaderboard.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(leaderboard);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try {
            FileInputStream fis = new FileInputStream("leaderboard.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            leaderboard = (ArrayList<Person>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}