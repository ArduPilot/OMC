/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.map.worldwind.impl.PickingHelper;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.flightplan.Flightplan;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;
import javafx.scene.Node;
import org.asyncfx.concurrent.Dispatcher;

public class ToolTipLayer extends AbstractLayer implements SelectListener, MouseMotionListener {

    Point point;
    private WeakReference<Object> userData;
    private WeakReference<Object> renderable;
    private Node node;
    private WorldWindow wwd;
    private boolean isShown;
    private final Dispatcher dispatcher;

    public ToolTipLayer(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setWorldWindow(WorldWindow wwd) {
        if (this.wwd != null) {
            this.wwd.removeSelectListener(this);
            wwd.getInputHandler().removeMouseMotionListener(this);
        }

        this.wwd = wwd;
        node = (Node)wwd;

        if (this.wwd != null) {
            this.wwd.addSelectListener(this);
            wwd.getInputHandler().addMouseMotionListener(this);
        }
    }

    public void setToolTip(Point point, Object userData, Object renderable) {
        this.point = point;
        isShown = true;
        this.userData = new WeakReference<>(userData);
        this.renderable = new WeakReference<>(renderable);
        firePropertyChange(AVKey.LAYER, null, this);
    }

    public void unsetToolTip() {
        if (!isShown) {
            return;
        }

        isShown = false;
        firePropertyChange(AVKey.LAYER, null, this);
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (!isEnabled() || !dispatcher.hasAccess()) {
            return;
        }

        if (!isShown || point == null) {
            return;
        }

        String string = null;
        if (userData.get() instanceof IWWPickableAdvancedTooltip) {
            IWWPickableAdvancedTooltip tooltipAble = (IWWPickableAdvancedTooltip)userData.get();
            string = tooltipAble.getTooltip(wwd.getCurrentPosition(), point);
        } else if (renderable.get() instanceof UserFacingIcon) {
            string = ((UserFacingIcon)renderable.get()).getToolTipText();
        } else if (string == null && userData.get() != null) {
            string = userData.get().toString();
        }

        // System.out.println("tip="+string);
        if (string == null) {
            return;
        }

        if (userData.get() instanceof IFlightplanRelatedObject && !(userData.get() instanceof Flightplan)) {
            IFlightplanRelatedObject fpO = (IFlightplanRelatedObject)userData.get();
            if (fpO.getFlightplan() != null) {
                string += " @ " + fpO.getFlightplan().toString();
            }
        }

        double scale = node.getScene().getWindow().getOutputScaleY();

        string = string.replaceAll(Pattern.quote("\u202F"), " ");

        Font font = FontHelper.getBaseFont(.9);
        OrderedText tip = new OrderedText(string, font, point.x, point.y, Color.black, 0x1);
        tip.setColorInterior(Color.white);
        dc.addOrderedRenderable(tip);
    }

    @Override
    public void selected(SelectEvent event) {
        if (event.isHover()) {
            if (event.hasObjects()) { // && !this.controller.getDragger().isDragging()) {
                // System.out.println("top ToolTip topObj:" + event.getTopObject());
                Object o = PickingHelper.getPickedObject(event, true, true, wwd);

                if (o instanceof IWWRenderableWithUserData) {
                    IWWRenderableWithUserData renderable = (IWWRenderableWithUserData)o;
                    // System.out.println("top ToolTip UserObj:" + renderable.getUserData());
                    if (!renderable.hasTooltip()) {
                        unsetToolTip();
                        return;
                    }

                    setToolTip(event.getPickPoint(), renderable.getUserData(), renderable);
                    return;
                }
            }

            unsetToolTip();
        } else if (event.isRollover() && isShown) {
            // FIX: the mouse coordinates are different for HOVER and ROLLOVER, so fix it
            // by using the viewports height, this is a rather dirty one ...
            Point rolloverPoint = event.getPickPoint();
            point = new Point(rolloverPoint.x, wwd.getView().getViewport().height - rolloverPoint.y);
        }
    }

    @Override
    public String getName() {
        return "ToolTipLayer";
    }

    @Override
    public void dispose() {
        super.dispose();
        wwd.removeSelectListener(this);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}
}
