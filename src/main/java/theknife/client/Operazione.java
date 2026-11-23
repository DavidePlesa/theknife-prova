package theknife.client;

public interface Operazione<T> {
    T esegui() throws Exception;
}