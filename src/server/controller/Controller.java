package server.controller;

import server.model.GameHandler;
import server.model.Person;
import server.model.WordHandler;
import server.model.LeaderboardAccess;

public class Controller {
    private WordHandler wordHandler;
    private GameHandler gameHandler;
    private LeaderboardAccess leaderboardAccess;

    public Controller() {
        wordHandler = new WordHandler();
        leaderboardAccess = new LeaderboardAccess();
    }

    public void addScore(String name) {
        leaderboardAccess.addScore(name);
    }

    public void updateScore(String name, boolean won) {
        leaderboardAccess.updateScore(name, won);
    }

    public String search(String name) {
        return leaderboardAccess.searchName(name);
    }

    public Person[] returnLeaderboard() {
        return leaderboardAccess.returnAll();
    }

    public String newGame(String name) {
        gameHandler = new GameHandler();
        return gameHandler.newGame(name);
    }

    public String checkString(String letters) {
        return gameHandler.guessWord(letters);
    }


}
