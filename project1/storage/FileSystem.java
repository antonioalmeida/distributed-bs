package storage;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class FileSystem {

    private static final String BACKUP_DIRECTORY = "backup";
    private static final String STORE_DIRECTORY = "store";

    private long maxStorage;

    private long usedStorage;

    private String baseDirectory;
    private String backupDirectory;

    public FileSystem(long maxStorage, String baseDir) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDir + "/";

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;
    }

    public boolean storeChunk(byte[] chunkData, String chunkFileID, int chunkNr) {
       if(usedStorage + chunkData.length > maxStorage) return false;

       try{
         Path chunkPath = Paths.get(FileSystem.STORE_DIRECTORY+"/"+chunkFileID+"_"+chunkNr);
         Files.write(chunkPath, chunkData);
       }
       catch(IOException e){
         e.printStackTrace();
       }

       this.usedStorage += chunkData.length;
       return true;
    }
}
