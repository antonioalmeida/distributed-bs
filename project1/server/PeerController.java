package server;

import channel.Message;
import channel.StoredMessage;
import receiver.*;
import storage.FileSystem;
import utils.Globals;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class PeerController {

    private Peer peer;

    private Dispatcher dispatcher;

    private Receiver MCReceiver;
    private Receiver MDBReceiver;
    private Receiver MDRReceiver;

    private FileSystem fileSystem;

    // Stored chunks
    // < fileID, chunkIndex > TODO: maybe use a ordered list (by index) to merge them easier later?
    private ConcurrentHashMap<String, ArrayList<Integer>> storedChunks;

    public PeerController(Peer peer, String MCAddress, int MCPort, String MDBAddress, int MDBPort, String MDRAddress, int MDRPort) {
        this.peer = peer;

        this.dispatcher = new Dispatcher(this, peer.getPeerID());

        // subscribe to multicast channels
        try {
            this.MCReceiver = new Receiver(MCAddress, MCPort, dispatcher);
            this.MDBReceiver = new Receiver(MDBAddress, MDBPort, dispatcher);
            this.MDRReceiver = new Receiver(MDRAddress, MDRPort, dispatcher);
        } catch (IOException e) {
            e.printStackTrace();
        }

        storedChunks = new ConcurrentHashMap<String, ArrayList<Integer>>();

        fileSystem = new FileSystem(Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getFileID());

        if(storedChunks.containsKey(message.getFileID()) && storedChunks.get(message.getFileID()).contains(message.getChunkNr())) {
            System.out.println("Already stored chunk");
            return;
        }

        if (!this.fileSystem.storeChunk(message))
            System.out.println("Not enough space to save chunk " + message.getChunkNr() + " of file " + message.getFileID());

        Message storedMessage = new StoredMessage(message.getVersion(), peer.getPeerID(), message.getFileID(), message.getChunkNr());

        MCReceiver.sendWithRandomDelay(0, Globals.MAX_STORED_WAITING_TIME, storedMessage);
        System.out.println("Sent Stored");
    }

    public void handleStoredMessage(Message message) {
        System.out.println("Received Stored Message: " + message.getFileID());
    }

}
