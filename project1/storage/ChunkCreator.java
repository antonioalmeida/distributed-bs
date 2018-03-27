package storage;

import utils.Globals;
import utils.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class ChunkCreator {

    private ArrayList<Chunk> chunkList;

    private int nChunks;

    private String fileID;
    private int replicationDegree;
    private int peerID;

    public ChunkCreator(String filePath, int replicationDegree, int peerID) {
        this.replicationDegree = replicationDegree;
        this.peerID = peerID;

        File file = new File(filePath);
        this.fileID = Utils.getFileID(file);

        initChunkList(file);
        createChunks(file);
    }

    private void initChunkList(File file) {
        long fileSize = file.length();

        this.nChunks = (int) (fileSize / Globals.CHUNK_MAX_SIZE + 1);
        this.chunkList = new ArrayList<Chunk>();
    }

    private void createChunks(File file) {
        try {
            FileInputStream filestream = new FileInputStream(file);
            BufferedInputStream bufferedfile = new BufferedInputStream(filestream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int chunkIndex = 0; chunkIndex < this.nChunks; chunkIndex++) {
            byte[] buf = new byte[Globals.CHUNK_MAX_SIZE];
            int nr_bytes = bufferedfile.read(buf);
            if(nr_bytes == -1) buf = new byte[0];
            chunkList.add(new Chunk(buf, this.fileID, chunkIndex, this.replicationDegree, this.peerID));
        }
    }

    public ArrayList<Chunk> getChunkList() {
        return chunkList;
    }
}
