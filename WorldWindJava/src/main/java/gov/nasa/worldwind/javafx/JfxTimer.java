/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.*;
import javafx.util.Duration;

class JfxTimer
{
    private final Duration delay;
    private final Runnable action;
    private final Timeline timeline;
    private int serial = 0;

    JfxTimer(Duration delay, Runnable action) {
        this.delay = delay;
        this.action = action;
        this.timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(delay));
        timeline.setCycleCount(1);
    }

    public void start() {
        stop();

        final int expectedSerial = this.serial;
        timeline.getKeyFrames().set(0, new KeyFrame(delay, new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (expectedSerial == serial)
                {
                    action.run();
                }
            }
        }));

        timeline.play();
    }

    public void stop() {
        timeline.stop();
        serial++;
    }
}
