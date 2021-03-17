/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.LowestAirspace;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.util.ButtonAnnotation;
import gov.nasa.worldwindx.examples.util.ProgressAnnotation;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

public class AirplaneBalloonLayer extends GlobeAnnotation
        implements IAirplaneListenerHealth,
            IAirplaneListenerPosition,
            IAirplaneListenerPlaneInfo,
            IAirplaneListenerFlightphase {

    private final AnnotationAttributes defaultAttributes;

    Font font;
    private final AirplaneCache cache;

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ExpertLayer";

    public static final String Layer_Name = "ExpertLayer";
    Vector<Integer> healthInd = new Vector<Integer>();
    HealthData healthData = null;
    PlaneInfo planeInfo = null;
    public final String colHtmlRed = "710000";
    public final String colHtmlYellow = "dfff00";
    public final String colHtmlGreen = "006900";
    int fontOffset = 0;
    public BufferedImage clock;
    public BufferedImage speed;
    public BufferedImage altitude;
    public BufferedImage distanceTraveled;
    public BufferedImage imgBlank;
    NumberFormat form;
    IAirplane plane;
    int id_GLONASS = -1;
    int id_GPSquality = -1;
    private Annotation titleLabelDrone;
    private ButtonAnnotation btnBattery;
    private ButtonAnnotation btnIconDrone;
    private Annotation planePosition;
    private Annotation titleLabelPhaseMode;
    private Annotation titleLabelVolt;
    private Annotation titleLabelBatteryPercentage;
    private Annotation titleLabelDistance;
    private Annotation titleLabelDistanceLeft;
    private static final double ELEVATION = 50;
    private ProgressAnnotation progress;
    private int phase = 0;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    /**
     * Constructor
     *
     * @param plane
     */
    public AirplaneBalloonLayer(IAirplane plane) {
        super("", new Position(LatLon.fromDegrees(49.246930, 8.641546), ELEVATION));
        this.plane = plane;

        // setPickEnabled(false);
        // Language.addListener(this);
        // uiScaleChanged();
        setAltitudeMode(WorldWind.ABSOLUTE);
        setAlwaysOnTop(true);

        form = NumberFormat.getInstance();
        form.setMinimumFractionDigits(0);
        form.setMaximumFractionDigits(1);
        form.setGroupingUsed(false);

        this.cache = plane.getAirplaneCache();

        // Create default attributes
        defaultAttributes = new AnnotationAttributes();
        defaultAttributes.setCornerRadius(ScaleHelper.scalePixelsAsInt(10));
        defaultAttributes.setInsets(ScaleHelper.scaleInsets(new Insets(5, 10, 5, 10)));
        defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, 0.9f));
        defaultAttributes.setTextColor(Color.WHITE);
        defaultAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        defaultAttributes.setTextAlign(AVKey.LEFT);
        defaultAttributes.setSize(new java.awt.Dimension(60, 0));
        defaultAttributes.setVisible(true);
        defaultAttributes.setHighlighted(true);

        this.getAttributes().setDefaults(defaultAttributes);

        reconstructAnnotation();

        // should be at end to prevent null pointer issues
        plane.addListener(this);
    }

    /** this health data object is read and the values are updated into the gui object */
    @Override
    public void recv_health(HealthData d) {
        try {
            healthData = d;
            planeInfo = cache.getPlaneInfo();
            Vector<Integer> ind = planeInfo.indexesToShow(d);
            if (!healthInd.equals(ind)) {
                healthInd = ind;
                // reconstructAnnotation();
            }

        } catch (AirplaneCacheEmptyException e) {
        }

        try {
            recv_position(cache.getPosition());
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    /** @see IAirplaneListenerPosition#recv_position(PositionData) */
    @Override
    public void recv_position(PositionData p) {
        if (cache != null) {
            try {
                moveTo(plane.getAirplaneCache().getCurPos());
            } catch (Exception e) {
            }
        }

        Ensure.notNull(cache, "cache");
        // 0 getting time airborne ---------------
        double seconds = cache.getAirborneTime();
        String time = StringHelper.secToShortDHMS(seconds);

        if (titleLabelDistance != null) {
            titleLabelDistance.setText(time);

            Flightplan fp = plane.getFPmanager().getOnAirFlightplan();
            try {
                if (fp != null) {
                    // double speed = fp.getPlatformDescription().getPlaneSpeedMperSec();
                    // double length = fp.getLengthInMeter();
                    // String totalTime = (String)CacheManager.getInstance().getValueForKey("totalTime");

                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

                    // Calendar cal = Calendar.getInstance();
                    // cal.set(Calendar.HOUR, 0);
                    // cal.set(Calendar.MINUTE, Integer.valueOf(totalTime.split(":")[0]));
                    // cal.set(Calendar.SECOND, Integer.valueOf(totalTime.split(":")[1]));
                    long _totalTime = Math.round(fp.getTimeInSec() * 1000); // cal.getTimeInMillis();

                    // Calendar cal1 = Calendar.getInstance();
                    // cal1.set(Calendar.HOUR, 0);
                    // cal1.set(Calendar.MINUTE, Integer.valueOf(time.split(":")[0]));
                    // cal1.set(Calendar.SECOND, Integer.valueOf(time.split(":")[1]));

                    long _time = Math.round(seconds * 1000); // cal1.getTimeInMillis();

                    long dateTotal = _totalTime; // Math.round(length / speed*1000);// sdf.parse(totalTime).getTime();
                    long date = Math.round(seconds * 1000); // sdf.parse(time).getTime();
                    long diffDate = dateTotal - date;

                    titleLabelDistanceLeft.setText("-" + sdf.format(new Date(diffDate)));
                    progress.setMax(100);

                    if (phase == 2) {

                        // long value= (Math.abs(date)*100) / Math.abs(dateTotal);
                        double value = ((_time * 1.0)) / (_totalTime * 1.0);
                        // double value2 =(value*1.0)/((Math.abs(date)*1.0)/(Math.abs(dateTotal))*1.0);

                        if (!sdf.format(new Date(diffDate)).equals("00:00") && value < 1) {
                            progress.setValue(progress.getValue() + 0.001);
                        }
                    }
                }
            } catch (Exception e2) {
            }
        }

        // 2 getting altitude -----------

        double altitudeAboveGround = 0;
        int groundSpeed = 0;
        try {
            altitudeAboveGround = cache.getCurPlaneElevOverGround();

            // rounded to nearest meter

        } catch (AirplaneCacheEmptyException e) {
            if (p != null) {
                altitudeAboveGround = Math.round(p.altitude / 100.);
            }
        }

        if (p != null) {
            groundSpeed = p.groundspeed;
        }

        // -------------------

        // 3 getting total distance

        double totalDist = cache.getAirborneDistance();

        // -------------------

        // inserting the airplane data into the annotation
        String annotationText = time + "<br\\>";
        if (p == null) {
            annotationText += "<br\\><br\\>";
        } else {
            annotationText += StringHelper.speedToIngName(groundSpeed / 100., -3, true) + "<br\\>";
            LowestAirspace lowest;
            try {
                lowest = cache.getMaxMAVAltitude();
            } catch (AirplaneCacheEmptyException e) {
                Debug.getLog().log(Level.SEVERE, "THIS should never happen", e);
                return;
            }

            double airspaceAltOverGround = lowest.getMinimalAltOverGround();

            String col = colHtmlGreen;
            String txt;
            String relationChar;
            if (airspaceAltOverGround <= 5) {
                col = colHtmlRed;
                relationChar = "≥";
            } else if (altitudeAboveGround < airspaceAltOverGround - Airspace.SAFETY_MARGIN_IN_METER) {
                col = colHtmlGreen;
                relationChar = "≤";
            } else if (altitudeAboveGround < airspaceAltOverGround) {
                col = colHtmlYellow;
                relationChar = "≤";
            } else {
                col = colHtmlRed;
                relationChar = "≥";
            }

            throw new RuntimeException("If this class is used, the next line must be implemented using QuantityFormat");
            //txt =
            //    UnitsHelper.Orders.alt_like.getFormat(altitudeAboveGround, -3, true)
            //        + " "
            //        + relationChar
            //        + " "
            //        + UnitsHelper.Orders.alt_like.getFormat(airspaceAltOverGround, -3, true);
            //annotationText += "<font color=\"#" + col + "\">" + txt + "<\\font><br\\>";
        }

        annotationText += StringHelper.lengthToIngName(totalDist, -3, true);
        try {
            annotationText += "  " + StringHelper.ratioToPercent(cache.getFpProgress(), 1, true);
        } catch (AirplaneCacheEmptyException e) {
        }

        for (int i = 0; i != healthInd.size(); i++) {
            if (id_GLONASS == i) {
                continue;
            }

            if (id_GPSquality == i) {
                continue;
            }

            int ind = healthInd.get(i);
            SingleHealthDescription hd = planeInfo.healthDescriptions.get(ind);
            float abs = healthData.absolute.get(ind);
            float percent = healthData.percent.get(ind);
            String col = colHtmlGreen; // Green as default
            String txt = form.format(abs) + hd.unit;
            if (!"%".equals(hd.unit) && percent >= PlaneConstants.minLevelForValidPercent) {
                txt = StringHelper.ratioToPercent(percent / 100., 0, true) + " " + txt;
            }

            if (hd.name.contains("UAV") && titleLabelVolt != null && titleLabelBatteryPercentage != null) {
                titleLabelVolt.setText(form.format(abs) + hd.unit);
                String percentage = StringHelper.ratioToPercent(percent / 100., 0, true);
                titleLabelBatteryPercentage.setText(percentage);

                if (percentage.equals("75%")) {
                    btnBattery.setImageSource("eu/mavinci/icons/new/icon_battery_3-4.svg");
                } else if (percentage.equals("50%")) {
                    btnBattery.setImageSource("eu/mavinci/icons/new/icon_battery_2-4.svg");
                } else if (percentage.equals("25%")) {
                    btnBattery.setImageSource("eu/mavinci/icons/new/icon_battery_1-4.svg");
                } else if (percentage.equals("0%")) {
                    btnBattery.setImageSource("eu/mavinci/icons/new/icon_battery_0-4.svg");
                }
            }

            txt = hd.name + ": " + txt;
            if (hd.isFlag()) {
                txt = hd.name + " ";
                if (hd.isGreen(abs)) {
                    txt += languageHelper.getString(KEY + ".ok");
                } else {
                    txt += languageHelper.getString(KEY + ".failure");
                }

                if (hd.isRed(abs)) {
                    col = colHtmlRed;
                } else if (hd.isYellow(abs)) {
                    col = colHtmlYellow;
                }
            } else {
                if (hd.name.equals("GPS")) {
                    if (hd.isRed(abs)) {
                        col = colHtmlRed;
                    } else if (hd.isYellow(abs)) {
                        col = colHtmlYellow;
                    }

                    txt = "GPS: " + (int)abs;
                    if (getTopconMode() == 1) {
                        try {
                            if (id_GLONASS >= 0 && healthData.absolute.size() > id_GLONASS) {
                                txt += " GLO: " + (int)(healthData.absolute.get(id_GLONASS).floatValue());
                            }
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "could not get GLONASS sat count", e);
                        }
                    }

                    try {
                        if (id_GPSquality >= 0
                                && healthData.absolute.size() > id_GPSquality
                                && GPSFixType.isValid(healthData.absolute.get(id_GPSquality))) {
                            GPSFixType fixType =
                                GPSFixType.values()[(int)(healthData.absolute.get(id_GPSquality)).floatValue()];
                            if (fixType != GPSFixType.rtkFixedBL && getTopconMode() == 1) { // &&
                                // plane.getCamera().getGPStype()==GPStype.DGPS_RTK
                                col = colHtmlYellow;
                            }

                            txt += " " + fixType.getName();
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "could not get GPS_Quality", e);
                    }
                } else if (hd.name.equals("Failevents")) {
                    col = colHtmlRed;
                    try {
                        txt = cache.getLastFailEvent().toString();
                    } catch (AirplaneCacheEmptyException e) {
                        Debug.getLog().log(Level.CONFIG, "could not format last fail event", e);
                    }
                } else if (hd.isRed(abs)) {
                    col = colHtmlRed;
                    if (abs < hd.minYellow) { // some unicode character to
                        // replace <>: ≤ ≥ ᐸ ᐳ
                        txt += "≤" + form.format(hd.minYellow) + hd.unit;
                    } else {
                        txt += "≥" + form.format(hd.maxYellow) + hd.unit;
                    }
                } else if (hd.isYellow(abs)) {
                    col = colHtmlYellow;
                    if (abs < hd.minGreen) {
                        txt += "≤" + form.format(hd.minGreen) + hd.unit;
                    } else {
                        txt += "≥" + form.format(hd.maxGreen) + hd.unit;
                    }
                }
            }

            annotationText += "<br\\><font color=\"#" + col + "\">" + txt + "<\\font>";
            planePosition.setText(annotationText);
        }

        firePropertyChange(AVKey.LAYER, null, this);
    }

    public int getTopconMode() {
        try {
            return plane.getAirplaneCache().getConf().MISC_TOPCONMODE;
        } catch (AirplaneCacheEmptyException e) {
            return PlaneConstants.DEF_MISC_TOPCONMODE;
        }
    }

    /** @param annotation */
    protected void setupContainer(Annotation annotation) {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        defaultAttribs = this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setBackgroundColor(new Color(0f, 0f, 0f, 0f));
        defaultAttribs.setTextColor(Color.WHITE);
        defaultAttribs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        defaultAttribs.setSize(new java.awt.Dimension(40, 0));

        annotation.setPickEnabled(false);
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    /**
     * @param attributes
     * @return
     */
    protected AnnotationAttributes setupDefaultAttributes(AnnotationAttributes attributes) {
        Color transparentBlack = new Color(0f, 0f, 0f, 0f);

        attributes.setTextColor(Color.WHITE);
        attributes.setBackgroundColor(transparentBlack);
        // attributes.setBorderColor(transparentBlack);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        // attributes.setHighlightScale(1);
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        // attributes.setLeader(AVKey.SHAPE_NONE);

        return attributes;
    }

    /** @param annotation */
    protected void setupLabel(Annotation annotation) {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        defaultAttribs = this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setAdjustWidthToText(AVKey.SIZE_FIXED);
        defaultAttribs.setSize(new java.awt.Dimension(40, 0));
        defaultAttribs.setEffect(AVKey.TEXT_EFFECT_NONE);

        annotation.setPickEnabled(false);
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    protected void setupPlanePostition(Annotation annotation) {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setSize(new java.awt.Dimension(120, 0));
        defaultAttribs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        defaultAttribs.setEffect(AVKey.TEXT_EFFECT_NONE);
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    /** @return */
    protected AnnotationAttributes getDefaultsAnnotationAttributes() {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        defaultAttribs.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        defaultAttribs.setTextColor(Color.WHITE);
        defaultAttribs.setDistanceMaxScale(1);

        return defaultAttribs;
    }

    /** @param annotation */
    protected void setupTitle(Annotation annotation) {
        this.setupLabel(annotation);
    }

    protected void setupProgressBar(ProgressAnnotation annotation) {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setSize(new java.awt.Dimension(120, 10));
        defaultAttribs.setBorderColor(Color.WHITE);
        defaultAttribs.setBorderWidth(1);
        defaultAttribs.setInsets(new Insets(3, 0, 0, 3));
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    /** */
    void reconstructAnnotation() {
        try {
            this.setPosition(new Position(cache.getCurLatLon(), cache.getCurPlaneElevOverGround()));
        } catch (AirplaneCacheEmptyException e) {
        }

        // Main container
        Annotation mainContainer = new ScreenAnnotation("", new java.awt.Point());
        mainContainer.setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.LEFT, 1, 5));
        this.setupContainer(mainContainer);

        // Drone Name
        this.btnIconDrone =
            new ButtonAnnotation(
                "eu/mavinci/icons/new/x2/icon_balloon_falcon_x2.png",
                "eu/mavinci/icons/new/x2/icon_balloon_falcon_x2.png");
        this.titleLabelDrone = new ScreenAnnotation("FALCON8+", new java.awt.Point());
        // titleLabelDrone.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        this.setupTitle(this.titleLabelDrone);
        Annotation contentContainer = new ScreenAnnotation("", new java.awt.Point());
        contentContainer.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 5, 5));
        contentContainer.addChild(this.btnIconDrone);
        contentContainer.addChild(this.titleLabelDrone);
        this.setupContainer(contentContainer);

        // Phase Mode
        this.titleLabelPhaseMode = new ScreenAnnotation("", new java.awt.Point());
        this.setupTitle(this.titleLabelPhaseMode);
        Annotation contentContainerPhaseMode = new ScreenAnnotation("", new java.awt.Point());
        contentContainerPhaseMode.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 1, 5));
        contentContainerPhaseMode.addChild(titleLabelPhaseMode);
        this.setupContainer(contentContainerPhaseMode);

        // Plane position
        this.planePosition = new ScreenAnnotation("", new java.awt.Point());
        this.setupPlanePostition(this.planePosition);
        Annotation contentContainerPlanePosition = new ScreenAnnotation("", new java.awt.Point());
        contentContainerPlanePosition.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 1, 5));
        contentContainerPlanePosition.addChild(planePosition);
        this.setupContainer(contentContainerPlanePosition);

        // Battery
        this.btnBattery =
            new ButtonAnnotation(
                "eu/mavinci/icons/new/icon_battery_4-4.png", "eu/mavinci/icons/new/icon_battery_4-4.png");
        this.setupTitle(this.btnBattery);
        // btnBattery.getAttributes().setInsets(new Insets(-5, 0, 0, 0));

        this.titleLabelVolt = new ScreenAnnotation("", new java.awt.Point());
        // titleLabelVolt.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        this.setupTitle(this.titleLabelVolt);

        this.titleLabelBatteryPercentage = new ScreenAnnotation("", new java.awt.Point());
        titleLabelBatteryPercentage.getAttributes().setInsets(new Insets(0, 10, 0, 0));
        this.titleLabelBatteryPercentage.getAttributes().setTextAlign(AVKey.RIGHT);
        this.setupTitle(this.titleLabelBatteryPercentage);

        Annotation contentContainerBattery = new ScreenAnnotation("", new java.awt.Point());
        contentContainerBattery.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 10, 5));
        contentContainerBattery.addChild(btnBattery);
        contentContainerBattery.addChild(titleLabelVolt);
        contentContainerBattery.addChild(titleLabelBatteryPercentage);
        this.setupContainer(contentContainerBattery);

        // Distance
        this.titleLabelDistance = new ScreenAnnotation("0:00", new java.awt.Point());
        this.setupTitle(this.titleLabelDistance);

        // String totalTime = (String)CacheManager.getInstance().getValueForKey("totalTime");
        String totalTime = "";
        Flightplan fp = plane.getFPmanager().getOnAirFlightplan();
        if (fp != null) {
            totalTime = StringHelper.secToShortDHMS(fp.getTimeInSec());
        }

        this.titleLabelDistanceLeft = new ScreenAnnotation("-" + totalTime, new java.awt.Point());
        this.setupTitle(this.titleLabelDistanceLeft);
        this.titleLabelDistanceLeft.getAttributes().setInsets(new Insets(0, 39, 0, 0));
        this.titleLabelDistanceLeft.getAttributes().setTextAlign(AVKey.RIGHT);
        Annotation contentContainerDistance = new ScreenAnnotation("", new java.awt.Point());
        contentContainerDistance.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 45, 5));
        contentContainerDistance.addChild(titleLabelDistance);
        contentContainerDistance.addChild(titleLabelDistanceLeft);
        this.setupContainer(contentContainerDistance);

        // Progress Bar

        this.progress = new ProgressAnnotation(0, 0, 100);
        this.progress.setInteriorColor(Color.WHITE);
        this.progress.getInteriorInsets().set(0, 0, 0, 0);
        this.setupProgressBar(this.progress);

        Annotation contentContainerProgress = new ScreenAnnotation("", new java.awt.Point());
        contentContainerProgress.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 0, 0));
        contentContainerProgress.addChild(progress);
        this.setupContainer(contentContainerProgress);

        // Add the contents to the main
        mainContainer.addChild(contentContainer);
        mainContainer.addChild(contentContainerPhaseMode);
        mainContainer.addChild(contentContainerPlanePosition);
        mainContainer.addChild(contentContainerBattery);
        mainContainer.addChild(contentContainerDistance);
        mainContainer.addChild(contentContainerProgress);

        // Add the mainContainer
        this.removeAllChildren();
        this.addChild(mainContainer);

        firePropertyChange(AVKey.LAYER, null, this);
    }

    @Override
    public void recv_planeInfo(PlaneInfo info) {
        id_GLONASS = -1;
        id_GPSquality = -1;
        for (int i = 0; i != info.healthDescriptions.size(); i++) {
            SingleHealthDescription hd = info.healthDescriptions.get(i);
            if (hd.name.equals(PlaneConstants.DEF_GLONASS)) {
                id_GLONASS = i;
                continue;
            }

            if (hd.name.equals(PlaneConstants.DEF_GPS_QUALITY)) {
                id_GPSquality = i;
                continue;
            }
        }
    }

    @Override
    public void recv_flightPhase(Integer fp) {
        this.phase = fp;
        if (this.titleLabelPhaseMode != null) {
            this.titleLabelPhaseMode.setText(AirplaneFlightphase.getEnumByValue(fp));
        }
    }

    @Override
    public void renderNow(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        boolean depthEnabled = gl.glIsEnabled(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_DEPTH_TEST);
        super.renderNow(dc);
        if (depthEnabled) {
            gl.glEnable(GL.GL_DEPTH_TEST);
        }
    }
}
