package receiver;

import server.PeerController;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class ControlReceiver extends Receiver {
    public ControlReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
        super(address, port, dispatcher);
    }

}
