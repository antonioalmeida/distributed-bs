package storage;

import channel.ChunkMessage;
import utils.Globals;
import utils.Utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class ChunkCreator {

    private ArrayList<ChunkMessage> chunkList;

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
        this.chunkList = new ArrayList<ChunkMessage>();
    }

    private void createChunks(File file) {

        try {
            FileInputStream filestream = new FileInputStream(file);
            BufferedInputStream bufferedfile = new BufferedInputStream(filestream);

            for(int chunkIndex = 0; chunkIndex < this.nChunks; chunkIndex++) {
                byte[] buf = new byte[Globals.CHUNK_MAX_SIZE];
                int nr_bytes = bufferedfile.read(buf);
                if(nr_bytes == -1) buf = new byte[0];
                chunkList.add(new ChunkMessage(buf, this.fileID, chunkIndex, this.replicationDegree, this.peerID));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ChunkMessage> getChunkList() {
        return chunkList;
    }
}
