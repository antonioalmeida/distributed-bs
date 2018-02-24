import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length  < 4) {
            System.out.println("Usage; java Client <mcast addr> <mcast port> <oper> <oper args>*");
            return;
        }

        String multicastAddress = args[0];
        int multicastPort = Integer.valueOf(args[1]);
        System.out.println("Multicast Address: " + multicastAddress);
        System.out.println("Multicast Port: " + multicastPort);

        //create multicast socket
        MulticastSocket multicastSocket = new MulticastSocket(multicastPort);

        //join multicast group
        multicastSocket.joinGroup(InetAddress.getByName(multicastAddress));

        //receive multicast message
        byte[] mbuf = new byte[65535];
        DatagramPacket multicastPacket = new DatagramPacket(mbuf, mbuf.length);
        multicastSocket.receive(multicastPacket);
        multicastSocket.close();

        //parse service info
        String multicastReceived = new String(multicastPacket.getData()).trim();
        System.out.println("Received: " + multicastReceived);
        String[] split = multicastReceived.split(":");

        // create request message
        DatagramSocket socket = new DatagramSocket();
        String buf = new String();
        for(int i = 2; i < args.length; ++i) {
            buf += args[i];
            if(i < args.length - 1)
                buf += ":";
        }
        byte[] sbuf = buf.getBytes();
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, InetAddress.getByName(split[0]), Integer.valueOf(split[1]));
        System.out.println("Sending packet...");
        socket.send(packet);
        System.out.println("Sent");

        // get response
        byte[] rbuf = new byte[65535];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        socket.close();
    }
}
