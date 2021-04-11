package eu.mavinci.flightplan;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IMuteable;
import gov.nasa.worldwind.geom.LatLon;

// TODO ReferencePoint, TAKEOFF, LandingPoint and AOICorner should extend this
public class PointWithAltitudes implements IFlightplanPositionReferenced, IMuteable {

    protected double altInMAboveTakeoff;
    protected double altInMAboveReference;
    protected final Point point;

    public PointWithAltitudes(Point source) {
        this.point = source;
        updateAltitudes();
    }

    public PointWithAltitudes(double lat, double lon) {
        this.point = new Point(lat, lon);
    }

    public PointWithAltitudes(IFlightplanContainer container, double lat, double lon) {
        this.point = new Point(container, lat, lon);
    }

    public PointWithAltitudes(IFlightplanContainer container) {
        this.point = new Point(container);
    }

    @Override
    public void setAltInMAboveFPRefPoint(double altInM) {
        altInMAboveReference = altInM;
    }

    @Override
    public double getAltInMAboveFPRefPoint() {
        updateAltitudes();
        return altInMAboveReference + point.getAltitude();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof PointWithAltitudes) {
            PointWithAltitudes point = (PointWithAltitudes)o;
            return this.point.lat == point.point.lat
                && this.point.lon == point.point.lon
                && this.point.triggerImage == point.point.triggerImage
                && this.point.pitch == point.point.pitch
                && this.point.yaw == point.point.yaw
                && this.point.distanceMeter == point.point.distanceMeter
                && this.point.distanceSource == point.point.distanceSource
                && this.point.gsdMeter == point.point.gsdMeter;
        } else {
            return false;
        }
    }

    public boolean updateAltitudes() {
        // Todo Jan, this is sometimes, stored in the point, sometimes only volatile stuff depending on AOI type, not
        // sure what to doP
        double tmpAltRef = 0;
        double tmpAltTakeoff = 0;
        if (getFlightplan() != null) {
            double altWgs48 =
                StaticInjector.getInstance(IElevationModel.class).getElevationAsGoodAsPossible(getLatLon());
            tmpAltTakeoff = altWgs48 - getFlightplan().getTakeofftAltWgs84WithElevation();
            tmpAltRef = altWgs48 - getFlightplan().getRefPointAltWgs84WithElevation();
        }

        boolean hasChanged = tmpAltTakeoff != altInMAboveTakeoff || tmpAltRef != altInMAboveReference;
        altInMAboveTakeoff = tmpAltTakeoff;
        altInMAboveReference = tmpAltRef;

        return hasChanged;
    }

    public double getAltInMAboveTakeoff() {
        updateAltitudes();
        return altInMAboveTakeoff + point.getAltitude();
    }

    @Override
    public double getLon() {
        return point.getLon();
    }

    @Override
    public void setLon(double lon) {
        point.setLon(lon);
    }

    @Override
    public double getLat() {
        return point.getLat();
    }

    @Override
    public void setLat(double lat) {
        point.setLat(lat);
    }

    @Override
    public void setLatLon(double lat, double lon) {
        point.setLatLon(lat, lon);
    }

    @Override
    public boolean isStickingToGround() {
        return point.isStickingToGround();
    }

    @Override
    public CFlightplan getFlightplan() {
        return point.getFlightplan();
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new PointWithAltitudes((Point)point.getCopy());
    }

    @Override
    public void setParent(IFlightplanContainer container) {
        point.setParent(container);
    }

    @Override
    public IFlightplanContainer getParent() {
        return point.getParent();
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public void setMute(boolean mute) {
        point.setMute(mute);
    }

    @Override
    public void setSilentUnmute() {
        point.setSilentUnmute();
    }

    @Override
    public boolean isMute() {
        return point.isMute();
    }

    public LatLon getLatLon() {
        return point.getLatLon();
    }

    public boolean isTriggerImage() {
        return point.isTriggerImage();
    }

    public boolean isTarget() {
        return point.isTarget();
    }

    public void setTarget(boolean target) {
        point.setTarget(target);
    }

    public double getPitch() {
        return point.getPitch();
    }

    public void setPitch(double pitch) {
        point.pitch = pitch;
    }

    public void setYaw(double yaw) {
        point.yaw = yaw;
    }

    public double getYaw() {
        return point.getYaw();
    }

    public String getNote() {
        return point.getNote();
    }

    public void setNote(String note) {
        point.setNote(note);
    }

    public void setTriggerImage(boolean triggerImage) {
        point.setTriggerImage(triggerImage);
    }

    public double getDistanceMeter() {
        return point.getDistanceMeter();
    }

    public Point.DistanceSource getDistanceSource() {
        return point.getDistanceSource();
    }

    public void setDistanceSource(Point.DistanceSource source) {
        point.setDistanceSource(source);
    }

    public double getGsdMeter() {
        return point.gsdMeter;
    }

    public void setDistanceMeter(double value) {
        point.setDistanceMeter(value);
    }

    public double getFrameDiag() {
        return point.getFrameDiagonaleMeter();
    }

    public void setFrameDiag(double v) {
        point.setFrameDiagonaleMeter(v);
    }

    public void setGsdMeter(double v) {
        point.setGsdMeter(v);
    }
}
