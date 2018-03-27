package receiver;

import channel.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Dispatcher {

    private final int MAX_PROCESSING_THREADS = 10;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PROCESSING_THREADS);

    public Dispatcher() {

    }

    public void handleMessage(byte[] buf) {
        threadPool.submit(() -> {
            Message message = new Message(buf);

            System.out.println("Received Message: " + message.fileID);
        });
    }

}
