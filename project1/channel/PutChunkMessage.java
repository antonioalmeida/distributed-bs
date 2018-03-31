package channel;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class PutChunkMessage extends Message {

    public PutChunkMessage(String version, byte[] buf, String fileID, int chunkIndex, int replicationDegree, int peerID) {
        super(version, peerID, fileID, buf);

        this.type = MessageType.PUTCHUNK;
        this.repDegree = replicationDegree;
        this.chunkNr = chunkIndex;
    }
}
