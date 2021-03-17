/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.rendering;

import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FlightplanLayer;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserData;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.flightplan.PhantomCorner;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Vector;

public class TaggingPicAreaLayer extends AbstractLayer implements IMapLayerListener {

    public static final double HIGHLIGHT_SCALE = FlightplanLayer.HIGHLIGHT_SCALE;
    private static final INavigationService navigationService =
        DependencyInjector.getInstance().getInstanceOf(INavigationService.class);

    MapLayerPicArea pic;

    IconLayerCentered iconLayer = new IconLayerCentered();
    RenderableLayer lineLayer = new RenderableLayer();

    private final IMapController mapController;
    private final ISelectionManager selectionManager;
    private final SynchronizationRoot syncRoot;

    public TaggingPicAreaLayer(
            MapLayerPicArea pic,
            IMapController mapController,
            ISelectionManager selectionManager,
            SynchronizationRoot syncRoot) {
        this.pic = pic;
        this.mapController = mapController;
        this.syncRoot = syncRoot;
        this.selectionManager = selectionManager;
        iconLayer.setAlwaysUseAbsoluteElevation(false); // this will help to make shure, no icon is under the terrain
        iconLayer.setRenderAlwaysOverGround(true);

        pic.addMapListener(this);
        if (!isConstructed) {
            reconstructLayer();
        }

        selectionManager.currentSelectionProperty().addListener((observable, oldValue, newValue) -> reconstructLayer());
        mapController.mouseModeProperty().addListener((observable, oldValue, newValue) -> reconstructLayer());
    }

    boolean isConstructed = false;
    SurfacePolygonWithUserData area;

    boolean isSelected;
    eu.mavinci.flightplan.Point selectedPoint;

    protected void reconstructLayer() {
        syncRoot.dispatch(
            () -> {
                Object selection = selectionManager.getSelection();
                isSelected = false;
                selectedPoint = null;
                if (pic == selection) {
                    isSelected = true;
                    selectedPoint = null;
                } else if (selection instanceof eu.mavinci.flightplan.Point) {
                    eu.mavinci.flightplan.Point point = (eu.mavinci.flightplan.Point)selection;
                    if (point.getParent() == pic) {
                        isSelected = true;
                        selectedPoint = point;
                    }
                }

                isConstructed = true;
                iconLayer.removeAllIcons();
                lineLayer.removeAllRenderables();

                Vector<LatLon> cornersVec = new Vector<LatLon>(pic.sizeOfFlightplanContainer());
                // System.out.println("reconstructing TaggingPicAreaLayer");

                if (pic.sizeOfFlightplanContainer() > 0) {
                    for (IFlightplanStatement obj : pic) {
                        eu.mavinci.flightplan.Point point = (eu.mavinci.flightplan.Point)obj;
                        // System.out.println("point -> "+point);
                        cornersVec.add(point.getLatLon());
                        Position pos = new Position(point.getLatLon(), 0);

                        if (isSelected) {
                            if (obj == selectedPoint) {
                                iconLayer.addIcon(
                                    new FpIcon("com/intel/missioncontrol/gfx/point-selected.svg", pos, point));
                            } else {
                                iconLayer.addIcon(
                                    new FpIcon("com/intel/missioncontrol/gfx/point-normal.svg", pos, point));
                            }
                        }
                    }
                }

                area = new SurfacePolygonWithUserData(cornersVec);

                area.setUserData(pic);
                area.setSelectable(
                    mapController.isSelecting()
                        && navigationService.workflowStepProperty().get().equals(WorkflowStep.DATA_PREVIEW));
                area.setDraggable(false);
                area.setPopupTriggering(false);
                area.setHasTooltip(false);
                ShapeAttributes atr = new BasicShapeAttributes();
                atr.setDrawInterior(true);
                atr.setInteriorMaterial(
                    new Material(
                        isSelected ? FlightplanLayer.AOI_COLOR_HOVER_OR_SELECT : FlightplanLayer.AOI_COLOR_NORMAL));
                atr.setInteriorOpacity(.5);
                atr.setDrawOutline(true);
                atr.setOutlineMaterial(
                    new Material(
                        isSelected ? FlightplanLayer.AOI_COLOR_HOVER_OR_SELECT : FlightplanLayer.AOI_COLOR_NORMAL));
                area.setAttributes(atr);
                area.setPathType(AVKey.LINEAR);

                atr = atr.copy();
                atr.setInteriorMaterial(new Material(FlightplanLayer.AOI_COLOR_HOVER_OR_SELECT));
                atr.setOutlineMaterial(new Material(FlightplanLayer.AOI_COLOR_HOVER_OR_SELECT));
                area.setHighlightAttributes(atr);
                area.setSelectable(!isSelected);

                lineLayer.addRenderable(area);

                if (isSelected) {
                    LatLon posCenter = pic.getCenter();
                    if (posCenter != null) {
                        iconLayer.addIcon(new FpMoveIcon(pic, posCenter));
                    }

                    LinkedList<eu.mavinci.flightplan.Point> points = new LinkedList<>();
                    for (IFlightplanStatement obj : pic) {
                        eu.mavinci.flightplan.Point point = (eu.mavinci.flightplan.Point)obj;
                        // System.out.println("point -> "+point);
                        points.add(point);
                    }

                    points.add(points.getFirst());

                    // adding virtual vertices between vertices of the corners for ease redining lines.
                    eu.mavinci.flightplan.Point lastPoint = null;
                    LatLon lastLatLon = null;
                    for (eu.mavinci.flightplan.Point nextPoint : points) {
                        LatLon nextLatLon = nextPoint.getLatLon();
                        if (lastPoint != null && lastLatLon != null) {
                            LatLon center = LatLon.interpolate(0.5, lastLatLon, nextLatLon);
                            iconLayer.addIcon(
                                new FpSelectableIcon(
                                    "com/intel/missioncontrol/gfx/point-virtual.svg",
                                    new Position(center, 0),
                                    new PhantomCorner(lastPoint, center)));
                        }

                        lastPoint = nextPoint;
                        lastLatLon = nextLatLon;
                    }
                }

                firePropertyChange(AVKey.LAYER, null, this);
            });
    }

    private class FpIcon extends UserFacingIconWithUserData {

        public FpIcon(String iconPath, Position pos, Object userDataObject) {
            super(iconPath, pos, userDataObject);
            setHighlightScale(TaggingPicAreaLayer.HIGHLIGHT_SCALE);
            setHasTooltip(false);
            setSize(d64);
            if (userDataObject == selectionManager.getSelection()) {
                setSize(UserFacingIconWithUserData.d64);
            }
        }

        @Override
        public boolean isDraggable() {
            return true;
        }

        @Override
        public boolean isSelectable() {
            return isDraggable();
        }

    }

    private class FpMoveIcon extends FpSelectableIcon {

        public FpMoveIcon(MapLayerPicArea picArea, LatLon pos) {
            super("com/intel/missioncontrol/gfx/point-move.svg", new Position(pos, 0), picArea);
            setSize(d32);
        }
    }

    private class FpSelectableIcon extends FpIcon {

        public FpSelectableIcon(String iconPath, Position pos, Object userDataObject) {
            super(iconPath, pos, userDataObject);
        }

        @Override
        public boolean isSelectable() {
            return mapController.isSelecting();
        }
    }

    @Override
    protected void doRender(DrawContext dc) {
        lineLayer.render(dc);
        iconLayer.render(dc);
    }

    @Override
    protected void doPreRender(DrawContext dc) {
        super.doPreRender(dc);
        lineLayer.preRender(dc);
        iconLayer.preRender(dc);
    }

    @Override
    public boolean isPickEnabled() {
        return true;
    }

    @Override
    protected void doPick(DrawContext dc, Point point) {
        if (isPickEnabled()) {
            if (iconLayer.isPickEnabled()) {
                iconLayer.pick(dc, point);
            }

            if (lineLayer.isPickEnabled()) {
                lineLayer.pick(dc, point);
            }
        }
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        if (layer == pic) {
            reconstructLayer();
        }
    }

    @Override
    public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {}

    @Override
    public void childMapLayerInserted(int i, IMapLayer layer) {}

    @Override
    public void childMapLayerRemoved(int i, IMapLayer layer) {}

    @Override
    public void mapLayerStructureChanged(IMapLayer layer) {
        if (layer == pic) {
            reconstructLayer();
        }
    }

}
