import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Usage; java Server <port_number>");
            return;
        }
        HashMap<String, String> database = new HashMap<>();
        DatagramSocket socket = new DatagramSocket(Integer.valueOf(args[0]));

        int counter = 20; //Change value to alter amount of requests read
        while(counter > 0) {
            //receive request
            byte[] rbuf = new byte[65535];
            DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

            socket.receive(packet);
            // display request
            String request = new String(packet.getData()).trim();
            System.out.println("Received Request: " + request);

            String[] requestArgs = request.split(":");

            String response = "";
            switch(requestArgs[0]) {
                case "register":
                    if(!database.containsKey(requestArgs[1])) {
                      database.put(requestArgs[1], requestArgs[2]);
                      response = String.valueOf(database.size());
                    }
                    else
                      response = "-1";
                    break;
                case "lookup":
                  if(database.containsKey(requestArgs[1]))
                    response = database.get(requestArgs[1]);
                  else
                    response = "NOT_FOUND";
                  break;
                default:
                  break;
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
