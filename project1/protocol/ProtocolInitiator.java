package protocol;

import receiver.Channel;
import message.Message;
import peer.Peer;
import utils.Utils;

import java.util.ArrayList;

/**
 * Created by antonioalmeida on 21/03/2018.
 */
public abstract class ProtocolInitiator implements Runnable {
    /**
     * The Peer.
     */
    protected Peer peer;
    /**
     * The Channel.
     */
    protected Channel channel;

    /**
     * Instantiates a new Protocol initiator.
     *
     * @param peer    the peer
     * @param channel the message
     */
    public ProtocolInitiator(Peer peer, Channel channel) {
        this.peer = peer;
        this.channel = channel;
    }

    /**
     * Send messages.
     *
     * @param messageList  the message list
     * @param maxDelayTime the max delay time
     */
    protected void sendMessages(ArrayList<Message> messageList, int maxDelayTime) {
        try {
            for(Message message : messageList) {
                Thread.sleep(Utils.getRandomTime(maxDelayTime));
                this.channel.sendMessage(message);
                System.out.println("Sent putchunk message: " + message.getChunkIndex());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
