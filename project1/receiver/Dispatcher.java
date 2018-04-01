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

            switch(message.getType()) {
                case PUTCHUNK:
                    threadPool.submit(() -> {
                        controller.handlePutchunkMessage(message);
                    });
                    break;
                case STORED:
                    threadPool.submit(() -> {
                        controller.handleStoredMessage(message);
                    });
                    break;
                case GETCHUNK:
                    int randomWait = Utils.getRandomBetween(0, Globals.MAX_CHUNK_WAITING_TIME);
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
