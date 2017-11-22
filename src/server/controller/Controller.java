package server.controller;

import server.model.GameHandler;
import server.model.LeaderboardAccess;
import server.model.Person;

import java.io.IOException;

public class Controller {
    private GameHandler gameHandler;
    private LeaderboardAccess leaderboardAccess;

    public Controller() {
        try {
            leaderboardAccess = new LeaderboardAccess();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String newGame(String name) throws IOException, ClassNotFoundException {
        gameHandler = new GameHandler();
        return gameHandler.newGame(name);
    }

    public String checkString(String letters) throws IOException {
        return gameHandler.guessWord(letters);
    }

    public Person[] returnLeaderboard() {
        return leaderboardAccess.returnAll();
    }

    public void didUserEscapeWord() throws IOException {
        if (gameHandler != null)
            gameHandler.didUserEscapeWord();
    }

    public String getWord() {
        if(gameHandler != null)
            return gameHandler.getWord();
        return "";
    }

}
