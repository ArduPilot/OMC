/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Toast {

    private static final Duration DEFAULT_TIMEOUT = Duration.seconds(10);

    public static Toast createDefaultRunning(ILanguageHelper languageHelper, String name) {
        return Toast.of(ToastType.INFO)
            .setText(languageHelper.getString(Toast.class.getName() + ".defaultRunningMessage", name))
            .create();
    }

    public static Toast createDefaultSuccess(ILanguageHelper languageHelper, String name) {
        return Toast.of(ToastType.INFO)
            .setText(languageHelper.getString(Toast.class.getName() + ".defaultSuccessMessage", name))
            .create();
    }

    public static Toast createDefaultFailed(ILanguageHelper languageHelper, String name) {
        return Toast.of(ToastType.ALERT)
            .setText(languageHelper.getString(Toast.class.getName() + ".defaultFailedMessage", name))
            .create();
    }

    public static Toast createDefaultCancelled(ILanguageHelper languageHelper, String name) {
        return Toast.of(ToastType.INFO)
            .setText(languageHelper.getString(Toast.class.getName() + ".defaultCancelledMessage", name))
            .create();
    }

    @FunctionalInterface
    public interface DismissHandler {
        void toastDismissed(Toast toast);
    }

    public static class Accessor {
        public static void setRemainingTime(Toast toast, int millis) {
            toast.setRemainingTime(millis);
        }

        public static int getRemainingTime(Toast toast) {
            return toast.getRemainingTime();
        }

        public static void setIsShowing(Toast toast, boolean value) {
            toast.isShowing.set(value);
        }

        public static boolean getIsShowing(Toast toast) {
            return toast.isShowing.get();
        }

        public static void addDismissHandler(Toast toast, DismissHandler dismissHandler) {
            toast.dismissHandlers.add(dismissHandler);
        }
    }

    public static class ToastBuilder {

        private ToastType type;
        private boolean showIcon;
        private boolean checkableAction;
        private boolean autoClose;
        private String text;
        private String actionText;
        private Runnable action;
        private Executor actionExecutor;
        private DismissHandler customDismissHandler;
        private boolean closeable;
        private Duration timeout;

        ToastBuilder(ToastType type) {
            Expect.notNull(type, "type");
            this.type = type;
        }

        public ToastBuilder setShowIcon(boolean showsIcon) {
            this.showIcon = showsIcon;
            return this;
        }

        public ToastBuilder setText(String text) {
            this.text = text;
            return this;
        }

        /**
         * Configures the toast to include a clickable action.
         *
         * @param actionText The action text that appears on the toast.
         * @param action The action to be executed.
         * @param executor The executor that will be used to execute the action.
         */
        public ToastBuilder setAction(String actionText, Runnable action, Executor executor) {
            return setAction(actionText, false, false, action, executor);
        }

        /**
         * Configures the toast to include a clickable action.
         *
         * @param actionText The action text that appears on the toast.
         * @param actionCheckable If true, the action will appear as a check box. If false, it will appear as a link.
         * @param autoClose If true, the toast will auto-close after the action has been invoked.
         * @param action The action to be executed.
         * @param executor The executor that will be used to execute the action.
         */
        public ToastBuilder setAction(
                String actionText, boolean actionCheckable, boolean autoClose, Runnable action, Executor executor) {
            this.actionText = actionText;
            this.checkableAction = actionCheckable;
            this.autoClose = autoClose;
            this.action = action;
            this.actionExecutor = executor;
            return this;
        }

        public ToastBuilder setCloseable(boolean closeable) {
            this.closeable = closeable;
            return this;
        }

        public ToastBuilder setTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public ToastBuilder setOnDismissed(DismissHandler dismissHandler) {
            this.customDismissHandler = dismissHandler;
            return this;
        }

        public Toast create() {
            return new Toast(
                type,
                text,
                showIcon,
                checkableAction,
                autoClose,
                actionText,
                action,
                actionExecutor,
                customDismissHandler,
                timeout,
                closeable);
        }

    }

    private final ToastType type;
    private final String text;
    private final boolean showIcon;
    private final boolean checkableAction;
    private final boolean autoClose;
    private final String actionText;
    private final Runnable action;
    private final Executor actionExecutor;
    private final boolean closeable;
    private List<DismissHandler> dismissHandlers = new ArrayList<>();
    private int remainingTimeMillis;
    private BooleanProperty isShowing = new SimpleBooleanProperty();

    private Toast(
            ToastType type,
            String text,
            boolean showIcon,
            boolean checkableAction,
            boolean autoClose,
            @Nullable String actionText,
            @Nullable Runnable action,
            @Nullable Executor actionExecutor,
            @Nullable DismissHandler dismissHandler,
            @Nullable Duration timeout,
            boolean closeable) {
        if (actionText != null || action != null || actionExecutor != null) {
            Expect.notNull(
                actionText, "actionText",
                action, "action",
                actionExecutor, "actionExecutor");
        }

        this.type = type;
        this.showIcon = showIcon;
        this.checkableAction = checkableAction;
        this.autoClose = autoClose;
        this.text = text;
        this.actionText = actionText;
        this.action = action;
        this.actionExecutor = actionExecutor;
        this.closeable = closeable;
        this.remainingTimeMillis = timeout != null ? (int)timeout.toMillis() : (int)DEFAULT_TIMEOUT.toMillis();

        if (dismissHandler != null) {
            this.dismissHandlers.add(dismissHandler);
        }
    }

    public static ToastBuilder of(ToastType type) {
        return new ToastBuilder(type);
    }

    public ToastType getType() {
        return type;
    }

    public boolean showIcon() {
        return showIcon;
    }

    public boolean isCheckableAction() {
        return checkableAction;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public String getText() {
        return text;
    }

    public String getActionText() {
        return actionText;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public int getRemainingTime() {
        return remainingTimeMillis;
    }

    public ReadOnlyBooleanProperty isShowingProperty() {
        return isShowing;
    }

    public synchronized void dismiss() {
        if (!isShowing.get()) {
            return;
        }

        isShowing.set(false);

        for (DismissHandler handler : dismissHandlers) {
            handler.toastDismissed(this);
        }
    }

    private void setRemainingTime(int millis) {
        this.remainingTimeMillis = millis;
    }

    @Override
    public String toString() {
        return type == null ? text : (type.toString() + ": " + text);
    }

    void executeAction() {
        if (action != null) {
            actionExecutor.execute(action);
        }
    }

}
