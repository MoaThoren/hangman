package common;

import java.util.StringJoiner;

public class MessageHandler {
    private static final String MSG_LEN_DELIMITER = "###";
    private static final String MSG_RECEIVED_ERROR = "Message was corrupted.";
    private static final int MSG_TYPE_INDEX = 0;
    private static final int MSG_BODY_INDEX = 1;
    private final String WHOLE_MESSAGE_NOT_RECEIVED = "The whole message was not received";

    public static String addHeaderLength(String msg){
        StringJoiner join = new StringJoiner(MSG_LEN_DELIMITER);
        return join.add(Integer.toString(msg.length())).add(msg).toString();
    }

    public static String extractMsg(String received) throws common.MessageException {
        String[] msg = received.split(MSG_LEN_DELIMITER);
        if(msg[MSG_BODY_INDEX].length() != Integer.parseInt(msg[MSG_TYPE_INDEX]))
            throw new MessageException(MSG_RECEIVED_ERROR);
        return msg[MSG_BODY_INDEX];
    }

    public synchronized String appendReceivedString(String receivedMessage) {
        String[] splitAfterLength = receivedMessage.split(MSG_LEN_DELIMITER);
        if(splitAfterLength.length < 2) {
            System.out.println(receivedMessage);
            return WHOLE_MESSAGE_NOT_RECEIVED;
        }
        int statedLength = Integer.parseInt(splitAfterLength[0]);
        String message = splitAfterLength[1];
        if(!measureLength(statedLength, message.length() )) {
            return WHOLE_MESSAGE_NOT_RECEIVED;
        }
        return message;
    }

    private boolean measureLength(int statedLength, int actualLength) {
        return (statedLength == actualLength);
    }


}
