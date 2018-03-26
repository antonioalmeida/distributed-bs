package utils;

public class Utils {
  //Useful constants
  public static int MAX_RANDOM_WAIT_TIME = 400; //ms
  public static int MAX_PUTCHUNK_TRIES = 5;

  public static final String getFileID(File file) {
    String fileUniqueStr = file.lastModified() + file.getName();

    MessageDigest sha_256 = MessageDigest.getInstance("SHA-256");
    byte[] hashed = sha_256.digest(str.getBytes());

    return DatatypeConverter.printHexBinary(hashed);
  }
}
