package client.controller;

import client.net.Net;

public class Controller {
    private Net net = new Net();
    public void guessWord(String guess) {
        net.sendMessage(guess);
    }
}
