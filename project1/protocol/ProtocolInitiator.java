package protocol;

import channel.Message;
import server.Peer;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public abstract class ProtocolInitiator implements Runnable {
    protected Message request;

    protected Peer peer;

    public ProtocolInitiator(Peer peer) {
        this.peer = peer;
    }
}
