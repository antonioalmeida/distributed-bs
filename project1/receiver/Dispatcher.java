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

    private final int MAX_PROCESSING_THREADS = 10;
    private int peerID;
    private PeerController controller;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PROCESSING_THREADS);

    public Dispatcher(PeerController controller, int peerID) {
        this.peerID = peerID;
        this.controller = controller;
    }

    public void handleMessage(byte[] buf) {
        threadPool.submit(() -> {
            Message message = new Message(buf);
            //Ignore messages from self
            if(message.peerID.equals(this.peerID)) {
                System.out.println("Same peer ID");
                return;
            }

            switch(message.getType()) {
                case PUTCHUNK:
                    controller.handlePutchunkMessage(message);
                    break;
                default:
                    System.out.println("No valid type");
            }
        });
    }

}
