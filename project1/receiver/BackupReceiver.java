package receiver;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class BackupReceiver extends Receiver {

    public BackupReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
        super(address, port, dispatcher);
    }

    @Override
    public void sendSampleMessage() throws IOException {

    }
}
