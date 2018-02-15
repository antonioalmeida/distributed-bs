import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length < 4 || args.length > 5) {
            System.out.println("Usage; java Client <host_name> <port_number> <oper> <opnd>*");
            return;
        }

        DatagramSocket socket = new DatagramSocket();
        String buf = new String();
        for(int i = 2; i < args.length; ++i) {
            buf += args[i];
            if(i < args.length - 1)
                buf += ":";
        }
        byte[] sbuf = buf.getBytes();
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, Integer.valueOf(args[1]));
        socket.send(packet);

        // get response
        byte[] rbuf = new byte[65535];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        // display response
        String received = new String(packet.getData());
        for(int i = 2; i < args.length; ++i)
          System.out.print(args[i]+" ");
        System.out.println(": " + received);
        socket.close();
    }
}
