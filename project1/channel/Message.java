package channel;

import java.net.DatagramPacket;

/**
 * Created by antonioalmeida on 18/03/2018.
 *
 */

enum MessageType {
        PUTCHUNK, GETCHUNK, CHUNK //TODO: add others
    }

public class Message {
    // CR - 13 | LF - 10
    private static String CRLF = Character.toString((char) 13) + Character.toString((char) 10);

    private MessageType type;

    private String version;

    private int peerID;

    private String fileID;

    private int chunkNo;

    private int replicationDegree;

    public static Message parseString(String message) {
        //TODO: parse string and return Message object
        return new Message();
    }

}


