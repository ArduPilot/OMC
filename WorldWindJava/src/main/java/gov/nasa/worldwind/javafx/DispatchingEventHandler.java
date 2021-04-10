package gov.nasa.worldwind.javafx;

import javafx.event.Event;
import javafx.event.EventHandler;

class DispatchingEventHandler<T extends Event> implements EventHandler<T>
{
    private EventSink eventSink;

    void setEventSink(EventSink eventSink)
    {
        this.eventSink = eventSink;
    }

    @Override
    public final void handle(T event)
    {
        if (eventSink == null)
        {
            handleEvent(event);
        }
        else
        {
            eventSink.accept(() -> handleEvent(event), true);
        }
    }

    public void handleEvent(T event) {}
}
