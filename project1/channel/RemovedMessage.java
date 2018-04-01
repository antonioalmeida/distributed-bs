package channel;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class RemovedMessage extends Message {
    /**
     * Instantiates a new Removed message.
     *
     * @param version    the version
     * @param peerID     the peer id
     * @param fileID     the file id
     * @param chunkIndex the chunk index
     */
    public RemovedMessage(String version, int peerID, String fileID, int chunkIndex) {
        super(version, peerID, fileID, null);
        this.type = MessageType.REMOVED;
        this.chunkNr = chunkIndex;
    }
}
