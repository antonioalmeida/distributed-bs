package channel;

import java.lang.StringBuilder;

enum MessageType {
        PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
    }

public class Message {
    public static String CRLF = "\r\n";

    public MessageType type;
    public String version;
    public Integer peerID;
    public String fileID;
    //Not used on all messages so null until (eventually) overwritten
    public Integer chunkNr = null;
    public Integer repDegree = null;
    public byte[] body = null;

    //Processes a message's header line
    public void processHeader(String str) {
      String[] header = str.split("\\s+");
      switch(header[0]){
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

    //Processes a message given as a String
    public Message(String messageStr) {
        //Should split into two elements: 0 = header, 1 = body (if present)
        String[] messageComponents = messageStr.split("\\R\\R", 2);
        if(messageComponents.length > 1)
          this.body = messageComponents[1].getBytes();

        processHeader(messageComponents[0]);
    }

    //Processes a message given as a byte[] (directly from a DatagramPacket)
    public Message(byte[] message) {
      int headerLength = 0;
      for (int i = 0; i < message.length; ++i) {
            if((char)message[i] == '\r') { //TODO: Parse better
              headerLength = i+1;
              break;
            }
      }

      String header = new String(message, 0, headerLength);
      processHeader(header);
      this.body = new byte[message.length - headerLength];
      System.arraycopy(message, headerLength, this.body, 0, message.length-headerLength);
    }

    public Message(String version, Integer peerID, String fileID, byte[] body) {
        this.version = version;
        this.peerID = peerID;
        this.fileID = fileID;
        this.body = body;
    }

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
      byte[] packet = new byte[headerArr.length+this.body.length];
      System.arraycopy(headerArr, 0, packet, 0, headerArr.length);
      System.arraycopy(this.body, 0, packet, header.length(), this.body.length);

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
}
