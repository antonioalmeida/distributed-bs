package server;

import channel.*;
import javafx.util.Pair;
import receiver.*;
import storage.FileSystem;
import utils.Globals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
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
    // < fileID, chunkIndex >
    private ConcurrentHashMap<String, ArrayList<Integer>> storedChunks;

    // useful information of the chunks the peer has locally stored
    // < (fileID, chunkIndex), (desiredDegree, actualDegree) >
    private ConcurrentHashMap<Pair<String, Integer>, ChunkInfo> storedChunksInfo;

    // files the peer has backed up somewhere on the network
    // <fileName, (fileID, nChunks)>
    private ConcurrentHashMap<String, Pair<String, Integer>> backedUpFiles;

    // useful information of the chunks the peer has backed up somewhere on the network
    // <(fileID, chunkIndex), (desiredDegree, actualDegree)
    private ConcurrentHashMap<Pair<String, Integer>, ChunkInfo> backedUpChunksInfo;

    // chunks of the files the peer is currently restoring
    // <fileID, fileChunks[]>
    private ConcurrentHashMap<String, ConcurrentSkipListSet<Message>> restoringFiles;

    // useful information of the files the peer is currently restoring
    // <fileID, Pair<fileName, chunkAmount>
    private ConcurrentHashMap<String, Pair<String, Integer>> restoringFilesInfo;

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
        storedChunksInfo = new ConcurrentHashMap<>();

        backedUpFiles = new ConcurrentHashMap<>();
        backedUpChunksInfo = new ConcurrentHashMap<>();

        restoringFiles = new ConcurrentHashMap<>();
        restoringFilesInfo = new ConcurrentHashMap<>();

        fileSystem = new FileSystem(peer, Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peer.getPeerID());
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getChunkIndex());

        // check if chunks from this file are already being saved
        storedChunks.putIfAbsent(message.getFileID(), new ArrayList<>());

        Pair<String, Integer> chunkInfoKey = new Pair<>(message.getFileID(), message.getChunkIndex());
        storedChunksInfo.putIfAbsent(chunkInfoKey, new ChunkInfo(message.getRepDegree(), 1));

        // check if chunk is already stored
        if(!storedChunks.get(message.getFileID()).contains(message.getChunkIndex())) {
            if (!this.fileSystem.storeChunk(message)) {
                System.out.println("Not enough space to save chunk " + message.getChunkIndex() + " of file " + message.getFileID());
                return;
            }

            //update map of stored chunks
            ArrayList<Integer> fileStoredChunks = storedChunks.get(message.getFileID());
            fileStoredChunks.add(message.getChunkIndex());
            storedChunks.put(message.getFileID(), fileStoredChunks);
        }
        else
            System.out.println("Already stored chunk, sending STORED anyway.");

        //TODO: Is it correct here to put the putchunk message version in our stored response??
        Message storedMessage = new StoredMessage(message.getVersion(), peer.getPeerID(), message.getFileID(), message.getChunkIndex());

        MCReceiver.sendWithRandomDelay(0, Globals.MAX_STORED_WAITING_TIME, storedMessage);

        System.out.println("Sent Stored Message: " + storedMessage.getChunkIndex());
    }

    public void handleStoredMessage(Message message) {
        System.out.println("Received Stored Message: " + message.getChunkIndex());

        Pair<String, Integer> key = new Pair<>(message.getFileID(), message.getChunkIndex());
        ChunkInfo chunkInfo;

        // if this chunk is from a file the peer
        // has requested to backup (aka is the
        // initiator peer), update actual rep degree
        if (backedUpChunksInfo.containsKey(key)) {
            chunkInfo = backedUpChunksInfo.get(key);
            chunkInfo.incActualReplicationDegree();
            backedUpChunksInfo.put(key, chunkInfo);
        }

        // if this peer has this chunk stored,
        // update actual rep degree
        if(storedChunksInfo.containsKey(key)) {
            chunkInfo = storedChunksInfo.get(key);
            chunkInfo.incActualReplicationDegree();
            storedChunksInfo.put(key, chunkInfo);
        }
    }

    public void handleGetChunkMessage(Message message) {
        System.out.println("Received GetChunk Message: " + message.getChunkIndex());

        String fileID = message.getFileID();
        int chunkIndex = message.getChunkIndex();

        // if peer doesn't have any chunks from this file, return
        if(!storedChunks.containsKey(fileID))
            return;

        // if peer doesn't have this chunks, return
        if(!storedChunks.get(fileID).contains(chunkIndex))
            return;

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

        int fileChunkAmount = restoringFilesInfo.get(message.getFileID()).getValue();

        // stored all the file's chunks
        if(fileRestoredChunks.size() == fileChunkAmount) {
            saveRestoredFile(message.getFileID());

            // restoring complete
            restoringFiles.remove(message.getFileID());
            restoringFilesInfo.remove(message.getFileID());
        }
    }

    public void handleDeleteMessage(Message message) {
        System.out.println("Received Delete Message");

        // peer doesn't have this chunk
        if(!storedChunks.containsKey(message.getFileID()))
            return;

        ArrayList<Integer> fileChunks = storedChunks.get(message.getFileID());
        while(!fileChunks.isEmpty())
            deleteChunk(message.getFileID(), fileChunks.get(0), false);

        storedChunks.remove(message.getFileID());
        System.out.println("Delete Success: file deleted.");
    }

    public void handleRemovedMessage(Message message) {
        System.out.println("Received Removed Message: " + message.getChunkIndex());

        Pair<String, Integer> key = new Pair<>(message.getFileID(), message.getChunkIndex());
        if(backedUpChunksInfo.containsKey(key)) {
            ChunkInfo chunkInfo = backedUpChunksInfo.get(key);
            chunkInfo.decActualReplicationDegree();

            if(!chunkInfo.isDegreeSatisfied()) {
                // wait random between 0 and 400ms
            }
        }
    }

    public boolean reclaimSpace(long targetSpace) {
        while(fileSystem.getUsedStorage() > targetSpace) {
            Pair<String, Integer> toDelete = getMostSatisfiedChunk();

            // no more chunks to delete
            if (toDelete == null) {
                System.out.println("Nothing to delete");
                return fileSystem.getUsedStorage() < targetSpace;
            }

            String fileID = toDelete.getKey();
            int chunkIndex = toDelete.getValue();

            System.out.println("Deleting " + fileID + " - " + chunkIndex);
            deleteChunk(fileID, chunkIndex, true);

            Message removedMessage = new RemovedMessage(peer.getProtocolVersion(), peer.getPeerID(), fileID, chunkIndex);
            MCReceiver.sendMessage(removedMessage);
        }

        return true;
    }

    public void initBackedUpChunksInfo(Message chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());
        backedUpChunksInfo.putIfAbsent(key, new ChunkInfo(chunk.getRepDegree(), 0));
    }

    public int getBackedUpChunkRepDegree(Message chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());

        int currentDegree = 0;
        if(backedUpChunksInfo.containsKey(key))
            currentDegree = backedUpChunksInfo.get(key).getActualReplicationDegree();

        return currentDegree;
    }

    public void addBackedUpFile(String filePath, String fileID, int chunkAmount) {
        backedUpFiles.put(filePath, new Pair<>(fileID, chunkAmount));
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

    public Pair<String, Integer> getMostSatisfiedChunk() {
        if(storedChunksInfo.isEmpty()) {
            System.out.println("Stored chunks info is empty");
            return null;
        }

        System.out.println("Getting max satisfied chunk");
        ChunkInfo maxChunk = Collections.max(storedChunksInfo.values());

        for(Map.Entry<Pair<String, Integer>, ChunkInfo> chunk : storedChunksInfo.entrySet())
            if(chunk.getValue() == maxChunk)
                return chunk.getKey();

        System.out.println("Didn't find max chunk");
        return null;
    }

    public void deleteChunk(String fileID, int chunkIndex, boolean updateMaxStorage) {
        fileSystem.deleteChunk(fileID, chunkIndex, updateMaxStorage);

        Pair<String, Integer> key = new Pair<>(fileID, chunkIndex);
        if(storedChunksInfo.containsKey(key))
            storedChunksInfo.remove(key);

        if(storedChunks.get(fileID).contains(chunkIndex))
            storedChunks.get(fileID).remove((Integer) chunkIndex);
    }

    public void addToRestoringFiles(String fileID, String filePath, int chunkAmount) {
        restoringFiles.putIfAbsent(fileID, new ConcurrentSkipListSet<>());
        restoringFilesInfo.putIfAbsent(fileID, new Pair<>(filePath, chunkAmount));
    }

    public void saveRestoredFile(String fileID) {
        byte[] fileBody = mergeRestoredFile(fileID);
        String filePath = restoringFilesInfo.get(fileID).getKey();

        fileSystem.saveFile(filePath, fileBody);
    }

    public byte[] mergeRestoredFile(String fileID) {
        // get file's chunks
        ConcurrentSkipListSet<Message> fileChunks = restoringFiles.get(fileID);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            for(Message chunk : fileChunks)
                stream.write(chunk.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("Current Peer state:\n");

        output.append("Files whose backup was initiated by this peer:\n");
        for (Map.Entry<String, Pair<String, Integer>> entry : backedUpFiles.entrySet()) {
            output.append("\tPathname = " + entry.getKey() + ", fileID = " + entry.getValue().getKey()+"\n");

            Pair<String, Integer> firstChunkInfo = new Pair<>(entry.getValue().getKey(), 0);
            output.append("\t\tDesired replication degree: " + backedUpChunksInfo.get(firstChunkInfo).getDesiredReplicationDegree() + "\n");
            output.append("\t\tChunks:\n");

            for(int chunkNr = 0; chunkNr < entry.getValue().getValue(); ++chunkNr) {
                Pair<String, Integer> chunkEntry = new Pair<>(entry.getValue().getKey(), chunkNr);
                output.append("\t\t\tChunk nr. " + chunkNr + " | Perceived replication degree: " + backedUpChunksInfo.get(chunkEntry).getActualReplicationDegree() + "\n");
            }
            output.append("\n");
        }

        output.append("Chunks stored by this peer:\n");

        for (Map.Entry<String, ArrayList<Integer>> entry : storedChunks.entrySet()) {
            output.append("\tBacked up chunks of file with fileID " + entry.getKey() + ":\n");

            for (int chunkNr : entry.getValue()) {
                Pair<String, Integer> chunkEntry = new Pair<>(entry.getKey(), chunkNr);
                //TODO: Add chunk size here
                output.append("\t\tChunk nr. " + chunkNr + " | Perceived replication degree: " + storedChunksInfo.get(chunkEntry).getActualReplicationDegree() + "\n");
            }
            output.append("\n");
        }

        output.append("Max storage capacity (in kB): " + fileSystem.getMaxStorage() + "\n");
        output.append("Current storage capacity used (in kB): " + fileSystem.getUsedStorage() + "\n");
        return output.toString();
    }

}
