package channel;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class GetChunkMessage extends Message {

    public GetChunkMessage(String version, int peerID, String fileID, int chunkIndex) {
        super(version, peerID, fileID, null);

        this.type = MessageType.GETCHUNK;
        this.chunkNr = chunkIndex;
    }
}
