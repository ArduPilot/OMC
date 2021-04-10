/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.Logging;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

public class JfxInputHandler extends WWObjectImpl implements InputHandler
{
    private static final double DISCARD_CLICK_DEFAULT_MOVE_DISTANCE = 10;

    private class MouseDraggingInfo
    {
        private final Point pressedPoint;
        private boolean isCanceled;

        MouseDraggingInfo(Point point)
        {
            pressedPoint = point;
        }

        void update(Point point)
        {
            if (isCanceled)
            {
                return;
            }

            double dx = point.x - pressedPoint.x;
            double dy = point.y - pressedPoint.y;
            if (Math.sqrt(dx * dx + dy * dy) >= discardClickMoveDistance)
            {
                isCanceled = true;
            }
        }

        boolean isCanceled()
        {
            return isCanceled;
        }
    }

    private enum PointerMode
    {
        MOUSE, TOUCH
    }

    private final boolean flipY;
    private final boolean dpiAware;
    private WorldWindow wwd;
    private EventSink eventSink;
    private EventListenerList eventListeners = new EventListenerList();
    private PickedObjectList hoverObjects;
    private PickedObjectList objectsAtButtonPress;
    private SelectListener selectListener;
    private Point mousePoint;
    private TouchPoint firstTouchPoint;
    private boolean isSingleTouch;
    private boolean isHovering;
    private boolean isDragging;
    private boolean isPressed;
    private int currentTouchPoints;
    private PointerMode currentPointerMode;
    private MouseDraggingInfo primaryButtonDraggingInfo;
    private MouseDraggingInfo secondaryButtonDraggingInfo;
    private double discardClickMoveDistance = DISCARD_CLICK_DEFAULT_MOVE_DISTANCE;

    private JfxTimer hoverTimer = new JfxTimer(Duration.millis(600), new Runnable()
    {
        @Override
        public void run()
        {
            if (eventSink != null)
            {
                eventSink.accept(() -> {
                    if (pickMatches(hoverObjects))
                    {
                        isHovering = true;
                        callSelectListeners(new SelectEvent(wwd, SelectEvent.HOVER, mousePoint, hoverObjects));
                        hoverTimer.stop();
                    }
                }, true);
            }
            else
            {
                if (pickMatches(hoverObjects))
                {
                    isHovering = true;
                    callSelectListeners(new SelectEvent(wwd, SelectEvent.HOVER, mousePoint, hoverObjects));
                    hoverTimer.stop();
                }
            }
        }
    });

    private final DispatchingEventHandler<KeyEvent> keyPressedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(KeyEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            java.awt.event.KeyEvent keyEvent = convertFxToAwtEvent(event, java.awt.event.KeyEvent.KEY_PRESSED);
            callKeyPressedListeners(keyEvent);

            if (!keyEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().keyPressed(keyEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }
    };

    private final DispatchingEventHandler<KeyEvent> keyReleasedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(KeyEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            java.awt.event.KeyEvent keyEvent = convertFxToAwtEvent(event, java.awt.event.KeyEvent.KEY_RELEASED);
            callKeyReleasedListeners(keyEvent);

            if (!keyEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().keyReleased(keyEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }
    };

    private final DispatchingEventHandler<KeyEvent> keyTypedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(KeyEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            java.awt.event.KeyEvent keyEvent = convertFxToAwtEvent(event, java.awt.event.KeyEvent.KEY_TYPED);
            callKeyTypedListeners(keyEvent);

            if (!keyEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().keyTyped(keyEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mousePressedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isSynthesized())
            {
                return;
            }

            handlePointerPressed(
                PointerMode.MOUSE,
                convertMousePoint(event),
                event.getButton(),
                convertFxToAwtEvent(event, java.awt.event.MouseEvent.MOUSE_PRESSED));
        }
    };

    private final DispatchingEventHandler<TouchEvent> touchPressedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(TouchEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.MOUSE)
            {
                return;
            }

            currentTouchPoints = event.getTouchCount();

            if (currentTouchPoints == 1 && currentPointerMode != PointerMode.TOUCH)
            {
                if (firstTouchPoint == null)
                {
                    firstTouchPoint = event.getTouchPoint();
                    isSingleTouch = true;
                }

                handlePointerMoved(
                    convertTouchPoint(firstTouchPoint),
                    convertFxToAwtEvent(event, firstTouchPoint, java.awt.event.MouseEvent.MOUSE_MOVED));

                eventSink.defer(() ->
                    handlePointerPressed(
                        PointerMode.TOUCH,
                        convertTouchPoint(firstTouchPoint),
                        MouseButton.PRIMARY,
                        convertFxToAwtEvent(event, firstTouchPoint, java.awt.event.MouseEvent.MOUSE_PRESSED)),
                    true);
            }
            else if (currentTouchPoints > 1 && currentPointerMode == PointerMode.TOUCH)
            {
                isSingleTouch = false;
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseReleasedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isSynthesized())
            {
                return;
            }

            handlePointerReleased(
                convertMousePoint(event),
                convertFxToAwtEvent(event, java.awt.event.MouseEvent.MOUSE_RELEASED));
        }
    };

    private final DispatchingEventHandler<TouchEvent> touchReleasedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(TouchEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.MOUSE)
            {
                return;
            }

            currentTouchPoints = event.getTouchCount();

            if (currentTouchPoints == 1)
            {
                TouchPoint touchPoint = firstTouchPoint;

                if (primaryButtonDraggingInfo != null && primaryButtonDraggingInfo.isCanceled()) {
                    touchPoint = event.getTouchPoint();
                }

                handlePointerReleased(
                    convertTouchPoint(touchPoint),
                    convertFxToAwtEvent(event, touchPoint, java.awt.event.MouseEvent.MOUSE_RELEASED));

                if (isSingleTouch)
                {
                    handlePointerClicked(
                        MouseButton.PRIMARY,
                        1,
                        convertFxToAwtEvent(event, touchPoint, java.awt.event.MouseEvent.MOUSE_CLICKED));
                }

                firstTouchPoint = null;
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseMovedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isSynthesized())
            {
                return;
            }

            handlePointerMoved(
                convertMousePoint(event),
                convertFxToAwtEvent(event, java.awt.event.MouseEvent.MOUSE_MOVED));
        }
    };

    private final DispatchingEventHandler<TouchEvent> touchMovedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(TouchEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.MOUSE)
            {
                return;
            }

            if (currentTouchPoints == 1)
            {
                handlePointerDragged(
                    convertTouchPoint(event.getTouchPoint()),
                    convertFxToAwtEvent(event, event.getTouchPoint(), java.awt.event.MouseEvent.MOUSE_DRAGGED));
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseDraggedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isSynthesized())
            {
                return;
            }

            handlePointerDragged(
                convertMousePoint(event),
                convertFxToAwtEvent(event, java.awt.event.MouseEvent.MOUSE_DRAGGED));
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseClickedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isSynthesized())
            {
                return;
            }

            handlePointerClicked(
                event.getButton(),
                event.getClickCount(),
                convertFxToAwtEvent(event, java.awt.event.MouseEvent.MOUSE_CLICKED));
        }
    };

    private final DispatchingEventHandler<ScrollEvent> scrollHandler = new DispatchingEventHandler<>()
    {
        private static final int MAX_SCROLL_EVENT_SEPARATION_MILLIS = 100;
        private static final int PIXELS_PER_SCROLL_STEP = 10;

        private long lastTimestamp = System.currentTimeMillis();
        private double lastY;

        @Override
        public void handleEvent(ScrollEvent event)
        {
            if (wwd == null || currentPointerMode == PointerMode.TOUCH || event.isInertia())
            {
                return;
            }

            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - lastTimestamp > MAX_SCROLL_EVENT_SEPARATION_MILLIS)
            {
                lastTimestamp = currentTimestamp;
                lastY = 0;
            }

            lastY += event.getDeltaY();
            int scrollSteps = (int)lastY / PIXELS_PER_SCROLL_STEP;
            if (scrollSteps == 0)
            {
                return;
            }

            lastTimestamp = currentTimestamp;
            lastY = 0;

            java.awt.event.MouseWheelEvent mouseWheelEvent = convertFxToAwtEvent(event, -scrollSteps);
            callMouseWheelMovedListeners(mouseWheelEvent);

            if (!mouseWheelEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }
    };

    private final DispatchingEventHandler<ScrollEvent> scrollStartedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(ScrollEvent event) {
            if (wwd == null) {
                return;
            }

            currentPointerMode = PointerMode.TOUCH;
        }
    };

    private final DispatchingEventHandler<ScrollEvent> scrollFinishedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(ScrollEvent event) {
            if (wwd == null) {
                return;
            }

            currentPointerMode = null;
        }
    };

    private final DispatchingEventHandler<ZoomEvent> zoomHandler = new DispatchingEventHandler<>()
    {
        private static final int MAX_EVENT_SEPARATION_MILLIS = 100;
        private static final double ZOOM_DELTA_PER_STEP = 0.07;

        private long lastTimestamp = System.currentTimeMillis();
        private double lastZoomFactor;

        @Override
        public void handleEvent(ZoomEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - lastTimestamp > MAX_EVENT_SEPARATION_MILLIS)
            {
                lastTimestamp = currentTimestamp;
                lastZoomFactor = 0;
            }

            lastZoomFactor += 1.0 - event.getZoomFactor();
            int scrollSteps = (int)(lastZoomFactor / ZOOM_DELTA_PER_STEP);
            if (scrollSteps == 0)
            {
                return;
            }

            lastTimestamp = currentTimestamp;
            lastZoomFactor = 0;

            java.awt.event.MouseWheelEvent mouseWheelEvent = convertFxToAwtEvent(event, scrollSteps);
            callMouseWheelMovedListeners(mouseWheelEvent);

            if (!mouseWheelEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }
    };

    JfxInputHandler(boolean flipY, boolean dpiAware) {
        this.flipY = flipY;
        this.dpiAware = dpiAware;
    }

    void setEventSink(EventSink eventSink)
    {
        this.eventSink = eventSink;
        keyPressedHandler.setEventSink(eventSink);
        keyReleasedHandler.setEventSink(eventSink);
        keyTypedHandler.setEventSink(eventSink);
        mousePressedHandler.setEventSink(eventSink);
        mouseReleasedHandler.setEventSink(eventSink);
        mouseMovedHandler.setEventSink(eventSink);
        mouseDraggedHandler.setEventSink(eventSink);
        mouseClickedHandler.setEventSink(eventSink);
        touchPressedHandler.setEventSink(eventSink);
        touchReleasedHandler.setEventSink(eventSink);
        touchMovedHandler.setEventSink(eventSink);
        scrollHandler.setEventSink(eventSink);
        scrollStartedHandler.setEventSink(eventSink);
        scrollFinishedHandler.setEventSink(eventSink);
        zoomHandler.setEventSink(eventSink);
    }

    @Override
    public void setEventSource(WorldWindow newWorldWindow)
    {
        if (newWorldWindow != null && !(newWorldWindow instanceof Node))
        {
            String message = Logging.getMessage("Jfx.JfxInputHandler.EventSourceNotANode");
            Logging.logger().finer(message);
            throw new IllegalArgumentException(message);
        }

        if (newWorldWindow == wwd)
        {
            return;
        }

        eventListeners = new EventListenerList(); // make orphans of listener references

        if (wwd != null)
        {
            Node node = (Node) wwd;
            node.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            node.removeEventHandler(KeyEvent.KEY_TYPED, keyTypedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
            node.removeEventHandler(TouchEvent.TOUCH_PRESSED, touchPressedHandler);
            node.removeEventHandler(TouchEvent.TOUCH_RELEASED, touchReleasedHandler);
            node.removeEventHandler(TouchEvent.TOUCH_MOVED, touchMovedHandler);
            node.removeEventHandler(ScrollEvent.SCROLL, scrollHandler);
            node.removeEventHandler(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
            node.removeEventHandler(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
            node.removeEventHandler(ZoomEvent.ZOOM, zoomHandler);

            if (selectListener != null)
            {
                wwd.removeSelectListener(this.selectListener);
            }

            if (wwd.getSceneController() != null)
            {
                wwd.getSceneController().removePropertyChangeListener(AVKey.VIEW, this);
            }
        }

        wwd = newWorldWindow;
        if (wwd == null)
        {
            return;
        }

        Node node = (Node)wwd;
        node.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        node.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        node.addEventHandler(KeyEvent.KEY_TYPED, keyTypedHandler);
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        node.addEventHandler(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        node.addEventHandler(TouchEvent.TOUCH_PRESSED, touchPressedHandler);
        node.addEventHandler(TouchEvent.TOUCH_RELEASED, touchReleasedHandler);
        node.addEventHandler(TouchEvent.TOUCH_MOVED, touchMovedHandler);
        node.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
        node.addEventHandler(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
        node.addEventHandler(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
        node.addEventHandler(ZoomEvent.ZOOM, zoomHandler);

        selectListener = new SelectListener()
        {
            public void selected(SelectEvent event)
            {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                {
                    doHover(true);
                }
            }
        };

        wwd.addSelectListener(this.selectListener);

        if (wwd.getSceneController() != null)
        {
            wwd.getSceneController().addPropertyChangeListener(AVKey.VIEW, this);
        }
    }

    @Override
    public WorldWindow getEventSource()
    {
        return wwd;
    }

    @Override
    public void setHoverDelay(int delay)
    {
    }

    @Override
    public int getHoverDelay()
    {
        return 0;
    }

    @Override
    public synchronized void addSelectListener(SelectListener listener)
    {
        eventListeners.add(SelectListener.class, listener);
    }

    @Override
    public synchronized void removeSelectListener(SelectListener listener)
    {
        eventListeners.remove(SelectListener.class, listener);
    }

    @Override
    public synchronized void addKeyListener(KeyListener listener)
    {
        eventListeners.add(KeyListener.class, listener);
    }

    @Override
    public synchronized void removeKeyListener(KeyListener listener)
    {
        eventListeners.remove(KeyListener.class, listener);
    }

    @Override
    public synchronized void addMouseListener(MouseListener listener)
    {
        eventListeners.add(MouseListener.class, listener);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener listener)
    {
        eventListeners.remove(MouseListener.class, listener);
    }

    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener listener)
    {
        eventListeners.add(MouseMotionListener.class, listener);
    }

    @Override
    public synchronized void removeMouseMotionListener(MouseMotionListener listener)
    {
        eventListeners.remove(MouseMotionListener.class, listener);
    }

    @Override
    public synchronized void addMouseWheelListener(MouseWheelListener listener)
    {
        eventListeners.add(MouseWheelListener.class, listener);
    }

    @Override
    public synchronized void removeMouseWheelListener(MouseWheelListener listener)
    {
        eventListeners.remove(MouseWheelListener.class, listener);
    }

    @Override
    public void dispose()
    {
        this.hoverTimer.stop();
        this.hoverTimer = null;

        this.setEventSink(null);
        this.setEventSource(null);

        if (this.hoverObjects != null)
        {
            this.hoverObjects.clear();
        }

        this.hoverObjects = null;

        if (this.objectsAtButtonPress != null)
        {
            this.objectsAtButtonPress.clear();
        }

        this.objectsAtButtonPress = null;
    }

    @Override
    public boolean isForceRedrawOnMousePressed()
    {
        return false;
    }

    @Override
    public void setForceRedrawOnMousePressed(boolean forceRedrawOnMousePressed)
    {
        throw new UnsupportedOperationException();
    }

    public void setDiscardClickMoveDistance(double distanceInPixels)
    {
        discardClickMoveDistance = distanceInPixels;
    }

    public double getDiscardClickMoveDistance()
    {
        return discardClickMoveDistance;
    }

    private boolean isPickListEmpty(PickedObjectList pickList)
    {
        return pickList == null || pickList.size() < 1;
    }

    private void doHover(boolean reset)
    {
        PickedObjectList pickedObjects = wwd.getObjectsAtCurrentPosition();
        if (!(isPickListEmpty(hoverObjects) || isPickListEmpty(pickedObjects)))
        {
            PickedObject hover = hoverObjects.getTopPickedObject();
            PickedObject last = pickedObjects.getTopPickedObject();
            Object oh = hover == null ? null : hover.getObject() != null ? hover.getObject() : hover.getParentLayer();
            Object ol = last == null ? null : last.getObject() != null ? last.getObject() : last.getParentLayer();

            if (oh != null && oh.equals(ol))
            {
                return; // object picked is the hover object. don't do anything but wait for the timer to expire.
            }
        }

        this.cancelHover();

        if (!reset)
        {
            return;
        }

        if ((pickedObjects != null)
                && (pickedObjects.getTopObject() != null)
                && pickedObjects.getTopPickedObject().isTerrain())
        {
            return;
        }

        hoverObjects = pickedObjects;
        hoverTimer.start();
    }

    private void cancelHover()
    {
        if (isHovering)
        {
            callSelectListeners(new SelectEvent(wwd, SelectEvent.HOVER, mousePoint, null));
        }

        isHovering = false;
        hoverObjects = null;
        hoverTimer.stop();
    }

    private boolean pickMatches(PickedObjectList pickedObjects)
    {
        if (isPickListEmpty(wwd.getObjectsAtCurrentPosition()) || isPickListEmpty(pickedObjects))
        {
            return false;
        }

        PickedObject lastTop = wwd.getObjectsAtCurrentPosition().getTopPickedObject();

        if (null != lastTop && lastTop.isTerrain())
        {
            return false;
        }

        PickedObject newTop = pickedObjects.getTopPickedObject();
        //noinspection SimplifiableIfStatement
        if (lastTop == null || newTop == null || lastTop.getObject() == null || newTop.getObject() == null)
        {
            return false;
        }

        return lastTop.getObject().equals(newTop.getObject());
    }

    private void cancelDrag()
    {
        if (isDragging)
        {
            callSelectListeners(
                new DragSelectEvent(
                    wwd, SelectEvent.DRAG_END, null, objectsAtButtonPress, mousePoint));
        }

        isDragging = false;
    }

    private void handlePointerPressed(
            PointerMode pointerMode, Point newPoint, MouseButton button, java.awt.event.MouseEvent mouseEvent)
    {
        currentPointerMode = pointerMode;
        isPressed = true;

        // Determine if the mouse point has changed since the last mouse move event. This can happen if user switches to
        // another window, moves the mouse, and then switches back to the World Wind window.
        boolean mousePointChanged = !newPoint.equals(mousePoint);

        mousePoint = newPoint;

        cancelHover();
        cancelDrag();

        // If the mouse point has changed then we need to set a new pick point, and redraw the scene because the current
        // picked object list may not reflect the current mouse position.
        if (mousePointChanged && wwd.getSceneController() != null)
        {
            wwd.getSceneController().setPickPoint(mousePoint);
        }

        if (isForceRedrawOnMousePressed() || mousePointChanged)
        {
            wwd.redrawNow();
        }

        if (button == MouseButton.PRIMARY)
        {
            primaryButtonDraggingInfo = new MouseDraggingInfo(newPoint);
        }
        else if (button == MouseButton.SECONDARY)
        {
            secondaryButtonDraggingInfo = new MouseDraggingInfo(newPoint);
        }

        objectsAtButtonPress = wwd.getObjectsAtCurrentPosition();

        callMousePressedListeners(mouseEvent);

        if (objectsAtButtonPress != null && objectsAtButtonPress.getTopPickedObject() != null
                && !objectsAtButtonPress.getTopPickedObject().isTerrain())
        {
            // Something is under the cursor, so it's deemed "selected".
            if (button == MouseButton.PRIMARY)
            {
                callSelectListeners(
                    new SelectEvent(wwd, SelectEvent.LEFT_PRESS, mouseEvent, objectsAtButtonPress));
            }
            else if (button == MouseButton.SECONDARY)
            {
                callSelectListeners(
                    new SelectEvent(wwd, SelectEvent.RIGHT_PRESS, mouseEvent, objectsAtButtonPress));
            }

            // Initiate a repaint.
            wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());
        }

        if (!mouseEvent.isConsumed())
        {
            try
            {
                wwd.getView().getViewInputHandler().mousePressed(mouseEvent);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private void handlePointerReleased(Point newPoint, java.awt.event.MouseEvent mouseEvent)
    {
        currentPointerMode = null;

        if (isPressed)
        {
            isPressed = false;
            mousePoint = newPoint;

            callMouseReleasedListeners(mouseEvent);

            if (!mouseEvent.isConsumed()) {
                try
                {
                    wwd.getView().getViewInputHandler().mouseReleased(mouseEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }

            doHover(true);
            cancelDrag();
        }
    }

    private void handlePointerClicked(MouseButton button, int clickCount, java.awt.event.MouseEvent mouseEvent)
    {
        if (wwd.getView() == null)
        {
            return;
        }

        boolean primaryCanceled = primaryButtonDraggingInfo == null || primaryButtonDraggingInfo.isCanceled();
        boolean secondaryCanceled = secondaryButtonDraggingInfo == null
                || secondaryButtonDraggingInfo.isCanceled();
        primaryButtonDraggingInfo = null;
        secondaryButtonDraggingInfo = null;

        if (primaryCanceled && secondaryCanceled)
        {
            return;
        }

        PickedObjectList pickedObjects = wwd.getObjectsAtCurrentPosition();

        callMouseClickedListeners(mouseEvent);

        if (pickedObjects != null && pickedObjects.getTopPickedObject() != null
                && !pickedObjects.getTopPickedObject().isTerrain())
        {
            // Something is under the cursor, so it's deemed "selected".
            if (button == MouseButton.PRIMARY && !primaryCanceled)
            {
                if (clickCount <= 1)
                {
                    callSelectListeners(
                        new SelectEvent(wwd, SelectEvent.LEFT_CLICK, mouseEvent, pickedObjects));
                }
                else
                {
                    callSelectListeners(
                        new SelectEvent(
                            wwd, SelectEvent.LEFT_DOUBLE_CLICK, mouseEvent, pickedObjects));
                }
            }
            else if (button == MouseButton.SECONDARY && !secondaryCanceled)
            {
                callSelectListeners(new SelectEvent(wwd, SelectEvent.RIGHT_CLICK, mouseEvent, pickedObjects));
            }

            wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());
        }
        else
        {
            if (!mouseEvent.isConsumed())
            {
                //this.wwd.getView().getViewInputHandler().mouseClicked(mouseEvent);
            }
        }
    }

    private void handlePointerMoved(Point newPoint, java.awt.event.MouseEvent mouseEvent)
    {
        mousePoint = newPoint;
        callMouseMovedListeners(mouseEvent);

        if (!mouseEvent.isConsumed())
        {
            try
            {
                wwd.getView().getViewInputHandler().mouseMoved(mouseEvent);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }

        // Redraw to update the current position and selection.
        if (wwd.getSceneController() != null)
        {
            wwd.getSceneController().setPickPoint(mouseEvent.getPoint());
            wwd.redraw();
        }
    }

    private void handlePointerDragged(Point newPoint, java.awt.event.MouseEvent mouseEvent)
    {
        Point prevMousePoint = mousePoint;
        mousePoint = newPoint;

        if (primaryButtonDraggingInfo != null)
        {
            primaryButtonDraggingInfo.update(mousePoint);
        }

        if (secondaryButtonDraggingInfo != null)
        {
            secondaryButtonDraggingInfo.update(mousePoint);
        }

        callMouseDraggedListeners(mouseEvent);

        if ((java.awt.event.MouseEvent.BUTTON1_DOWN_MASK & mouseEvent.getModifiersEx()) != 0)
        {
            PickedObjectList pickedObjects = objectsAtButtonPress;
            if (isDragging
                    || (pickedObjects != null && pickedObjects.getTopPickedObject() != null
                    && !pickedObjects.getTopPickedObject().isTerrain()))
            {
                isDragging = true;
                DragSelectEvent selectEvent = new DragSelectEvent(
                    wwd, SelectEvent.DRAG, mouseEvent, pickedObjects, prevMousePoint);
                callSelectListeners(selectEvent);

                // If no listener consumed the event, then cancel the drag.
                if (!selectEvent.isConsumed())
                {
                    cancelDrag();
                }
            }
        }

        if (!isDragging)
        {
            if (!mouseEvent.isConsumed())
            {
                try
                {
                    wwd.getView().getViewInputHandler().mouseDragged(mouseEvent);
                }
                catch (Exception ex)
                {
                    Logging.logger().severe(ex.toString());
                }
            }
        }

        // Redraw to update the current position and selection.
        SceneController sc = wwd.getSceneController();
        if (sc != null)
        {
            sc.setPickPoint(mouseEvent.getPoint());
            wwd.redraw();
        }
    }

    private synchronized void callSelectListeners(SelectEvent event)
    {
        for (SelectListener listener : this.eventListeners.getListeners(SelectListener.class))
        {
            try
            {
                listener.selected(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callKeyReleasedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            try
            {
                listener.keyReleased(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callKeyPressedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            try
            {
                listener.keyPressed(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callKeyTypedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            try
            {
                listener.keyTyped(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMousePressedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            try
            {
                listener.mousePressed(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMouseReleasedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            try
            {
                listener.mouseReleased(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMouseClickedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            try
            {
                listener.mouseClicked(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMouseDraggedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            try
            {
                listener.mouseDragged(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMouseMovedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            try
            {
                listener.mouseMoved(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    private synchronized void callMouseWheelMovedListeners(java.awt.event.MouseWheelEvent event)
    {
        for (MouseWheelListener listener : this.eventListeners.getListeners(MouseWheelListener.class))
        {
            try
            {
                listener.mouseWheelMoved(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    protected synchronized void callMouseEnteredListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            try
            {
                listener.mouseEntered(event);
            }
            catch (Exception ex)
            {
                Logging.logger().severe(ex.toString());
            }
        }
    }

    protected synchronized void callMouseExitedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseExited(event);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (wwd == null)
        {
            return;
        }

        View view = wwd.getView();
        if (view == null || event == null)
        {
            return;
        }

        if (event.getPropertyName().equals(AVKey.VIEW) && (event.getSource() == wwd.getSceneController()))
        {
            view.getViewInputHandler().setWorldWindow(wwd);
        }
    }

    private Point convertMousePoint(MouseEvent event)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (dpiAware && wwd instanceof Node)
        {
            javafx.scene.Scene scene = ((Node)wwd).getScene();
            if (scene != null)
            {
                javafx.stage.Window window = scene.getWindow();
                if (window != null)
                {
                    scaleX = window.getOutputScaleX();
                    scaleY = window.getOutputScaleY();
                }
            }
        }

        return new Point((int) (event.getX() * scaleX),
            wwd.getView().getViewport().height - (int) (event.getY() * scaleY));
    }

    private Point convertTouchPoint(TouchPoint point)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (dpiAware && wwd instanceof Node)
        {
            javafx.scene.Scene scene = ((Node)wwd).getScene();
            if (scene != null)
            {
                javafx.stage.Window window = scene.getWindow();
                if (window != null)
                {
                    scaleX = window.getOutputScaleX();
                    scaleY = window.getOutputScaleY();
                }
            }
        }

        return new Point((int) (point.getX() * scaleX),
            wwd.getView().getViewport().height - (int) (point.getY() * scaleY));
    }

    private static int getModifiers(MouseEvent event)
    {
        int ret = 0;

        if (event.isAltDown())
        {
            ret |= java.awt.event.InputEvent.ALT_DOWN_MASK;
        }

        if (event.isControlDown())
        {
            ret |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
        }

        if (event.isShiftDown())
        {
            ret |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        }

        if (event.isMetaDown())
        {
            ret |= java.awt.event.InputEvent.META_DOWN_MASK;
        }

        if (event.isPrimaryButtonDown())
        {
            ret |= java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
        }

        if (event.isSecondaryButtonDown())
        {
            ret |= java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
        }

        if (event.isMiddleButtonDown())
        {
            ret |= java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
        }

        return ret;
    }

    private static int getModifiers(TouchEvent event)
    {
        int ret = 0;

        if (event.isAltDown())
        {
            ret |= java.awt.event.InputEvent.ALT_DOWN_MASK;
        }

        if (event.isControlDown())
        {
            ret |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
        }

        if (event.isShiftDown())
        {
            ret |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        }

        if (event.isMetaDown())
        {
            ret |= java.awt.event.InputEvent.META_DOWN_MASK;
        }

        ret |= java.awt.event.InputEvent.BUTTON1_DOWN_MASK;

        return ret;
    }

    private static int getModifiers(GestureEvent event)
    {
        int ret = 0;

        if (event.isAltDown())
        {
            ret |= java.awt.event.InputEvent.ALT_DOWN_MASK;
        }

        if (event.isControlDown())
        {
            ret |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
        }

        if (event.isShiftDown())
        {
            ret |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        }

        if (event.isMetaDown())
        {
            ret |= java.awt.event.InputEvent.META_DOWN_MASK;
        }

        return ret;
    }

    private static int getModifiers(KeyEvent event)
    {
        int ret = 0;

        if (event.isAltDown())
        {
            ret |= java.awt.event.InputEvent.ALT_DOWN_MASK;
        }

        if (event.isControlDown())
        {
            ret |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
        }

        if (event.isShiftDown())
        {
            ret |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        }

        if (event.isMetaDown())
        {
            ret |= java.awt.event.InputEvent.META_DOWN_MASK;
        }

        return ret;
    }

    private static int getButtonType(MouseEvent event)
    {
        switch (event.getButton())
        {
            case NONE:
                return java.awt.event.MouseEvent.NOBUTTON;
            case PRIMARY:
                return java.awt.event.MouseEvent.BUTTON1;
            case MIDDLE:
                return java.awt.event.MouseEvent.BUTTON2;
            case SECONDARY:
                return java.awt.event.MouseEvent.BUTTON3;
            default:
                throw new IllegalArgumentException("mouseButton");
        }
    }

    private static class DummyComponent extends Component
    {
    }

    private final DummyComponent dummyComponent = new DummyComponent();

    private java.awt.event.MouseEvent convertFxToAwtEvent(MouseEvent event, int id)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (dpiAware && wwd instanceof Node)
        {
            javafx.scene.Scene scene = ((Node) wwd).getScene();
            if (scene != null)
            {
                javafx.stage.Window window = scene.getWindow();
                if (window != null)
                {
                    scaleX = window.getOutputScaleX();
                    scaleY = window.getOutputScaleY();
                }
            }
        }

        int clickCount = 0;
        int buttonType = 0;

        if (id != java.awt.event.MouseEvent.MOUSE_DRAGGED)
        {
            clickCount = event.getClickCount();
            buttonType = getButtonType(event);
        }

        return new java.awt.event.MouseEvent(
            dummyComponent,
            id,
            System.currentTimeMillis(),
            getModifiers(event),
            (int) (event.getX() * scaleX),
            flipY ? (wwd.getView().getViewport().height - (int)(event.getY() * scaleY)) : (int)(event.getY() * scaleY),
            clickCount,
            event.isPopupTrigger(),
            buttonType);
    }

    private java.awt.event.MouseEvent convertFxToAwtEvent(TouchEvent event, TouchPoint point, int id)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (dpiAware && wwd instanceof Node)
        {
            javafx.scene.Scene scene = ((Node) wwd).getScene();
            if (scene != null)
            {
                javafx.stage.Window window = scene.getWindow();
                if (window != null)
                {
                    scaleX = window.getOutputScaleX();
                    scaleY = window.getOutputScaleY();
                }
            }
        }

        int clickCount = 0;
        int buttonType = 0;

        if (id != java.awt.event.MouseEvent.MOUSE_DRAGGED)
        {
            clickCount = 1;
            buttonType = java.awt.event.MouseEvent.BUTTON1;
        }

        return new java.awt.event.MouseEvent(
            dummyComponent,
            id,
            System.currentTimeMillis(),
            getModifiers(event),
            (int)(point.getX() * scaleX),
            flipY ? (wwd.getView().getViewport().height - (int)(point.getY() * scaleY)) : (int)(point.getY() * scaleY),
            clickCount,
            false,
            buttonType);
    }

    private MouseWheelEvent convertFxToAwtEvent(GestureEvent event, int scrollSteps)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (dpiAware && wwd instanceof Node)
        {
            javafx.scene.Scene scene = ((Node) wwd).getScene();
            if (scene != null)
            {
                javafx.stage.Window window = scene.getWindow();
                if (window != null)
                {
                    scaleX = window.getOutputScaleX();
                    scaleY = window.getOutputScaleY();
                }
            }
        }

        return new MouseWheelEvent(
            dummyComponent,
            MouseWheelEvent.MOUSE_WHEEL,
            System.currentTimeMillis(),
            getModifiers(event),
            (int) (event.getX() * scaleX),
            flipY ? (wwd.getView().getViewport().height - (int)(event.getY() * scaleY)) : (int)(event.getY() * scaleY),
            0,
            false,
            MouseWheelEvent.WHEEL_UNIT_SCROLL,
            1,
            scrollSteps);
    }

    private java.awt.event.KeyEvent convertFxToAwtEvent(KeyEvent event, int id)
    {
        return new java.awt.event.KeyEvent(
            dummyComponent,
            id,
            System.currentTimeMillis(),
            getModifiers(event),
            id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : event.getCode().getCode(),
            event.getCharacter().charAt(0));
    }
}
