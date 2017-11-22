package client.net;


import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public class Net {
    private boolean connected;
    private SocketChannel socketChannel;
    private Selector selector;
    private InetSocketAddress serverAddress;
    private int port = 5555;
    private CommunicationHandler outputhandler;


    private void newConnection(String host) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();

    }

    public void disconnectFromServer() throws IOException {
        connected = false;
        sendMsg();
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }

    public void startSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void startConnection() throws IOException {
        SocketChannel = SocketChannel.open();
        SocketChannel.configureBlocking(false);
        SocketChannel.connect(serverAddress);
        connected = true;
    }

    private void finishConnection (SelectionKey ) {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                notifyConnectionDone(remoteAddress);
        }
        catch(IOException usingDefaultinsteadofRemote) {
            notifyConnectionDone(serverAddress);
        }
    }

}