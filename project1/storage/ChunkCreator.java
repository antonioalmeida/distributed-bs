package storage;

import channel.Message;
import channel.PutChunkMessage;
import utils.Globals;
import utils.Utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class ChunkCreator {

    private ArrayList<Message> chunkList;

    private int nChunks;

    private String fileID;
    private int replicationDegree;
    private int peerID;
    private String version;

    public ChunkCreator(String filePath, int replicationDegree, int peerID, String version) {
        this.replicationDegree = replicationDegree;
        this.peerID = peerID;
        this.version = version;

        File file = new File(filePath);
        this.fileID = Utils.getFileID(file);

        initChunkList(file);
        createChunks(file);
    }

    private void initChunkList(File file) {
        long fileSize = file.length();

        this.nChunks = (int) (fileSize / Globals.CHUNK_MAX_SIZE + 1);
        this.chunkList = new ArrayList<>();
    }

    private void createChunks(File file) {

        try {
            FileInputStream filestream = new FileInputStream(file);
            BufferedInputStream bufferedfile = new BufferedInputStream(filestream);

            for(int chunkIndex = 0; chunkIndex < this.nChunks; chunkIndex++) {
                byte[] buf = new byte[Globals.CHUNK_MAX_SIZE];
                int nr_bytes = bufferedfile.read(buf);
                if(nr_bytes == -1) buf = new byte[0];
                chunkList.add(new PutChunkMessage(this.version, buf, this.fileID, chunkIndex, this.replicationDegree, this.peerID));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Message> getChunkList() {
        return chunkList;
    }
}
