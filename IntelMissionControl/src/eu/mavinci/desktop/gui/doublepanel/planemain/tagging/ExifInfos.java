/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thebuzzmedia.exiftool.ExifTool;
import thebuzzmedia.exiftool.ExifTool.Tag;

public class ExifInfos {

    private static Logger LOGGER = LoggerFactory.getLogger(ExifInfos.class);

    public static boolean enableAllWarning = true;
    public static final int MIN_SIZE_TO_TEST_EXIF = 10 * 1024;
    public static final int FOCAL_LENGHT_UNKNOWABLE = -2;
    public static final String TIME_PREFIX = "time_";

    public String xmpMake = null;
    public String model = null;
    public Date datetime = null;
    public double exposureSec = -1;
    public double focalLengthMM = -1;
    public String userComment = null;
    public int wavelength = -1;
    public double aperture = -1;
    public double iso = -1;
    public boolean fromThumpFilename = false;
    public double timestamp = -1;
    public CPhotoLogLine embeddedLog = null; // extracted from exif comments
    public int meteringMode = -1;

    File file;

    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    /** generating empty default class */
    public ExifInfos() {}

    /**
     * parsing file name if it matches certain pattern, or parsing exif header immediately
     *
     * @param file
     * @throws IOException
     */
    public ExifInfos(File file) throws IOException {
        this();
        this.file = file;
        try {
            // Log.d("exif", "Attempting to extract EXIF date/time from image at " +
            // imagePath);
            // long start1 = System.currentTimeMillis();
            try {
                String name = file.getName();
                if (name.startsWith(TIME_PREFIX)) {
                    name = name.split(Pattern.quote("_"))[1];
                    fromThumpFilename = true;
                    datetime = new Date(Long.parseLong(name));
                }

                if (name.contains("_GapIndex_")) {
                    // rikola tif file
                    // example filename K00016.DAT-CalibratedData-12062014_214422-_01_01_1904 02-20-18_758_0.0000 0.0000
                    // _WL_896,41_GapIndex_ 745.tifDateTimeOriginalBackup
                    SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy HHmmss");
                    String tmp[] = name.split("_|-|,");
                    // System.out.println(Arrays.asList(tmp));
                    datetime = formatter.parse(tmp[2] + " " + tmp[3]);
                    // System.out.println(datetime);
                    for (int i = 4; i < tmp.length - 1; i++) {
                        if (tmp[i].equalsIgnoreCase("wl")) {
                            wavelength = Integer.parseInt(tmp[i + 1]);
                            break;
                        }
                        // TODO scan for exposure time
                    }

                    exposureSec = 10. / 1000; // 10ms TODO read this from filenames in future, ask rikola to add this!
                    focalLengthMM = FOCAL_LENGHT_UNKNOWABLE; // mark as unknown, but it is ok that this is unknown!
                    return;
                }

            } catch (Exception e) {
                LOGGER.trace(
                    "Unable to extract timestamp from a image which filename looks like it contains it" + file, e);
            }

            if (DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .getOperationLevel()
                    != OperationLevel.DEBUG) {
                if (!file.exists()) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }

                if (file.length() == 0 || !file.canRead()) {
                    throw new FileSystemException(file.getAbsolutePath());
                }
            }

            if (file.length() < MIN_SIZE_TO_TEST_EXIF) {
                LOGGER.trace(
                    "File Size ("
                        + file.length()
                        + "<"
                        + MIN_SIZE_TO_TEST_EXIF
                        + ") is too small, so EXIF extraction is not tested");
                return;
            }

            // long start2 = System.currentTimeMillis();
            Map<Tag, String> valueMap;

            try {
                valueMap =
                    ExifTool.instance.getImageMeta(
                        file,
                        Tag.DATE_TIME_ORIGINAL,
                        Tag.EXPOSURE_TIME,
                        Tag.FOCAL_LENGTH,
                        Tag.XMP_MAKE,
                        Tag.MODEL,
                        Tag.APERTURE,
                        Tag.USER_COMMENT,
                        Tag.ORIENTATION,
                        Tag.ISO,
                        Tag.METERING_MODE,
                        Tag.DATE_TIME_ORIGINAL_BACKUP,
                        Tag.DATE_TIME_CREATED,
                        Tag.GPS_TIMESTAMP,
                        Tag.GPS_DATESTAMP);
            } catch (Exception e) {
                if (enableAllWarning) {
                    LOGGER.error("Unable to run ExifTool from image at " + file, e);
                }

                return;
            }

            if (userComment == null) {
                try {
                    userComment = valueMap.get(Tag.USER_COMMENT);
                    // System.out.println("comment:"+userComment);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.error("Unable to extract EXIF userComment via ExifTool from image at " + file + ".", e1);
                    }
                }
            }

            if (datetime == null) {
                String d = null;
                try {
                    d = valueMap.get(Tag.DATE_TIME_ORIGINAL_BACKUP);
                    if (d != null) {
                        if (d.equals("null")) {
                            // handle as null
                        } else {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                            datetime = formatter.parse(d);
                        }
                    }
                } catch (Exception e1) {
                    d = null;
                }

                if (d == null && datetime == null) {
                    try {
                        d = valueMap.get(Tag.DATE_TIME_ORIGINAL);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        datetime = formatter.parse(d);
                    } catch (Exception e1) { // TODO d = null; change error level
                        d = null;
                    }
                }

                // case with flir
                if (d == null || datetime == null || datetime.getTime() <= 0) {
                    try {
                        var date = valueMap.get(Tag.GPS_DATESTAMP);
                        d = date + " " + valueMap.get(Tag.GPS_TIMESTAMP);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        datetime = formatter.parse(d);
                    } catch (Exception e1) { // TODO d = null; change error level
                        d = null;
                        datetime = null;
                    }
                }
            }

            if (datetime == null) {
                String d = null;
                if (d == null && datetime == null) {
                    try {
                        d = valueMap.get(Tag.DATE_TIME_CREATED);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        datetime = formatter.parse(d);
                    } catch (Exception e1) {
                        if (enableAllWarning) {
                            LOGGER.error(
                                "Unable to extract / process EXIF datetime "
                                    + d
                                    + " via ExifTool from image at "
                                    + file
                                    + ". Using file last modification time as backup",
                                e1);
                        }
                    }
                }
            }

            if (exposureSec <= 0) {
                String d = null;
                try {
                    d = valueMap.get(Tag.EXPOSURE_TIME);
                    exposureSec = Double.parseDouble(d);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.error(
                            "Unable to extract / process EXIF exposure "
                                + d
                                + " time via ExifTool from image at "
                                + file,
                            e1);
                    }
                }
            }

            if (focalLengthMM <= 0) {
                String d = null;
                try {
                    d = valueMap.get(Tag.FOCAL_LENGTH);
                    focalLengthMM = Double.parseDouble(d);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.error(
                            "Unable to extract / process EXIF focallength " + d + " via ExifTool from image at " + file,
                            e1);
                    }
                }
            }

            if (xmpMake == null && valueMap.containsKey(Tag.XMP_MAKE)) {
                xmpMake = valueMap.get(Tag.XMP_MAKE).trim();
            }

            if (model == null) {
                String d = null;
                try {
                    d = valueMap.get(Tag.MODEL);
                    model = d.trim();
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract EXIF modelname " + d + " via ExifTool from image at " + file, e1);
                    }
                }
            }

            if (aperture < 0) {
                String d = null;
                try {
                    d = valueMap.get(Tag.APERTURE);
                    aperture = Double.parseDouble(d);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace(
                            "Unable to extract /process aperture " + d + " via ExifTool from image at " + file, e1);
                    }
                }
            }

            if (iso < 0) {
                String d = null;
                try {
                    d = valueMap.get(Tag.ISO);
                    iso = Double.parseDouble(d);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace("Unable to extract /process iso " + d + " via ExifTool from image at " + file, e1);
                    }
                }
            }

            if (meteringMode < 0) {
                String d = null;
                try {
                    d = valueMap.get(Tag.METERING_MODE);
                    meteringMode = Integer.parseInt(d);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace(
                            "Unable to extract /process metering mode " + d + " via ExifTool from image at " + file,
                            e1);
                    }
                }
            }

            // System.out.println("time2:"+ ( System.currentTimeMillis()-start2));

        } finally {
            if (datetime != null) {
                timestamp = datetime.getTime() / 1000.;
            } else {
                timestamp = -1;
            }

            if (Double.isNaN(aperture)) {
                aperture = -1;
            }

            if (Double.isInfinite(aperture)) {
                aperture = -1;
            }

            if (Double.isNaN(iso)) {
                iso = -1;
            }

            if (Double.isInfinite(iso)) {
                iso = -1;
            }

            if (userComment == null) {
                return;
            }

            userComment = userComment.trim();
            if (!userComment.isEmpty()) {
                try {
                    embeddedLog = new CPhotoLogLine(userComment);
                } catch (Exception e) {
                    LOGGER.warn("Problems parsing command EXIF-user comment: " + userComment, e);
                }
            }
        }
    }

    Position pos;

    /**
     * warning, this call is maybe slow..
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Position getGPSPosition() throws IllegalArgumentException, SecurityException, IOException {
        if (pos != null) {
            return pos;
        }

        Map<Tag, String> valueMap =
            ExifTool.instance.getImageMeta(
                file,
                Tag.MODEL,
                Tag.GPS_ALTITUDE,
                Tag.GPS_ALTITUDE_REF,
                Tag.GPS_LATITUDE,
                Tag.GPS_LATITUDE_REF,
                Tag.GPS_LONGITUDE,
                Tag.GPS_LONGITUDE_REF);

        double lat = Double.parseDouble(valueMap.get(Tag.GPS_LATITUDE));
        double lon = Double.parseDouble(valueMap.get(Tag.GPS_LONGITUDE));
        double alt = Double.parseDouble(valueMap.get(Tag.GPS_ALTITUDE));
        if (valueMap.get(Tag.GPS_ALTITUDE_REF).equals("1")) {
            alt = -alt;
        }

        if (valueMap.get(Tag.GPS_LONGITUDE_REF).equals("W")) {
            lon = -lon;
        }

        if (valueMap.get(Tag.GPS_LATITUDE_REF).equals("S")) {
            lat = -lat;
        }

        pos = Position.fromDegrees(lat, lon, alt);
        return pos;
    }

    /**
     * Extract GPS latitute and longitude from XMP metadata.
     *
     * <p>warning, this call is maybe slow.
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public LatLon getGPSLatLonFromXmp() throws IllegalArgumentException, SecurityException, IOException {
        Map<Tag, String> valueMap = ExifTool.instance.getImageMeta(file, Tag.XMP_LATITUDE, Tag.XMP_LONGITUDE);

        double lat = Double.parseDouble(valueMap.get(Tag.XMP_LATITUDE));
        double lon = Double.parseDouble(valueMap.get(Tag.XMP_LONGITUDE));

        return LatLon.fromDegrees(lat, lon);
    }

    /**
     * Extract relative altitude from XMP metadata.
     *
     * <p>warning, this call is maybe slow.
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public double getRelativeAltitudeFromXmp() throws IllegalArgumentException, SecurityException, IOException {
        Map<Tag, String> valueMap = ExifTool.instance.getImageMeta(file, Tag.XMP_RELATIVE_ALTITUDE);

        return Double.parseDouble(valueMap.get(Tag.XMP_RELATIVE_ALTITUDE));
    }

    Orientation orientation;

    /**
     * warning, this call is maybe slow..
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Orientation getOrientation() throws Exception {
        if (orientation != null) {
            return orientation;
        }

        Map<Tag, String> valueMap =
            ExifTool.instance.getImageMeta(
                file, Tag.ROLL, Tag.PITCH, Tag.YAW, Tag.GPS_PITCH_ANGLE, Tag.GPS_ROLL_ANGLE, Tag.GPS_IMG_DIRECTION);
        double roll = 0;
        double pitch = 0;
        double yaw = 0;
        boolean parsed = false;
        try {
            roll = -Double.parseDouble(valueMap.get(Tag.ROLL));
            parsed = true;
        } catch (Exception e) {
            try {
                roll = -Double.parseDouble(valueMap.get(Tag.GPS_ROLL_ANGLE));
                parsed = true;
            } catch (Exception e1) {
            }
        }

        try {
            pitch = 90 + Double.parseDouble(valueMap.get(Tag.PITCH));
            parsed = true;
        } catch (Exception e) {
            try {
                pitch = 90 + Double.parseDouble(valueMap.get(Tag.GPS_PITCH_ANGLE));
                parsed = true;
            } catch (Exception e1) {
            }
        }

        try {
            yaw = Double.parseDouble(valueMap.get(Tag.YAW));
            parsed = true;
        } catch (Exception e) {
            try {
                yaw = Double.parseDouble(valueMap.get(Tag.GPS_IMG_DIRECTION));
                parsed = true;
            } catch (Exception e1) {
            }
        }

        if (!parsed) {
            throw new Exception("cannot read orientation from exif metadata: " + file);
        }

        orientation = new Orientation(roll, pitch, yaw);
        return orientation;
    }

    /**
     * Extract payload orientation from XMP metadata.
     *
     * <p>warning, this call is maybe slow..
     *
     * @return Payload orientation
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Orientation getOrientationFromXmp() throws Exception {
        if (orientation != null) {
            return orientation;
        }

        Map<Tag, String> valueMap =
            ExifTool.instance.getImageMeta(
                file,
                Tag.XMP_GIMBAL_ROLL_DEGREE,
                Tag.XMP_GIMBAL_PITCH_DEGREE,
                Tag.XMP_GIMBAL_YAW_DEGREE,
                Tag.XMP_FLIGHT_ROLL_DEGREE,
                Tag.XMP_FLIGHT_PITCH_DEGREE,
                Tag.XMP_FLIGHT_YAW_DEGREE);
        double roll = 0;
        double pitch = 0;
        double yaw = 0;
        boolean parsed = false;
        try {
            // TODO: verify
            roll = -Double.parseDouble(valueMap.get(Tag.XMP_GIMBAL_ROLL_DEGREE));
            pitch = 90 + Double.parseDouble(valueMap.get(Tag.XMP_GIMBAL_PITCH_DEGREE));
            // yaw = Double.parseDouble(valueMap.get(Tag.XMP_GIMBAL_YAW_DEGREE));

            // roll = roll + Double.parseDouble(valueMap.get(Tag.XMP_FLIGHT_ROLL_DEGREE));
            // pitch = pitch + Double.parseDouble(valueMap.get(Tag.XMP_FLIGHT_PITCH_DEGREE));
            yaw = Double.parseDouble(valueMap.get(Tag.XMP_FLIGHT_YAW_DEGREE));
        } catch (Exception e) {
            throw new Exception("cannot read orientation from xmp metadata: " + file);
        }

        orientation = new Orientation(roll, pitch, yaw);
        return orientation;
    }

    @Override
    public String toString() {
        return super.toString()
            + " "
            + model
            + " "
            + datetime
            + " exposureTime:"
            + exposureSec
            + "s f:"
            + focalLengthMM
            + "mm comment:"
            + userComment
            + " wavelength:"
            + wavelength
            + " apperature:"
            + aperture
            + " iso:"
            + iso
            + " fromThump:"
            + fromThumpFilename;
    }

    public String isCameraMatchingThis(
            IGenericCameraConfiguration cameraConfiguration, boolean checkCameraTrueImageFalse) {
        if (fromThumpFilename) {
            return null;
            // System.out.println("cam:"+camera+ " exifModel:"+exifModel+" focalLenMM:"+focalLengthMM+ "
            // aperture:"+aperture + "
            // exposureSec:"+exposureSec +" timestamp:"+timestamp);
        }

        String fixString =
            "\n"
                + languageHelper.getString(PhotoFile.KEY + (checkCameraTrueImageFalse ? ".checkCamera" : ".checkFile"));

        if (model != null
                && !model.isEmpty()
                && !cameraConfiguration.getDescription().getExifModels().isEmpty()
                && !cameraConfiguration.getDescription().getExifModels().contains(model)) {
            return languageHelper.getString(
                    PhotoFile.KEY + ".wrongCam", model, cameraConfiguration.getDescription().getExifModels().get(0))
                + fixString;
        }

        if (aperture != -2) { // -2 means this was never been read out, since the data was generated with an old mavinci
            // dekstop release <
            // 5.0
            if (cameraConfiguration.getLens().getDescription().isLensManual()) {
                // only manual compatibleLenseIds have broken aperture
                // all further checks are not applicable, since the data is unknown due to manual lens
                if (aperture > 0) {
                    return languageHelper.getString(PhotoFile.KEY + ".notManualLens");
                }

                return null;
            } else {
                if (aperture <= 0) { // negative means exif does not cotain any info about this
                    return languageHelper.getString(PhotoFile.KEY + ".undefinedAperture");
                }
            }
        }

        if (aperture == -2 && cameraConfiguration.getLens().getDescription().isLensManual()) {
            return null; // could not check focal length!!
        }

        final double lensFocalLength =
            cameraConfiguration
                .getLens()
                .getDescription()
                .getFocalLength()
                .convertTo(Unit.MILLIMETER)
                .getValue()
                .doubleValue();
        if (focalLengthMM > 0 && Math.abs(lensFocalLength / focalLengthMM - 1) > 0.05) { // use
            // aperture to
            // check if
            // focallength
            // could be valid
            return languageHelper.getString(PhotoFile.KEY + ".wrongFocal", focalLengthMM, lensFocalLength) + fixString;
        }

        if (cameraConfiguration.getDescription().isExposureTimeFixed()) {
            // times, checks
            // make no sense
            final double exposureTime1OverS =
                cameraConfiguration
                    .getDescription()
                    .getOneOverExposureTime()
                    .convertTo(Unit.SECOND)
                    .getValue()
                    .doubleValue();
            if (exposureSec > 0 && Math.abs(exposureTime1OverS * exposureSec - 1) > 0.01) {
                return languageHelper.getString(
                    PhotoFile.KEY + ".wrongExposure", MathHelper.round(1 / exposureSec, -1), exposureTime1OverS);
            }
        }

        if (timestamp <= 0) {
            return languageHelper.getString(PhotoFile.KEY + ".clockBroken");
        }

        return null;
    }
}
