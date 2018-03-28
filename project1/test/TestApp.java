package test;


import rmi.RemoteService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class TestApp {

    public static void main(final String args[]) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);

            RemoteService stub = (RemoteService) registry.lookup("remote");
            stub.backupFile("number.txt", 1);
            System.out.println("response");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
