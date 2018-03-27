package storage;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class Chunk {

    private byte[] fileData;

    private String fileID;

    private int chunkIndex;

    private int replicationDegree;

    private int peerID;

    public Chunk(byte[] buf, String fileID, int chunkIndex, int replicationDegree, int peerID) {
        this.fileData = buf;
        this.fileID = fileID;
        this.chunkIndex = chunkIndex;
        this.replicationDegree = replicationDegree;
        this.peerID = peerID;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public int getPeerID() {
        return peerID;
    }

}
