package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class ControlChannel extends Channel {
    public ControlChannel(String address, int port) throws IOException {
        super(address, port);
    }

    @Override
    public void sendSampleMessage() throws IOException {
                //create multicast message
                String buf = "Sample MULTICAST message";
                byte[] rbuf = buf.getBytes();

                this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }
}
