package protocol;

import channel.Channel;
import channel.DeleteMessage;
import channel.Message;
import server.Peer;
import utils.Utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class DeleteInitiator extends ProtocolInitiator {

    private String filePath;

    public DeleteInitiator(Peer peer, String filePath, Channel channel) {
        super(peer, channel);
        this.filePath = filePath;
    }

    @Override
    public void run() {
        File file = new File(filePath);
        String fileID = Utils.getFileID(file);

        Message message = new DeleteMessage("1.0", peer.getPeerID(), fileID);
        channel.sendMessage(message);
    }
}
