import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        HashMap<String, String> database = new HashMap<>();
        DatagramSocket socket = new DatagramSocket(4445);

        int counter = 20;
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
}
