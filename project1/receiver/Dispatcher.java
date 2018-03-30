package receiver;

import channel.Message;
import server.Peer;
import server.PeerController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Dispatcher {

    private final int MAX_DISPATCHER_THREADS = 50;
    private int peerID;
    private PeerController controller;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_DISPATCHER_THREADS);

    public Dispatcher(PeerController controller, int peerID) {
        this.peerID = peerID;
        this.controller = controller;
    }

    public void handleMessage(byte[] buf) {
        threadPool.submit(() -> {
            Message message = new Message(buf);

            //Ignore messages from self
            if(message.getPeerID().equals(this.peerID))
                return;

            switch(message.getType()) {
                case PUTCHUNK:
                    controller.handlePutchunkMessage(message);
                    break;
                case STORED:
                    controller.handleStoredMessage(message);
                    break;
                case GETCHUNK:
                    controller.handleGetChunkMessage(message);
                    break;
                case CHUNK:
                    controller.handleChunkMessage(message);
                    break;
                case DELETE:
                    controller.handleDeleteMessage(message);
                    break;
                default:
                    System.out.println("No valid type");
            }
        });
    }
}
