package receiver;

import server.PeerController;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class BackupReceiver extends Receiver {

    /**
     * Instantiates a new Backup receiver.
     *
     * @param address    the address
     * @param port       the port
     * @param dispatcher the dispatcher
     * @throws IOException the io exception
     */
    public BackupReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
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
