import channel.BackupChannel;
import channel.Channel;
import channel.ControlChannel;
import channel.RestoreChannel;
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

        // subscribe to multicast channels
        this.MC = new ControlChannel(args[3], Integer.parseInt(args[4]));
        this.MC.run();

        this.MDB = new BackupChannel(args[5], Integer.parseInt(args[6]));
        this.MDB.run();

        this.MDR = new RestoreChannel(args[7], Integer.parseInt(args[8]));
        this.MDR.run();
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
