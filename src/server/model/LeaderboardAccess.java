package server.model;

import java.io.*;
import java.util.ArrayList;

public class LeaderboardAccess {
    private ArrayList<Person> leaderboard;

    public LeaderboardAccess() {
        File f = new File("leaderboard.ser");
        if (f.isFile()) {
            long size = f.length();
            if (size != 0) {
                load();
            }
        } else  leaderboard = new ArrayList<>();
    }

    public void addScore(String name) {
        int PersonInt = search(name);
        if(PersonInt == -1) leaderboard.add(new Person(name));
        else    System.out.println("Exists already");
        save();
    }

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

    public String searchName(String name) {
        for (Person person:leaderboard) {
            if(person.name.equalsIgnoreCase(name)) {
                return name + " has " + person.score + " points.";
            }
        }
        return name + " has no score yet :(";
    }

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