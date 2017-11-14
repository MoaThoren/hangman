package client.controller;

import client.net.Messagehandler;
import client.net.Net;

public class Controller {
    private Net net = new Net();
    public void guessWord(String guess) {
        net.sendMessage(guess);
    }

    public void newConnection(String host, Messagehandler messagehandler) {
        net.newConnection(host, messagehandler);
    }
}
