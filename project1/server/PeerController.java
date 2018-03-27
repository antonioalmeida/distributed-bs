package server;

import channel.Message;
import receiver.*;

import java.io.IOException;

/**
 * Created by antonioalmeida on 27/03/2018.
 */
public class PeerController {

    private Peer peer;

    private Dispatcher dispatcher;

    private Receiver MCReceiver;
    private Receiver MDBReceiver;
    private Receiver MDRReceiver;

    public PeerController(Peer peer, String MCAddress, int MCPort, String MDBAddress, int MDBPort, String MDRAddress, int MDRPort) {
        this.peer = peer;

        this.dispatcher = new Dispatcher(this, peer.getPeerID());

        // subscribe to multicast channels
        try {
            this.MCReceiver = new ControlReceiver(MCAddress, MCPort, dispatcher);
            this.MDBReceiver = new BackupReceiver(MDBAddress, MDBPort, dispatcher);
            this.MDRReceiver = new RestoreReceiver(MDRAddress, MDRPort, dispatcher);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(MCReceiver).start();
        new Thread(MDBReceiver).start();
        new Thread(MDRReceiver).start();
    }

    public void handlePutchunkMessage(Message message) {
        System.out.println("Putchunk Message: " + message.fileID);
    }
}
