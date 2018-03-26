package channel;

import java.lang.StringBuilder;

enum MessageType {
        PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
    }

public class Message {
    public static String CRLF = "\\r\\n";

    public MessageType type;
    public String version;
    public Integer peerID;
    public String fileID;
    //Not used on all messages so null until (eventually) overwritten
    public Integer chunkNr = null;
    public Integer repDegree = null;
    public byte[] body = null;

    public Message(String messageStr) {
        //Should split into two elements: 0 = header, 1 = body (if present)
        String[] messageComponents = messageStr.split("\\R\\R", 2);
        if(messageComponents.length > 1)
          this.body = messageComponents[1].getBytes();

        String[] header = messageComponents[0].split("\\s+");
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
      result.append(this.body);

      return result.toString();
    }
}
