package storage;

import channel.Message;

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

    private String baseDirectory;
    private String backupDirectory;

    public FileSystem(long maxStorage, String baseDirectory) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDirectory + "/";

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;

        initDirectories();
    }

    public boolean storeChunk(Message chunk) {
        if(!hasFreeSpace(chunk.getBody().length))
            return false;

        try {
            Path chunkPath = Paths.get(this.backupDirectory + "/"+chunk.getFileID()+"_"+chunk.getChunkIndex());
            System.out.println(chunkPath.toAbsolutePath());

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
