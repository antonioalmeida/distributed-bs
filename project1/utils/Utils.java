package utils;

import java.io.File;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Utils.
 */
public class Utils {

    /**
     * Gets file id.
     *
     * @param file the file
     * @return the file id
     */
    public static final String getFileID(File file) {
        String fileUniqueStr = file.lastModified() + file.getName();

        MessageDigest sha_256 = null;
        try {
            sha_256 = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e) {
            System.out.println("SHA-256 not found");
            System.exit(-1);
        }
        byte[] hashed = sha_256.digest(fileUniqueStr.getBytes());

        return DatatypeConverter.printHexBinary(hashed);
    }

    /**
     * Gets random time.
     *
     * @param max the max
     * @return the random time
     */
    public static final int getRandomTime(int max) {
        return ThreadLocalRandom.current().nextInt(max+1);
    }

    /**
     * Gets random between.
     *
     * @param min the min
     * @param max the max
     * @return the random between
     */
    public static final int getRandomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
