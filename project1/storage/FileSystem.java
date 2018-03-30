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
    private static final String RESTORE_DIRECTORY = "restore";

    private long maxStorage;

    private long usedStorage;

    private Peer peer;

    private String baseDirectory;
    private String backupDirectory;
    private String restoreDirectory;

    public FileSystem(Peer peer, long maxStorage, String baseDirectory) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDirectory + "/";
        this.peer = peer;

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;
        this.restoreDirectory = baseDirectory + RESTORE_DIRECTORY;

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

    public synchronized void saveFile(String filePath, byte[] body) {
        Path path = Paths.get(this.restoreDirectory + "/" + filePath);
        System.out.println("FULL PATH: " + path.toAbsolutePath());

        try {
            if(!Files.exists(path))
                Files.createFile(path);

            Files.write(path, body);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("File " + filePath + " restored successfully");
    }

    public synchronized void deleteChunk(String fileID, int chunkIndex, boolean updateMaxStorage) {
        Path path = Paths.get(this.backupDirectory+"/"+fileID+"_"+chunkIndex);
        long savedStorage = 0;

        try {
            if(Files.exists(path)) {
                savedStorage = Files.size(path);
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        usedStorage -= savedStorage;
        if(updateMaxStorage)
            maxStorage -= savedStorage;
    }

    private void initDirectories() {
        Path backupPath = Paths.get(this.backupDirectory);
        Path restorePath = Paths.get(this.restoreDirectory);

        try {
            if (!Files.exists(backupPath))
                Files.createDirectories(backupPath);

            if (!Files.exists(restorePath))
                Files.createDirectories(restorePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasFreeSpace(long size) {
        return usedStorage + size > maxStorage;
    }

    public long getUsedStorage() {
        return usedStorage;
    }
}
