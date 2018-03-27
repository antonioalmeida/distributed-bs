package receiver;

import channel.Message;
import Peer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Dispatcher {

    private final int MAX_PROCESSING_THREADS = 10;
    private Peer peer;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PROCESSING_THREADS);

    public Dispatcher(Peer p) {
      this.peer = p;
    }

    public void handleMessage(byte[] buf) {
        threadPool.submit(() -> {
            Message message = new Message(buf);
            //Ignore messages from self
            if(message.peerID.equals(this.peer.peerID))
              return;
            System.out.println("Received Message: " + message.fileID);
        });
    }

}
