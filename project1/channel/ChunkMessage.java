package channel;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class ChunkMessage extends Message {
    public ChunkMessage(String version, Integer peerId, String fileID, int chunkIndex, byte[] body) {
        super(version, peerId, fileID, body);

        this.chunkNr = chunkIndex;
        this.type = MessageType.CHUNK;
    }
}
