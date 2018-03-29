package storage;

import channel.ChunkMessage;
import channel.Message;
import server.Peer;
import utils.Globals;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class FileSystem {

    private static final String BACKUP_DIRECTORY = "backup";

    private long maxStorage;

    private long usedStorage;

    private Peer peer;

    private String baseDirectory;
    private String backupDirectory;

    public FileSystem(Peer peer, long maxStorage, String baseDirectory) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDirectory + "/";
        this.peer = peer;

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;

        initDirectories();
    }

    public synchronized boolean storeChunk(Message chunk) {
        if(!hasFreeSpace(chunk.getBody().length))
            return false;

        try {
            Path chunkPath = Paths.get(this.backupDirectory + "/"+chunk.getFileID()+"_"+chunk.getChunkIndex());

            if(!Files.exists(chunkPath))
                Files.createFile(chunkPath);

            Files.write(chunkPath, chunk.getBody());
        }
        catch(IOException e){
            e.printStackTrace();
        }

        this.usedStorage += chunk.getBody().length;
        return true;
    }

    public synchronized Message retrieveChunk(String fileID, int chunkIndex) {
        Path chunkPath = Paths.get(this.backupDirectory + "/"+fileID+"_"+chunkIndex);

        byte[] buf = new byte[Globals.CHUNK_MAX_SIZE];
        try {
            buf = Files.readAllBytes(chunkPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message chunk = new ChunkMessage("1.0", peer.getPeerID(), fileID, chunkIndex, buf);
        return chunk;
    }

    private void initDirectories() {
        Path backupPath = Paths.get(this.backupDirectory);

        if(!Files.exists(backupPath))
            try {
                Files.createDirectories(backupPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean hasFreeSpace(long size) {
        return usedStorage + size > maxStorage;
    }
}
