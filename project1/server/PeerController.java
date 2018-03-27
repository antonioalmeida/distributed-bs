package server;

import channel.Message;
import receiver.*;
import storage.FileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

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
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Putchunk Message: " + message.fileID);

        //TODO: process putchunk message

        // for now:
        // if not in storedChunks
            //fileSystem.storeChunk;
        // else ignore message

    }
}
