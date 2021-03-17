/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.jogamp.opengl.GL2;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import java.util.OptionalDouble;
import java.util.logging.Level;

/**
 * This view can handle the different modes from {@link ViewMode} therefore it prevents different kinds of user
 * interaction in certain modes
 *
 * @author Marco
 */
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class MView2d extends BasicOrbitView {

    public static final String HEADING = MView.class.getName() + ".heading";
    public static final String PITCH = MView.class.getName() + ".pitch";
    public static final String ZOOM = MView.class.getName() + ".zoom";
    public static final String EYE_POSITION = MView.class.getName() + ".eyePosition";
    public static final String CENTER_POSITION = MView.class.getName() + ".centerPosition";

    protected ViewMode viewMode = ViewMode.DEFAULT;

    protected Position lastPosition = getCenterPosition();
    protected boolean shouldInitFollowZoom = false;
    protected boolean isFlatEarth = false;

    public static final int MAX_COMMON_ZOOM = 1000;

    public static final double MIN_VALID_ZOOM = 0.6; // values <=0.5 will cause a black map in 2D mode
    public static final double MAX_VALID_ZOOM = 1e8;

    private static final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);

    private IHardwareConfiguration hardwareConfiguration;

    public MView2d(IHardwareConfiguration hardwareConfiguration) {
        super();
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public boolean isOverridingHeading() {
        return (viewMode != null && viewMode.isPlaneCentered());
    }

    public boolean isOverridingPitch() {
        return (viewMode != null && viewMode.isPlaneCentered()) || isFlatEarth;
    }

    public boolean isOverridingZoom() {
        return viewMode != null && viewMode.isPlaneCentered();
    }

    public boolean isOverridingPosition() {
        return viewMode != null && !viewMode.equals(ViewMode.DEFAULT);
    }

    // workaround to prevent view changing by mouse interaction:
    // remove the common access methods
    @Override
    public void setHeading(Angle heading) {
        if (!isOverridingHeading()) {
            Angle oldHeading = this.heading;
            super.setHeading(heading);
            firePropertyChange(HEADING, oldHeading, this.heading);
        }
    }

    @Override
    public void setPitch(Angle pitch) {
        if (!isOverridingPitch()) {
            Angle oldPitch = this.pitch;
            super.setPitch(pitch);
            firePropertyChange(PITCH, oldPitch, this.pitch);
        }
    }

    @Override
    public void setZoom(double zoom) {
        zoom = MathHelper.intoRange(zoom, MIN_VALID_ZOOM, MAX_VALID_ZOOM);
        if (!isOverridingZoom()) {
            double oldZoom = this.zoom;
            super.setZoom(zoom);
            firePropertyChange(ZOOM, oldZoom, this.zoom);
        }
    }

    @Override
    public void setEyePosition(Position eyePosition) {
        if (!isOverridingPosition()) {
            Position oldPos = this.eyePosition;
            super.setEyePosition(eyePosition);
            firePropertyChange(EYE_POSITION, oldPos, this.eyePosition);
        }
    }

    @Override
    public void setCenterPosition(Position center) {
        if (!isOverridingPosition()) {
            Position oldPos = this.center;
            super.setCenterPosition(center);
            firePropertyChange(CENTER_POSITION, oldPos, this.center);
        }
    }

    @Override
    public boolean isDetectCollisions() {
        // remove collision handling in cockpit mode, to get a unnoisy view next
        // to the ground...
        if (isOverridingHeading()) {
            return false;
        } else {
            return super.isDetectCollisions();
        }
    }

    public Angle getRoll() {
        return roll;
    }

    // ###################

    // adding new access methods for cockpit usage
    public void setRollInt(Angle roll) {
        this.roll = ViewUtil.normalizedRoll(roll);
    }

    public void setHeadingInt(Angle heading) {
        Angle oldHeading = this.heading;
        this.heading = normalizedHeading(heading);
        firePropertyChange(HEADING, oldHeading, this.heading);
    }

    public void setPitchInt(Angle pitch) {
        Angle oldPitch = this.pitch;
        this.pitch = normalizedPitch(pitch);
        firePropertyChange(PITCH, oldPitch, this.pitch);
    }

    public void setZoomInt(double zoom) {
        double oldZoom = this.zoom;
        this.zoom = zoom;
        firePropertyChange(ZOOM, oldZoom, this.zoom);
    }

    public void setEyePositionInt(Position eyePosition) {
        Position oldPos = this.eyePosition;
        this.eyePosition = eyePosition;
        firePropertyChange(EYE_POSITION, oldPos, this.eyePosition);
    }

    public void setCenterPositionInt(Position center) {
        if (center == null) {
            return;
        }

        double newZoom = 1000;
        stopAnimations();
        stopMovement();
        stopMovementOnCenter();
        // if this function is called the first time in Follwing mode
        // way get a jump in the zoomlevel, in case we dont handle the fact
        // that the new center isnt laying on the ground.

        // the follwing code performs:
        // if the new center is visible, we try to substain the distance to the eye

        if (shouldInitFollowZoom) {
            // System.out.println("shouldInitFollowZoom");
            Vec4 vCenter = getGlobe().computePointFromPosition(center);
            if (!getFrustumInModelCoordinates().contains(vCenter)) {
                shouldInitFollowZoom = false;
            }

            Vec4 vEye = getEyePoint();
            newZoom = vCenter.distanceTo3(vEye);
        }

        Position oldCenter = this.center;
        super.setCenterPosition(center);
        firePropertyChange(CENTER_POSITION, oldCenter, this.center);

        if (shouldInitFollowZoom) {
            // System.out.println("setting new Zoom");
            setZoom(newZoom);
            shouldInitFollowZoom = false;
        }
    }

    /*
     * orbitView moves smoothly into position
     */
    public void flyToPositon(Position center) {
        // System.err.println("flying to: "+ center + " viewMode:"+viewMode + " globe"+globe + "
        // thread:"+Thread.currentThread().toString());
        // Debug.printStackTrace();

        // System.out.println("flyToPosition");
        // System.out.println("globe"+ globe);

        // moves to a particlar position
        if (center == null) {
            return;
        }

        if (viewMode != ViewMode.DEFAULT) {
            return;
        }

        double zoom = getZoom();
        if (zoom > MAX_COMMON_ZOOM) {
            zoom = MAX_COMMON_ZOOM;
        }
        // addPanToAnimator(getCenterPosition(), center, getHeading(), getHeading(),
        // getPitch(), getPitch(), getZoom(), zoom, false);
        stopAnimations();
        if (globe == null) {
            setCenterPosition(center);
            setZoom(zoom);
        } else {
            addPanToAnimator(center, getHeading(), getPitch(), zoom, false);
        }
        // addCenterAnimator(getCenterPosition(), center, true); //this is not zooming out between the positions
    }

    public void flyToSector(Sector sector, OptionalDouble maxElev) {
        if (sector == null || sector.equals(Sector.EMPTY_SECTOR)) {
            return;
        }

        flyToSector(sector, maxElev.orElse(elevationModel.getMaxElevation(sector).max), false);
    }

    /**
     * Zooming to fit a given sector into screen with a maximal elevation maxElev of elements in the sector
     *
     * @param sector
     * @param maxElev
     */
    public void flyToSector(Sector sector, double maxElev, boolean instantaniously) {
        // System.out.println("flyToSector" + sector + " maxElev=" + maxElev + " globe2" + globe);
        // Debug.printStackTrace();

        if (sector == null || sector.equals(Sector.EMPTY_SECTOR)) {
            return;
        }

        LatLon latlon = sector.getCentroid();
        double elev = elevationModel.getElevationAsGoodAsPossible(latlon);
        if (isFlatEarth) {
            elev = 0;
            maxElev = 0;
        }

        if (maxElev == Double.NEGATIVE_INFINITY) {
            maxElev = elev;
        }

        if (sector.getDeltaLatDegrees() == 0 && sector.getDeltaLonDegrees() == 0) {
            flyToPositon(new Position(sector.getCentroid(), maxElev));
            return;
        }

        if (sector.contains(Sector.FULL_SPHERE)) {
            sector = Sector.FULL_SPHERE;
        }

        if (viewMode != null && !viewMode.equals(ViewMode.DEFAULT)) {
            return;
        }

        /*
         * method found here: http://forum.worldwindcentral.com/showthread.php?t=20650
         */
        // the sector's min lat and long is at (0°,0°)
        // this method in principle seems to be working
        // System.out.println("flyToSector");
        // Angle maxLat = sector.getMaxLatitude();
        // Angle maxLon = sector.getMaxLongitude();
        //
        // Angle minLat = sector.getMinLatitude();
        // Angle minLon = sector.getMinLongitude();

        // Sector s = new Sector(minLat, maxLat, minLon, maxLon);

        double delta_x = sector.getDeltaLonRadians();
        double delta_y = sector.getDeltaLatRadians();

        double earthRadius = Earth.WGS84_EQUATORIAL_RADIUS; // globe.getRadius(); //globe can be null

        final double shrinkTo = 0.8;

        double horizDistance = earthRadius * delta_x / 2 / shrinkTo;
        double vertDistance = earthRadius * delta_y / 2 / shrinkTo;

        if (getGlobe() instanceof FlatGlobe) {
            Vec4 delta =
                getGlobe()
                    .computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), 0)
                    .subtract3(
                        getGlobe().computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), 0));
            horizDistance = delta.x / 2 / shrinkTo;
            vertDistance = delta.y / 2 / shrinkTo;
            maxElev = 0;
        } else {
            double horzHalfAngle = horizDistance / globe.getRadius() / 2;
            double vertHalfAngle = vertDistance / globe.getRadius() / 2;
            horizDistance = 2 * Math.sin(horzHalfAngle) * globe.getRadius();
            vertDistance = 2 * Math.sin(vertHalfAngle) * globe.getRadius();
            double horizShift = globe.getRadius() * (1 - Math.cos(horzHalfAngle));
            double vertShift = globe.getRadius() * (1 - Math.cos(vertHalfAngle));
            maxElev -= (horizShift + vertShift) / 2;
        }

        // System.out.println("horDist="+horizDistance + " vertDist="+vertDistance);
        // System.out.println("viewport="+getViewport());
        if (getViewport().height == 0 || getViewport().width == 0) {
            throw new RuntimeException("window size is ZERO while trying to compute zoomlevel");
        }

        double altitudeHor = horizDistance / (Math.tan(getFieldOfView().radians / 2));
        double altitudeVert =
            vertDistance / (Math.tan(getFieldOfView().radians / 2) * getViewport().height / getViewport().width);
        // System.out.println("altitudeHor="+altitudeHor + " altitudeVert="+altitudeVert);
        double altitude = Math.max(altitudeHor, altitudeVert);

        // Form a triangle consisting of the longest distance on the ground and
        // the ray from the eye to the center point
        // The ray from the eye to the midpoint on the ground bisects the FOV
        // double distance = Math.max(horizDistance, vertDistance) / 2;
        // distance /= 0.8;
        boolean isZeroExtend = (altitude == 0.);
        // double altitude = distance / Math.tan(getFieldOfView().radians / 2);
        // System.out.println("fieldOfView " + getFieldOfView().degrees);

        if (maxElev > Double.NEGATIVE_INFINITY) {
            altitude += maxElev;
            // System.out.println("maxElev " + maxElev);
            // System.out.println("alt " + altitude);
        }

        // System.out.println(elev);

        // by sticking this to ground (elev), the camera tilt over ground, and
        // not in the air!!
        if (Double.isNaN(elev) || Double.isInfinite(elev)) {
            return;
        }

        Position pos = new Position(latlon, elev);

        double zoom = altitude - elev;
        // System.out.println("zoom1:"+zoom);
        // System.out.println("altitude:"+altitude);
        // System.out.println("computeNearClipDistance():"+computeNearClipDistance());
        // assures that nothing will be clipped
        // if (maxElev +10> altitude - computeNearClipDistance()){
        // zoom += maxElev +10- (altitude - computeNearClipDistance());
        // }
        // System.out.println("zoom1:"+zoom);
        if (isZeroExtend) {
            zoom = getZoom();
            if (zoom > MAX_COMMON_ZOOM) {
                zoom = MAX_COMMON_ZOOM;
                // System.out.println("zoom3:"+zoom);
            }
        }

        stopAnimations();
        if (globe == null) {
            setCenterPosition(pos);
            setZoom(zoom);
        } else if (instantaniously) {
            setCenterPosition(pos);
            setZoom(zoom);
            setPitch(Angle.ZERO);
            setHeading(Angle.ZERO);
        } else {
            // System.out.println("zoom4:"+zoom);
            // System.out.println("pos:"+pos);
            addPanToAnimator(
                getCenterPosition(), pos, getHeading(), Angle.ZERO, getPitch(), Angle.ZERO, getZoom(), zoom, false);
        }
    }

    protected void doApply(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null) {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGlobe() == null) {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Update DrawContext and Globe references.
        this.dc = dc;
        this.globe = this.dc.getGlobe();

        // ========== modelview matrix state ==========//
        // Compute the current modelview matrix.
        if (viewMode.isPlaneCentered()) {

            // if, for some reason, we do find the right ViewMode
            // in the subsequent code, we use the identity matrix as default
            Matrix transform = Matrix.IDENTITY;

            if (viewMode == ViewMode.COCKPIT) {
                transform = Matrix.fromRotationX(Angle.NEG90);
                fieldOfView = Angle.fromDegrees(110);
            } else if (viewMode == ViewMode.PAYLOAD) {
                fieldOfView = Angle.fromDegrees(45);
                if (hardwareConfiguration != null) {
                    IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
                    if (platformDescription != null) {
                        if (platformDescription.isInCopterMode()) {
                            // TODO FIXME, this is a very ugly dirty fix of a falcon camera view.. lets rethink the
                            // concept of these view modes in general
                            transform =
                                Matrix.fromRotationX(
                                    Angle.NEG90); // .multiply( cam.getCameraJustageTransform().getInverse() );
                        } else {
                            transform =
                                Matrix.fromRotationZ(Angle.NEG90)
                                    .multiply(
                                        CameraHelper.getCameraJustageTransform(hardwareConfiguration).getInverse());
                        }
                    }
                } else {
                    transform = Matrix.IDENTITY;
                }
            } else {
                transform = Matrix.IDENTITY;
                Debug.getLog().log(Level.WARNING, "not implemented view mode: " + viewMode, new Exception());
            }

            transform =
                transform.multiply(
                    MathHelper.getRollPitchYawTransformationMAVinicAngles(
                        roll.degrees, pitch.degrees, heading.degrees));

            // FIXME!! maybe X and Y vertauscht??
            //			Matrix transform = Matrix.IDENTITY;
            //	        transform = transform.multiply(Matrix.fromRotationX(pitch.multiply(-1.0)));
            //	        transform = transform.multiply(Matrix.fromRotationY(roll));
            //	        transform = transform.multiply(Matrix.fromRotationZ(heading));
            transform =
                transform.multiply(
                    ViewUtil.computeTransformMatrix(this.globe, this.center, Angle.ZERO, Angle.ZERO, Angle.ZERO));

            // TODO FIXME, try if current helper code in WW is fixing this issue
            // The WorldWind code uses an other convention for the rotation order
            //			this.modelview = ViewUtil.computeTransformMatrix(this.globe,
            //					this.center, this.heading, this.pitch, this.roll);
            this.modelview = transform;
        } else {
            fieldOfView = Angle.fromDegrees(45);
            this.modelview =
                OrbitViewInputSupport.computeTransformMatrix(
                    this.globe, this.center, this.heading, this.pitch, Angle.ZERO, this.zoom);
        }

        if (this.modelview == null) {
            this.modelview = Matrix.IDENTITY;
        }
        // Compute the current inverse-modelview matrix.
        this.modelviewInv = this.modelview.getInverse();
        if (this.modelviewInv == null) {
            this.modelviewInv = Matrix.IDENTITY;
        }

        // ========== projection matrix state ==========//
        // Get the current OpenGL viewport state.
        int[] viewportArray = new int[4];
        this.dc.getGL().getGL2().glGetIntegerv(GL2.GL_VIEWPORT, viewportArray, 0);
        this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
        // Compute the current clip plane distances.

        this.nearClipDistance = computeNearClipDistance();
        if (viewMode == ViewMode.COCKPIT) {
            this.nearClipDistance = 1;
        }

        this.farClipDistance = computeFarClipDistance();
        // Compute the current viewport dimensions.
        double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
        double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();
        // Compute the current projection matrix.
        this.projection =
            Matrix.fromPerspective(
                this.fieldOfView, viewportWidth, viewportHeight, this.nearClipDistance, this.farClipDistance);
        // Compute the current frustum.
        this.frustum =
            Frustum.fromPerspective(
                this.fieldOfView, (int)viewportWidth, (int)viewportHeight, this.nearClipDistance, this.farClipDistance);

        // ========== load GL matrix state ==========//
        loadGLViewState(dc, this.modelview, this.projection);

        // ========== after apply (GL matrix state) ==========//
        afterDoApply();
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return hardwareConfiguration;
    }

    public void setHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public void setViewCenter(Position center) {
        flyToPositon(center);
    }

    public void setViewCenter(Sector sector, double maxElev) {
        flyToSector(sector, maxElev, false);
    }

    public void setFlatEarth(boolean enabled) {
        if (isFlatEarth == enabled) {
            return;
        }

        stopMovement();
        stopMovementOnCenter();
        stopAnimations();

        LatLon center = getCenterPosition();
        isFlatEarth = enabled;

        if (enabled) {
            setRollInt(Angle.ZERO);
            setPitchInt(Angle.ZERO);
            setEyePositionInt(new Position(center, 0));
        } else {
            setEyePositionInt(new Position(center, elevationModel.getElevationAsGoodAsPossible(center)));
        }

        final double ZOOM_MODE_SWITCH_CORRECTION = 1.2;

        setZoom(enabled ? getZoom() * ZOOM_MODE_SWITCH_CORRECTION : getZoom() / ZOOM_MODE_SWITCH_CORRECTION);
        updateModelViewStateID();
    }

    // The following code is related to see:FlatOrbitView.class

    protected static final double MINIMUM_FAR_DISTANCE = 100;

    public double computeFarClipDistance() {
        if (isFlatEarth) {
            // Use the current eye point to auto-configure the far clipping plane distance.
            Vec4 eyePoint = this.getCurrentEyePoint();
            return computeFarClipDistance(eyePoint);
        }

        return super.computeFarClipDistance();
    }

    public double computeHorizonDistance() {
        if (isFlatEarth) {
            // Use the eye point from the last call to apply() to compute horizon distance.
            Vec4 eyePoint = this.getEyePoint();
            return this.computeHorizonDistance(eyePoint);
        }

        return super.computeHorizonDistance();
    }

    protected double computeFarClipDistance(Vec4 eyePoint) {
        double far = this.computeHorizonDistance(eyePoint);
        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }

    protected double computeHorizonDistance(Vec4 eyePoint) {
        if (isFlatEarth) {
            double horizon = 0;
            // Compute largest distance to flat globe 'corners'.
            if (this.globe != null && eyePoint != null) {
                double dist = 0;
                Vec4 p;
                // Use max distance to six points around the map
                p = this.globe.computePointFromPosition(Angle.POS90, Angle.NEG180, 0); // NW
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                p = this.globe.computePointFromPosition(Angle.POS90, Angle.POS180, 0); // NE
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                p = this.globe.computePointFromPosition(Angle.NEG90, Angle.NEG180, 0); // SW
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                p = this.globe.computePointFromPosition(Angle.NEG90, Angle.POS180, 0); // SE
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                p = this.globe.computePointFromPosition(Angle.ZERO, Angle.POS180, 0); // E
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                p = this.globe.computePointFromPosition(Angle.ZERO, Angle.NEG180, 0); // W
                dist = Math.max(dist, eyePoint.distanceTo3(p));
                horizon = dist;
            }

            return horizon;
        }

        return super.computeHorizonDistance(lastPosition);
    }

    public Position getCenterPositionToStore() {
        return getCenterPosition();
    }

    public double getZoomToStore() {
        return getZoom();
    }

    public Angle getHeadingToStore() {
        return getHeading();
    }

    public Angle getPitchToStore() {
        return getPitch();
    }

}
