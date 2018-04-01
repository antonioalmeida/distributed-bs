package protocol;

import channel.Message;
import channel.MessageType;
import receiver.Receiver;
import server.PeerController;
import utils.Globals;

/**
 * Created by antonioalmeida on 01/04/2018.
 */
public class SingleBackupInitiator implements Runnable {

    private Message message;
    private Receiver receiver;
    private PeerController controller;

    public SingleBackupInitiator(PeerController controller, Message chunk, int replicationDegree, Receiver receiver) {
        //create putchunk message from chunk
        chunk.setRepDegree(replicationDegree);
        chunk.setType(MessageType.PUTCHUNK);

        message = chunk;
        this.controller = controller;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        System.out.println("RUNNING BITCHES");

        //if chunk degree was satisfied meanwhile, cancel
        if(controller.getBackedUpChunkRepDegree(message) >= message.getRepDegree()) {
            System.out.println("Chunk " + message.getChunkIndex() + " satisfied meanwhile, canceling");
            return;
        }

        // notify controller to listen for this chunk's stored messages
        controller.initBackedUpChunksInfo(message);

        int tries = 0;
        int waitTime = 500;

        do {
            receiver.sendMessage(message);
            tries++; waitTime *= 2;

            if(tries > Globals.MAX_PUTCHUNK_TRIES) {
                System.out.println("Aborting backup, attempt limit reached");
                return;
            }
        } while(!confirmStoredMessage(message, waitTime));
    }

    private boolean confirmStoredMessage(Message message, int waitTime) {
        try {
            //TODO: remove sleeps
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(controller.getBackedUpChunkRepDegree(message) >= message.getRepDegree())
            return true;

        return false;
    }
}
