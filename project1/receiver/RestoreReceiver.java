package receiver;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class RestoreReceiver extends Receiver {
    public RestoreReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
        super(address, port, dispatcher);
    }

    @Override
    public void sendSampleMessage() throws IOException {

    }
}
