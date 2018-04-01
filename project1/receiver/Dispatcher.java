package receiver;

import channel.Message;
import server.Peer;
import server.PeerController;
import utils.Globals;
import utils.Utils;

import java.util.concurrent.*;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Dispatcher {

    private final int MAX_DISPATCHER_THREADS = 50;
    private int peerID;
    private PeerController controller;

    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(MAX_DISPATCHER_THREADS);

    public Dispatcher(PeerController controller, int peerID) {
        this.peerID = peerID;
        this.controller = controller;
    }

    public void handleMessage(byte[] buf, int size) {
            Message message = new Message(buf, size);

            //Ignore messages from self
            if(message.getPeerID().equals(this.peerID))
                return;

            int randomWait;

            switch(message.getType()) {
                case PUTCHUNK:
                    //TODO: make a proper verification
                    if(message.getVersion() != "1.0") {
                        controller.listenForStoreReplies(message.getFileID(), message.getChunkIndex());
                        randomWait = Utils.getRandomBetween(0, Globals.MAX_BACKUP_ENH_WAIT_TIME);
                    }
                    else
                        randomWait = 0;

                    threadPool.schedule(() -> {
                        controller.handlePutchunkMessage(message);
                    }, randomWait, TimeUnit.MILLISECONDS);
                    break;
                case STORED:
                    threadPool.submit(() -> {
                        controller.handleStoredMessage(message);
                    });
                    break;
                case GETCHUNK:
                    randomWait = Utils.getRandomBetween(0, Globals.MAX_CHUNK_WAITING_TIME);
                    threadPool.schedule(() -> {
                        controller.handleGetChunkMessage(message);
                    }, randomWait, TimeUnit.MILLISECONDS);
                    break;
                case CHUNK:
                    threadPool.submit(() -> {
                        controller.handleChunkMessage(message);
                    });
                    break;
                case DELETE:
                    threadPool.submit(() -> {
                        controller.handleDeleteMessage(message);
                    });
                    break;
                case REMOVED:
                    threadPool.submit(() -> {
                        controller.handleRemovedMessage(message);
                    });
                    break;
                default:
                    System.out.println("No valid type");
            }
    }
}
