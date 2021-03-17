/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata.Metadata;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata.MetadataAdapter;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata.MetadataTagMapper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thebuzzmedia.exiftool.ExifTool;
import thebuzzmedia.exiftool.ExifTool.Tag;

public class ExifInfos {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExifInfos.class);

    public static final int MIN_SIZE_TO_TEST_EXIF = 10 * 124; // 1024
    public static final int FOCAL_LENGTH_UNKNOWABLE = -2;
    public static final String TIME_PREFIX = "time_";

    public static boolean enableAllWarning = true;

    public String xmpMake = null;
    public String model = null;
    public Date datetime = null;
    public double exposureSec = -1;
    public double focalLengthMM = -1;
    public int imageWidth = -1;
    public int imageHeight = -1;
    public String userComment = null;
    public int wavelength = -1;
    public double aperture = -1;
    public double iso = -1;
    public boolean fromThumpFilename = false;
    public double timestamp = -1;
    public CPhotoLogLine embeddedLog = null; // extracted from exif comments
    public int meteringMode = -1;

    private Position position;
    private Position baseStationPosition;
    private String baseStationFixType;
    private File file;

    private static final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);

    /** generating empty default class */
    public ExifInfos() {}

    /**
     * parsing file name if it matches certain pattern, or parsing exif header immediately
     *
     * @param file
     * @throws IOException
     */
    public ExifInfos(final File file) throws IOException {
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
                    String[] tmp = name.split("_|-|,");
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
                    focalLengthMM = FOCAL_LENGTH_UNKNOWABLE; // mark as unknown, but it is ok that this is unknown!
                    return;
                }

            } catch (Exception e) {
                LOGGER.trace(
                    "Unable to extract timestamp from a image which filename looks like it contains it" + file, e);
            }

            if (StaticInjector.getInstance(ISettingsManager.class).getSection(GeneralSettings.class).getOperationLevel()
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
                    getMetadata(
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
                        Tag.IMAGE_WIDTH,
                        Tag.IMAGE_HEIGHT);
            } catch (Exception e) {
                if (enableAllWarning) {
                    LOGGER.error("Unable to run ExifTool from image at " + file, e);
                }

                return;
            }

            if (valueMap.isEmpty()) {
                if (enableAllWarning) {
                    LOGGER.error("Empty result from ExifTool from image at " + file);
                }

                return;
            }

            if (userComment == null) {
                try {
                    userComment = valueMap.get(Tag.USER_COMMENT);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.error("Unable to extract EXIF userComment via ExifTool from image at " + file + ".", e1);
                    }
                }
            }

            if (datetime == null) {
                String dateOriginal;

                try {
                    dateOriginal = valueMap.get(Tag.DATE_TIME_ORIGINAL_BACKUP);

                    if (dateOriginal != null) {
                        if (!dateOriginal.equals("null")) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                            datetime = formatter.parse(dateOriginal);
                        }
                    }
                } catch (Exception e1) {
                    dateOriginal = null;
                }

                if (dateOriginal == null && datetime == null) {
                    try {
                        dateOriginal = valueMap.get(Tag.DATE_TIME_ORIGINAL);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        datetime = formatter.parse(dateOriginal);
                    } catch (Exception e1) {
                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                            datetime = formatter.parse(dateOriginal);
                        } catch (Exception e2) { // TODO d = null; change error level
                            dateOriginal = null;
                        }
                    }
                }
            }

            if (datetime == null) {
                String dateCreated = null;
                try {
                    dateCreated = valueMap.get(Tag.DATE_TIME_CREATED);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    datetime = formatter.parse(dateCreated);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.error(
                            "Unable to extract / process EXIF datetime "
                                + dateCreated
                                + " via ExifTool from image at "
                                + file
                                + ". Using file last modification time as backup",
                            e1);
                    }
                }
            }

            if (exposureSec <= 0) {
                String exposureTime = null;

                try {
                    exposureTime = valueMap.get(Tag.EXPOSURE_TIME);
                    exposureSec = Double.parseDouble(exposureTime);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract / process EXIF exposure "
                                + exposureTime
                                + " time via ExifTool from image at "
                                + file,
                            e1);
                    }
                }
            }

            if (focalLengthMM <= 0) {
                String focalLength = null;

                try {
                    focalLength = valueMap.get(Tag.FOCAL_LENGTH);
                    focalLengthMM = Double.parseDouble(focalLength);
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract / process EXIF focallength "
                                + focalLength
                                + " via ExifTool from image at "
                                + file,
                            e1);
                    }
                }
            }

            if (xmpMake == null && valueMap.containsKey(Tag.XMP_MAKE)) {
                xmpMake = valueMap.get(Tag.XMP_MAKE).trim();
            }

            if (model == null) {
                String model = null;

                try {
                    model = valueMap.get(Tag.MODEL);
                    this.model = model.trim();
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract EXIF modelname " + model + " via ExifTool from image at " + file, e1);
                    }
                }
            }

            if (imageWidth <= 0) {
                String imageWidth = null;

                try {
                    imageWidth = valueMap.get(Tag.IMAGE_WIDTH);
                    this.imageWidth = Integer.parseInt(imageWidth.trim());
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract EXIF imageWidth " + imageWidth + " via ExifTool from image at " + file,
                            e1);
                    }
                }
            }

            if (imageHeight <= 0) {
                String imageHeight = null;

                try {
                    imageHeight = valueMap.get(Tag.IMAGE_HEIGHT);
                    this.imageHeight = Integer.parseInt(imageHeight.trim());
                } catch (Exception e1) {
                    if (enableAllWarning) {
                        LOGGER.warn(
                            "Unable to extract EXIF imageHeight " + imageHeight + " via ExifTool from image at " + file,
                            e1);
                    }
                }
            }

            if (aperture < 0) {
                String aperture = null;

                try {
                    aperture = valueMap.get(Tag.APERTURE);
                    this.aperture = Double.parseDouble(aperture);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace(
                            "Unable to extract /process aperture " + aperture + " via ExifTool from image at " + file,
                            e1);
                    }
                }
            }

            if (iso < 0) {
                String iso = null;

                try {
                    iso = valueMap.get(Tag.ISO);
                    this.iso = Double.parseDouble(iso);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace(
                            "Unable to extract /process iso " + iso + " via ExifTool from image at " + file, e1);
                    }
                }
            }

            if (meteringMode < 0) {
                String meteringMode = null;

                try {
                    meteringMode = valueMap.get(Tag.METERING_MODE);
                    this.meteringMode = Integer.parseInt(meteringMode);

                } catch (Exception e1) {
                    // this is not very severe, since some compatibleLenseIds are not providing this information!
                    if (enableAllWarning) {
                        LOGGER.trace(
                            "Unable to extract /process metering mode "
                                + meteringMode
                                + " via ExifTool from image at "
                                + file,
                            e1);
                    }
                }
            }
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

            if (!(userComment == null)) {
                userComment = userComment.trim();

                if (!userComment.isEmpty()) {
                    try {
                        embeddedLog = new CPhotoLogLine(userComment);
                    } catch (Exception e) {
                        LOGGER.info("Problems parsing command EXIF-user comment for embeddedLog: " + userComment, e);
                    }
                }
            }
        }
    }

    /**
     * warning, this call is maybe slow..
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Position getGPSPosition() throws IllegalArgumentException, SecurityException, IOException {
        if (position != null) {
            return position;
        }

        Map<Tag, String> valueMap =
            getMetadata(
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
        double alt = Double.parseDouble(valueMap.get(Tag.GPS_ALTITUDE)); // TODO for GH /100

        /** Seems like exiftool already takes care of the format of the lat/lon/alt depending on the type */
        if (valueMap.get(Tag.GPS_ALTITUDE_REF).equals("1") && alt > 0) {
            alt = -alt;
        }

        if (valueMap.get(Tag.GPS_LONGITUDE_REF).equals("W") && lon > 0) {
            lon = -lon;
        }

        if (valueMap.get(Tag.GPS_LATITUDE_REF).equals("S") && lat > 0) {
            lat = -lat;
        }

        position = Position.fromDegrees(lat, lon, alt);
        return position;
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
        Map<Tag, String> valueMap = getMetadata(file, Tag.XMP_LATITUDE, Tag.XMP_LONGITUDE);

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
        Map<Tag, String> valueMap = getMetadata(file, Tag.XMP_RELATIVE_ALTITUDE);

        return Double.parseDouble(valueMap.get(Tag.XMP_RELATIVE_ALTITUDE));
    }

    private Orientation orientation;

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
            getMetadata(
                file, Tag.ROLL, Tag.PITCH, Tag.YAW, Tag.GPS_PITCH_ANGLE, Tag.GPS_ROLL_ANGLE, Tag.GPS_IMG_DIRECTION);
        double roll = 0;
        double pitch = 0;
        double yaw = 0;
        boolean parsed = false;
        try {
            roll = -Double.parseDouble(valueMap.get(Tag.ROLL));
            // roll = Double.parseDouble(valueMap.get(Tag.ROLL));
            parsed = true;
        } catch (Exception e) {
            try {
                roll = -Double.parseDouble(valueMap.get(Tag.GPS_ROLL_ANGLE));
                // roll = Double.parseDouble(valueMap.get(Tag.GPS_ROLL_ANGLE));
                parsed = true;
            } catch (Exception e1) {
            }
        }

        try {
            pitch = 90 + Double.parseDouble(valueMap.get(Tag.PITCH)); // for GH jpg correct/equal to other function
            // pitch = Double.parseDouble(valueMap.get(Tag.PITCH));
            parsed = true;
        } catch (Exception e) {
            try {
                pitch = 90 + Double.parseDouble(valueMap.get(Tag.GPS_PITCH_ANGLE));
                pitch = Double.parseDouble(valueMap.get(Tag.GPS_PITCH_ANGLE)); // TODO check
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
        // for DJI
        Map<Tag, String> valueMap =
            getMetadata(
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

    /**
     * warning, this call is maybe slow..
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Orientation getOrientationFromGH() throws Exception {
        if (orientation != null) {
            return orientation;
        }

        Map<Tag, String> valueMap =
            getMetadata(
                file,
                Tag.AIRFRAME_ROLL_DEGREE,
                Tag.AIRFRAME_PITCH_DEGREE,
                Tag.AIRFRAME_YAW_DEGREE,
                Tag.GIMBAL_PITCH_DEGREE,
                Tag.GIMBAL_ROLL_DEGREE,
                Tag.GIMBAL_YAW_DEGREE);
        double roll = 0;
        double pitch = 0;
        double yaw = 0;

        try {
            // TODO: verify
            roll = -Double.parseDouble(valueMap.get(Tag.GIMBAL_ROLL_DEGREE));
            // roll = Double.parseDouble(valueMap.get(Tag.GIMBAL_ROLL_DEGREE));
            pitch = 90 + Double.parseDouble(valueMap.get(Tag.GIMBAL_PITCH_DEGREE));
            // pitch = Double.parseDouble(valueMap.get(Tag.GIMBAL_PITCH_DEGREE));
            yaw = Double.parseDouble(valueMap.get(Tag.GIMBAL_YAW_DEGREE));

            // roll = roll + Double.parseDouble(valueMap.get(Tag.AIRFRAME_ROLL_DEGREE));
            // pitch = pitch + Double.parseDouble(valueMap.get(Tag.AIRFRAME_PITCH_DEGREE));
            // yaw = Double.parseDouble(valueMap.get(Tag.AIRFRAME_YAW_DEGREE));
        } catch (Exception e) {
            throw new Exception("cannot read orientation from xmp metadata: " + file);
        }

        orientation = new Orientation(roll, pitch, yaw);
        return orientation;
    }
    /**
     * warning, this call is maybe slow..
     *
     * @return
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public Position getBaseStationPosition() throws IllegalArgumentException, SecurityException, IOException {
        if (baseStationPosition != null) {
            return baseStationPosition;
        }

        try {
            Map<Tag, String> valueMap =
                getMetadata(
                    file, Tag.MODEL, Tag.BASE_STATION_ALTITUDE, Tag.BASE_STATION_LATITUDE, Tag.BASE_STATION_LONGITUDE);
            double lat = Double.parseDouble(valueMap.get(Tag.BASE_STATION_LATITUDE));
            double lon = Double.parseDouble(valueMap.get(Tag.BASE_STATION_LONGITUDE));
            double alt = Double.parseDouble(valueMap.get(Tag.BASE_STATION_ALTITUDE));
            baseStationPosition = Position.fromDegrees(lat, lon, alt);
        } catch (Exception e) {
            baseStationPosition = null;
        }

        return baseStationPosition;
    }

    public String getBaseStationFixType() throws IllegalArgumentException, SecurityException, IOException {
        if (baseStationFixType != null) {
            return baseStationFixType;
        }

        Map<Tag, String> valueMap = getMetadata(file, Tag.MODEL, Tag.BASE_STATION_FIX_TYPE);
        baseStationFixType = valueMap.get(Tag.BASE_STATION_FIX_TYPE);
        return baseStationFixType;
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
                if (aperture <= 0
                        && !cameraConfiguration
                            .getLens()
                            .getDescription()
                            .isLensApertureNotAvailable()) { // negative means exif does not cotain any info about this
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

    private Map<Tag, String> getMetadata(final File image, final Tag... tags)
            throws IllegalArgumentException, SecurityException, IOException {
        /* request image description tag */
        if (MFileFilter.tiffFilter.accept(image)) {
            return applyImageDescription(image, Tag.IMAGE_DESCRIPTION, tags);
        } else if (MFileFilter.jpegFilter.accept(image)) { // TODO add only if GH
            return applyImageDescription(image, Tag.USER_COMMENT, tags);
        } else {
            Map<Tag, String> valueMap = ExifTool.instance.getImageMeta(image, tags);
            return valueMap;
        }
    }

    private Map<Tag, String> applyImageDescription(File image, Tag sourceTag, Tag[] tags)
            throws IllegalArgumentException, SecurityException, IOException {
        Tag[] tagsWithDescription = Arrays.copyOf(tags, tags.length + 1);
        tagsWithDescription[tags.length] = sourceTag;
        Map<Tag, String> valueMap = ExifTool.instance.getImageMeta(image, tagsWithDescription);
        var imageDescription = valueMap.get(sourceTag);
        if ((imageDescription != null) && (imageDescription.length() != 0)) {
            applyImageDescription(valueMap, sourceTag, tags);
        }

        return valueMap;
    }

    private void applyImageDescription(final Map<Tag, String> valueMap, Tag sourceTag, final Tag... tags) {
        var serializer = new Gson();
        if (valueMap.get(sourceTag).isEmpty() || valueMap.get(sourceTag).equals("default")) {
            return;
        }

        // LOGGER.warn("JSON TAG: " + valueMap.get(sourceTag));
        Metadata metadata = null;
        try {
            metadata = serializer.fromJson(valueMap.get(sourceTag), Metadata.class);
            if (metadata == null) {
                return;
            }
        } catch (JsonSyntaxException e) {
            LOGGER.info("No description info " + " via ExifTool from image at " + file, e);
        }

        var adapter = new MetadataAdapter(metadata);
        var mapper = new MetadataTagMapper(adapter);

        for (Tag tag : tags) {
            try {
                // dont overwrite if already available or new value is empty
                if ((valueMap.get(tag) == null) || (valueMap.get(tag).length() == 0)) {
                    var value = mapper.getValueByTag(tag);
                    if (value != null) {
                        valueMap.put(tag, value);
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
