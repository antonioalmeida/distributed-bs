package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public interface RemoteService extends Remote {

    void backupFile(String filePath, int replicationDegree) throws RemoteException;

    void recoverFile(String filePath) throws RemoteException;

    void deleteFile(String filePath) throws RemoteException;

    void reclaimSpace(long space) throws RemoteException;

    void retrieveState() throws RemoteException;
}
