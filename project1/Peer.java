import channel.*;
import rmi.RemoteService;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class Peer implements RemoteService {
    private int peerID;

    private String rmiAccessPoint;

    private Channel MC;
    private Channel MDB;
    private Channel MDR;

    // Peer args
    //<protocol version> <server id> <service access point> <MC address> <MC port> <MDB address> <MDB port> <MDR address> <MDR port>
    public static void main(final String args[]) throws IOException {
        Peer peer;
        if(!checkArgs(args))
            printUsage();
        else
            peer = new Peer(args);
    }

    private Peer(final String args[]) throws IOException {
        this.peerID = Integer.parseInt(args[1]);

        // RMI
        this.rmiAccessPoint = args[2];
        this.initRemoteStub(rmiAccessPoint);

        // subscribe to multicast channels
        this.MC = new ControlChannel(args[3], Integer.parseInt(args[4]));
        this.MDB = new BackupChannel(args[5], Integer.parseInt(args[6]));
        this.MDR = new RestoreChannel(args[7], Integer.parseInt(args[8]));

        new Thread(MC).start();
        new Thread(MDB).start();
        new Thread(MDR).start();
    }

    private static boolean checkArgs(final String args[]) {
        //TODO: add thorough args verification
        if(args.length < 9)
            return false;
        return true;
    }

    private static void printUsage() {
        //TODO: add thorough usage information
        System.out.println("Usage:");
        System.out.println("Java Peer : <protocol version> <server id> <service access point> <MC address> <MC port> <MDB address> <MDB port> <MDR address> <MDR port>");
    }

    protected void initRemoteStub(String accessPoint) {
        try {
            RemoteService stub = (RemoteService) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String test() throws RemoteException {
        System.out.println("Testing RMI");
        return "Testing RMI";
    }

    @Override
    public void backupFile(String filePath, int replicationDegree) throws RemoteException{
        String header = "PUTCHUNK 1.0 " + this.peerID + " FileID 1 1 " + Message.CRLF + Message.CRLF;
        try {
            MDB.sendMessage(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
