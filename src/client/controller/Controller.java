package client.controller;

import client.net.Messagehandler;
import client.net.Net;

import java.io.IOException;

public class Controller {
    private Net net = new Net();
    public void guessWord(String guess) {
        net.sendMessage(guess);
    }

    public void newConnection(String host, Messagehandler messagehandler) throws IOException {
        net.newConnection(host, messagehandler);
    }

    public void disconnect() throws IOException {
        net.disconnect();
    }
}
