package channel;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class ChunkMessage extends Message {

    public ChunkMessage(byte[] buf, String fileID, int chunkIndex, int replicationDegree, int peerID) {
        super("1.0", peerID, fileID, buf);

        this.type = MessageType.PUTCHUNK;
        this.repDegree = replicationDegree;
        this.chunkNr = chunkIndex;
    }
}
