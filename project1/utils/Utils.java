package utils;

import java.io.File;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {
  //Useful constants
  public static int MAX_RANDOM_WAIT_TIME = 400; //ms
  public static int MAX_PUTCHUNK_TRIES = 5;

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

  public static final int getRandomTime(int max) {
      Random rand = new Random();
      return rand.nextInt(max);
  }
}
