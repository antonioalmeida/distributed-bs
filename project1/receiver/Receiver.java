package receiver;

import message.Message;
import utils.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Receiver {

    /**
     * The address.
     */

    protected InetAddress address;
    /**
     * The port.
     */
    protected int port;

    /**
     * The socket.
     */
    protected MulticastSocket socket;

    /**
     * The Dispatcher.
     */
    protected Dispatcher dispatcher;

    /**
     * Instantiates a new Receiver.
     *
     * @param address    the address
     * @param port       the port
     * @param dispatcher the dispatcher
     * @throws IOException the io exception
     */
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

    /**
      * Starts listening for messages, dispatching them as they are received
      */
    private void startListening() {
        new Thread(() -> {
            byte[] mbuf = new byte[65535];

            int count = 0;
            while(count < 10) {
                DatagramPacket multicastPacket = new DatagramPacket(mbuf, mbuf.length);

                try {
                    //TODO: ignore messages sent by itself
                    this.socket.receive(multicastPacket);
                    this.dispatcher.handleMessage(multicastPacket.getData(), multicastPacket.getLength(), multicastPacket.getAddress());
                } catch (IOException e) {
                    System.out.println("Error receiving multicast message");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Sends a message through the socket.
     *
     * @param message the message to be sent
     */
    public void sendMessage(Message message) {
        byte[] rbuf = message.buildMessagePacket();

        try {
            this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message through the socket.
     *
     * @param message the message to be sent
     */
    public void sendMessage(Message message, boolean sendBody) {
        byte[] rbuf = message.buildMessagePacket(sendBody);

        try {
            this.socket.send(new DatagramPacket(rbuf, rbuf.length, address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message with random delay.
     *
     * @param min     the min delay
     * @param max     the max delay
     * @param message the message to be sent
     */
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
