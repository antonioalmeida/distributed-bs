package channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class ChunkInfo implements Comparable<ChunkInfo> {

    private int desiredReplicationDegree;
    private int actualReplicationDegree;

    // peers that are storing this chunk
    private ArrayList<Integer> peerList;

    /**
     * Instantiates a new Chunk info.
     *
     * @param desiredRepDegree the desired rep degree
     * @param actualRepDegree  the actual rep degree
     */
    public ChunkInfo(int desiredRepDegree, int actualRepDegree) {
        this.desiredReplicationDegree = desiredRepDegree;
        this.actualReplicationDegree = actualRepDegree;
        this.peerList = new ArrayList<>();
    }

    /**
     * Gets actual replication degree.
     *
     * @return the actual replication degree
     */
    public int getActualReplicationDegree() {
        return actualReplicationDegree;
    }

    /**
     * Gets desired replication degree.
     *
     * @return the desired replication degree
     */
    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    /**
     * Inc actual replication degree.
     */
    public void incActualReplicationDegree() {
        actualReplicationDegree++;
    }

    /**
     * Dec actual replication degree.
     */
    public void decActualReplicationDegree() {
        actualReplicationDegree--;
    }

    /**
     * Gets degree satisfaction.
     *
     * @return the degree satisfaction
     */
    public int getDegreeSatisfaction() {
        return actualReplicationDegree - desiredReplicationDegree;
    }

    /**
     * Is degree satisfied boolean.
     *
     * @return the boolean
     */
    public boolean isDegreeSatisfied() {
        return actualReplicationDegree >= desiredReplicationDegree;
    }

    /**
     * Is backed up by peer boolean.
     *
     * @param peerID the peer id
     * @return the boolean
     */
    public boolean isBackedUpByPeer(int peerID) {
        return peerList.contains((Integer) peerID);
    }

    /**
     * Add peer.
     *
     * @param peerID the peer id
     */
    public void addPeer(int peerID) {
        peerList.add(peerID);
    }

    @Override
    public int compareTo(ChunkInfo o) {
        return this.getDegreeSatisfaction() - o.getDegreeSatisfaction();
    }

}
