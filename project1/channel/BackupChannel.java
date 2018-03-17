package channel;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class BackupChannel extends Channel {

    public BackupChannel(String address, int port) throws IOException {
        super(address, port);
    }

}
