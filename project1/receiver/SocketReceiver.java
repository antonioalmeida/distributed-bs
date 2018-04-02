package receiver;

import message.Message;
import receiver.Receiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioalmeida on 02/04/2018.
 */
public class SocketReceiver implements Runnable {

    private static final int MAX_TCP_SOCKETS = 50;

    private ServerSocket serverSocket;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_TCP_SOCKETS);

    private Dispatcher dispatcher;

    public SocketReceiver(int port, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> {
                    try {
                        socketHandler(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void socketHandler(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = null;

        try {
            stream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error creating Object Input Stream");
            e.printStackTrace();
        }

        Message message;
        while((message = (Message) stream.readObject()) != null) {
            System.out.println("Reading bytes");

            dispatcher.handleMessage(message, null);
            System.out.println("Read message from TCP");
        }
    }
}
