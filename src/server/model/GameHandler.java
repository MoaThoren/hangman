package server.model;

import java.io.IOException;

public class GameHandler {
    private final LeaderboardAccess leaderboardAccess = new LeaderboardAccess();
    private final WordHandler wh = new WordHandler();
    private String word = wh.randomWord();
    private String[] wordArray = new String[1];
    private int bodyParts = 0;
    private boolean gameInProgress = false;

    public GameHandler() {
        newWord();
    }

    public String newGame(String name) throws IOException {
        leaderboardAccess.addScore(name);
        return "The word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")";
    }

    public String guessWord(String s) throws IOException {
        String ret = "";
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
            ret = ret + ("Oh no! You lost " + errors + " body part(s)! \nYou now have " + bodyParts + " body parts left.");
        }

        if (wordDone()) {         //WORD CORRECTLY GUESSED
            leaderboardAccess.updateScore(true);
            gameInProgress = false;
            ret = ret + ("Good job! The word was " + arrToString() + "! \n" + leaderboardAccess.getCurrentScore());
            ret = ret + newWord();
        } else {                   //SHOWS STATUS
            ret = ret + (arrToString());
        }

        if (bodyParts <= 0) {     //IF YOU'RE DEAD
            leaderboardAccess.updateScore(false);
            ret = ("Ouch! You haven't got any body parts left!\nYour new score is " + leaderboardAccess.getCurrentScore());
            ret = ret + newWord();
        }

        return ret;
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
        return ("\nThe new word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")");
    }
}
