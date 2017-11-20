package server.model;

import java.io.Serializable;

/**
 * Serializable class which has a variable field for the <code>name</code>, stored as a <code>String</code>,
 * and <code>score</code>, stored as an <code>int</code>. Has getters for these values as well as two functions
 * for handling the score:<p>
 * <code>won</code>: Increases the score.
 * <p>
 * <code>lost</code>: Lowers the score.
 */
public class Person implements Serializable {
    private static final long id = 0;
    final String name;
    int score;
    private String title;

    Person(String name) {
        this.name = name;
        this.score = 0;
    }

    int won() {
        score += 3;
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

