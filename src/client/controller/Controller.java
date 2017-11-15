package client.controller;

import client.net.MessageHandler;
import client.net.Net;

import java.io.IOException;

public class Controller {
    private final Net net = new Net();

    public void guessWord(String guess) {
        net.sendMessage(guess);
    }

    public void newConnection(String host, MessageHandler messageHandler) throws IOException {
        net.newConnection(host, messageHandler);
    }

    public void disconnect() throws IOException {
        net.disconnect();
    }
}
