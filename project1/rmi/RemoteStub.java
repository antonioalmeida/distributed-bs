package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by antonioalmeida on 17/03/2018.
 */
public class RemoteStub implements RemoteService {

    @Override
    public String test() {
        System.out.println("Testing RMI");
        return "Testing RMI";
    }

    protected void initRemoteStub(String accessPoint) {
        try {
            RemoteStub obj = new RemoteStub();
            RemoteService stub = (RemoteService) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
