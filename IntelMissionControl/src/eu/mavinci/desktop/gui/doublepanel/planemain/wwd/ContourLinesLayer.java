/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016: Using parts of NASA code to build a contour lines layer
 *
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.settings.ExpertSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ContourLine;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TimedExpirySupport;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class ContourLinesLayer extends RenderableLayer {

    class MContourLine extends ContourLine {

        public MContourLine(double elevation) {
            super(elevation);
            setViewClippingEnabled(true);
            maxConnectingDistance = 5;
        }

        TimedExpirySupport expirySupportM = new TimedExpirySupport(2000, 3000);

        public void render(DrawContext dc) {
            if (dc == null) {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (!this.isEnabled()) {
                return;
            }

            if (!this.getSector().intersects(dc.getVisibleSector())) {
                return;
            }

            if (!this.isValid(dc)) {
                makeContourLine(dc);
                this.expirySupportM.restart(dc);
                this.globeStateKey = dc.getGlobe().getGlobeStateKey(dc);
            }

            for (Renderable r : this.getRenderables()) {
                r.render(dc);
            }
        }

        protected boolean isValid(DrawContext dc) {
            if (this.expirySupportM.isExpired(dc)) {
                return false;
            }

            return this.globeStateKey != null && this.globeStateKey.equals(dc.getGlobe().getStateKey(dc));
        }

        /** Update the contour line according to the current terrain geometry. */
        public void update() {
            this.expirySupportM.setExpired(true);
        }

        /**
         * Update the renderable list with appropriate renderables to display the contour line.
         *
         * @param dc the current <code>DrawContext</code>.
         */
        protected void makeContourLine(DrawContext dc) {
            if (dc == null) {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.getRenderables().clear();

            // the following test is key to speeding up this enough, to make almost 1000 line levels work!
            Sector s = dc.getVisibleSector();

            if (lastSector == null || !s.equals(lastSector)) {
                lastMinMax = dc.getGlobe().getMinAndMaxElevations(dc.getVisibleSector());
                lastSector = s;
            }

            double elev = getElevation();
            if (elev <= lastMinMax[0] || elev >= lastMinMax[1]) {
                return;
            }

            double min = Math.ceil(lastMinMax[0] / step) * step;
            double max = Math.floor(lastMinMax[1] / step) * step;
            int c = (int)Math.round(((max - min) / step)) + 1;
            int i = (int)Math.round(elev / step);
            int sparsing = WWMath.powerOfTwoCeiling(c / maxVisLines); // in the Netherlands this value can become 0 !!
            if (sparsing > 1 && i % sparsing != 0) {
                return;
            }

            // Get intersection points with terrain
            double ve = dc.getVerticalExaggeration();
            Intersection[] interArray = dc.getSurfaceGeometry().intersect(this.getElevation() * ve, this.getSector());

            if (interArray != null) {
                ArrayList<Intersection> inter = new ArrayList<Intersection>(Arrays.asList(interArray));

                // Filter intersection segment list
                if (isViewClippingEnabled()) {
                    inter = filterIntersectionsOnViewFrustum(dc, inter);
                }

                inter = filterIntersections(dc, inter);

                // Create polyline segments
                makePolylinesConnected(dc, inter, this.maxConnectingDistance);
            }
        }
    }

    double[] lastMinMax;
    Sector lastSector;
    final ExpertSettings expertSettings = DependencyInjector.getInstance().getInstanceOf(ExpertSettings.class);
    int maxVisLines = expertSettings.getContourLinesMaxVisLines();
    int step = expertSettings.getContourLinesStep();
    int stepBold = expertSettings.getContourLinesStepBold();
    int stepBoldBlue = expertSettings.getContourLinesStepBoldBlue();
    int width = expertSettings.getContourLinesWidth();
    int widthBold = expertSettings.getContourLinesWidthBold();

    public ContourLinesLayer() {
        setPickEnabled(false);

        // make sure if we start stepping from min, we are passing by ZERO
        double min = Earth.ELEVATION_MIN;
        min /= step;
        min = Math.floor(min);
        min *= step;

        for (double x = min; x < Earth.ELEVATION_MAX; x += step) {
            // for (int x =200; x < 201; x+=step){
            // System.out.println("addLine:" + x);
            ContourLine contourLine = new MContourLine(x);
            contourLine.setLineWidth(width);
            if (x % stepBoldBlue == 0) {
                contourLine.setLineWidth(widthBold);
                contourLine.setColor(new Color(0.0f, 0.1f, 0.6f));
            }

            if (x % stepBold == 0) {
                contourLine.setLineWidth(widthBold);
            }

            addRenderable(contourLine);
        }
    }

}
