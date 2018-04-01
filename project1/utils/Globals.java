package utils;

/**
 * Created by antonioalmeida on 26/03/2018.
 */
public class Globals {

    public static int CHUNK_MAX_SIZE = 64000;

    public static int MAX_STORED_WAITING_TIME = 400;
    public static int MAX_CHUNK_WAITING_TIME = 400;

    public static long MAX_PEER_STORAGE = 8*10^9; //8MB

    public static int MAX_PUTCHUNK_TRIES = 5;

    public static int MAX_BACKUP_ENH_WAIT_TIME = 1000;

    public static String PEER_FILESYSTEM_DIR = "peers";
}
