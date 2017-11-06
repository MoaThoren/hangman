package backend.controller;

import backend.model.Person;
import backend.model.WordHandler;
import backend.model.LeaderboardAccess;

public class Controller {
    private WordHandler wordHandler;
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

    public String randomWord() {
        return wordHandler.randomWord();
    }
}
