package gov.nasa.worldwind.javafx;

public interface Sink
{
    void accept(Runnable runnable);

    void acceptAndWait(Runnable runnable);
}
