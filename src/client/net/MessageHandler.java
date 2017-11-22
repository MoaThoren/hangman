package client.net;

import java.util.StringJoiner;

/**
 * Created by enfet on 2017-11-21.
 */
public class MessageHandler {
    private static final String MSG_TYPE_DELIMITER = "##";
    private static final String MSG_LEN_DELIMITER = "###";
    private static final int MSG_TYPE_INDEX = 0;
    private static final int MSG_BODY_INDEX = 0;


    public static String addHeaderLength(String msg){
        StringJoiner join = new StringJoiner(MSG_LEN_DELIMITER);
        join.add(Integer.toString(msg.length()));
        join.add(msg);
        return join.toString();
    }



}
