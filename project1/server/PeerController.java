package server;

import channel.ChunkMessage;
import channel.Message;
import channel.StoredMessage;
import javafx.util.Pair;
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

    // < (fileID, chunkIndex), replicationDegree >
    private ConcurrentHashMap<Pair<String, Integer>, Integer> chunksReplicationDegree;

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
        chunksReplicationDegree = new ConcurrentHashMap<Pair<String, Integer>, Integer>();

        fileSystem = new FileSystem(Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getFileID());

        if(storedChunks.containsKey(message.getFileID()) && storedChunks.get(message.getFileID()).contains(message.getChunkIndex())) {
            System.out.println("Already stored chunk");
            return;
        }

        if (!this.fileSystem.storeChunk(message))
            System.out.println("Not enough space to save chunk " + message.getChunkIndex() + " of file " + message.getFileID());

        Message storedMessage = new StoredMessage(message.getVersion(), peer.getPeerID(), message.getFileID(), message.getChunkIndex());

        MCReceiver.sendWithRandomDelay(0, Globals.MAX_STORED_WAITING_TIME, storedMessage);
        System.out.println("Sent Stored");
    }

    public void handleStoredMessage(Message message) {
        System.out.println("Received Stored Message: " + message.getFileID());

        Pair key = new Pair<String, Integer>(message.getFileID(), message.getChunkIndex());
        int currentDegree = chunksReplicationDegree.getOrDefault(key, 0);

        // Increment chunk degree
        chunksReplicationDegree.put(key, currentDegree+1);
    }

    public int getChunkReplicationDegree(ChunkMessage chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());
        return chunksReplicationDegree.getOrDefault(key, 0);
    }

}
