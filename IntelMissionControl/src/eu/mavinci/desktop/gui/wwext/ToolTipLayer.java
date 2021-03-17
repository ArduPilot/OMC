/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.map.worldwind.impl.PickingHelper;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.flightplan.Flightplan;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.javafx.WWNode;
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

public class ToolTipLayer extends AbstractLayer implements SelectListener, MouseMotionListener {

    Point point;
    private WeakReference<Object> userData;
    private WeakReference<Object> renderable;
    private WWNode wwdNode;
    private WorldWindow wwd;
    private boolean isShown;
    private final SynchronizationContext synchronizationContext;

    public ToolTipLayer(SynchronizationContext synchronizationContext) {
        this.synchronizationContext = synchronizationContext;
    }

    public void setWorldWindow(WorldWindow wwd) {
        if (this.wwd != null) {
            this.wwd.removeSelectListener(this);
            wwd.getInputHandler().removeMouseMotionListener(this);
        }

        this.wwd = wwd;
        wwdNode = (WWNode)wwd;

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
        if (!isEnabled() || !synchronizationContext.hasAccess()) {
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

        double scale = wwdNode.getScene().getWindow().getOutputScaleY();

        string = string.replaceAll(Pattern.quote("\u202F"), " ");

        Font font = FontHelper.getBaseFont(.9);
        OrderedText tip =
            new OrderedText(
                string, font, point.x, (int)Math.round(wwdNode.getHeight() * scale - point.y), Color.black, 0x1);
        tip.setColorInterior(Color.white);
        dc.addOrderedRenderable(tip);
    }

    @Override
    public void selected(SelectEvent event) {
        // System.out.println(event.getPickPoint());
        if (event.getEventAction().equals(SelectEvent.HOVER)) {
            // System.out.println("top ToolTip hoovering");
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
    public void mouseMoved(MouseEvent mouseEvent) {
        if (!isShown) {
            return;
        }

        point = mouseEvent.getPoint();
        firePropertyChange(AVKey.LAYER, null, this);
    }
}
