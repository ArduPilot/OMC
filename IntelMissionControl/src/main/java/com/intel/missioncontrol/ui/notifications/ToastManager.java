package com.intel.missioncontrol.ui.notifications;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.asyncfx.concurrent.Dispatcher;

public class ToastManager {
    private final ListProperty<Toast> toasts = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final AnimationTimer toastTimer =
        new AnimationTimer() {
            private long lastTimestamp;

            @Override
            public void handle(long now) {
                if (lastTimestamp == 0) {
                    lastTimestamp = now;
                    return;
                }

                long elapsedTimeNanoseconds = now - lastTimestamp;
                lastTimestamp = now;
                handleElapsedTime((int)(elapsedTimeNanoseconds / 1000000));
            }
        };

    public ToastManager() {
        toastTimer.start();
    }

    public ReadOnlyListProperty<Toast> toastsProperty() {
        return toasts;
    }

    public void addToast(Toast toast) {
        if (Toast.Accessor.getIsShowing(toast)) {
            throw new IllegalArgumentException("The specified toast is already visible.");
        }

        if (Platform.isFxApplicationThread()) {
            toasts.add(toast);
            Toast.Accessor.addDismissHandler(toast, this::handleToastDismissed);
            Toast.Accessor.setIsShowing(toast, true);
        } else {
            Dispatcher.platform().runLater(() -> addToast(toast));
        }
    }

    private void handleElapsedTime(int millis) {
        boolean changed = false;
        for (Toast notification : toasts) {
            int remainingTime = Toast.Accessor.getRemainingTime(notification);
            remainingTime = remainingTime - millis;
            if (remainingTime < 0) {
                remainingTime = 0;
                changed = true;
            }

            Toast.Accessor.setRemainingTime(notification, remainingTime);
        }

        if (changed) {
            List<Toast> remainingToasts = new ArrayList<>();
            List<Toast> removedToasts = new ArrayList<>();
            for (Toast notification : toasts) {
                int remainingTime = Toast.Accessor.getRemainingTime(notification);
                if (remainingTime > 0) {
                    remainingToasts.add(notification);
                } else {
                    removedToasts.add(notification);
                }
            }

            toasts.setAll(remainingToasts);

            for (Toast toast : removedToasts) {
                toast.dismiss();
            }
        }
    }

    private void handleToastDismissed(final Toast toast) {
        if (Platform.isFxApplicationThread()) {
            toasts.remove(toast);
        } else {
            Dispatcher.platform().runLater(() -> toasts.remove(toast));
        }
    }

}
