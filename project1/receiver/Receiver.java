package receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 07/03/2018.
 */
public abstract class Receiver implements Runnable {

    // Multicast channel is defined by its address and port
    protected InetAddress address;
    protected int port;

    protected MulticastSocket socket;

    public Receiver(String address, int port) throws IOException {
        // create multicast socket
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        this.address = InetAddress.getByName(address);
        this.port = port;

        //join multicast group
        socket.joinGroup(this.address);

        System.out.println("Joined Multicast Receiver " + address + ":" + port);
    }

    @Override
    public void run() {
        //continuously receive multicast messages

        byte[] mbuf = new byte[65535];

        int count = 0;
        while(count < 10) {
            DatagramPacket multicastPacket = new DatagramPacket(mbuf, mbuf.length);

            try {
                //TODO: ignore messages sent by itself
                this.socket.receive(multicastPacket);
            } catch (IOException e) {
                System.out.println("Error receiving multicast message");
                e.printStackTrace();
            }
        }
    }

    public void parseMessage(DatagramPacket packet) {
        String request = new String(packet.getData()).trim();
    }

    public abstract void sendSampleMessage() throws IOException;

    public void sendMessage(String message) throws IOException {
        byte[] rbuf = message.getBytes();
        this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }

}
