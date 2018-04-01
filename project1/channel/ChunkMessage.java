package channel;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class ChunkMessage extends Message {
    /**
     * Instantiates a new Chunk message.
     *
     * @param version    the version
     * @param peerId     the peer id
     * @param fileID     the file id
     * @param chunkIndex the chunk index
     * @param body       the body
     */
    public ChunkMessage(String version, Integer peerId, String fileID, int chunkIndex, byte[] body) {
        super(version, peerId, fileID, body);

        this.chunkNr = chunkIndex;
        this.type = MessageType.CHUNK;
    }
}
