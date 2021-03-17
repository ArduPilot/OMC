/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geometry.Vec4;
import com.intel.missioncontrol.geospatial.IConformalTransformFactory;
import com.intel.missioncontrol.geospatial.ITransform;
import com.intel.missioncontrol.geospatial.LatLon;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.geospatial.ProjectedPosition;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import java.util.OptionalDouble;
import org.asyncfx.collections.LockedList;

public class Mission extends AbstractMission {

    private ITransform<ProjectedPosition> currentTransform;

    public Mission() {}

    public Mission(Mission other) {
        super(other);
    }

    public Mission(MissionSnapshot other) {
        super(other);
    }

    public Mission(CompositeDeserializationContext context) {
        super(context);
    }

    public void updateOrigin(IConformalTransformFactory transformFactory) {
        LatLon oldOriginWgs = getOrigin();
        ITransform<ProjectedPosition> oldTransform = transformFactory.createFromReference(oldOriginWgs);

        try (LockedList<Placeable> placeables = placeablesProperty().lock()) {
            for (Placeable p : placeables) {
                p.updateOrigin();
            }

            ProjectedPosition oldOriginProjected =
                oldTransform.transform(
                    Position.fromDegrees(oldOriginWgs.getLatitude(), oldOriginWgs.getLongitude(), 0));

            double xAvg = 0;
            double yAvg = 0;
            for (Placeable p : placeables) {
                Vec2 tmpOrigin = p.getOrigin();

                Vec4 tmpOriginVec4 = new Vec4(tmpOrigin.x, tmpOrigin.y, 0, 1);
                double pointProjX = tmpOriginVec4.x + oldOriginProjected.x;
                double pointProjY = tmpOriginVec4.y + oldOriginProjected.y;

                xAvg += pointProjX;
                yAvg += pointProjY;
            }

            double num = placeables.size();
            xAvg /= num;
            yAvg /= num;

            Position newOriginWGS = oldTransform.transform(new ProjectedPosition(xAvg, yAvg, 0));
            LatLon newOriginLatLon = LatLon.fromDegrees(newOriginWGS.getLatitude(), newOriginWGS.getLongitude());
            ITransform<ProjectedPosition> newTransform = transformFactory.createFromReference(newOriginLatLon);

            for (Placeable p : placeables) {
                p.applyTransformations(oldTransform, newTransform);
            }

            origin.set(newOriginLatLon);
            this.currentTransform = newTransform;
        }
    }

    public Position transformToGlobe(Vec2 value, IConformalTransformFactory transformFactory) {
        currentTransform = transformFactory.createFromReference(getOrigin());
        return currentTransform.transform(new ProjectedPosition(value.x, value.y, 0));
    }

    public ProjectedPosition transformFromGlobe(Position pos, IConformalTransformFactory transformFactory) {
        if (currentTransform == null) {
            origin.set(LatLon.fromDegrees(pos.getLatitude(), pos.getLongitude()));
        }

        currentTransform = transformFactory.createFromReference(getOrigin());
        return currentTransform.transform(pos);
    }

    public Sector getSector() {
        return null;
    }

    public OptionalDouble getMaxElev() {
        return null;
    }

}
