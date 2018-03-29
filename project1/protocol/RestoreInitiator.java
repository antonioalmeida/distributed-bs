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

        int chunkAmmout = peer.getController().getBackedUpFileChunkAmount(filePath);
        if(chunkAmmout == 0) {
            System.out.println("Restore Error: error retrieving chunk ammount");
            return;
        }

        ArrayList<Message> getChunkList = new ArrayList<>();
        for(int i = 0; i < chunkAmmout; i++) {
            getChunkList.add(new GetChunkMessage("1.0", peer.getPeerID(), fileID, i));
        }

        sendMessages(getChunkList, MAX_GETCHUNK_DELAY_TIME);
    }


}
