import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length  < 2) {
            System.out.println("Usage; java Client <hostname> <string>");
            return;
        }

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
    }
}
