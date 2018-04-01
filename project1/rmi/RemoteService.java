package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public interface RemoteService extends Remote {

    /**
     * Backup file.
     *
     * @param filePath          the file path
     * @param replicationDegree the replication degree
     * @throws RemoteException the remote exception
     */
    void backupFile(String filePath, int replicationDegree) throws RemoteException;

    /**
     * Recover file.
     *
     * @param filePath the file path
     * @throws RemoteException the remote exception
     */
    void recoverFile(String filePath) throws RemoteException;

    /**
     * Delete file.
     *
     * @param filePath the file path
     * @throws RemoteException the remote exception
     */
    void deleteFile(String filePath) throws RemoteException;

    /**
     * Reclaim space.
     *
     * @param space the space
     * @throws RemoteException the remote exception
     */
    void reclaimSpace(long space) throws RemoteException;

    /**
     * Retrieve state.
     *
     * @throws RemoteException the remote exception
     */
    void retrieveState() throws RemoteException;
}
