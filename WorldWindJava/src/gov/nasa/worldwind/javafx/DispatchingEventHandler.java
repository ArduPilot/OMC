package gov.nasa.worldwind.javafx;

import javafx.event.Event;
import javafx.event.EventHandler;

class DispatchingEventHandler<T extends Event> implements EventHandler<T>
{
    private Sink sink;

    void setSink(Sink sink)
    {
        this.sink = sink;
    }

    @Override
    public final void handle(T event)
    {
        if (sink == null)
        {
            handleEvent(event);
        }
        else
        {
            sink.accept(() -> handleEvent(event));
        }
    }

    public void handleEvent(T event) {}
}
