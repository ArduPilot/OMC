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

    private WorldWindow wwd;
    private Sink sink;
    private EventListenerList eventListeners = new EventListenerList();
    private PickedObjectList hoverObjects;
    private PickedObjectList objectsAtButtonPress;
    private SelectListener selectListener;
    private Point mousePoint;
    private boolean isHovering = false;
    private boolean isDragging = false;
    private MouseDraggingInfo primaryButtonDraggingInfo;
    private MouseDraggingInfo secondaryButtonDraggingInfo;
    private double discardClickMoveDistance = DISCARD_CLICK_DEFAULT_MOVE_DISTANCE;

    private JfxTimer hoverTimer = new JfxTimer(Duration.millis(600), new Runnable()
    {
        @Override
        public void run()
        {
            if (sink != null)
            {
                sink.accept(() -> {
                    if (pickMatches(hoverObjects))
                    {
                        isHovering = true;
                        callSelectListeners(new SelectEvent(wwd, SelectEvent.HOVER, mousePoint, hoverObjects));
                        hoverTimer.stop();
                    }
                });
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
                wwd.getView().getViewInputHandler().keyPressed(keyEvent);
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
                wwd.getView().getViewInputHandler().keyReleased(keyEvent);
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
                wwd.getView().getViewInputHandler().keyTyped(keyEvent);
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mousePressedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            Point newPoint = convertMousePoint(event);

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

            if (event.getButton() == MouseButton.PRIMARY)
            {
                primaryButtonDraggingInfo = new MouseDraggingInfo(newPoint);
            }
            else if (event.getButton() == MouseButton.SECONDARY)
            {
                secondaryButtonDraggingInfo = new MouseDraggingInfo(newPoint);
            }

            objectsAtButtonPress = wwd.getObjectsAtCurrentPosition();

            java.awt.event.MouseEvent mouseEvent = convertFxToAwtEvent(event,
                    java.awt.event.MouseEvent.MOUSE_PRESSED);
            callMousePressedListeners(mouseEvent);

            if (objectsAtButtonPress != null && objectsAtButtonPress.getTopPickedObject() != null
                    && !objectsAtButtonPress.getTopPickedObject().isTerrain())
            {
                // Something is under the cursor, so it's deemed "selected".
                if (event.getButton() == MouseButton.PRIMARY)
                {
                    callSelectListeners(
                            new SelectEvent(wwd, SelectEvent.LEFT_PRESS, mouseEvent, objectsAtButtonPress));
                }
                else if (event.getButton() == MouseButton.SECONDARY)
                {
                    callSelectListeners(
                            new SelectEvent(wwd, SelectEvent.RIGHT_PRESS, mouseEvent, objectsAtButtonPress));
                }

                // Initiate a repaint.
                wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());
            }

            if (!mouseEvent.isConsumed())
            {
                wwd.getView().getViewInputHandler().mousePressed(mouseEvent);
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseReleasedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            mousePoint = convertMousePoint(event);
            java.awt.event.MouseEvent mouseEvent = convertFxToAwtEvent(event,
                    java.awt.event.MouseEvent.MOUSE_RELEASED);
            callMouseReleasedListeners(mouseEvent);
            if (!mouseEvent.isConsumed())
            {
                wwd.getView().getViewInputHandler().mouseReleased(mouseEvent);
            }

            doHover(true);
            cancelDrag();
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseMovedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            mousePoint = convertMousePoint(event);
            java.awt.event.MouseEvent mouseEvent = convertFxToAwtEvent(event,
                    java.awt.event.MouseEvent.MOUSE_MOVED);
            callMouseMovedListeners(mouseEvent);

            if (!mouseEvent.isConsumed())
            {
                wwd.getView().getViewInputHandler().mouseMoved(mouseEvent);
            }

            // Redraw to update the current position and selection.
            if (wwd.getSceneController() != null)
            {
                wwd.getSceneController().setPickPoint(mouseEvent.getPoint());
                wwd.redraw();
            }
        }
    };

    private final DispatchingEventHandler<MouseEvent> mouseDraggedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            Point prevMousePoint = mousePoint;
            mousePoint = convertMousePoint(event);

            if (primaryButtonDraggingInfo != null)
            {
                primaryButtonDraggingInfo.update(mousePoint);
            }

            if (secondaryButtonDraggingInfo != null)
            {
                secondaryButtonDraggingInfo.update(mousePoint);
            }

            java.awt.event.MouseEvent mouseEvent = convertFxToAwtEvent(event,
                    java.awt.event.MouseEvent.MOUSE_DRAGGED);
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
                    wwd.getView().getViewInputHandler().mouseDragged(mouseEvent);
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
    };

    private final DispatchingEventHandler<MouseEvent> mouseClickedHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(MouseEvent event)
        {
            if (wwd == null)
            {
                return;
            }

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

            java.awt.event.MouseEvent mouseEvent = convertFxToAwtEvent(event,
                    java.awt.event.MouseEvent.MOUSE_CLICKED);
            callMouseClickedListeners(mouseEvent);

            if (pickedObjects != null && pickedObjects.getTopPickedObject() != null
                    && !pickedObjects.getTopPickedObject().isTerrain())
            {
                // Something is under the cursor, so it's deemed "selected".
                if (event.getButton() == MouseButton.PRIMARY && !primaryCanceled)
                {
                    if (event.getClickCount() <= 1)
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
                else if (event.getButton() == MouseButton.SECONDARY && !secondaryCanceled)
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
            if (wwd == null)
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
            int scrollSteps = (int) lastY / PIXELS_PER_SCROLL_STEP;
            if (scrollSteps == 0)
            {
                return;
            }

            lastTimestamp = currentTimestamp;
            lastY = 0;

            java.awt.event.MouseWheelEvent mouseWheelEvent = convertFxToAwtEvent(event, scrollSteps);
            callMouseWheelMovedListeners(mouseWheelEvent);

            if (!mouseWheelEvent.isConsumed())
            {
                wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
            }
        }
    };

    private final DispatchingEventHandler<ZoomEvent> zoomHandler = new DispatchingEventHandler<>()
    {
        @Override
        public void handleEvent(ZoomEvent event)
        {
            if (wwd == null)
            {
                return;
            }

            java.awt.event.MouseWheelEvent mouseWheelEvent = convertFxToAwtEvent(event);
            callMouseWheelMovedListeners(mouseWheelEvent);

            if (!mouseWheelEvent.isConsumed())
            {
                wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
            }
        }
    };

    public void setSink(Sink sink)
    {
        this.sink = sink;
        keyPressedHandler.setSink(sink);
        keyReleasedHandler.setSink(sink);
        keyTypedHandler.setSink(sink);
        mousePressedHandler.setSink(sink);
        mouseReleasedHandler.setSink(sink);
        mouseMovedHandler.setSink(sink);
        mouseDraggedHandler.setSink(sink);
        mouseClickedHandler.setSink(sink);
        scrollHandler.setSink(sink);
        zoomHandler.setSink(sink);
    }

    public Sink getSink()
    {
        return sink;
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
            node.removeEventHandler(ScrollEvent.SCROLL, scrollHandler);

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
        node.addEventHandler(ScrollEvent.SCROLL, scrollHandler);

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

        this.setSink(null);
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

            Object oh = hover == null ? null : hover.getObject() != null ? hover.getObject() :
                    hover.getParentLayer() != null ? hover.getParentLayer() : null;
            Object ol = last == null ? null : last.getObject() != null ? last.getObject() :
                    last.getParentLayer() != null ? last.getParentLayer() : null;

            if (oh != null && ol != null && oh.equals(ol))
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

    private synchronized void callSelectListeners(SelectEvent event)
    {
        for (SelectListener listener : this.eventListeners.getListeners(SelectListener.class))
        {
            listener.selected(event);
        }
    }

    private synchronized void callKeyReleasedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyReleased(event);
        }
    }

    private synchronized void callKeyPressedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyPressed(event);
        }
    }

    private synchronized void callKeyTypedListeners(java.awt.event.KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyTyped(event);
        }
    }

    private synchronized void callMousePressedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mousePressed(event);
        }
    }

    private synchronized void callMouseReleasedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseReleased(event);
        }
    }

    private synchronized void callMouseClickedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseClicked(event);
        }
    }

    private synchronized void callMouseDraggedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            listener.mouseDragged(event);
        }
    }

    private synchronized void callMouseMovedListeners(java.awt.event.MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            listener.mouseMoved(event);
        }
    }

    private synchronized void callMouseWheelMovedListeners(java.awt.event.MouseWheelEvent event)
    {
        for (MouseWheelListener listener : this.eventListeners.getListeners(MouseWheelListener.class))
        {
            listener.mouseWheelMoved(event);
        }
    }

    protected synchronized void callMouseEnteredListeners(java.awt.event.MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseEntered(event);
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
        if (wwd instanceof Node)
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

        return new Point((int) (event.getX() * scaleX),
                wwd.getView().getViewport().height - (int) (event.getY() * scaleY));
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
        if (wwd instanceof Node)
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
                wwd.getView().getViewport().height - (int) (event.getY() * scaleY),
                clickCount,
                event.isPopupTrigger(),
                buttonType);
    }

    private MouseWheelEvent convertFxToAwtEvent(ScrollEvent event, int scrollSteps)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;
        if (wwd instanceof Node)
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
                wwd.getView().getViewport().height - (int) (event.getY() * scaleY),
                event.getTouchCount(),
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                1,
                -scrollSteps);
    }

    private MouseWheelEvent convertFxToAwtEvent(ZoomEvent event)
    {
        double scaleX = 1.0;
        double scaleY = 1.0;
        if (wwd instanceof Node)
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
                wwd.getView().getViewport().height - (int) (event.getY() * scaleY),
                1,
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                1,
                -(int) event.getZoomFactor());
    }

    private java.awt.event.KeyEvent convertFxToAwtEvent(KeyEvent event, int id)
    {
        return new java.awt.event.KeyEvent(
                dummyComponent,
                id,
                System.currentTimeMillis(),
                getModifiers(event),
                // id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED: event.getCode().ordinal() :
                id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : event.getCode().getCode(),
                event.getCharacter().charAt(0));
    }
}
