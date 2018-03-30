package channel;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class RemovedMessage extends Message {
    public RemovedMessage(String version, int peerID, String fileID, int chunkIndex) {
        super(version, peerID, fileID, null);
        this.type = MessageType.REMOVED;
        this.chunkNr = chunkIndex;
    }
}
