package receiver;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class RestoreReceiver extends Receiver {
    /**
     * Instantiates a new Restore receiver.
     *
     * @param address    the address
     * @param port       the port
     * @param dispatcher the dispatcher
     * @throws IOException the io exception
     */
    public RestoreReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
        super(address, port, dispatcher);
    }

    /**
     * Send sample message.
     *
     * @throws IOException the io exception
     */
    @Override
    public void sendSampleMessage() throws IOException {

    }
}
