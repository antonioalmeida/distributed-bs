import rmi.RemoteStub;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class Peer extends RemoteStub {
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

        // "all peers must subscribe the MC channel"
        this.MC = new Channel(args[3], Integer.parseInt(args[4]), true);
        this.MDB = new Channel(args[5], Integer.parseInt(args[6]), false);
        this.MDR = new Channel(args[7], Integer.parseInt(args[8]), false);
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
}
