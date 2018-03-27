package server;

import channel.Message;
import receiver.*;
import storage.FileSystem;
import utils.Globals;

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
            this.MCReceiver = new ControlReceiver(MCAddress, MCPort, dispatcher);
            this.MDBReceiver = new BackupReceiver(MDBAddress, MDBPort, dispatcher);
            this.MDRReceiver = new RestoreReceiver(MDRAddress, MDRPort, dispatcher);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(MCReceiver).start();
        new Thread(MDBReceiver).start();
        new Thread(MDRReceiver).start();

        storedChunks = new ConcurrentHashMap<String, ArrayList<Integer>>();

        fileSystem = new FileSystem(Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Putchunk Message: " + message.getFileID());

        //TODO: process putchunk message
        if(storedChunks.containsKey(message.getFileID()) && storedChunks.get(message.getFileID()).contains(message.getChunkNr())) {
            System.out.println("Already stored chunk");
            return;
        }

        if (!this.fileSystem.storeChunk(message))
            System.out.println("Not enough space to save chunk " + message.getChunkNr() + " of file " + message.getFileID());
    }

}
