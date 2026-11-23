package theknife.client;

import javafx.application.Platform;
import theknife.common.TheKnifeService;
import theknife.common.TheKnifeServiceFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;

public final class GestoreRichieste {

    private static final int NUM_THREAD = 2;
    private static final GestoreRichieste ISTANZA = new GestoreRichieste();

    // coda e stato: protetti da synchronized su this
    private final LinkedList<Lavoro> coda = new LinkedList<Lavoro>();
    private final Thread[] threads = new Thread[NUM_THREAD];
    private boolean avviato;
    private boolean terminato;

    // servizio RMI: protetto da un lock separato
    private final Object lockServizio = new Object();
    private TheKnifeService servizio;

    private GestoreRichieste() {
    }

    public static GestoreRichieste getInstance() {
        return ISTANZA;
    }

    // ---------- Servizio RMI condiviso (chiamare solo dentro un'Operazione) ----------

    public TheKnifeService getService() throws Exception {
        synchronized (lockServizio) {
            if (servizio == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                TheKnifeServiceFactory factory =
                        (TheKnifeServiceFactory) registry.lookup("TheKnifeFactory");
                servizio = factory.getService();
            }
            return servizio;
        }
    }

    public void resetServizio() {
        synchronized (lockServizio) {
            servizio = null;
        }
    }

    // ---------- Lavori ----------

    private interface Lavoro {
        void svolgi();
    }

    private class LavoroConcreto<T> implements Lavoro {
        private final Operazione<T> operazione;
        private final Esito<T> esito;

        LavoroConcreto(Operazione<T> operazione, Esito<T> esito) {
            this.operazione = operazione;
            this.esito = esito;
        }

        @Override
        public void svolgi() {
            try {
                final T risultato = operazione.esegui();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        esito.successo(risultato);
                    }
                });
            } catch (final Exception ex) {
                if (ex instanceof RemoteException) {
                    resetServizio();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        esito.errore(ex);
                    }
                });
            }
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Lavoro l = prossimoLavoro();
                    if (l == null) {
                        break;
                    }
                    l.svolgi();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private synchronized Lavoro prossimoLavoro() throws InterruptedException {
        while (coda.isEmpty() && !terminato) {
            wait();
        }
        if (terminato) {
            return null;
        }
        return coda.removeFirst();
    }

    /** Da chiamare dal thread JavaFX. */
    public synchronized <T> void exec(Operazione<T> operazione, Esito<T> esito) {
        if (terminato) {
            return;
        }
        if (!avviato) {
            for (int i = 0; i < NUM_THREAD; i++) {
                threads[i] = new Thread(new Worker());
                threads[i].start();
            }
            avviato = true;
        }
        coda.addLast(new LavoroConcreto<T>(operazione, esito));
        notifyAll();
    }

    /** Da chiamare alla chiusura dell'applicazione. */
    public void chiudi() {
        synchronized (this) {
            terminato = true;
            coda.clear();
            notifyAll();
        }
        for (int i = 0; i < NUM_THREAD; i++) {
            if (threads[i] != null) {
                try {
                    threads[i].join(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}