package peer;

import receiver.Channel;
import protocol.*;
import rmi.RemoteService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class Peer implements RemoteService {

    private static final int MAX_INITIATOR_THREADS = 50;

    private int peerID;
    private String version;

    private String rmiAccessPoint;

    private String MCAddress;
    private int MCPort;

    private String MDBAddress;
    private int MDBPort;

    private String MDRAddress;
    private int MDRPort;

    private Channel MC;
    private Channel MDB;
    private Channel MDR;

    private PeerController controller;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_INITIATOR_THREADS);

    /**
     * Main.
     *
     * @param args the args
     * @throws IOException the io exception
     */
// peer.Peer args
    //<protocol version> <peer id> <service access point> <MCReceiver address> <MCReceiver port> <MDBReceiver address> <MDBReceiver port> <MDRReceiver address> <MDRReceiver port>
    public static void main(final String args[]) throws IOException {
        Peer peer;
        if(!checkArgs(args))
            printUsage();
        else
            peer = new Peer(args);
    }

    private Peer(final String args[]) throws IOException {
        System.out.println("Starting Peer with protocol version " + args[0]);
        this.version = args[0];
        System.out.println("Starting Peer with ID " + args[1]);
        this.peerID = Integer.parseInt(args[1]);

        // RMI
        this.rmiAccessPoint = args[2];
        this.initRemoteStub(rmiAccessPoint);

        this.MCAddress = args[3]; this.MCPort = Integer.parseInt(args[4]);
        this.MDBAddress = args[5]; this.MDBPort = Integer.parseInt(args[6]);
        this.MDRAddress = args[7]; this.MDRPort = Integer.parseInt(args[8]);

        this.controller = new PeerController(this, MCAddress, MCPort, MDBAddress, MDBPort, MDRAddress, MDRPort);

        this.MC = new Channel(args[3], Integer.parseInt(args[4]));
        this.MDB = new Channel(args[5], Integer.parseInt(args[6]));
        this.MDR = new Channel(args[7], Integer.parseInt(args[8]));
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
        System.out.println("Java peer.Peer : <protocol version> <peer id> <service access point> <MCReceiver address> <MCReceiver port> <MDBReceiver address> <MDBReceiver port> <MDRReceiver address> <MDRReceiver port>");
    }

    /**
     * Init remote stub.
     *
     * @param accessPoint the access point
     */
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

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return version;
    }

    /**
     * Gets peer id.
     *
     * @return the peer id
     */
    public int getPeerID() {
        return peerID;
    }

    /**
     * Gets controller.
     *
     * @return the controller
     */
    public PeerController getController() {
        return controller;
    }

    @Override
    public void backupFile(String filePath, int replicationDegree) throws RemoteException{
        ProtocolInitiator backupInstance = new BackupInitiator(this, filePath, replicationDegree, MDB);
        threadPool.submit(backupInstance);
    }

    @Override
    public void recoverFile(String filePath) throws RemoteException {
        ProtocolInitiator recoverInstance = new RestoreInitiator(this, filePath, MC);
        threadPool.submit(recoverInstance);
    }

    @Override
    public void deleteFile(String filePath) throws RemoteException {
        ProtocolInitiator deleteInstance = new DeleteInitiator(this, filePath, MC);
        threadPool.submit(deleteInstance);
    }

    @Override
    public void reclaimSpace(long space) throws RemoteException {
        ProtocolInitiator reclaimInstance = new ReclaimInitiator(this, space);
        threadPool.submit(reclaimInstance);
    }

    @Override
    public void retrieveState() throws RemoteException {
        System.out.println(controller);
    }
}
