import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 07/03/2018.
 */
public class Channel {

    // Multicast channel is defined by its address and port
    private InetAddress address;
    private int port;

    private MulticastSocket socket;

    public Channel(String address, int port, boolean subscribe) throws IOException {
        // create multicast socket
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        if(subscribe) {
            //join multicast group
            this.address = InetAddress.getByName((address));
            socket.joinGroup(this.address);
        }
    }
}
