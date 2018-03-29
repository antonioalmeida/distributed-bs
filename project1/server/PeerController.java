package server;

import channel.*;
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

    // files the peer has backed up somewhere on the network
    // <fileName, (fileID, nChunks)>
    private ConcurrentHashMap<String, Pair<String, Integer>> backedUpFiles;

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
        backedUpFiles = new ConcurrentHashMap<String, Pair<String, Integer>>();

        fileSystem = new FileSystem(peer, Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getChunkIndex());

        if(storedChunks.containsKey(message.getFileID()) && storedChunks.get(message.getFileID()).contains(message.getChunkIndex())) {
            System.out.println("Already stored chunk");
            return;
        }

        if (!this.fileSystem.storeChunk(message))
            System.out.println("Not enough space to save chunk " + message.getChunkIndex() + " of file " + message.getFileID());

        Message storedMessage = new StoredMessage(message.getVersion(), peer.getPeerID(), message.getFileID(), message.getChunkIndex());

        MCReceiver.sendWithRandomDelay(0, Globals.MAX_STORED_WAITING_TIME, storedMessage);
        System.out.println("Sent Stored Message: " + storedMessage.getChunkIndex());
    }

    public void handleStoredMessage(Message message) {
        System.out.println("Received Stored Message: " + message.getChunkIndex());

        Pair key = new Pair<String, Integer>(message.getFileID(), message.getChunkIndex());
        int currentDegree = chunksReplicationDegree.getOrDefault(key, 0);

        // Increment chunk degree
        chunksReplicationDegree.put(key, currentDegree+1);
    }

    public void handleGetChunkMessage(Message message) {
        System.out.println("Received GetChunk Message: " + message.getChunkIndex());

        String fileID = message.getFileID();
        int chunkIndex = message.getChunkIndex();

        Message chunkMessage = fileSystem.retrieveChunk(fileID, chunkIndex);
        MDRReceiver.sendWithRandomDelay(0, Globals.MAX_CHUNK_WAITING_TIME, chunkMessage);
    }

    public void handleChunkMessage(Message message) {
        System.out.println("Received Chunk Message: " + message.getChunkIndex());
    }

    public int getChunkReplicationDegree(Message chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());
        return chunksReplicationDegree.getOrDefault(key, 0);
    }

    public void addBackedUpFile(String filePath, String fileID, int chunkAmount) {
        backedUpFiles.put(filePath, new Pair(fileID, chunkAmount));
    }

    public String getBackedUpFileID(String filePath) {
        if(!backedUpFiles.containsKey(filePath))
            return null;

        Pair<String, Integer> fileInfo = backedUpFiles.get(filePath);
        return fileInfo.getKey();
    }

    public Integer getBackedUpFileChunkAmount(String filePath) {
        if(!backedUpFiles.containsKey(filePath))
            return 0;

        Pair<String, Integer> fileInfo = backedUpFiles.get(filePath);
        return fileInfo.getValue();
    }


    /*
    public ArrayList<Ch> getStoredChunksByFileID(String fileID) {
        // chunk is not stored in this peer
        if(!storedChunks.containsKey(fileID))
            return null;

        ArrayList<Integer> chunkIndexList = storedChunks.get(fileID);
        ArrayList<> chunkList = new ArrayList<>();

        for(index : chunkIndexList) {
           chunkIndexList.add(fileSystem.getC)
        }
    }
    */

}
