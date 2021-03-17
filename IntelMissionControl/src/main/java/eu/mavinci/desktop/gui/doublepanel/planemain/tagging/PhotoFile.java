/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.ExpertSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.doublepanel.mapmanager.IResourceFileReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.BufferedImageCache;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.apache.commons.math3.exception.NullArgumentException;
import thebuzzmedia.exiftool.ExifTool;
import thebuzzmedia.exiftool.ExifTool.Tag;

public class PhotoFile implements Comparable<PhotoFile>, IResourceFileReferenced {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile";
    /** Very small changes in gps lat/lon wont be takes as wrong meta data */
    public static final double TINY = 0.00001;

    public static final String PREFIX_PREVIEW_IMG = "prev_";
    public static final String FOLDER_PREVIEW_IMG = "Thumbnails";
    public static final String PREFIX_FOLDER_PREVIEW_IMG = FOLDER_PREVIEW_IMG + File.separator + PREFIX_PREVIEW_IMG;
    public static int PREVIEW_WIDTH = -1;
    static DecimalFormat f = new DecimalFormat("#0.0000");

    static {
        // disable warning that native libs are not found
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    public int noInFolder;
    File file;
    ExifInfos exif;
    CPhotoLogLine log;
    double wavelength = -1;
    MapLayerMatch match;
    Object thumpLock = new Object();
    File thumpFile;

    public PhotoFile(File file, ExifInfos exif) {
        this.file = file;
        this.exif = exif;
    }

    public PhotoFile(File file) throws Exception {
        this(file, new ExifInfos(file));
    }

    public PhotoFile(File file, ExifInfos exif, double wavelength) {
        this(file, exif);
        this.wavelength = wavelength;
    }

    public PhotoFile(MapLayerMatch match, File file, ExifInfos exif) {
        this(file, exif);
        this.match = match;
    }

    public PhotoFile(MapLayerMatch match, File file) throws Exception {
        this(match, file, new ExifInfos(file));
    }

    public PhotoFile(MapLayerMatch match, File file, ExifInfos exif, double wavelength) {
        this(match, file, exif);
        this.wavelength = wavelength;
    }

    public File createThumpFileName(File file) {
        // String extension = (file.getName().toLowerCase().endsWith("jpg") ? "":".jpg");
        if(match == null ||  match.getMatching() == null) {
            Debug.getLog().log(Level.WARNING, "Create thumbfile, no correct matching defined, use default path");
            return  new File(
                    file.getParentFile().getParent(),
                    PREFIX_FOLDER_PREVIEW_IMG + MFileFilter.jpegFilter.removeExtension(file.getName()) + ".jpg");
        }
        return new File(
                match.getMatching().getMatchingFolder(),
            PREFIX_FOLDER_PREVIEW_IMG + MFileFilter.jpegFilter.removeExtension(file.getName()) + ".jpg");
    }

    // @SuppressWarnings("deprecation")
    public void extractThumbnail(File source, File target) throws Exception {
        // Debug.printStackTrace("make thump " + target);
        if (target.exists() && target.length() > 0 || source.getName().toUpperCase().endsWith("XMP")) {
            return;
        }

        if (MFileFilter.imagesWithoutPreview.acceptWithoutFolders(source)) {
            return;
        }

        try {
            if (PREVIEW_WIDTH < 1) {
                PREVIEW_WIDTH =
                    DependencyInjector.getInstance().getInstanceOf(ExpertSettings.class).getExifPreviewWidth();
            }
            // System.out.println("PREVIEW WIDTH =" + PREVIEW_WIDTH);
            // System.out.println("soruce: " + source);
            // System.out.println("target: " + target);

            target.getParentFile().mkdirs();

            File tmpTarget = new File(target.getAbsolutePath() + "_");
            if (MFileFilter.tiffFilter.accept(source)) {
                // speedup everything by this shortcat
                // System.out.println("-> speedup to java scale");
                scaleImageJava(source, tmpTarget);
                target.delete();
                tmpTarget.renameTo(target);
                return;
            }

            try {
                File oldThump = createThumpFileName(source);
                if (oldThump.exists()) {
                    FileHelper.copyFile(oldThump, tmpTarget);
                    target.delete();
                    tmpTarget.renameTo(target);
                    return;
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }

            Map<Tag, String> valueMap;
            valueMap = ExifTool.instance.getImageMeta(source, Tag.IMAGE_WIDTH, Tag.IMAGE_HEIGHT);

            String d = valueMap.get(Tag.IMAGE_WIDTH);
            if (d == null && source.exists()) {
                // Too long pathname for Exiftool! Workaround: copy somewhere and delete... (also windows has problems)
                File tmp = new File(target.getAbsolutePath() + "~");
                tmp.deleteOnExit();
                FileHelper.copyFile(source, tmp);
                valueMap = ExifTool.instance.getImageMeta(tmp, Tag.IMAGE_WIDTH, Tag.IMAGE_HEIGHT);
                source = tmp;
                // tmp.delete();
                d = valueMap.get(Tag.IMAGE_WIDTH);
            }

            int widthOrg = Integer.parseInt(d);
            d = valueMap.get(Tag.IMAGE_HEIGHT);
            int heightOrg = Integer.parseInt(d);
            // Dimension dimOrg = new Dimension(widthOrg,heightOrg);
            // System.out.println("dimOrg:"+dimOrg);
            if (widthOrg <= PREVIEW_WIDTH) {
                FileHelper.copyFile(source, tmpTarget);
                target.delete();
                tmpTarget.renameTo(target);
                return;
            }

            // File tmpTarget2= new File(target.getAbsolutePath()+"_2");
            try {
                // long t= System.currentTimeMillis();
                // if (PREVIEW_WIDTH>200){

                // preview has in general a bigger resolution, so thumbs might be nicer!
                // source

                byte[] prevData;
                prevData = ExifTool.instance.extractPreview(source);
                if (prevData == null || prevData.length == 0) {
                    throw new RuntimeException("no preview data contained in JPG");
                }

                // byte[] prevData= ExifTool.instance.extractThumbnail(source);

                // }else{
                // this seems to be even slower, even the file is smaller!
                // tmpTarget2 = ExifTool.instance.extractThumbnain(source);
                // }
                // System.out.println("extract :" + (System.currentTimeMillis() - t));
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(prevData));

                Dimension dimPreview = new Dimension(img.getWidth(), img.getHeight());
                // t= System.currentTimeMillis();

                // System.out.println("dimOrg="+dimOrg);
                // System.out.println("dimPreview="+dimPreview);
                // System.out.println("PREVIEW_WIDTH="+PREVIEW_WIDTH);
                double ratioPrev = dimPreview.height / ((double)dimPreview.width);
                double ratioTarget = heightOrg / ((double)widthOrg);
                // System.out.println("ratioPreview="+ratioPrev);

                // check aspect ratio.. otherwise crop image!!
                // if (Math.abs(dimPreview.width-dimPreview.height/ratioTarget) >=1.1){//more than some pixels off in
                // widht
                // System.out.println("scale AND crop in java");

                // System.out.println("ratioTarget="+ratioTarget);
                int width = PREVIEW_WIDTH;
                int height = (int)Math.round(width * ratioTarget);
                // System.out.println("widthPRev="+dimPreview.width+"
                // scaledOrgWidht="+((double)(dimOrg.width)/dimOrg.height*dimPreview.height));
                int xOffset = 0;
                int yOffset = 0;
                if ((dimPreview.width - dimPreview.height / ratioTarget) < 0) {
                    // current preview isn't wide enough, so black borders are on top and bottom
                    // System.out.println("A");
                    width = Math.min(width, dimPreview.width);
                    height = (int)Math.round(width * ratioTarget);
                    yOffset = (int)Math.round((width * ratioPrev - height) / 2);
                } else {
                    // current preview isn't height enough, so black borders are left and right.
                    // height would stay as it is, except scaling
                    // System.out.println("B");
                    height = Math.min(dimPreview.height, height);
                    width = (int)Math.round(height / ratioTarget);
                    xOffset = (int)Math.round((height / ratioPrev - width) / 2);
                }
                // System.out.println("height="+height+"\twidth="+width+"\txOffset="+xOffset+"\tyOffset="+yOffset);

                BufferedImage bufImg =
                    new BufferedImage(
                        width,
                        height,
                        img.getType() == BufferedImage.TYPE_BYTE_GRAY
                            ? BufferedImage.TYPE_BYTE_GRAY
                            : BufferedImage.TYPE_3BYTE_BGR);
                // System.out.println("bufImg-out:"+bufImg);
                // System.out.println("bufImg-out:"+bufImg.getHeight() + " " + bufImg.getWidth());
                Graphics2D g = bufImg.createGraphics();
                g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.drawImage(img, -xOffset, -yOffset, width + 2 * xOffset, height + 2 * yOffset, null);
                g.dispose();
                ImageIO.write(bufImg, "jpeg", tmpTarget);

                // } else {
                //
                // //preview might be still too large, even if ratio fits
                //// System.out.println("exif-previewIMG + jaca scaling (NOT cropping)");
                // //scale the image
                // scaleImageJava(tmpTarget2,tmpTarget);
                // }

                // System.out.println("done by exiftool + java cropping and final scaling in java if nessesary");
                target.delete();
                tmpTarget.renameTo(target);

                // System.out.println("other:" + (System.currentTimeMillis() - t));
                return; // this function writes by its own onto the disk
            } catch (Exception e) {
                // e.printStackTrace();
                Debug.getLog().log(Level.FINE, "EXIF-TOOL Preview failed", e);
                // } finally{
                // try{
                // tmpTarget2.delete();
                // } catch (Exception e){}
            }

            // fallback pure java
            scaleImageJava(source, tmpTarget);
            target.delete();
            tmpTarget.renameTo(target);
        } finally {
            // notify cache that preview might be different
            BufferedImageCache.dropFromCache(target);
        }
    }

    public static BufferedImage normalizeImageHistogram(BufferedImage source) {
        var img = new ImagePlus(source.toString(), source);
        var e = new ContrastEnhancer();
        e.equalize(img);
        return img.getBufferedImage();
    }

    public static void scaleImageJava(File source, File destination) throws IOException {
        // System.out.println("scale java"+source + " -> " + destination);
        BufferedImage img = ImageHelper.loadImage(source);
        boolean needRewrite = !MFileFilter.tiffFilter.accept(source);

        // System.out.println("scale java:" + source + " width="+img.getWidth());
        if (PREVIEW_WIDTH >= img.getWidth() && !needRewrite) { // allready small enough
            // System.out.println("dont scale java");
            FileHelper.copyFile(source, destination);
        } else {
            // TEST:
            // TODO: only do this for TIFF / Flir, assumed hardware should be checked evtl. param from camera json
            if (Arrays.asList(FileFilter.RAW.getExtensions())
                    .contains("*" + source.getName().substring(source.getName().lastIndexOf(".")))) {
                img = normalizeImageHistogram(img);
            }

            img = convertFloatBuffImgIfNeeded(img);
            int heigh = (int)Math.round(((double)PREVIEW_WIDTH) / img.getWidth() * img.getHeight());
            BufferedImage bufImg =
                new BufferedImage(
                    PREVIEW_WIDTH,
                    heigh,
                    img.getType() == BufferedImage.TYPE_BYTE_GRAY
                        ? BufferedImage.TYPE_BYTE_GRAY
                        : BufferedImage.TYPE_3BYTE_BGR);
            // System.out.println("bufImg-out:"+bufImg);
            // System.out.println("bufImg-out:"+bufImg.getHeight() + " " + bufImg.getWidth());
            Graphics2D g = bufImg.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.drawImage(img, 0, 0, PREVIEW_WIDTH, heigh, null);
            g.dispose();
            ImageIO.write(bufImg, "jpeg", destination);
        }
        // System.out.println("written scaled java: " + destination);
    }

    public static BufferedImage convertFloatBuffImgIfNeeded(BufferedImage imgIn) {
        if (imgIn == null) {
            throw new NullArgumentException();
        }

        if (imgIn.getRaster() == null) {
            throw new NullPointerException();
        }

        DataBuffer bufIn = imgIn.getRaster().getDataBuffer();
        if (bufIn == null) {
            throw new NullPointerException();
        }
        // System.out.println("convertBufImg");
        if (bufIn.getDataType() != DataBuffer.TYPE_FLOAT) {
            return imgIn;
        }

        BufferedImage imgOut = new BufferedImage(imgIn.getWidth(), imgIn.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        // System.out.println("w:"+imgOut.getWidth() + " x h:"+imgOut.getHeight());

        DataBuffer bufOut = imgOut.getRaster().getDataBuffer();
        final int max = bufIn.getSize();
        MinMaxPair range = new MinMaxPair();
        for (int i = 0; i != max; i++) {
            float val = bufIn.getElemFloat(i);
            range.update(val);
        }
        // System.out.println("range:"+range);
        // System.out.println("max: "+max + " "+(imgIn.getWidth()*imgIn.getHeight()));
        for (int i = 0; i != max; i++) {
            float val = bufIn.getElemFloat(i);
            // System.out.println(""+(range.mapToInterval(val)*255));
            bufOut.setElem(i, (int)(range.mapToInterval(val) * 255 + .5));
            // bufOut.setElem(i, i%256);
        }
        // System.out.println("really converted");
        // System.out.println("w:"+imgOut.getWidth() + " x h:"+imgOut.getHeight());
        return imgOut;
    }

    public static BufferedImage convertFloatBuffImgIfNeededRGB(BufferedImage imgIn) {
        DataBuffer bufIn = imgIn.getRaster().getDataBuffer();
        // System.out.println("convertBufImg");
        if (bufIn.getDataType() != DataBuffer.TYPE_FLOAT) {
            return imgIn;
        }

        BufferedImage imgOut = new BufferedImage(imgIn.getWidth(), imgIn.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        // System.out.println("w:"+imgOut.getWidth() + " x h:"+imgOut.getHeight());

        DataBuffer bufOut = imgOut.getRaster().getDataBuffer();
        final int max = bufIn.getSize();
        MinMaxPair range = new MinMaxPair();
        for (int i = 0; i != max; i++) {
            float val = bufIn.getElemFloat(i);
            range.update(val);
        }
        // System.out.println("range:"+range);
        // System.out.println("max: "+max + " "+(imgIn.getWidth()*imgIn.getHeight()));
        for (int i = 0; i != max; i++) {
            float val = bufIn.getElemFloat(i);
            // System.out.println(""+(range.mapToInterval(val)*255));
            int out = (int)(range.mapToInterval(val) * 255 + .5);
            bufOut.setElem(3 * i, out);
            bufOut.setElem(3 * i + 1, out);
            bufOut.setElem(3 * i + 2, out);
            // bufOut.setElem(i, i%256);
        }
        // System.out.println("really converted");
        // System.out.println("w:"+imgOut.getWidth() + " x h:"+imgOut.getHeight());
        return imgOut;
    }

    public ExifInfos getExif() {
        return exif;
    }

    public CPhotoLogLine getPhotoLogLine() {
        if (match != null) {
            return match.line;
        }

        return log;
    }

    public void setPhotoLogLine(CPhotoLogLine line) {
        this.log = line;
    }

    public boolean isTimestampValid() {
        return getExif().timestamp > 0;
    }

    public String isCameraMatchingThis(
            IGenericCameraConfiguration cameraConfiguration, boolean checkCameraTrueImageFalse) {
        return getExif().isCameraMatchingThis(cameraConfiguration, checkCameraTrueImageFalse);
    }

    public MapLayerMatch getMatch() {
        return match;
    }

    public void setMatch(MapLayerMatch match) {
        this.match = match;
    }

    public File makeTimePreviewFilename(File folder) {
        return new File(folder, ExifInfos.TIME_PREFIX + getExif().datetime.getTime() + "_" + file.getName());
    }

    @Override
    public int compareTo(PhotoFile o) {
        // workaround for a broken clock, fallback to filename order!
        if (!isTimestampValid() || !o.isTimestampValid()) {
            // System.out.println("compare broken PhotFiles");
            return file.getName().compareTo(o.file.getName());
        }

        int t = getExif().datetime.compareTo(o.getExif().datetime);
        if (t != 0) {
            return t;
        }

        t = Double.compare(wavelength, o.wavelength);
        if (t != 0) {
            return t;
        }

        t = file.compareTo(o.file);
        // if (t==0 && o !=this) System.out.println("files equal? " + o + " " + this);
        return t;
        // return date.compareTo(o.date); //wont work if timestamps are equal!!
    }

    @Override
    public String toString() {
        return file
            + "@"
            + f.format(getExif().timestamp)
            + "<->"
            + log
            + " focalLength="
            + getExif().focalLengthMM
            + " exposureTime="
            + getExif().exposureSec
            + " aperture="
            + getExif().aperture;
    }

    public void writeMetadata(IHardwareConfiguration hardwareConfiguration) throws Exception {
        writeMetadata(
            hardwareConfiguration,
            match.getMatching().getEstimatedStartingElevationInMoverWGS84(true),
            match.getRtkOffset());
    }

    private void writeMetadata(IHardwareConfiguration hardwareConfiguration, double groundElevation, Vec4 rtkOffset)
            throws Exception {
        Ensure.notNull(hardwareConfiguration, "hardwareConfiguration");

        CPhotoLogLine line = getPhotoLogLine();
        double lon;
        double lat;
        double ele;

        double pitch;
        double roll;
        double yaw;

        if (Double.isNaN(groundElevation)) {
            // System.out.println("evevation was nan");
            groundElevation = (line.gps_altitude_cm + line.gps_ellipsoid_cm - line.alt) / 100.;
        }

        Position p = CameraHelper.shiftPosition(line, groundElevation, rtkOffset, hardwareConfiguration);
        lat = p.getLatitude().getDegrees();
        lon = p.getLongitude().getDegrees();
        ele = p.elevation;

        Map<Tag, String> map = new TreeMap<Tag, String>();
        map.put(Tag.GPS_ALTITUDE, Math.abs(ele) + "");
        map.put(Tag.GPS_ALTITUDE_REF, ele >= 0 ? "0" : "1");
        map.put(Tag.GPS_LATITUDE, Math.abs(lat) + "");
        map.put(Tag.GPS_LATITUDE_REF, lat >= 0 ? "N" : "S");
        map.put(Tag.GPS_LONGITUDE, Math.abs(lon) + "");
        map.put(Tag.GPS_LONGITUDE_REF, lon >= 0 ? "E" : "W");
        if (line.rawString != null) {
            map.put(Tag.USER_COMMENT, line.rawString);
        }

        map.put(Tag.ORIENTATION, "1"); // here orientation fixing is for free ;-)
        // according to Agisoft:

        final ExpertSettings expertSettings = DependencyInjector.getInstance().getInstanceOf(ExpertSettings.class);
        boolean useExifLevelArmProcessing = expertSettings.getUseExifLevelArmProcessing();
        if (useExifLevelArmProcessing) {
            // 90deg offset is stored inside the levelArm Offset / TODO check for Pix4D how to handle
            // double time = cam.computePLGdataDelayMS(line)/1000;
            double time = CameraHelper.computePlgDataDelay(line, hardwareConfiguration) / 1000;

            pitch = line.cameraPitch + line.cameraPitchRate * time;
            roll = -(line.cameraRoll + line.cameraRollRate * time);
            yaw = line.cameraYaw + line.cameraYawRate * time;
        } else {
            boolean useExif90Deg = expertSettings.getUseExif90Deg();

            if (useExif90Deg) {
                OrientationData o = CameraHelper.getCorrectedOrientation(line, 90, hardwareConfiguration);

                pitch = o.pitch;
                roll = o.roll;
                yaw = o.yaw;
            } else {
                double[] camOrientation =
                    MathHelper.transformationToRollPitchYaw(
                        CameraHelper.getCameraJustageTransform(90, hardwareConfiguration));
                while (camOrientation[2] < 0) camOrientation[2] += 360;
                while (camOrientation[2] >= 360) camOrientation[2] -= 360;

                pitch = camOrientation[0];
                roll = camOrientation[1];
                yaw = camOrientation[2];
                //					System.out.println("opitch:"+pitch+" roll:"+roll+" yaw:"+yaw);
                pitch = line.cameraPitch - pitch;
                roll = -line.cameraRoll + roll;
                yaw = line.cameraYaw - yaw;
            }
        }

        //			System.out.println("cpitch:"+line.cameraPitch+" roll:"+line.cameraRoll+" yaw:"+line.cameraYaw);
        //			System.out.println(" pitch:"+pitch+" roll:"+roll+" yaw:"+yaw);
        //			OrientationData o = cam.getCorrectedOrientation(line, yaw); // ohne 90 pitch/-roll/yaw  / -> 15/3/180  /
        // Test_Falcon_gym_camNull_1101_t1_klein6
        //			OrientationData o = cam.getCorrectedOrientation(line); // looks to the left instead on wall
        //			pitch = o.pitch;
        //			roll = -o.roll;
        //			yaw = o.yaw;

        // TODO for TEST, can also get modified via camera orientation
        int paraYawNeg = expertSettings.getExifMatchingYawNeg();
        int paraPitchNeg = expertSettings.getExifMatchingPitchNeg();
        int paraRollNeg = expertSettings.getExifMatchingRollNeg();
        int paraYawAdd = expertSettings.getExifMatchingYawAdd();
        int paraPitchAdd = expertSettings.getExifMatchingPitchAdd();

        pitch = (pitch * paraPitchNeg) + paraPitchAdd;
        roll = roll * paraRollNeg;
        yaw = (yaw * paraYawNeg) + paraYawAdd;
        // if (yaw < 0 || yaw >= 360) System.out.println("yaw"+yaw + " "+file);
        // if (pitch < -180 || pitch >= 180) System.out.println("pitch"+pitch + " "+file);
        // if (roll < -180 || roll >= 180) System.out.println("roll"+roll + " "+file);

        boolean useExifRollPitchYaw = expertSettings.getUseExifRollPitchYaw();
        if (useExifRollPitchYaw) {
            map.put(Tag.PITCH, pitch + "");
            map.put(Tag.ROLL, roll + "");
            map.put(Tag.YAW, yaw + "");
        } else {
            map.put(Tag.PITCH, "");
            map.put(Tag.ROLL, "");
            map.put(Tag.YAW, "");
        }

        boolean useExifNavigator = expertSettings.getUseExifNavigator();
        if (useExifNavigator) {
            while (yaw < 0) yaw += 360;
            while (yaw >= 360) yaw -= 360;
            map.put(Tag.GPS_MAP_DATUM, "WGS-84");
            map.put(Tag.GPS_PITCH_ANGLE, pitch + ""); // -180 +180
            map.put(Tag.GPS_ROLL_ANGLE, roll + ""); // -90 +90
            map.put(Tag.GPS_IMG_DIRECTION, yaw + "");
            map.put(Tag.GPS_IMG_DIRECTION_REF, "T");
        } else {
            map.put(Tag.GPS_MAP_DATUM, "");
            map.put(Tag.GPS_PITCH_ANGLE, "");
            map.put(Tag.GPS_ROLL_ANGLE, "");
            map.put(Tag.GPS_IMG_DIRECTION, "");
            map.put(Tag.GPS_IMG_DIRECTION_REF, "");
        }

        // System.out.println("p pitch:"+pitch+"?-180+180 roll:"+roll+"?-90+90 yaw:"+yaw+"?0<360");
        ILensDescription lensDescription =
            hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class).getLens().getDescription();
        if (lensDescription.isLensManual()) {
            map.put(
                Tag.FOCAL_LENGTH,
                "" + lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue());
            map.put(Tag.FOCAL_LENGTH_35MM, "" + CameraHelper.getFocalLength35mm(hardwareConfiguration));
        }

        Map<Tag, String> valueMap;
        valueMap =
            ExifTool.instance.getImageMeta(
                file,
                Tag.CREATION_DATE,
                Tag.DATE_TIME_ORIGINAL,
                Tag.CREATION_DATE_BACKUP,
                Tag.DATE_TIME_ORIGINAL_BACKUP,
                Tag.DATE_TIME_CREATED);

        String creationDateBackup = valueMap.get(Tag.CREATION_DATE_BACKUP);
        String dateTimeOriginalBackup = valueMap.get(Tag.DATE_TIME_ORIGINAL_BACKUP);
        if (creationDateBackup == null || creationDateBackup.isEmpty()) {
            String creationDate = valueMap.get(Tag.CREATION_DATE); // evtl without 00 => Tag.DATE_TIME_CREATED
            if (creationDate != null && !creationDate.isEmpty()) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    Date datetime = formatter.parse(creationDate);
                } catch (Exception e1) {
                    creationDate = valueMap.get(Tag.DATE_TIME_CREATED);
                }
            }

            if (creationDate != null && !creationDate.isEmpty()) {
                map.put(Tag.CREATION_DATE_BACKUP, creationDate);
            } else {
                map.put(Tag.CREATION_DATE_BACKUP, "null");
            }
        }

        if (dateTimeOriginalBackup == null || dateTimeOriginalBackup.isEmpty()) {
            String dateTimeOriginal = valueMap.get(Tag.DATE_TIME_ORIGINAL); // evtl without 00 => Tag.DATE_TIME_CREATED
            if (dateTimeOriginal != null && !dateTimeOriginal.isEmpty()) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    Date datetime = formatter.parse(dateTimeOriginal);
                } catch (Exception e1) {
                    dateTimeOriginal = valueMap.get(Tag.DATE_TIME_CREATED);
                }
            }

            if (dateTimeOriginal != null && !dateTimeOriginal.isEmpty()) {
                map.put(Tag.DATE_TIME_ORIGINAL_BACKUP, dateTimeOriginal);
            } else {
                map.put(Tag.DATE_TIME_ORIGINAL_BACKUP, "null");
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String date = format.format(new Date((long)(line.getTimestamp() * 1000)));

        map.put(Tag.DATE_TIME_ORIGINAL, date);
        map.put(Tag.CREATION_DATE, date);

        // System.out.println("exif data:" + map);

        ExifTool.instance.setImageMeta(file, map);
    }
    // public static final int PREVIEW_HEIGHT = 120; //auto scaled..

    void maybeFixOrientation() {
        String d = null;
        try {
            Map<Tag, String> valueMap;

            try {
                valueMap = ExifTool.instance.getImageMeta(file, Tag.ORIENTATION);
            } catch (Exception e) {
                if (ExifInfos.enableAllWarning) {
                    Debug.getLog().log(Level.SEVERE, "Unable to run ExifTool from image at " + file, e);
                }

                return;
            }
            // System.out.println("valueMap:" + valueMap);
            d = valueMap.get(Tag.ORIENTATION);
            double orientation = Double.parseDouble(d);
            if (orientation != 1) {
                Map<Tag, String> map = new TreeMap<Tag, String>();
                map.put(Tag.ORIENTATION, "1");
                ExifTool.instance.setImageMeta(file, map);

                valueMap = ExifTool.instance.getImageMeta(file, Tag.ORIENTATION);
                d = valueMap.get(Tag.ORIENTATION);
                orientation = Double.parseDouble(d);
                // System.out.println("final orientation "+orientation);
                if (orientation != 1) {
                    if (ExifInfos.enableAllWarning) {
                        Debug.getLog().log(Level.WARNING, "orientation was not resetted even I tries at file " + file);
                    }
                }
                // } else {
                // System.out.println("orientation was ok " + file);
            }

        } catch (Exception e1) {
            if (ExifInfos.enableAllWarning) {
                Debug.getLog().log(Level.WARNING, "Unable to reset orientation " + d + " at file " + file, e1);
            }
        }
    }

    public File getFile() {
        return file;
    }

    @Override
    public File getResourceFile() {
        return getFile();
    }

    public File getThumpFile() {
        if (thumpFile == null) {
            thumpFile = createThumpFileName(getFile());
        }

        return thumpFile;
    }

    public boolean thumpFileExists() {
        File thumpFile = getThumpFile();
        return thumpFile != null && thumpFile.exists() && thumpFile.length() > 0;
    }

    public void generateThumpFile() throws Exception {
        // since this takes REALLY long, dont synchronize the whole object on this!!
        synchronized (thumpLock) {
            if (thumpFileExists()) {
                return;
            }

            extractThumbnail(file, getThumpFile());
        }
    }

    public synchronized void extractThumbnail(File target) throws Exception {
        extractThumbnail(file, target);
    }
}
