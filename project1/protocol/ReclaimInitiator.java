package protocol;

import server.Peer;

/**
 * Created by antonioalmeida on 30/03/2018.
 */
public class ReclaimInitiator extends ProtocolInitiator {

    private long space;

    public ReclaimInitiator(Peer peer, long space) {
        super(peer, null);
        this.space = space;
    }

    @Override
    public void run() {
        if(peer.getController().reclaimSpace(space))
            System.out.println("Sucessfully reclaimed down to " + space + " bytes");
        else
            System.out.println("Couldn't reclaim" + space + " bytes");
    }
}
