package server;

import channel.*;
import javafx.util.Pair;
import receiver.*;
import storage.FileSystem;
import utils.Globals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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

    // < (fileID, chunkIndex), replicationDegree >
    private ConcurrentHashMap<Pair<String, Integer>, Integer> chunksReplicationDegree;

    // files the peer has backed up somewhere on the network
    // <fileName, (fileID, nChunks)>
    private ConcurrentHashMap<String, Pair<String, Integer>> backedUpFiles;

    // files the peer is currently restoring
    // <fileID, fileChunks[]>
    private ConcurrentHashMap<String, ConcurrentSkipListSet<Message>> restoringFiles;

    // chunk ammount of the files the peer is currently restoring
    // <fileID, chunkAmmount>
    //TODO: find another way, this is redundant
    private ConcurrentHashMap<String, Integer> restoringFilesChunkAmmout;

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

        storedChunks = new ConcurrentHashMap<>();
        chunksReplicationDegree = new ConcurrentHashMap<>();
        backedUpFiles = new ConcurrentHashMap<>();
        restoringFiles = new ConcurrentHashMap<>();
        restoringFilesChunkAmmout = new ConcurrentHashMap<>();

        fileSystem = new FileSystem(peer, Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getChunkIndex());

        if(!storedChunks.containsKey(message.getFileID()))
            storedChunks.putIfAbsent(message.getFileID(), new ArrayList<>());

        // check if chunk is already stored
        if(!storedChunks.get(message.getFileID()).contains(message.getChunkIndex())) {

            if (!this.fileSystem.storeChunk(message))
                System.out.println("Not enough space to save chunk " + message.getChunkIndex() + " of file " + message.getFileID());

            ArrayList<Integer> fileStoredChunks = storedChunks.get(message.getFileID());
            fileStoredChunks.add(message.getChunkIndex());
            storedChunks.put(message.getFileID(), fileStoredChunks);
        }
        else
            System.out.println("Already stored chunk, sending STORED anyway.");

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

        // Not restoring this file
        if(!restoringFiles.containsKey(message.getFileID()))
            return;

        ConcurrentSkipListSet<Message> fileRestoredChunks = restoringFiles.get(message.getFileID());
        fileRestoredChunks.add(message);

        restoringFiles.put(message.getFileID(), fileRestoredChunks);

        if(fileRestoredChunks.size() == restoringFilesChunkAmmout.get(message.getFileID()))
            System.out.println("Restored Success: all chunks received. " + fileRestoredChunks.size() + " - " + restoringFilesChunkAmmout.get(message.getFileID()));
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

    public void addToRestoringFiles(String fileID, int chunkAmount) {
        restoringFiles.putIfAbsent(fileID, new ConcurrentSkipListSet<>());
        restoringFilesChunkAmmout.putIfAbsent(fileID, chunkAmount);
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
