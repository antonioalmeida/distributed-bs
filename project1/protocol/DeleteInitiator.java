package protocol;

import receiver.Channel;
import message.DeleteMessage;
import message.Message;
import peer.Peer;
import utils.Utils;

import java.io.File;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class DeleteInitiator extends ProtocolInitiator {

    private String filePath;

    /**
     * Instantiates a new Delete initiator.
     *
     * @param peer     the peer
     * @param filePath the file path
     * @param channel  the message
     */
    public DeleteInitiator(Peer peer, String filePath, Channel channel) {
        super(peer, channel);
        this.filePath = filePath;
    }

    @Override
    public void run() {
        File file = new File(filePath);
        String fileID = Utils.getFileID(file);

        Message message = new DeleteMessage(peer.getProtocolVersion(), peer.getPeerID(), fileID);
        channel.sendMessage(message);
    }
}
