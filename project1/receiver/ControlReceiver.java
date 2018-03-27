package receiver;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class ControlReceiver extends Receiver {
    public ControlReceiver(String address, int port, Dispatcher dispatcher) throws IOException {
        super(address, port, dispatcher);
    }

    @Override
    public void sendSampleMessage() throws IOException {
                //create multicast message
                String buf = "Sample MULTICAST message";
                byte[] rbuf = buf.getBytes();

                this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }
}
