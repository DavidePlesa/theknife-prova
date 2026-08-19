package theknife.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TheKnifeServiceFactory extends Remote {
    TheKnifeService getService() throws RemoteException;
}