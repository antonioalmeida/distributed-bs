package receiver;

import channel.Message;
import server.PeerController;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by antonioalmeida on 07/03/2018.
 */
public class Receiver {

    // Multicast channel is defined by its address and port
    protected InetAddress address;
    protected int port;

    protected MulticastSocket socket;

    protected Dispatcher dispatcher;

    public Receiver(String address, int port, Dispatcher dispatcher) throws IOException {
        // create multicast socket
        this.socket = new MulticastSocket(port);
        this.socket.setTimeToLive(1);

        this.address = InetAddress.getByName(address);
        this.port = port;

        this.dispatcher = dispatcher;

        //join multicast group
        socket.joinGroup(this.address);

        startListening();

        System.out.println("Joined Multicast Receiver " + address + ":" + port);
    }

    private void startListening() {
        new Thread(() -> {
            byte[] mbuf = new byte[65535];

            int count = 0;
            while(count < 10) {
                DatagramPacket multicastPacket = new DatagramPacket(mbuf, mbuf.length);

                try {
                    //TODO: ignore messages sent by itself
                    this.socket.receive(multicastPacket);
                    this.dispatcher.handleMessage(multicastPacket.getData(), multicastPacket.getLength());
                } catch (IOException e) {
                    System.out.println("Error receiving multicast message");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void parseMessage(DatagramPacket packet) {
        String request = new String(packet.getData()).trim();
    }

    public void sendMessage(Message message) {
        byte[] rbuf = message.buildMessagePacket();

        try {
            this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendWithRandomDelay(int min, int max, Message message) {
        int random = Utils.getRandomBetween(min, max);

        try {
            //TODO: remove Thread.sleep everywhere
            Thread.sleep(random);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendMessage(message);
    }

}
