package test;


import rmi.RemoteService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class TestApp {

    private String host = null;
    private int port = 1099; //Default port
    private String name = null;

    /**
     * Main.
     *
     * @param args the args
     * @throws RemoteException the remote exception
     */
    public static void main(final String args[]) throws RemoteException {
        //TODO: add input validation
        new TestApp(args);
    }

    private TestApp(String[] args) throws RemoteException {
        parseLocation(args[0]);
        RemoteService stub = initApp();

        //only one of the following will be used
        String filePath = args[2];
        int extraArg;

        switch(args[1]) {
            case "backup":
                extraArg = Integer.parseInt(args[3]);
                stub.backupFile(filePath, extraArg);
                break;
            case "restore":
                stub.recoverFile(filePath);
                break;
            case "delete":
                stub.deleteFile(filePath);
                break;
            case "reclaim":
                extraArg = Integer.parseInt(args[2]);
                stub.reclaimSpace(extraArg);
                break;
            case "state":
                stub.retrieveState();
                break;
        }
    }

    private RemoteService initApp() {
        RemoteService stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            stub = (RemoteService) registry.lookup(name);
        } catch (Exception e) {
            System.err.println("Error connecting to remote object '" + name + "' on " + host);
            e.printStackTrace();
        }

        return stub;
    }

    private boolean parseLocation(String location) {
        // 1st group: host (mandatory)
        // 2nd group: port (optional)
        // 3rd group: name (mandatory)
        Pattern p = Pattern.compile("//([\\w.]+)(?::(\\d+))?/(\\w+)");
        Matcher m = p.matcher(location);

        if(!m.matches())
            return false;

        this.host = m.group(1);
        this.name = m.group(3);

        // if port exists
        if(m.group(2) != null)
            this.port = Integer.parseInt(m.group(2));

        return true;
    }
}
