package client.controller;

import client.net.CommunicationListener;
import client.net.Net;

import java.io.IOException;

public class Controller {
    private Net net = new Net();

    public void guessWord(String guess) {
        net.sendMessage(guess);
    }

    public void newConnection(String host, CommunicationListener communicationListener) throws IOException {
        net.newConnection(host, communicationListener);
    }

    public void disconnect() throws IOException {
        net.sendDisconnectMessage();
    }
}
