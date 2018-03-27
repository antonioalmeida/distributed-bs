package receiver;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class RestoreReceiver extends Receiver {
    public RestoreReceiver(String address, int port) throws IOException {
        super(address, port);
    }

    @Override
    public void sendSampleMessage() throws IOException {

    }
}
