/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.geometry.Mat4;
import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geospatial.ITransform;
import com.intel.missioncontrol.geospatial.ProjectedPosition;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import java.util.ArrayList;
import java.util.List;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.LockedList;

public class ExtrudedPolygonGoal extends AbstractExtrudedPolygonGoal {

    private AsyncObjectProperty<Mat4> transformToParent = new SimpleAsyncObjectProperty<>(this);
    private AsyncObjectProperty<Mat4> transformFromParent = new SimpleAsyncObjectProperty<>(this);

    public ExtrudedPolygonGoal() {
        init();
    }

    public ExtrudedPolygonGoal(ExtrudedPolygonGoal source) {
        super(source);
        init();
    }

    public ExtrudedPolygonGoal(ExtrudedPolygonGoalSnapshot source) {
        super(source);
        init();
    }

    public ExtrudedPolygonGoal(CompositeDeserializationContext context) {
        super(context);
        init();
    }

    private void init() {
        transformToParent.bind(
            originProperty(),
            value -> {
                if (getOrigin() != null) {
                    return Mat4.fromTranslation(getOrigin().x, getOrigin().y, 0)
                        .multiply(Mat4.fromRotationZ(getDirection()));
                }

                return null;
            });
        transformToParent.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    transformFromParent.set(newValue.invert());
                }
            });
    }

    @Override
    void updateOrigin() {
        double xAvg = 0;
        double yAvg = 0;
        Vec2 oldOrigin = getOrigin() == null ? new Vec2(0, 0) : getOrigin();
        List<Vec2> newCorners = new ArrayList<>();

        try (LockedList<Vec2> corners = geometryProperty().get().getVertices().lock()) {
            for (Vec2 corner : corners) {
                Vec2 toMission = transformToMission(corner);
                xAvg += toMission.x;
                yAvg += toMission.y;
            }

            double num = corners.size();
            xAvg /= num;
            yAvg /= num;

            Vec2 newOrigin = new Vec2(xAvg, yAvg);

            double xTranslation = newOrigin.x - oldOrigin.x;
            double yTranslation = newOrigin.y - oldOrigin.y;

            for (Vec2 corner : corners) {
                Vec2 newCorner = new Vec2(corner.x - xTranslation, corner.y - yTranslation);
                newCorners.add(newCorner);
            }

            this.origin.set(newOrigin);
        }

        this.geometryProperty().get().getVertices().setAll(newCorners);
    }

    @Override
    public Vec2 transformFromMission(Vec2 value) {
        double tmpX = value.x - getOrigin().x;
        double tmpY = value.y - getOrigin().y;
        double sin = Math.sin(getDirection());
        double cos = Math.cos(getDirection());
        double cornerXFromParent = tmpX * cos - tmpY * sin;
        double cornerYFromParent = tmpX * sin + tmpY * cos;

        return new Vec2(cornerXFromParent, cornerYFromParent);
    }

    @Override
    public Vec2 transformToMission(Vec2 value) {
        double tmpX = value.x + getOrigin().x;
        double tmpY = value.y + getOrigin().y;
        double sin = Math.sin(getDirection());
        double cos = Math.cos(getDirection());
        double cornerXToParent = tmpX * cos + tmpY * sin;
        double cornerYToParent = -tmpX * sin + tmpY * cos;

        return new Vec2(cornerXToParent, cornerYToParent);
    }

    @Override
    void applyTransformations(ITransform<ProjectedPosition> oldTransform, ITransform<ProjectedPosition> newTransform) {
        ExtrudedPolygonGeometry geom = this.getGeometry();
        List<Vec2> newCorners1 = new ArrayList<>();
        List<Vec2> newCorners2 = new ArrayList<>();
        try (LockedList<Vec2> vec2s = geom.verticesProperty().get().lock()) {
            for (Vec2 v : vec2s) {
                var value = this.transformToMission(v);
                var pos = oldTransform.transform(new ProjectedPosition(value.x, value.y, 0));
                var value2 = newTransform.transform(pos);
                newCorners1.add(new Vec2(value2.x, value2.y));
            }
        }

        Vec2 oldOrigProj = this.getOrigin();
        var oldOrigPos = oldTransform.transform(new ProjectedPosition(oldOrigProj.x, oldOrigProj.y, 0));
        var newOrigProj = newTransform.transform(oldOrigPos);
        this.origin.set(new Vec2(newOrigProj.x, newOrigProj.y));

        for (Vec2 v : newCorners1) {
            var value3 = this.transformFromMission(v);
            newCorners2.add(value3);
        }

        geom.verticesProperty().get().setAll(newCorners2);
    }
}
