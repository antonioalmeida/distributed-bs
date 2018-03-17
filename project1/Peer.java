import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class Peer {

    private Channel MC;
    private Channel MDB;
    private Channel MDR;

    private int peerID;

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

        this.MC = new Channel(args[3], Integer.parseInt(args[4]));
        this.MDB = new Channel(args[5], Integer.parseInt(args[6]));
        this.MDR = new Channel(args[7], Integer.parseInt(args[8]));
    }

    private static boolean checkArgs(final String args[]) {
        //TODO: add thorough args verification
        if(args.length < 9)
            return false;
    }

    private static void printUsage() {
        //TODO: add thorough usage information
        System.out.println("Usage:");
        System.out.println("Java Peer : <protocol version> <server id> <service access point> <MC address> <MC port> <MDB address> <MDB port> <MDR address> <MDR port>");
    }
}
