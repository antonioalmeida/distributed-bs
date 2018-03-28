package channel;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class ChunkMessage extends Message {

    /*
    public ChunkMessage(Chunk chunk, int degree) {
        super("1.0", chunk.getPeerID(), chunk.getFileID(), chunk.getFileData());

        this.type = MessageType.PUTCHUNK;
        this.repDegree = degree;
        this.chunkNr = chunk.getChunkIndex();
    }
    */

    public ChunkMessage(byte[] buf, String fileID, int chunkIndex, int replicationDegree, int peerID) {
        super("1.0", peerID, fileID, buf);

        this.type = MessageType.PUTCHUNK;
        this.repDegree = replicationDegree;
        this.chunkNr = chunkIndex;
    }

    public int getChunkIndex() {
       return this.chunkNr;
    }
}
