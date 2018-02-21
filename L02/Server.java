import javax.print.DocFlavor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    public MulticastSocket multicastSocket;
    public DatagramPacket multicastPacket;

    public Server(int servicePort, int multicastPort, String multicastAddress) throws IOException {
        // create multicast socket
        this.multicastSocket = new MulticastSocket(multicastPort);

        //create multicast message
        String buf = "localhost:" + servicePort;
        byte[] rbuf = buf.getBytes();
        this.multicastPacket = new DatagramPacket(rbuf, rbuf.length, InetAddress.getByName(multicastAddress), multicastPort);
    }

    public static void main(String[] args) throws IOException {
        HashMap<String, String> database = new HashMap<>();

        int servicePort = Integer.valueOf(args[0]);
        String multicastAddress = args[1];
        int multicastPort = Integer.valueOf(args[2]);

        // create server
        Server server = new Server(servicePort, multicastPort, multicastAddress);

        // create multicast sender task
        Timer multicastTimer = new Timer();
        MulticastTask multicastTask = new MulticastTask();

        multicastTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    int ttl = server.multicastSocket.getTimeToLive();
                    server.multicastSocket.setTimeToLive(1);
                    server.multicastSocket.send(server.multicastPacket);
                    server.multicastSocket.setTimeToLive(ttl);
                } catch (IOException e) {

                }
            }
        }, 0, 1000);

        int counter = 20;
        //create unicast socket
        DatagramSocket socket = new DatagramSocket(servicePort);

        while(counter > 0) {
            //receive request
            byte[] rbuf = new byte[65535];
            DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
            socket.receive(packet);

            // display request
            String request = new String(packet.getData()).trim();
            System.out.println("Received Request: " + request);

            String[] requestArgs = request.split(":");

            String response = "-1";
            switch(requestArgs[0]) {
                case "register":
                    System.out.println("Registering " + requestArgs[1] + " " + requestArgs[2]);
                    database.put(requestArgs[1], requestArgs[2]);
                    response = database.size() + " " + requestArgs[1] + " " + requestArgs[2];
                    break;
                case "lookup":
                    String lookupResult = database.get(requestArgs[1]);
                    if(lookupResult != null) {
                        System.out.println("Found: " + lookupResult);
                        response = database.size() + " " + requestArgs[1] + " " + lookupResult;
                    }
                    break;
                default:
            }

            // send response
            byte[] responseBuf = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBuf, responseBuf.length, packet.getAddress(), packet.getPort());
            socket.send(responsePacket);

            counter--;
        }

        System.out.println("Server closing...");
        socket.close();
    }

    private static class MulticastTask extends TimerTask {

        public MulticastTask() {
            super();
        }

        @Override
        public void run() {

        }
    }
}
