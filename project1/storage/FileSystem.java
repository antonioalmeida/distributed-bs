package storage;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class FileSystem {

    private static final String BACKUP_DIRECTORY = "backup";

    private long maxStorage;

    private long usedStorage;

    private String baseDirectory;
    private String backupDirectory;

    public FileSystem(long maxStorage, String baseDir) {
        this.maxStorage = maxStorage;
        this.baseDirectory = baseDir + "/";

        this.backupDirectory = baseDirectory + BACKUP_DIRECTORY;
    }

    public boolean storeChunk() {
       //TODO: this
    }
}
