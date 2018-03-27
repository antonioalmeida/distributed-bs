package protocol;

import channel.Message;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public abstract class ProtocolInitiator implements Runnable {
    protected Message request;

    public ProtocolInitiator() {
    }
}
