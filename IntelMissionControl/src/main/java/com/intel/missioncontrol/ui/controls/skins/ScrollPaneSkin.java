/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls.skins;

import com.sun.javafx.event.CompositeEventHandler;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.NodeEventDispatcher;
import com.sun.javafx.scene.control.Properties;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * For some reason, the scrolling speed of the default ScrollPaneSkin implementation scales with the length of the
 * scrollable content. This skin implements a scrolling behavior that is invariant with regards to content length.
 */
@SuppressWarnings({"unused", "unchecked"})
public class ScrollPaneSkin extends javafx.scene.control.skin.ScrollPaneSkin {

    private static final double SCROLL_MULTIPLIER = 4;

    private static Field viewRectField;
    private static Field nodeHeightField;
    private static Field nodeWidthField;
    private static Field contentHeightField;
    private static Field contentWidthField;
    private static Field mouseDownField;
    private static Field touchDetectedField;
    private static Field contentsToViewTimelineField;
    private static Field eventHandlerMapField;
    private static Method startSBReleasedAnimationMethod;
    private static Method startContentsToViewportMethod;

    static {
        try {
            viewRectField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("viewRect");
            viewRectField.setAccessible(true);

            nodeHeightField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("nodeHeight");
            nodeHeightField.setAccessible(true);

            nodeWidthField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("nodeWidth");
            nodeWidthField.setAccessible(true);

            contentHeightField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("contentHeight");
            contentHeightField.setAccessible(true);

            contentWidthField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("contentWidth");
            contentWidthField.setAccessible(true);

            mouseDownField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("mouseDown");
            mouseDownField.setAccessible(true);

            touchDetectedField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("touchDetected");
            touchDetectedField.setAccessible(true);

            contentsToViewTimelineField =
                javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("contentsToViewTimeline");
            contentsToViewTimelineField.setAccessible(true);

            eventHandlerMapField = EventHandlerManager.class.getDeclaredField("eventHandlerMap");
            eventHandlerMapField.setAccessible(true);

            startSBReleasedAnimationMethod =
                javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredMethod("startSBReleasedAnimation");
            startSBReleasedAnimationMethod.setAccessible(true);

            startContentsToViewportMethod =
                javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredMethod("startContentsToViewport");
            startContentsToViewportMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private WeakReference<Timeline> scrollTimeline;
    private double currentV;
    private double currentH;

    public ScrollPaneSkin(final ScrollPane scrollpane) {
        super(scrollpane);

        try {
            StackPane viewRect = (StackPane)viewRectField.get(this);
            viewRect.setCache(false);
            NodeEventDispatcher internalEventDispatcher = (NodeEventDispatcher)viewRect.getEventDispatcher();
            Map<EventType<? extends Event>, CompositeEventHandler<? extends Event>> eventHandlerMap =
                (Map<EventType<? extends Event>, CompositeEventHandler<? extends Event>>)
                    eventHandlerMapField.get(internalEventDispatcher.getEventHandlerManager());
            eventHandlerMap.remove(ScrollEvent.SCROLL);

            currentV = getVerticalScrollBar().getValue();
            currentH = getHorizontalScrollBar().getValue();

            viewRect.addEventHandler(
                ScrollEvent.SCROLL,
                event -> {
                    try {
                        if (Properties.IS_TOUCH_SUPPORTED) {
                            startSBReleasedAnimationMethod.invoke(this);
                        }

                        ScrollBar vsb = getVerticalScrollBar();
                        ScrollBar hsb = getHorizontalScrollBar();
                        double nodeHeight = (double)nodeHeightField.get(this);
                        double nodeWidth = (double)nodeWidthField.get(this);
                        double contentHeight = (double)contentHeightField.get(this);
                        double contentWidth = (double)contentWidthField.get(this);
                        boolean mouseDown = (boolean)mouseDownField.get(this);
                        boolean touchDetected = (boolean)touchDetectedField.get(this);
                        Timeline contentsToViewTimeline = (Timeline)contentsToViewTimelineField.get(this);

                        if (vsb.getVisibleAmount() < vsb.getMax()) {
                            double vRange = vsb.getMax() - vsb.getMin();
                            double newValue = vsb.getMin();

                            if (nodeHeight > contentHeight) {
                                double delta = -event.getDeltaY() * SCROLL_MULTIPLIER;
                                double currentValue = (currentV - vsb.getMin()) / vRange;
                                newValue =
                                    (currentValue + delta / (nodeHeight - contentHeight)) * vRange + vsb.getMin();
                            }

                            if (!Properties.IS_TOUCH_SUPPORTED) {
                                if (newValue < vsb.getMin()) {
                                    currentV = vsb.getMin();
                                    scrollTo(vsb, vsb.getMin());
                                } else if (newValue > vsb.getMax()) {
                                    currentV = vsb.getMax();
                                    scrollTo(vsb, vsb.getMax());
                                } else if (currentV != newValue) {
                                    currentV = newValue;
                                    scrollTo(vsb, newValue);
                                    event.consume();
                                }
                            } else {
                                if (!event.isInertia()
                                        || (contentsToViewTimeline == null
                                            || contentsToViewTimeline.getStatus() == Animation.Status.STOPPED)) {
                                    vsb.setValue(newValue);
                                    if ((newValue > vsb.getMax() || newValue < vsb.getMin())
                                            && (!mouseDown && !touchDetected)) {
                                        startContentsToViewportMethod.invoke(this);
                                    }

                                    event.consume();
                                }
                            }
                        }

                        if (hsb.getVisibleAmount() < hsb.getMax()) {
                            double hRange = hsb.getMax() - hsb.getMin();
                            double newValue = hsb.getMin();

                            if (nodeHeight > contentHeight) {
                                double delta = -event.getDeltaX() * SCROLL_MULTIPLIER;
                                double currentValue = (currentH - hsb.getMin()) / hRange;
                                newValue = (currentValue + delta / (nodeWidth - contentWidth)) * hRange * hsb.getMin();
                            }

                            if (!Properties.IS_TOUCH_SUPPORTED) {
                                if (newValue < hsb.getMin()) {
                                    currentH = hsb.getMin();
                                    scrollTo(hsb, hsb.getMin());
                                } else if (newValue > hsb.getMax()) {
                                    currentH = hsb.getMax();
                                    scrollTo(hsb, hsb.getMax());
                                } else if (currentH != newValue) {
                                    currentH = newValue;
                                    scrollTo(hsb, newValue);
                                }
                            } else {
                                if (!event.isInertia()
                                        || (contentsToViewTimeline == null
                                            || contentsToViewTimeline.getStatus() == Animation.Status.STOPPED)) {
                                    hsb.setValue(newValue);

                                    if ((newValue > hsb.getMax() || newValue < hsb.getMin())
                                            && (!mouseDown && !touchDetected)) {
                                        startContentsToViewportMethod.invoke(this);
                                    }

                                    event.consume();
                                }
                            }
                        }
                    } catch (ReflectiveOperationException ex) {
                        ex.printStackTrace();
                    }
                });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void scrollTo(ScrollBar scrollBar, double newValue) {
        scrollBar.setValue(newValue);
    }

    // Could be used in place of scrollTo, but needs some improvement to make it look smooth.
    private void animateTo(ScrollBar scrollBar, double newValue) {
        Timeline currentTimeline = scrollTimeline != null ? scrollTimeline.get() : null;
        if (currentTimeline != null) {
            currentTimeline.stop();
        }

        Timeline timeline =
            new Timeline(
                new KeyFrame(
                    Duration.millis(200), new KeyValue(scrollBar.valueProperty(), newValue, Interpolator.LINEAR)));

        timeline.play();

        scrollTimeline = new WeakReference<>(timeline);
    }

}
