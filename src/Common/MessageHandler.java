package Common;

import client.net.Constants;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;

/**
 * Created by enfet on 2017-11-21.
 */
public class MessageHandler {
    private static final String MSG_LEN_DELIMITER = "###";
    private static final int MSG_TYPE_INDEX = 0;
    private static final int MSG_BODY_INDEX = 1;
    private final Queue<String> messageQueue = new ArrayDeque<>();
    private final String WHOLE_MESSAGE_NOT_RECEIVED = "The whole message was not received";

    public static String addHeaderLength(String msg){
        StringJoiner join = new StringJoiner(MSG_LEN_DELIMITER);
        join.add(Integer.toString(msg.length()));
        join.add(msg);
        return join.toString();
    }

    public synchronized boolean hasNext() {
        return !messageQueue.isEmpty();
    }

    public synchronized String nextMessage() {
        return messageQueue.poll();
    }

    public synchronized String appendReceivedString(String receivedMessage) {
        String[] splitAfterLength = receivedMessage.split(MSG_LEN_DELIMITER);
        if(splitAfterLength.length < 2) {
            return WHOLE_MESSAGE_NOT_RECEIVED;
        }
        int statedLength = Integer.parseInt(splitAfterLength[0]);
        String
        if(!measureLength(statedLength, )) {

        }

    }

    private boolean measureLength(int statedLength, int actualLength) {
        return (statedLength == actualLength);
    }


}
