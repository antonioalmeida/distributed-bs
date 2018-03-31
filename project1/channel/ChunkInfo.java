package channel;

import java.util.Collections;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class ChunkInfo implements Comparable<ChunkInfo> {

    private int desiredReplicationDegree;
    private int actualReplicationDegree;

    public ChunkInfo(int desiredRepDegree, int actualRepDegree) {
        this.desiredReplicationDegree = desiredRepDegree;
        this.actualReplicationDegree = actualRepDegree;
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

    @Override
    public int compareTo(ChunkInfo o) {
        return this.getDegreeSatisfaction() - o.getDegreeSatisfaction();
    }
}
