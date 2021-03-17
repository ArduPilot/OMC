/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.GlobeAnnotationBalloon;

import java.awt.Dimension;
import java.awt.Point;

public class GlobeAnnotationBalloonAdjustingSize extends GlobeAnnotationBalloon {

    public GlobeAnnotationBalloonAdjustingSize(String text, Position position) {
        super(text, position);
    }

    @Override
    protected void applyAttributesToAnnotation() {
        super.applyAttributesToAnnotation();

        AnnotationAttributes annotationAttrs = annotation.getAttributes();
        annotationAttrs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        annotationAttrs.setTextAlign(AVKey.CENTER);
    }

    @Override
    protected void computeOffsets(DrawContext dc) {
        // super.computeOffsets(dc);
        AnnotationAttributes annotationAttrs = this.getAnnotation().getAttributes();
        annotationAttrs.setSize(new Dimension(-1, -1));
        annotationAttrs.setDrawOffset(new Point(0, 0));
    }

    protected GlobeAnnotation createAnnotation() {
        GlobeAnnotation annotation = new OptimalHightAnnotation(this.getDecodedText(), this.position);

        // Don't make the balloon bigger when it is highlighted, the text looks blurry when it is scaled up.
        annotation.getAttributes().setHighlightScale(1);

        return annotation;
    }

    private class OptimalHightAnnotation extends GlobeAnnotation {

        public OptimalHightAnnotation(String text, Position position) {
            super(text, position);
        }

        @Override
        protected Dimension adjustSizeToText(DrawContext dc, int width, int height) {
            Dimension d = super.adjustSizeToText(dc, width, height);
            return super.adjustSizeToText(dc, d.width, 0);
        }

    }
}
