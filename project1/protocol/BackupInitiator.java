package protocol;

import channel.Channel;
import channel.ChunkMessage;
import storage.Chunk;
import storage.ChunkCreator;
import utils.Globals;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public class BackupInitiator extends ProtocolInitiator {

    private String filePath;
    private int replicationDegree;
    private int peerID;

    Channel channel;

    public BackupInitiator(String filePath, int replicationDegree, int peerID, Channel channel) {
        super();
        this.filePath = filePath;
        this.replicationDegree = replicationDegree;
        this.peerID = peerID;
        this.channel = channel;
    }

    @Override
    public void run() {

        ChunkCreator creator = new ChunkCreator(filePath, replicationDegree, peerID);
        ArrayList<Chunk> chunkList = creator.getChunkList();

        sendChunks(chunkList);
        System.out.println("Backup Instance running");
    }

    private void sendChunks(ArrayList<Chunk> chunkList) {
        System.out.println(chunkList.get(0).getChunkIndex());
        try {

            for(Chunk chunk : chunkList) {
                // Wait random delay uniformly distributed between 0 and 400 ms
                try {
                    Thread.sleep(Utils.getRandomTime(Globals.MAX_PUTCHUNK_WAITING_TIME));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ChunkMessage message = new ChunkMessage(chunk, 1);
                this.channel.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}