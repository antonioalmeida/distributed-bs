package protocol;

import channel.Channel;
import channel.GetChunkMessage;
import channel.Message;
import server.Peer;

import java.util.ArrayList;

/**
 * Created by antonioalmeida on 28/03/2018.
 */
public class RestoreInitiator extends ProtocolInitiator {

    private static final int MAX_GETCHUNK_DELAY_TIME = 100;

    private String filePath;

    public RestoreInitiator(Peer peer, String filePath, Channel channel) {
        super(peer, channel);
        this.filePath = filePath;
    }

    @Override
    public void run() {
        String fileID = peer.getController().getBackedUpFileID(filePath);
        if(fileID == null) {
            System.out.println("Restore Error: file " + filePath + " is not backed up.");
            return;
        }

        int chunkAmount = peer.getController().getBackedUpFileChunkAmount(filePath);
        if(chunkAmount == 0) {
            System.out.println("Restore Error: error retrieving chunk ammount.");
            return;
        }

        ArrayList<Message> getChunkList = new ArrayList<>();
        for(int i = 0; i < chunkAmount; i++) {
            getChunkList.add(new GetChunkMessage(peer.getProtocolVersion(), peer.getPeerID(), fileID, i));
        }

        peer.getController().addToRestoringFiles(fileID, filePath, chunkAmount);
        System.out.println("Restoring file with " + chunkAmount + " chunks");

        sendMessages(getChunkList, MAX_GETCHUNK_DELAY_TIME);
    }

}
