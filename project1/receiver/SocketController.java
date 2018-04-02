package receiver;

import message.Message;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SocketController {

    private static final int MAX_TCP_SOCKET_THREADS = 50;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_TCP_SOCKET_THREADS);

    private int port;

    /**
      * Instantiates a new SocketController
      *
      * @param port controller port
      */
    public SocketController(int port) {
        this.port = port;
    }

    /**
      * Send a message
      *
      * @param message message to be sent
      * @param address destination address
      */
    public void sendMessage(Message message, InetAddress address) {
        threadPool.submit(() -> {
            Socket socket = null;
            try {
                socket = new Socket(address, port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ObjectOutputStream stream = null;

            try {
                stream = new ObjectOutputStream(socket.getOutputStream());
                stream.writeObject(message);
                System.out.println("Sent CHUNK message " + message.getChunkIndex() + " via TCP");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
