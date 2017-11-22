package client.controller;

import client.net.MessagePrinter;
import client.net.Net;

import java.io.IOException;

public class Controller {
    private final Net net = new Net();

    public void guessWord(String guess) {
        net.sendMessage(guess);
    }

    public void newConnection(String host, MessagePrinter messagePrinter) throws IOException {
        net.newConnection(host, messagePrinter);
    }

    public void disconnect() throws IOException {
        net.disconnect();
    }
}
