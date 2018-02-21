import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length  < 2) {
            System.out.println("Usage; java Client <hostname> <string>");
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

        //parse service info
        String multicastReceived = new String(multicastPacket.getData());
        System.out.println("Received: " + multicastReceived);

        /*
        // create request message
        DatagramSocket socket = new DatagramSocket();
        String buf = new String();
        for(int i = 1; i < args.length; ++i) {
            buf += args[i];
            if(i < args.length - 1)
                buf += ":";
        }
        byte[] sbuf = buf.getBytes();
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, 4445);
        socket.send(packet);

        // get response
        byte[] rbuf = new byte[65535];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        socket.close();
        */
    }
}
