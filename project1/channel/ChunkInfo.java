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

    public ChunkInfo(int desiredRepDegree, int actualRepDegree) {
        this.desiredReplicationDegree = desiredRepDegree;
        this.actualReplicationDegree = actualRepDegree;
        this.peerList = new ArrayList<>();
    }

    public int getActualReplicationDegree() {
        return actualReplicationDegree;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public void incActualReplicationDegree() {
        actualReplicationDegree++;
    }

    public void decActualReplicationDegree() {
        actualReplicationDegree--;
    }

    public int getDegreeSatisfaction() {
        return actualReplicationDegree - desiredReplicationDegree;
    }

    public boolean isDegreeSatisfied() {
        return actualReplicationDegree >= desiredReplicationDegree;
    }

    public boolean isBackedUpByPeer(int peerID) {
        return peerList.contains((Integer) peerID);
    }

    public void addPeer(int peerID) {
        peerList.add(peerID);
    }

    @Override
    public int compareTo(ChunkInfo o) {
        return this.getDegreeSatisfaction() - o.getDegreeSatisfaction();
    }

}
