package message;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class DeleteMessage extends Message {

    /**
     * Instantiates a new Delete message.
     *
     * @param version the version
     * @param peerID  the peer id
     * @param fileID  the file id
     */
    public DeleteMessage(String version, int peerID, String fileID) {
        super(version, peerID, fileID, null);
        this.type = MessageType.DELETE;
    }
}
