package utils;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class Globals {

    /**
     * The constant CHUNK_MAX_SIZE.
     */
    public static int CHUNK_MAX_SIZE = 64000;

    /**
     * The constant MAX_STORED_WAITING_TIME.
     */
    public static int MAX_STORED_WAITING_TIME = 400;
    /**
     * The constant MAX_CHUNK_WAITING_TIME.
     */
    public static int MAX_CHUNK_WAITING_TIME = 400;

    /**
     * The constant MAX_CHUNK_WAITING_TIME.
     */
    public static int MAX_REMOVED_WAITING_TIME = 400;

    /**
     * The constant MAX_PEER_STORAGE.
     */
    public static long MAX_PEER_STORAGE = 8*10^9; //8MB

    /**
     * The constant MAX_PUTCHUNK_TRIES.
     */
    public static int MAX_PUTCHUNK_TRIES = 5;

    /**
     * The constant MAX_BACKUP_ENH_WAIT_TIME.
     */
    public static int MAX_BACKUP_ENH_WAIT_TIME = 1000;

    /**
     * Time between each peer information local saving (in seconds)
     */
    public static int PEER_CONTROLLER_SAVING_RATE = 3;

    /**
     * The constant PEER_FILESYSTEM_DIR.
     */
    public static String PEER_FILESYSTEM_DIR = "peers";
}
