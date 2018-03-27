package channel;

import storage.Chunk;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class ChunkMessage extends Message {

    private Chunk chunk;

    public ChunkMessage(Chunk chunk, int degree) {
        super("1.0", chunk.getPeerID(), chunk.getFileID(), chunk.getFileData());

        this.type = MessageType.PUTCHUNK;
        this.chunk = chunk;
        this.repDegree = degree;
        this.chunkNr = chunk.getChunkIndex();
    }
}
