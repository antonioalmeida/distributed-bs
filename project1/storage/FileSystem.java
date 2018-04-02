package storage;

import message.ChunkMessage;
import message.Message;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class FileSystem implements Serializable {

    private static final String BACKUP_DIRECTORY = "backup";
    private static final String RESTORE_DIRECTORY = "restore";

    private long maxStorage;

    private long usedStorage;

    private String peerVersion;
    private int peerID;

    private String baseDirectory;
    private String backupDirectory;
    private String restoreDirectory;

    /**
     *
     * @param peerVersion
     * @param peerID
     * @param maxStorage
     * @param baseDirectory
     */
    public FileSystem(String peerVersion, int peerID, long maxStorage, String baseDirectory) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDirectory + "/";

        this.peerVersion = peerVersion;
        this.peerID = peerID;

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;
        this.restoreDirectory = baseDirectory + RESTORE_DIRECTORY;

        initDirectories();
    }

    /**
     * Store chunk boolean.
     *
     * @param chunk the chunk
     * @return the boolean
     */
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

    /**
     * Retrieve chunk message.
     *
     * @param fileID     the file id
     * @param chunkIndex the chunk index
     * @return the message
     */
    public synchronized Message retrieveChunk(String fileID, int chunkIndex) {
        Path chunkPath = Paths.get(this.backupDirectory + "/"+fileID+"_"+chunkIndex);

        byte[] buf = null;
        try {
            buf = Files.readAllBytes(chunkPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message chunk = new ChunkMessage(peerVersion, peerID, fileID, chunkIndex, buf);
        return chunk;
    }

    /**
     * Save file.
     *
     * @param filePath the file path
     * @param body     the body
     */
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

    /**
     * Delete chunk.
     *
     * @param fileID           the file id
     * @param chunkIndex       the chunk index
     * @param updateMaxStorage the update max storage
     */
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

    /**
     * Gets used storage.
     *
     * @return the used storage
     */
    public long getUsedStorage() {
        return usedStorage;
    }

    /**
     * Gets max storage.
     *
     * @return the max storage
     */
    public long getMaxStorage() {
        return maxStorage;
    }
}
