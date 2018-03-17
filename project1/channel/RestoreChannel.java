package channel;

import java.io.IOException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class RestoreChannel extends Channel{
    public RestoreChannel(String address, int port) throws IOException {
        super(address, port);
    }
}
