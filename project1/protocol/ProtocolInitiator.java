package protocol;

import channel.Channel;
import channel.Message;
import channel.PutChunkMessage;
import server.Peer;
import utils.Globals;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public abstract class ProtocolInitiator implements Runnable {
    protected Peer peer;
    protected Channel channel;

    public ProtocolInitiator(Peer peer, Channel channel) {
        this.peer = peer;
        this.channel = channel;
    }

    protected void sendMessages(ArrayList<Message> messageList, int maxDelayTime) {
        try {
            for(Message message : messageList) {
                Thread.sleep(Utils.getRandomTime(maxDelayTime));
                this.channel.sendMessage(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
