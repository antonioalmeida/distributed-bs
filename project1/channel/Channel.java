package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 07/03/2018.
 */
public abstract class Channel extends Thread {

    // Multicast channel is defined by its address and port
    protected InetAddress address;
    protected int port;

    protected MulticastSocket socket;

    public Channel(String address, int port) throws IOException {
        // create multicast socket
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        //join multicast group
        this.address = InetAddress.getByName((address));
        socket.joinGroup(this.address);
    }

    @Override
    public void run() {
        //continuously receive multicast messages

        byte[] mbuf = new byte[65535];

        int count = 0;
        while(count < 10) {
            DatagramPacket multicastPacket = new DatagramPacket(mbuf, mbuf.length);

            try {
                this.socket.receive(multicastPacket);

                //sample parse received message
                String multicastReceived = new String(multicastPacket.getData()).trim();
                System.out.println("Received: " + multicastReceived);
                count++;
            } catch (IOException e) {
                System.out.println("Error receiving multicast message");
                e.printStackTrace();
            }
        }
    }
}
