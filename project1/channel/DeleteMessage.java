package channel;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class DeleteMessage extends Message {

    public DeleteMessage(String version, int peerID, String fileID) {
        super(version, peerID, fileID, null);
        this.type = MessageType.DELETE;
    }
}
