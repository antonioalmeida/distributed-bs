package channel;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class ControlChannel extends Channel {
    public ControlChannel(String address, int port) throws IOException {
        super(address, port);
    }

    @Override
    public void run() {
        super.run();
    }
}
