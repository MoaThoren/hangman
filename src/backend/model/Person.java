package backend.model;

import java.io.Serializable;

public class Person implements Serializable {
    private static final long id = 0;
    private String title;
    String name;
    int score;

    Person(String name) {
        this.name = name;
        this.score = 0;
    }

    int won() {
        score += 1;
        return score;
    }

    int lost() {
        score -= 1;
        return score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }
}

