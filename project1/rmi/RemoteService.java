package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public interface RemoteService extends Remote {
    // not sure what to add here yet
    String test() throws RemoteException;

    void backupFile(String filePath, int replicationDegree) throws RemoteException;

    void recoverFile(String filePath) throws RemoteException;
}
