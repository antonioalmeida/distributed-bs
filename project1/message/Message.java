package message;

import java.io.Serializable;

/**
 * The type Message.
 */
public class Message implements Comparable, Serializable {
    /**
     * The constant CRLF.
     */
    public static String CRLF = "\r\n";

    /**
     * The Type.
     */
    protected MessageType type;
    /**
     * The Version.
     */
    protected String version;
    /**
     * The Peer id.
     */
    protected Integer peerID;
    /**
     * The File id.
     */
    protected String fileID;

    /**
     * The Chunk nr.
     */
    protected Integer chunkNr = null; //Not used on all messages so null until (eventually) overwritten
    /**
     * The Rep degree.
     */
    protected Integer repDegree = null; //Not used on all messages so null until (eventually) overwritten
    /**
     * The Body.
     */
    protected byte[] body = null; //Not used on all messages so null until (eventually) overwritten

    /**
     * Process a message header given as a string.
     *
     * @param str the header
     */
    public void processHeader(String str) {
      String[] header = str.split("\\s+");
      switch(header[0]) {
          case "PUTCHUNK":
              this.type = MessageType.PUTCHUNK;
              this.repDegree = Integer.parseInt(header[5]);
              break;
          case "STORED":
              this.type = MessageType.STORED;
              break;
          case "GETCHUNK":
              this.type = MessageType.GETCHUNK;
              break;
          case "CHUNK":
              this.type = MessageType.CHUNK;
              break;
          case "DELETE":
              this.type = MessageType.DELETE;
              break;
          case "REMOVED":
              this.type = MessageType.REMOVED;
              break;
          default:
              break;
      }

      this.version = header[1];
      this.peerID = Integer.parseInt(header[2]);
      this.fileID = header[3];
      if(header.length > 4)
        this.chunkNr = Integer.parseInt(header[4]);
    }

/*
    //TODO: Remove? Not being used anywhere (I think)
    public Message(String messageStr) {
        //Should split into two elements: 0 = header, 1 = body (if present)
        String[] messageComponents = messageStr.split("\\R\\R", 2);
        if(messageComponents.length > 1)
          this.body = messageComponents[1].getBytes();

        processHeader(messageComponents[0]);
    }
*/
    /**
     * Processes a message given as a byte[] (directly from a DatagramPacket)
     *
     * @param message the message
     * @param size    the message size
     */
    public Message(byte[] message, int size) {
        int headerLength = 0;
        for (int i = 0; i < message.length; ++i) {
            if((char)message[i] == '\r' && (char)message[i+1] == '\n') {
                headerLength = i+4;
                break;
            }
        }

        String header = new String(message, 0, headerLength);
        processHeader(header);

        int bodyLength = size - headerLength;
        this.body = new byte[bodyLength];
        System.arraycopy(message, headerLength, this.body, 0, bodyLength);
    }

    /**
     * Instantiates a new Message.
     *
     * @param version the version
     * @param peerID  the peer id
     * @param fileID  the file id
     * @param body    the body
     */
    public Message(String version, Integer peerID, String fileID, byte[] body) {
        this.version = version;
        this.peerID = peerID;
        this.fileID = fileID;
        this.body = body;
    }

    /**
     * Build message packet byte.
     *
     * @return message as byte[]
     */
    public byte[] buildMessagePacket() {
      StringBuilder result = new StringBuilder();
      switch(this.type){
          case PUTCHUNK:
              result.append("PUTCHUNK ");
              break;
          case STORED:
              result.append("STORED ");
              break;
          case GETCHUNK:
              result.append("GETCHUNK ");
              break;
          case CHUNK:
              result.append("CHUNK ");
              break;
          case DELETE:
              result.append("DELETE ");
              break;
          case REMOVED:
              result.append("REMOVED ");
              break;
          default:
              break;
      }

      result.append(this.version);
      result.append(" ");
      result.append(this.peerID);
      result.append(" ");
      result.append(this.fileID);
      result.append(" ");
      if(this.chunkNr != null) {
        result.append(this.chunkNr);
        result.append(" ");
      }
      if(this.repDegree != null) {
        result.append(this.repDegree);
        result.append(" ");
      }
      result.append(Message.CRLF+Message.CRLF);

      String header = result.toString();
      byte[] headerArr = header.getBytes();

      int bodyLength;
      if(this.body == null) bodyLength = 0;
      else bodyLength = this.body.length;

      byte[] packet = new byte[headerArr.length + bodyLength];
      System.arraycopy(headerArr, 0, packet, 0, headerArr.length);

      if(this.body != null)
          System.arraycopy(this.body, 0, packet, header.length(), bodyLength);

      return packet;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        switch(this.type){
            case PUTCHUNK:
                result.append("PUTCHUNK ");
                break;
            case STORED:
                result.append("STORED ");
                break;
            case GETCHUNK:
                result.append("GETCHUNK ");
                break;
            case CHUNK:
                result.append("CHUNK ");
                break;
            case DELETE:
                result.append("DELETE ");
                break;
            case REMOVED:
                result.append("REMOVED ");
                break;
            default:
                break;
        }

        result.append(this.version);
        result.append(" ");
        result.append(this.peerID);
        result.append(" ");
        result.append(this.fileID);
        result.append(" ");
        if(this.chunkNr != null) {
            result.append(this.chunkNr);
            result.append(" ");
        }
        if(this.repDegree != null) {
            result.append(this.repDegree);
            result.append(" ");
        }
        result.append(Message.CRLF+Message.CRLF);
        if(this.body != null)
            result.append(new String(this.body));

        return result.toString();
    }

    /**
     * Gets the message type.
     *
     * @return the type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the message version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets sender peer id.
     *
     * @return the peer id
     */
    public Integer getPeerID() {
        return peerID;
    }

    /**
     * Gets file id.
     *
     * @return the file id
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * Gets chunk index.
     *
     * @return the chunk index
     */
    public Integer getChunkIndex() {
        return chunkNr;
    }

    /**
     * Gets rep degree.
     *
     * @return the rep degree
     */
    public Integer getRepDegree() {
        return repDegree;
    }

    /**
     * Get body byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getBody() {
        return body;
    }

    /**
      * Sets the message type
      * @param type new type
      */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
      * Sets the replication degree (used for backup)
      * @param degree new rep degree
      */
    public void setRepDegree(int degree) {
        this.repDegree = degree;
    }

    /**
      * Compares Message objects by the chunkNr
      * @param o object to compare to
      * @return difference between chunk numbers
      */
    @Override
    public int compareTo(Object o) {
        return this.chunkNr - ((Message) o).getChunkIndex();
    }
}
