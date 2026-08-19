package theknife.server;

import theknife.common.TheKnifeService;
import theknife.common.TheKnifeServiceFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TheKnifeServiceFactoryImpl extends UnicastRemoteObject implements TheKnifeServiceFactory {

    public TheKnifeServiceFactoryImpl() throws RemoteException {
        super();
    }

    @Override
    public TheKnifeService getService() throws RemoteException {
        TheKnifeServiceImpl service = new TheKnifeServiceImpl();
        return (TheKnifeService) UnicastRemoteObject.exportObject(service, 0);
    }
}