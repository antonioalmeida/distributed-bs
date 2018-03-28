package protocol;

import channel.Channel;
import channel.ChunkMessage;
import com.sun.javafx.scene.control.GlobalMenuAdapter;
import server.Peer;
import storage.ChunkCreator;
import utils.Globals;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public class BackupInitiator extends ProtocolInitiator {

    private String filePath;
    private int replicationDegree;

    private ScheduledExecutorService executor;

    Channel channel;

    public BackupInitiator(Peer peer, String filePath, int replicationDegree, Channel channel) {
        super(peer);
        this.filePath = filePath;
        this.replicationDegree = replicationDegree;
        this.channel = channel;

        this.executor = new ScheduledThreadPoolExecutor(10);
    }

    @Override
    public void run() {
        ChunkCreator creator = new ChunkCreator(filePath, replicationDegree, peer.getPeerID());
        ArrayList<ChunkMessage> chunkList = creator.getChunkList();

        do {
            sendChunks(chunkList);
        } while(!confirmStoredMessages(chunkList));

        System.out.println("Backup Instance running");
    }

    private void sendChunks(ArrayList<ChunkMessage> chunkList) {
        System.out.println(chunkList.get(0).getChunkIndex());
        try {

            for(ChunkMessage chunk : chunkList) {
                // Wait random delay uniformly distributed between 0 and 400 ms
                try {
                    Thread.sleep(Utils.getRandomTime(Globals.MAX_PUTCHUNK_WAITING_TIME));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.channel.sendMessage(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean confirmStoredMessages(ArrayList<ChunkMessage> chunkList) {
        try {
            //TODO: remove sleeps
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < chunkList.size(); i++) {
            // if degree is satisfied, remove from list
            ChunkMessage chunk = chunkList.get(i);
            if (peer.getController().getChunkReplicationDegree(chunk) >= chunk.getRepDegree()) {
                chunkList.remove(chunk);
                i--;
            }
        }

        return chunkList.isEmpty();
    }
}
