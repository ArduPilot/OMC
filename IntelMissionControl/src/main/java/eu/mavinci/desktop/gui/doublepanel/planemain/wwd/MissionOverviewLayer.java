/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MissionInfo;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserData;
import eu.mavinci.desktop.gui.wwext.UserFacingTextLayer;
import eu.mavinci.desktop.gui.wwext.UserFacingTextWithUserData;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;

public class MissionOverviewLayer extends AbstractLayer {

    private final RenderableLayer polygonLayer = new RenderableLayer();
    private final UserFacingTextLayer textLayer = new UserFacingTextLayer();
    private final Map<MissionInfo, SurfacePolygonWithUserData> polygons = new HashMap<>();
    private final Material missionMaterial;
    private final Material missionSelectedMaterial;
    private final Globe nonFlatEarth;
    private final ReadOnlyAsyncListProperty<MissionInfo> recentMissions;
    private final List<MissionPreview> missions = new ArrayList<>();
    private MissionInfo selectedMission;

    public MissionOverviewLayer(
            IMissionManager missionManager,
            Globe nonFlatEarth,
            Dispatcher dispatcher,
            ISelectionManager selectionManager) {
        this.nonFlatEarth = nonFlatEarth;
        this.missionMaterial = new Material(new Color(0xf3, 0xf3, 0xf3));
        this.missionSelectedMaterial = new Material(new Color(0x00, 0xBB, 0xFF));
        setPickEnabled(true);
        setName("MissionOverview");
        polygonLayer.setMaxActiveAltitude(3000000);
        polygonLayer.setPickEnabled(true);
        textLayer.setMaxActiveAltitude(300000);
        textLayer.setPickEnabled(true);
        textLayer.getTextRenderer().setAlwaysOnTop(true);
        textLayer.getTextRenderer().setOnTopEyeDistance(11);
        textLayer.getTextRenderer().setEffect(AVKey.TEXT_EFFECT_SHADOW);
        recentMissions = missionManager.recentMissionInfosProperty();
        recentMissions.addListener((ListChangeListener<MissionInfo>)c -> reconstruct(), dispatcher::run);
        selectionManager
            .currentSelectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue instanceof MissionInfo) {
                        setSelectedMission((MissionInfo)newValue);
                    } else {
                        setSelectedMission(null);
                    }
                },
                dispatcher::run);
        dispatcher.run(this::reconstruct);
    }

    @Override
    protected void doRender(DrawContext dc) {
        polygonLayer.render(dc);
        textLayer.render(dc);
    }

    @Override
    protected void doPreRender(DrawContext dc) {
        super.doPreRender(dc);
        polygonLayer.preRender(dc);
        textLayer.preRender(dc);
    }

    @Override
    protected void doPick(DrawContext dc, Point point) {
        super.doPick(dc, point);
        polygonLayer.pick(dc, point);
        textLayer.pick(dc, point);
    }

    private void setSelectedMission(MissionInfo mission) {
        SurfacePolygonWithUserData oldPolygon = polygons.get(selectedMission);
        if (oldPolygon != null) {
            oldPolygon.getAttributes().setInteriorMaterial(missionMaterial);
            oldPolygon.getAttributes().setOutlineMaterial(missionMaterial);
        }

        SurfacePolygonWithUserData polygon = polygons.get(mission);
        if (polygon != null) {
            polygon.getAttributes().setInteriorMaterial(missionSelectedMaterial);
            polygon.getAttributes().setOutlineMaterial(missionSelectedMaterial);
        }

        selectedMission = mission;
    }

    private void reconstruct() {
        // TODO FIXME speed this up a lot, by caching polygones in case of airspace list change: most polygones could be
        // reused!!
        polygonLayer.removeAllRenderables();
        textLayer.clear();
        polygons.clear();

        for (MissionPreview mission : getMissions()) {
            if (mission.getSector() == null) {
                continue;
            }

            List<Position> positions = new ArrayList<>(4);
            for (gov.nasa.worldwind.geom.LatLon latLon : mission.getSector().getCorners()) {
                positions.add(new Position(latLon, 0));
            }

            SurfacePolygonWithUserData polygon = new SurfacePolygonWithUserData(positions);
            polygon.setPathType(AVKey.LINEAR);

            var area = polygon.getArea(nonFlatEarth);
            if (area > 1e8 || area <= 0.0) {
                /* added <= 0 check, because if the area is _really_ large,
                like globa spanning large, areaMeasurer sometimes returns 0*/
                // dont render if larger then 100km2 for performance reasonsn
                continue;
            }

            polygon.setUserData(mission.getMissionInfo());
            polygon.setSelectable(true);
            polygon.setHasTooltip(true);

            ShapeAttributes atr = new BasicShapeAttributes();
            if (mission.getMissionInfo() == selectedMission) {
                atr.setInteriorMaterial(missionSelectedMaterial);
                atr.setOutlineMaterial(missionSelectedMaterial);
            } else {
                atr.setInteriorMaterial(missionMaterial);
                atr.setOutlineMaterial(missionMaterial);
            }

            atr.setDrawInterior(true);
            atr.setInteriorOpacity(0.3);
            atr.setDrawOutline(true);
            atr.setOutlineWidth(2);
            atr.setOutlineOpacity(0.8);
            polygon.setAttributes(atr);

            atr = atr.copy();
            atr.setInteriorMaterial(missionSelectedMaterial);
            atr.setOutlineMaterial(missionSelectedMaterial);
            polygon.setHighlightAttributes(atr);
            polygonLayer.addRenderable(polygon);

            UserFacingTextWithUserData text =
                new UserFacingTextWithUserData(mission.getName(), polygon.getReferencePosition(), mission);
            text.setSelectable(true);
            text.setBackgroundColor(Color.BLACK);

            text.setFont(FontHelper.getBaseFont(1));
            textLayer.add(text);

            polygons.put(mission.missionInfo, polygon);
        }

        firePropertyChange(AVKey.LAYER, null, this);
    }

    public List<MissionPreview> getMissions() {
        if (recentMissions == null || missions.size() == recentMissions.size()) {
            return missions;
        }

        missions.clear();
        try (LockedList<MissionInfo> locked = recentMissions.lock()) {
            for (MissionInfo missionInfo : locked) {
                if (missionInfo.getSector() == Sector.EMPTY_SECTOR) {
                    continue;
                }

                missions.add(new MissionPreview(missionInfo.getName(), missionInfo.getSector(), missionInfo));
            }
        }

        return missions;
    }

    public static class MissionPreview {
        public final String name;
        public final Sector sector;
        public final MissionInfo missionInfo;

        public MissionPreview(String name, Sector sector, MissionInfo missionInfo) {
            this.name = name;
            this.sector = sector;
            this.missionInfo = missionInfo;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public Sector getSector() {
            return sector;
        }

        public MissionInfo getMissionInfo() {
            return missionInfo;
        }
    }

}
