package gov.nasa.worldwind.javafx;

public interface EventSink
{
    void accept(Runnable runnable, boolean preemptable);

    void acceptAndWait(Runnable runnable);

    void defer(Runnable runnable, boolean preemptable);
}
