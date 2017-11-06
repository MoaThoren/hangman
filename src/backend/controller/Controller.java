package backend.controller;

import backend.model.WordHandler;

public class Controller {
    public String randomWord() {
        WordHandler wordHandler = new WordHandler();
        return wordHandler.randomWord();
    }
}
