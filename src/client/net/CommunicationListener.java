package client.net;

import java.net.InetSocketAddress;

public interface CommunicationListener {
    void recvdMsg(String msg);

    void connected(InetSocketAddress serverAddress);

    void disconnected();
}
