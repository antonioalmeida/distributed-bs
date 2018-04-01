package message;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class StoredMessage extends Message {

    /**
     * Instantiates a new Stored message.
     *
     * @param version the version
     * @param peerID  the peer id
     * @param fileID  the file id
     * @param chunkNr the chunk nr
     */
    public StoredMessage(String version, int peerID, String fileID, int chunkNr) {
        super(version, peerID, fileID, null);
        this.chunkNr = chunkNr;
        this.type = MessageType.STORED;
    }

}
