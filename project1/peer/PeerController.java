package peer;

import message.ChunkInfo;
import message.Message;
import message.RemovedMessage;
import message.StoredMessage;
import javafx.util.Pair;
import protocol.SingleBackupInitiator;
import receiver.Dispatcher;
import receiver.Receiver;
import storage.FileSystem;
import utils.Globals;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class PeerController {

    private String peerVersion;
    private int peerID;

    private Dispatcher dispatcher;

    private Receiver MCReceiver;
    private Receiver MDBReceiver;
    private Receiver MDRReceiver;

    private FileSystem fileSystem;

    private boolean backupEnhancement;

    private ScheduledExecutorService putchunkThreadPool = Executors.newScheduledThreadPool(50);

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

    // useful information of the files other peers are currently trying to restore
    // < fileID, chunkID[] >
    private ConcurrentHashMap<String, ArrayList<Integer>> getChunkRequestsInfo;

    //FOR BACKUP ENHANCEMENT
    // useful information for the backup protocol enhancement, storing info about received STORED messages
    // < (fileID, chunkIndex), boolean received >
    private ConcurrentHashMap< Pair<String, Integer>, Boolean> storedRepliesInfo;

    /**
     * Instantiates a new Peer controller.
     *
     * @param peerVersion the peers's protocol version
     * @param peerID      the peer's ID
     * @param MCAddress   the mc address
     * @param MCPort      the mc port
     * @param MDBAddress  the mdb address
     * @param MDBPort     the mdb port
     * @param MDRAddress  the mdr address
     * @param MDRPort     the mdr port
     */
    public PeerController(String peerVersion, int peerID, String MCAddress, int MCPort, String MDBAddress, int MDBPort, String MDRAddress, int MDRPort) {
        this.peerVersion = peerVersion;
        this.peerID = peerID;

        this.dispatcher = new Dispatcher(this, peerID);

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

        getChunkRequestsInfo = new ConcurrentHashMap<>();

        //TODO: make proper verification
        if(peerVersion != "1.0") {
            System.out.println("BACKUP enhancement activated");
            backupEnhancement = true;
        }
        else
            backupEnhancement = false;
        
        storedRepliesInfo = new ConcurrentHashMap<>();

        fileSystem = new FileSystem(peerVersion, peerID, Globals.MAX_PEER_STORAGE, Globals.PEER_FILESYSTEM_DIR + "/" + peerID);
    }

    /**
     * Handle putchunk message.
     *
     * @param message the message
     */
    public void handlePutchunkMessage(Message message) {
        System.out.println("Received Putchunk: " + message.getChunkIndex());

        String fileID = message.getFileID();
        int chunkIndex = message.getChunkIndex();

        if(backupEnhancement && message.getVersion() != "1.0") {
            Pair<String, Integer> key = new Pair<>(fileID, chunkIndex);

            if(storedRepliesInfo.containsKey(key)) {
                //if received a stored message meanwhile, ignore (and remove storedRepliesInfo)
                if(storedRepliesInfo.get(key)) {
                    System.out.println("Received a STORED message for " + message.getChunkIndex() + " meanwhile, ignoring request");
                    storedRepliesInfo.remove(key);
                    return;
                }
            }
        }

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

        Message storedMessage = new StoredMessage(message.getVersion(), peerID, message.getFileID(), message.getChunkIndex());

        MCReceiver.sendWithRandomDelay(0, Globals.MAX_STORED_WAITING_TIME, storedMessage);

        System.out.println("Sent Stored Message: " + storedMessage.getChunkIndex());
    }

    /**
     * Handle stored message.
     *
     * @param message the message
     */
    public void handleStoredMessage(Message message) {
        System.out.println("Received Stored Message: " + message.getChunkIndex());

        Pair<String, Integer> key = new Pair<>(message.getFileID(), message.getChunkIndex());
        ChunkInfo chunkInfo;

        // TODO: are the following ifs mutually exclusive? I think so

        // if this chunk is from a file the peer
        // has requested to backup (aka is the
        // initiator peer), and hasn't received
        // a stored message, update actual rep degree,
        // and add peer
        if (backedUpChunksInfo.containsKey(key) && !backedUpChunksInfo.get(key).isBackedUpByPeer(message.getPeerID())) {
            chunkInfo = backedUpChunksInfo.get(key);
            chunkInfo.incActualReplicationDegree();
            chunkInfo.addPeer(message.getPeerID());
            backedUpChunksInfo.put(key, chunkInfo);
        }

        // if this peer has this chunk stored,
        // and hasn't received stored message
        // from this peer yet, update actual
        // rep degree, and add peer
        if(storedChunksInfo.containsKey(key) && !storedChunksInfo.get(key).isBackedUpByPeer(message.getPeerID())) {
            chunkInfo = storedChunksInfo.get(key);
            chunkInfo.incActualReplicationDegree();
            chunkInfo.addPeer(message.getPeerID());
            storedChunksInfo.put(key, chunkInfo);
        }

        if(backupEnhancement && message.getVersion() != "1.0") {
            //if currently listening for this chunk's stored message, set found to true
            if(storedRepliesInfo.containsKey(key))
                storedRepliesInfo.put(key, true);
        }
    }

    /**
     * Handle get chunk message.
     *
     * @param message the message
     */
    public void handleGetChunkMessage(Message message) {
        System.out.println("Received GetChunk Message: " + message.getChunkIndex());

        String fileID = message.getFileID();
        int chunkIndex = message.getChunkIndex();

        // if received a chunk message for this chunk meanwhile, return
        if(getChunkRequestsInfo.containsKey(fileID)) {
            ArrayList<Integer> chunkList = getChunkRequestsInfo.get(fileID);

            if(chunkList.contains(chunkIndex)) {
                chunkList.remove((Integer) chunkIndex);
                getChunkRequestsInfo.put(fileID, chunkList);
                System.out.println("Received a CHUNK message for " + chunkIndex + " meanwhile, ignoring request");
                return;
            }
        }

        // if peer doesn't have any chunks from this file, return
        if(!storedChunks.containsKey(fileID))
            return;

        // if peer doesn't have this chunk, return
        if(!storedChunks.get(fileID).contains(chunkIndex))
            return;

        Message chunkMessage = fileSystem.retrieveChunk(fileID, chunkIndex);
        MDRReceiver.sendWithRandomDelay(0, Globals.MAX_CHUNK_WAITING_TIME, chunkMessage);
    }

    /**
     * Handle chunk message.
     *
     * @param message the message
     */
    public void handleChunkMessage(Message message) {
        System.out.println("Received Chunk Message: " + message.getChunkIndex());

        String fileID = message.getFileID();
        int chunkIndex = message.getChunkIndex();

        if(getChunkRequestsInfo.containsKey(fileID)) {
            ArrayList<Integer> chunkList = getChunkRequestsInfo.get(fileID);
            
            if(!chunkList.contains((Integer) chunkIndex)) {
                chunkList.add(chunkIndex);
                getChunkRequestsInfo.put(fileID, chunkList);
                System.out.println("Added Chunk " + chunkIndex + " to requests info.");
            }
        }
        else
            getChunkRequestsInfo.put(fileID, new ArrayList<>());

        // Not restoring this file
        if(!restoringFiles.containsKey(fileID))
            return;

        ConcurrentSkipListSet<Message> fileRestoredChunks = restoringFiles.get(fileID);
        fileRestoredChunks.add(message);

        restoringFiles.put(message.getFileID(), fileRestoredChunks);

        int fileChunkAmount = restoringFilesInfo.get(fileID).getValue();

        // stored all the file's chunks
        if(fileRestoredChunks.size() == fileChunkAmount) {
            saveRestoredFile(fileID);

            // restoring complete
            restoringFiles.remove(fileID);
            restoringFilesInfo.remove(fileID);
        }
    }

    /**
     * Handle delete message.
     *
     * @param message the message
     */
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

    /**
     * Handle removed message.
     *
     * @param message the message
     */
    public void handleRemovedMessage(Message message) {
        System.out.println("Received Removed Message: " + message.getChunkIndex());

        Pair<String, Integer> key = new Pair<>(message.getFileID(), message.getChunkIndex());
        if(storedChunksInfo.containsKey(key)) {
            ChunkInfo chunkInfo = storedChunksInfo.get(key);
            chunkInfo.decActualReplicationDegree();

            // if replication degree isn't satisfied anymore,
            // initiate new putchunk protocol for the chunk,
            // but wait between 0-400 ms and check if degree
            // satisfied in the meantime
            if(!chunkInfo.isDegreeSatisfied()) {
                System.out.println("Chunk " + message.getChunkIndex() + " not satisfied anymore.");
                Message chunk = fileSystem.retrieveChunk(message.getFileID(), message.getChunkIndex());

                putchunkThreadPool.schedule( new SingleBackupInitiator(this, chunk, chunkInfo.getDesiredReplicationDegree(), MDBReceiver),
                Utils.getRandomBetween(0, Globals.MAX_REMOVED_WAITING_TIME), TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Reclaim space boolean.
     *
     * @param targetSpace the target space
     * @return the boolean
     */
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

            Message removedMessage = new RemovedMessage(peerVersion, peerID, fileID, chunkIndex);
            MCReceiver.sendMessage(removedMessage);
        }

        return true;
    }

    /**
     * Init backed up chunks info.
     *
     * @param chunk the chunk
     */
    public void initBackedUpChunksInfo(Message chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());
        backedUpChunksInfo.putIfAbsent(key, new ChunkInfo(chunk.getRepDegree(), 0));
    }

    /**
     * Gets backed up chunk rep degree.
     *
     * @param chunk the chunk
     * @return the backed up chunk rep degree
     */
    public int getBackedUpChunkRepDegree(Message chunk) {
        Pair<String, Integer> key = new Pair<>(chunk.getFileID(), chunk.getChunkIndex());

        int currentDegree = 0;
        if(backedUpChunksInfo.containsKey(key))
            currentDegree = backedUpChunksInfo.get(key).getActualReplicationDegree();

        return currentDegree;
    }

    /**
     * Add backed up file.
     *
     * @param filePath    the file path
     * @param fileID      the file id
     * @param chunkAmount the chunk amount
     */
    public void addBackedUpFile(String filePath, String fileID, int chunkAmount) {
        backedUpFiles.put(filePath, new Pair<>(fileID, chunkAmount));
    }

    /**
     * Gets backed up file id.
     *
     * @param filePath the file path
     * @return the backed up file id
     */
    public String getBackedUpFileID(String filePath) {
        if(!backedUpFiles.containsKey(filePath))
            return null;

        Pair<String, Integer> fileInfo = backedUpFiles.get(filePath);
        return fileInfo.getKey();
    }

    /**
     * Gets backed up file chunk amount.
     *
     * @param filePath the file path
     * @return the backed up file chunk amount
     */
    public Integer getBackedUpFileChunkAmount(String filePath) {
        if(!backedUpFiles.containsKey(filePath))
            return 0;

        Pair<String, Integer> fileInfo = backedUpFiles.get(filePath);
        return fileInfo.getValue();
    }

    /**
     * Gets most satisfied chunk.
     *
     * @return the most satisfied chunk
     */
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

    /**
     * Delete chunk.
     *
     * @param fileID           the file id
     * @param chunkIndex       the chunk index
     * @param updateMaxStorage the update max storage
     */
    public void deleteChunk(String fileID, int chunkIndex, boolean updateMaxStorage) {
        fileSystem.deleteChunk(fileID, chunkIndex, updateMaxStorage);

        Pair<String, Integer> key = new Pair<>(fileID, chunkIndex);
        if(storedChunksInfo.containsKey(key))
            storedChunksInfo.remove(key);

        if(storedChunks.get(fileID).contains(chunkIndex))
            storedChunks.get(fileID).remove((Integer) chunkIndex);
    }

    /**
     * Add to restoring files.
     *
     * @param fileID      the file id
     * @param filePath    the file path
     * @param chunkAmount the chunk amount
     */
    public void addToRestoringFiles(String fileID, String filePath, int chunkAmount) {
        restoringFiles.putIfAbsent(fileID, new ConcurrentSkipListSet<>());
        restoringFilesInfo.putIfAbsent(fileID, new Pair<>(filePath, chunkAmount));
    }

    /**
     * Save restored file.
     *
     * @param fileID the file id
     */
    public void saveRestoredFile(String fileID) {
        byte[] fileBody = mergeRestoredFile(fileID);
        String filePath = restoringFilesInfo.get(fileID).getKey();

        fileSystem.saveFile(filePath, fileBody);
    }

    /**
     * Listen for store replies.
     *
     * @param fileID     the file id
     * @param chunkIndex the chunk index
     */
    public void listenForStoreReplies(String fileID, int chunkIndex) {
        Pair<String, Integer> key = new Pair<>(fileID, chunkIndex);
        storedRepliesInfo.putIfAbsent(key, false);
    }

    /**
     * Merge restored file byte [ ].
     *
     * @param fileID the file id
     * @return the byte [ ]
     */
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
