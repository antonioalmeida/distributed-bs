package protocol;

import receiver.Channel;
import message.GetChunkMessage;
import message.Message;
import peer.Peer;

import java.net.ServerSocket;
import java.util.ArrayList;

public class RestoreInitiator extends ProtocolInitiator {

    // TODO: remove this
    private static final int MAX_GETCHUNK_DELAY_TIME = 0;

    private String filePath;

    /**
     * Instantiates a new Restore initiator.
     *
     * @param peer     the peer
     * @param filePath the file path
     * @param channel  the message
     */
    public RestoreInitiator(Peer peer, String filePath, Channel channel) {
        super(peer, channel);
        this.filePath = filePath;
    }

    /**
      * Method to be executed when thread starts running. Executes the restore protocol as an initiator peer
      */
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
