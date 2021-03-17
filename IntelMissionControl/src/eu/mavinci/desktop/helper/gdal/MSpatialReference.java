/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.settings.SrsPrivateSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.ReferencePoint;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.LocalElevationModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class MSpatialReference implements Comparable<MSpatialReference> {

    public static final String SEPARATOR_ELEMENTS = "</|/|<";
    public static final String SEPARATOR_LINEBREAK = ">/|/|>";

    public static final String LINEBREAK_REPLACEMENT = Matcher.quoteReplacement("\r\n");
    public static final Pattern SEPARATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(SEPARATOR_ELEMENTS));
    public static final Pattern SEPARATOR_LINEBREAK_PAT = Pattern.compile(Pattern.quote(SEPARATOR_LINEBREAK));

    public static final String PREFIX_SEPARATOR = ":";
    public static final String PREFIX_EPSG = "EPSG" + PREFIX_SEPARATOR;
    public static final String WGS84_ID = PREFIX_EPSG + 4326;

    public final String id;
    public final String name;
    public String wkt;
    public String msg;
    protected SpatialReference sr;

    protected final ISrsManager srsManager;

    /** Is used in case of local coordinate system. (0, 0) by default. */
    protected ReferencePoint origin = new ReferencePoint();

    private boolean isPrivate;

    private boolean isLocal;
    private boolean isGeocentric;
    private boolean isGeographic;

    private double angularUnits;
    private double linearUnits;
    private String linearUnitsName;

    private final Globe globe;

    /**
     * pulling all we need out of gdal
     *
     * @param id
     */
    public MSpatialReference(String id, String name, String wkt, ISrsManager srsManager, Globe globe) throws Exception {
        this.id = id;
        this.srsManager = srsManager;
        this.name = name;
        this.globe = globe;
        try {
            this.wkt = getWktFromGdal(wkt); // fix wkt format stuff if user does
            // this wrong. important for exports
            // e.g. to PhotoSCan
        } catch (Exception e) {
            Debug.getLog().log(Debug.WARNING, "could not cleanup custom WKT", e);
            this.wkt = wkt;
        }

        initFromWkt();
    }

    public MSpatialReference(String id, String name, SpatialReference sr, ISrsManager srsManager, Globe globe)
            throws Exception {
        this.id = id;
        this.srsManager = srsManager;
        this.name = name;
        this.sr = sr;
        this.globe = globe;
        initFromSR();
    }

    public MSpatialReference(String stuff, ISrsManager srsManager, Globe globe) throws Exception {
        this.srsManager = srsManager;
        this.globe = globe;
        String[] parts = SEPARATOR_ELEMENTS_PAT.split(stuff);
        id = parts[0];
        name = parts[1];
        wkt = SEPARATOR_LINEBREAK_PAT.matcher(parts[2]).replaceAll(LINEBREAK_REPLACEMENT);
        angularUnits = Double.parseDouble(parts[3]);
        isLocal = Boolean.parseBoolean(parts[4]);
        isGeographic = Boolean.parseBoolean(parts[5]);
        isGeocentric = Boolean.parseBoolean(parts[6]);
        linearUnits = Double.parseDouble(parts[7]);
        linearUnitsName = parts[8];
        isPrivate = Boolean.parseBoolean(parts[9]);
        check();
    }

    public MSpatialReference(String name, SpatialReference sr, ISrsManager srsManager, Globe globe) throws Exception {
        this.id = MSpatialReference.PREFIX_EPSG + (srsManager.getMaxId() + 1);
        this.srsManager = srsManager;
        this.name = name;
        this.sr = sr;
        this.globe = globe;
        initFromSR();
    }

    public static MSpatialReference getSpatialReferenceFromFile(Globe globe, ISrsManager srsManager, Path file)
            throws Exception {
        String fileString = new String(Files.readAllBytes(file));
        SpatialReference srs = new SpatialReference();
        srs.SetFromUserInput(fileString);

        MSpatialReference src =
            new MSpatialReference((String)file.getFileName().toString(), (SpatialReference)srs, srsManager, globe);
        DependencyInjector.getInstance().getInstanceOf(SrsPrivateSettings.class).add(src);
        srsManager.loadFromApp();

        return src;
    }

    private void initFromWkt() throws Exception {
        String wkt = getWkt();
        if (wkt == null) {
            throw new Exception("could not receive WKT for: " + id);
        }

        SpatialReference srs = new SpatialReference(wkt);
        angularUnits = srs.GetAngularUnits();
        isLocal = srs.IsLocal() == 1;
        isGeographic = srs.IsGeographic() == 1;
        linearUnits = srs.GetLinearUnits();
        linearUnitsName = srs.GetLinearUnitsName();
        String parts[] = id.split(Pattern.quote(PREFIX_SEPARATOR));
        if (parts.length > 0) {
            Integer no = Integer.valueOf(parts[parts.length - 1]);
            isPrivate = no == null || (no > srsManager.ID_PRIVATE_MIN && no < srsManager.ID_PRIVATE_MAX);
        } else {
            isPrivate = true;
        }

        check();
    }

    private void initFromSR() throws Exception {
        if (sr != null) {
            this.wkt = sr.ExportToWkt();
            angularUnits = sr.GetAngularUnits();
            isLocal = sr.IsLocal() == 1;
            isGeographic = sr.IsGeographic() == 1;
            linearUnits = sr.GetLinearUnits();
            linearUnitsName = sr.GetLinearUnitsName();
        }

        String parts[] = null;
        if (id != null) {
            parts = id.split(Pattern.quote(PREFIX_SEPARATOR));
        }

        if (parts != null && parts.length > 0) {
            Integer no = StringHelper.parseInteger(parts[parts.length - 1]);
            isPrivate = no == null || (no > srsManager.ID_PRIVATE_MIN && no < srsManager.ID_PRIVATE_MAX);
        } else {
            isPrivate = true;
        }

        check();
    }

    synchronized SRStransformCacheEntry callGDALtransform(
            boolean trueIntoFalseFrom, String id, double x0, double x1, double x2, String ppszInput) throws Exception {
        SRStransformCacheEntry[] resArr;
        resArr =
            callGDALtransform(
                trueIntoFalseFrom, id, new double[] {x0}, new double[] {x1}, new double[] {x2}, ppszInput);
        return resArr[0];
    }

    CoordinateTransformation trafoFrom;
    CoordinateTransformation trafoTo;

    synchronized SRStransformCacheEntry[] callGDALtransform(
            final boolean trueIntoFalseFrom,
            final String id,
            final double[] x0,
            final double[] x1,
            final double[] x2,
            final String ppszInput)
            throws Exception {
        SpatialReference srsWgs84 = srsManager.getDefault().getSR();

        SpatialReference srs = getSR();

        LocalElevationModel elev = getGeoid();

        CoordinateTransformation trafo;
        if (trueIntoFalseFrom) {
            if (trafoTo == null) trafoTo = new CoordinateTransformation(srsWgs84, srs);
            trafo = trafoTo;
        } else {
            if (trafoFrom == null) trafoFrom = new CoordinateTransformation(srs, srsWgs84);
            trafo = trafoFrom;
        }

        int N = x0.length;
        boolean flat = Double.isNaN(x2[0]);
        double[][] mat = new double[N][flat ? 2 : 3];
        for (int i = 0; i < N; i++) {
            //			if (elev!=null && trueIntoFalseFrom){
            //				x2[i]-=elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i]));
            //				System.out.println("pre:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "  -> "
            // +elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i])) );
            //			}
            mat[i][0] = x0[i];
            mat[i][1] = x1[i];
            if (!flat) mat[i][2] = x2[i];
            //			System.out.println("in: " + x0[i] + " " + x1[i] + " " + x2[i]);
        }

        trafo.TransformPoints(mat);

        SRStransformCacheEntry[] target = new SRStransformCacheEntry[N];

        for (int i = 0; i < N; i++) {
            if (!flat && elev != null) {
                if (trueIntoFalseFrom) {
                    Angle lat = Angle.fromDegrees(x1[i]);
                    Angle lon = Angle.fromDegrees(x0[i]);
                    if (elev.contains(lat, lon)) {
                        mat[i][2] = x2[i] - elev.getElevation(lat, lon);
                        //						System.out.println("pre:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "  ->
                        // " +elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i])) );
                    }
                } else {
                    Angle lat = Angle.fromDegrees(mat[i][1]);
                    Angle lon = Angle.fromDegrees(mat[i][0]);
                    if (elev.contains(lat, lon)) {
                        mat[i][2] = x2[i] + elev.getElevation(lat, lon);
                        //						System.out.println("post:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "
                        // -> " +elev.getElevation(Angle.fromDegrees(mat[i][1]), Angle.fromDegrees(mat[i][0])) );
                    }
                }
            }

            if (flat) {
                //				System.out.println("out: " + mat[i][0] + " " + mat[i][1]);
                target[i] = new SRStransformCacheEntry(mat[i][0], mat[i][1], Double.NaN);
            } else {
                //				System.out.println("out: " + mat[i][0] + " " + mat[i][1] + " " + mat[i][2]);
                target[i] = new SRStransformCacheEntry(mat[i][0], mat[i][1], mat[i][2]);
            }
        }

        return target;
    }

    public File getGeoidFile() {
        return getGeoidFile(getSR());
    }

    LocalElevationModel geoid;

    public LocalElevationModel getGeoid() throws IOException {
        // if (geoid!=null) return geoid;
        File geoidFile = getGeoidFile();
        if (geoidFile != null) {
            LocalElevationModel elev = new LocalElevationModel();
            elev.addElevations(geoidFile);
            geoid = elev;
        }

        return geoid;
    }

    protected static File getGeoidFile(SpatialReference sr) {
        return getGeoidFile(sr.GetAttrValue("VERT_DATUM", 1), sr.GetAttrValue("VERT_DATUM", 0));
    }

    protected static File getGeoidFile(String vertDatum, String vertDatumName) {
        if ("2005".equals(vertDatum)) {
            File geoid =
                new File(
                    DependencyInjector.getInstance().getInstanceOf(IPathProvider.class).getGeoidDirectory().toFile(),
                    vertDatumName + ".tif");
            // System.out.println("geoid:"+geoid);
            if (geoid.exists()) {
                return geoid;
            }

            throw new RuntimeException("could not find geoid file for: " + vertDatumName);
        }

        return null;
    }

    public File getGeoidFileName() {
        if ("2005".equals(sr.GetAttrValue("VERT_DATUM", 1))) {
            File geoid =
                new File(
                    DependencyInjector.getInstance().getInstanceOf(IPathProvider.class).getGeoidDirectory().toFile(),
                    sr.GetAttrValue("VERT_DATUM", 0) + ".tif");
            return geoid;
        }

        return null;
    }

    public SRStransformCacheEntry fromWgs84(Position p) throws Exception {
        if (origin.isDefined()) {
            double elevation =
                globe.getElevation(Angle.fromDegrees(origin.getLat()), Angle.fromDegrees(origin.getLon()));
            Matrix m =
                globe.computeSurfaceOrientationAtPosition(
                        Angle.fromDegrees(origin.getLat()),
                        Angle.fromDegrees(origin.getLon()),
                        elevation + origin.getAltInMAboveFPRefPoint())
                    .multiply(Matrix.fromRotationZ(Angle.fromDegrees(origin.getYaw())))
                    .getInverse();
            Vec4 local =
                globe.computePointFromPosition(new Position(p, Double.isNaN(p.elevation) ? 0 : p.elevation))
                    .transformBy4(m);
            return new SRStransformCacheEntry(local.x, local.y, local.z);
        }

        SRStransformCacheEntry result =
            callGDALtransform(true, id, p.longitude.degrees, p.latitude.degrees, p.elevation, isPrivate() ? wkt : null);

        return result;
    }

    public List<SRStransformCacheEntry> fromWgs84(List<? extends LatLon> ps) throws Exception {
        ArrayList<SRStransformCacheEntry> result = new ArrayList<>();
        int len = ps.size();
        double[] x = new double[len];
        double[] y = new double[len];
        double[] z = new double[len];
        int i = 0;
        for (LatLon p : ps) {
            x[i] = p.longitude.degrees;
            y[i] = p.latitude.degrees;
            if (p instanceof Position) {
                Position p2 = (Position)p;
                z[i] = p2.elevation;
            } else {
                z[i] = Double.NaN;
            }

            result.add(fromWgs84(new Position(p, z[i])));
            i++;
        }
        // SRStransformCacheEntry[] result = callGDALtransform(true, id, x, y, z, isPrivate() ? wkt : null);

        return result;
    }

    public Position toWgs84(SRStransformCacheEntry v) throws Exception {
        Vec4 vec = new Vec4(v.x, v.y, Double.isNaN(v.z) ? 0 : v.z);
        return toWgs84(vec);
    }

    public Position toWgs84(Vec4 v) throws Exception {
        if (origin.isDefined()) {
            double elevation =
                globe.getElevation(Angle.fromDegrees(origin.getLat()), Angle.fromDegrees(origin.getLon()));
            Matrix m =
                globe.computeSurfaceOrientationAtPosition(
                        Angle.fromDegrees(origin.getLat()),
                        Angle.fromDegrees(origin.getLon()),
                        elevation + origin.getAltInMAboveFPRefPoint())
                    .multiply(Matrix.fromRotationZ(Angle.fromDegrees(origin.getYaw())));
            Vec4 ecef = v.transformBy4(m);
            return globe.computePositionFromPoint(ecef);
        }

        SRStransformCacheEntry result = callGDALtransform(false, id, v.x, v.y, v.z, isPrivate() ? wkt : (String)null);
        return Position.fromDegrees(result.y, result.x, result.z);
    }

    public String getXLabel() throws Exception {
        if (isGeographic()) {
            return "Lon";
        } else {
            return "x";
        }
    }

    public String getYLabel() throws Exception {
        if (isGeographic()) {
            return "Lat";
        } else {
            return "y";
        }
    }

    public String getXyUnit() {
        if (isGeographic()) {
            if (Math.abs(angularUnits - Math.toRadians(1)) < 1e-15) {
                return "°";
            } else {
                return "rad";
            }
        } else {
            return cleanLinearUnits(linearUnitsName);
        }
    }

    public String cleanLinearUnits(String unit) {
        if (unit.equalsIgnoreCase("metre") || unit.equalsIgnoreCase("meter")) {
            return "m";
        } else if (unit.equalsIgnoreCase("foot")) {
            return "ft";
        } else {
            return unit.trim();
        }
    }

    public String getZLabel() throws Exception {
        return "z";
    }

    public String getZUnit() {
        return cleanLinearUnits(linearUnitsName);
    }

    public static final double DEFAULT_RESOLUTION_METER = 0.001;

    private double[] resolutions;

    public double[] getResolutions() {
        if (resolutions == null) {
            double xRes = DEFAULT_RESOLUTION_METER;
            double yRes = DEFAULT_RESOLUTION_METER;
            double zRes = DEFAULT_RESOLUTION_METER;
            if (isGeographic()) {
                xRes /= Earth.WGS84_EQUATORIAL_RADIUS * angularUnits;
                yRes /= Earth.WGS84_POLAR_RADIUS * angularUnits;
            } else {
                xRes /= linearUnits;
                yRes /= linearUnits;
            }

            zRes /= linearUnits;
            resolutions = new double[] {xRes, yRes, zRes};
        }

        return resolutions;
    }

    public String fromWgs84Formatted(Position p) {
        try {
            double[] res = getResolutions();

            SRStransformCacheEntry v = fromWgs84(p);
            return getXLabel()
                + ":"
                + MathHelper.roundLike(v.x, res[0])
                + getXyUnit()
                + " "
                + getYLabel()
                + ":"
                + MathHelper.roundLike(v.y, res[1])
                + getXyUnit()
                + " "
                + getZLabel()
                + ":"
                + MathHelper.roundLike(v.z, res[2])
                + getZUnit();
        } catch (Exception e) {
            Debug.getLog().log(Level.CONFIG, "fallback to latlonAlt", e);
            return "lon:" + p.longitude.degrees + "° lat:" + p.latitude.degrees + "° elev:" + p.elevation + "m";
        }
    }

    @Override
    public String toString() {
        if (id != null) {
            return id + " " + name;
        } else {
            return name;
        }
    }

    public String getCathegory() {
        if (isGeographic()) {
            return "";
        }

        int pos = name.indexOf("/");
        if (pos <= 0) {
            return name;
        } else {
            return name.substring(0, pos).trim();
        }
    }

    @Override
    public int compareTo(MSpatialReference o) {
        // sorting order: name,id
        return (name + id).compareTo(o.name + o.id);
    }

    public String toStringTiny() {
        if (id != null) {
            return id;
        } else {
            if (name.length() > 11) {
                return name.substring(0, 8) + "...";
            }

            return name;
        }
    }

    public static final double MAXDIAMETER =
        Math.max(Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS) * 2 * Math.PI;

    public boolean isGeographic() {
        return isGeographic;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public int getNo() {
        if (id == null || id.equals("")) {
            return 0;
        }

        String parts[] = id.split(Pattern.quote(PREFIX_SEPARATOR));
        if (parts != null && parts.length > 0) {
            return StringHelper.parseInteger(parts[parts.length - 1]);
        }

        return 0;
    }

    public boolean isSRS(MSpatialReference srs) {
        if (srs.id.equals(this.id)) {
            return equalsSRS(srs);
        }

        return false;
    }

    public boolean equalsSRS(MSpatialReference srs) {
        if (srs.getSR().ExportToPrettyWkt().equals(this.getSR().ExportToPrettyWkt()) && srs.name.equals(this.name)) {
            return true;
        }

        return false;
    }

    public MSpatialReference getEqual() {
        Iterator<Entry<String, MSpatialReference>> it = srsManager.getReferences().entrySet().iterator();
        while (it.hasNext()) {
            try {
                MSpatialReference srs = it.next().getValue();
                if (this.equalsSRS(srs)) {
                    return srs;
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        return null;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public ReferencePoint getOrigin() {
        return origin;
    }

    public void setOrigin(ReferencePoint origin) {
        this.origin = origin;
    }

    public void check() throws Exception {
        // check if this system is geocentric, and if yes, refuse to load!

        // Should both be ok now
        /*
         * if (isLocal){ throw new Exception( "Open Mission Control is not supporting Local Coordinate Systems: " + id); } if (isGeocentric){
         * throw new Exception( "Open Mission Control is not supporting Geocentric Coordinate Systems: " + id); }
         */
    }

    public SpatialReference getSR() {
        // TODO Proceduren mit WKT-String-Formatierungen in MSpatialReference
        // übernehmen; dann diese verwenden
        // dito für ID und Name / Kapseln
        if (sr == null) {
            sr = new SpatialReference(wkt);
        }

        return sr;
    }

    public static String getWktFromGdal(String id) {
        try {
            SpatialReference srs = new SpatialReference();
            srs.SetFromUserInput(id);
            return srs.ExportToPrettyWkt();

        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "could not get WKT for EPSG:" + id, e);
            return null;
        }
    }

    public String getWkt() {
        if (wkt == null) {
            wkt = getWktFromGdal(id);
        }

        return wkt;
    }

    public String getWktWithoutVertical() {
        if (getSR().IsCompound() == 1) {
            String wkt = getWkt();

            // parsing this string to extract the "PROJCS" node
            StringTokenizer tokenizer = new StringTokenizer(wkt, " ,\"[]", true);
            boolean insideString = false;
            boolean insidePROJCS = false;
            int bracketsStack = 0;
            String out = "";
            int insidePROJCSstartDeth = -1;

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.equals("\"")) {
                    insideString = !insideString;
                }

                if (insidePROJCS) {
                    out += token;
                }

                if (insideString) {
                    continue;
                }

                if (token.equals("[")) {
                    bracketsStack++;
                }

                if (token.equals("]")) {
                    bracketsStack--;
                }

                if (token.equals("PROJCS")) {
                    out += token;
                    insidePROJCSstartDeth = bracketsStack;
                    insidePROJCS = true;
                } else if (insidePROJCS && insidePROJCSstartDeth == bracketsStack) {
                    // insidePROJCS=false;
                    break; // done, thats it!
                }
            }

            return out;
        } else {
            return getWkt();
        }
    }

}
