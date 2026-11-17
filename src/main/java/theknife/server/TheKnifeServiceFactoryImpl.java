package theknife.server;

import theknife.common.TheKnifeService;
import theknife.common.TheKnifeServiceFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class TheKnifeServiceFactoryImpl extends UnicastRemoteObject implements TheKnifeServiceFactory {

    public TheKnifeServiceFactoryImpl() throws RemoteException {
        super();
    }

    @Override
    public TheKnifeService getService() throws RemoteException {
        try {
            TheKnifeServiceImpl service = new TheKnifeServiceImpl();
            return (TheKnifeService) UnicastRemoteObject.exportObject(service, 0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Impossibile creare TheKnifeServiceImpl: " + e.getMessage(), e);
        }
    }
}