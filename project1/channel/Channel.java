package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class Channel {

    /**
     * The Address.
     */
// Multicast channel is defined by its address and port
    protected InetAddress address;
    /**
     * The Port.
     */
    protected int port;

    /**
     * The Socket.
     */
    protected MulticastSocket socket;

    /**
     * Instantiates a new Channel.
     *
     * @param address the address
     * @param port    the port
     * @throws IOException the io exception
     */
    public Channel(String address, int port) throws IOException {
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        this.address = InetAddress.getByName(address);
        this.port = port;

        System.out.println("Joined Multicast Receiver " + address + ":" + port);
    }

    /**
     * Send message.
     *
     * @param message the message
     * @throws IOException the io exception
     */
    public void sendMessage(String message) throws IOException {
        byte[] rbuf = message.getBytes();
        this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
    }

    /**
     * Send message.
     *
     * @param message the message
     */
    public void sendMessage(Message message) {
        byte[] rbuf = message.buildMessagePacket();
        try {
            this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
