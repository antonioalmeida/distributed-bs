package channel;

import channel.Message;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class StoredMessage extends Message {

    public StoredMessage(String version, int peerID, String fileID, int chunkNr) {
        super(version, peerID, fileID, null);
        this.chunkNr = chunkNr;
        this.type = MessageType.STORED;
    }

}
