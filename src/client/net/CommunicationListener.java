package client.net;

import java.net.InetSocketAddress;

public interface CommunicationListener {
    void receivedMsg(String msg);

    void connected(InetSocketAddress serverAddress);

    void disconnected();
}
