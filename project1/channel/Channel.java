package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Channel {

    // Multicast channel is defined by its address and port
    protected InetAddress address;
    protected int port;

    protected MulticastSocket socket;

    public Channel(String address, int port) throws IOException {
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        this.address = InetAddress.getByName(address);
        this.port = port;

        System.out.println("Joined Multicast Receiver " + address + ":" + port);
    }

    public void sendMessage(String message) throws IOException {
        byte[] rbuf = message.getBytes();
        this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }

    public void sendMessage(Message message) throws IOException {
        byte[] rbuf = message.buildMessagePacket();
        this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }

}
