package theknife.client;

public interface Esito<T> {
    void successo(T risultato);
    void errore(Exception e);
}