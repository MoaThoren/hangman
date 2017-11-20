package server.model;

import java.io.IOException;

public class GameHandler {
    private LeaderboardAccess leaderboardAccess;
    private WordHandler wh;
    private String word;
    private String name;
    private String[] wordArray = new String[1];
    private int bodyParts = 0;
    private boolean gameInProgress = false;

    public GameHandler() throws IOException, ClassNotFoundException {
        leaderboardAccess = new LeaderboardAccess();
        wh = new WordHandler();
        word  = wh.randomWord();
        newWord();
    }

    public String newGame(String name) throws IOException {
        leaderboardAccess.addScore(name);
        this.name = name;
        return "The word is " + word.length() + " characters long, meaning you have " + word.length() + " tries.\nPlease enter a letter:";
    }

    public String guessWord(String s) throws IOException {
        StringBuilder ret = new StringBuilder();
        gameInProgress = true;
        int errors = 0;
        for (int i = 0; i < s.length(); i++) {
            boolean correct = false;
            for (int j = 0; j < wordArray.length; j++) {
                if (s.charAt(i) == word.charAt(j)) {
                    wordArray[j] = s.substring(i, i + 1);
                    correct = true;
                }
            }
            if (!correct) {
                errors++;
            }
        }
        if (errors > 0) {         //INCORRECT LETTER(S)
            bodyParts = bodyParts - errors;
            ret.append("Oh no ").append(name).append("! You lost ").append(errors).append(" body part(s)! \nYou now have ").append(bodyParts).append(" body parts left.\n");
        }

        if (wordDone()) {         //WORD CORRECTLY GUESSED
            leaderboardAccess.updateScore(true);
            gameInProgress = false;
            ret.append("Good job ").append(name).append("!").append("You now have ").append(leaderboardAccess.getCurrentScore()).append("The word was ").append(arrToString()).append("!");
            ret.append(newWord());
        } else {                   //SHOWS STATUS
            ret.append(arrToString());
        }

        if (bodyParts <= 0) {     //IF YOU'RE DEAD
            leaderboardAccess.updateScore(false);
            ret.replace(0, ret.length(),"Ouch! You haven't got any body parts left!\nYour new score is " + leaderboardAccess.getCurrentScore() + " Your word was: " + word).append(newWord());
        }

        return ret.toString();
    }

    public String getWord() {
        return word;
    }

    private Boolean wordDone() {
        for (String aWordArray : wordArray) {
            if (aWordArray.equals("-") || bodyParts <= 0) {
                return false;
            }
        }
        return true;
    }

    public void didUserEscapeWord() throws IOException {
        if (gameInProgress)
            leaderboardAccess.updateScore(false);
    }

    private String arrToString() {
        StringBuilder s = new StringBuilder();
        for (String word : wordArray) {
            s.append(word);
        }
        return s.toString();
    }

    private String newWord() {
        word = wh.randomWord().toLowerCase();
        wordArray = new String[word.length()];
        for (int i = 0; i < wordArray.length; i++) {
            wordArray[i] = "-";
        }
        bodyParts = word.length();
        return ("\nThe new word is " + word.length() + " characters long, meaning you have " + word.length() + " tries.");
    }
}
