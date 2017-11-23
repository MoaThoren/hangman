package common;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;

public class MessageHandler {
    private static final String MSG_LEN_DELIMITER = "###";
    private static final String MSG_RECEIVED_ERROR = "Message was corrupted.";
    private static final int MSG_TYPE_INDEX = 0;
    private static final int MSG_BODY_INDEX = 1;
    private final Queue<String> messageQueue = new ArrayDeque<>();

    public static String addHeaderLength(String msg){
        StringJoiner join = new StringJoiner(MSG_LEN_DELIMITER);
        return join.add(Integer.toString(msg.length())).add(msg).toString();
    }

    public static String extractMsg(String received) throws MessageException {
        String[] msg = received.split(MSG_LEN_DELIMITER);
        if(msg[MSG_BODY_INDEX].length() != Integer.parseInt(msg[MSG_TYPE_INDEX]))
            throw new MessageException(MSG_RECEIVED_ERROR);
        return msg[MSG_BODY_INDEX];
    }

    public synchronized boolean hasNext() {
        return !messageQueue.isEmpty();
    }

    public synchronized String nextMessage() {
        return messageQueue.poll();
    }

}
