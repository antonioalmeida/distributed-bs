package test;


import rmi.RemoteService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class TestApp {

    public static void main(final String args[]) throws RemoteException {

        String host = (args.length < 1) ? null : args[0];
        RemoteService stub = initApp(host);
        String filePath = args[2];

        switch(args[1]) {
            case "backup":
                stub.backupFile(filePath, 1);
                break;
            case "restore":
                stub.recoverFile(filePath);
                break;
        }
    }

    private static RemoteService initApp(String host) {
        RemoteService stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(null);
            stub = (RemoteService) registry.lookup(host);
        } catch (Exception e) {
            System.err.println("Error connecting to remote object '" + host + "'");
            e.printStackTrace();
        }

        return stub;
    }
}
