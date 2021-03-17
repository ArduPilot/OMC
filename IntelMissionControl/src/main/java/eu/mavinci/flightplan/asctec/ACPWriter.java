/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.asctec;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.FalconDataExportSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.ProcessHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.FMLWriter;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Takeoff;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.DefaultPrettyPrinter;
import org.codehaus.jackson.impl.Indenter;
import org.w3c.dom.Document;
import thebuzzmedia.exiftool.ExifTool;

public class ACPWriter {
    private static final int DEFAULT_TAKEOFF_TOLERANCE_RADIUS = 5;

    private final Globe globe;
    private final IValidationService validationService;

    private CFlightplan plan;
    private JFileChooser chooser;
    private static File targetJpg;
    private File targetJson;

    private static final String KEY = "eu.mavinci.flightplan.ACPWriter";
    private static Vector<File> files;
    private File target;
    private MFileFilter writerType = null;
    boolean isRunning = false;
    boolean ready = false;
    private String xsl;
    private String jpgDescription;
    private File parentFile;
    private IMProgressMonitor progressMonitor;
    private File targetCSV;

    protected BufferedImage screenshot;

    public ACPWriter(
            Globe globe,
            IValidationService validationService,
            BufferedImage screenshot,
            Sector jpgSector,
            String flightplanName) {
        this.globe = globe;
        this.validationService = validationService;
        this.screenshot = screenshot;
        this.jpgDescription = fillScreenshotMetadata(flightplanName, jpgSector);
    }

    private String fillScreenshotMetadata(String flightplanName, Sector jpgSector) {
        double x_diff =
            LatLon.greatCircleDistance(jpgSector.getCorners()[3], jpgSector.getCorners()[2]).radians
                * globe.getRadius();
        double y_diff =
            LatLon.greatCircleDistance(jpgSector.getCorners()[1], jpgSector.getCorners()[2]).radians
                * globe.getRadius();

        String screenshotMetadata =
            "Description: "
                + flightplanName
                + "|IntelMissionControl|"
                + jpgSector.getCorners()[3].longitude.degrees
                + "|"
                + jpgSector.getCorners()[3].latitude.degrees
                + "|"
                + jpgSector.getCorners()[2].longitude.degrees
                + "|"
                + jpgSector.getCorners()[2].latitude.degrees
                + "|"
                + jpgSector.getCorners()[0].longitude.degrees
                + "|"
                + jpgSector.getCorners()[0].latitude.degrees
                + "|"
                + jpgSector.getCorners()[1].longitude.degrees
                + "|"
                + jpgSector.getCorners()[1].latitude.degrees
                + "|"
                + jpgSector.getCentroid().longitude.degrees
                + "|"
                + jpgSector.getCentroid().latitude.degrees
                + "|"
                + x_diff
                + "|"
                + y_diff;
        return screenshotMetadata;
    }

    public void flightplanToJpg(CFlightplan plan, File parentFile, String xsl, boolean createMap, boolean createCsv)
            throws IOException {

        // ADD jpg - Extract picture Flightplan
        // zip file to: *.ACP

        // map.jpg
        // project.json
        // zipped together to: *.acp

        this.plan = plan;
        this.xsl = xsl;
        writerType = MFileFilter.jpegFilterAscTec;
        this.target = null;
        ready = false;
        isRunning = false;
        writeFlightplanJpg(parentFile, createMap, createCsv);
    }

    public static JFileChooser getChooserFile(File defaultPath, CFlightplan plan, File parentFile, MFileFilter filter) {
        String filePath;
        File file = plan.getFile();
        System.out.println("def:" + defaultPath + " paren:" + parentFile);
        if (defaultPath == null) {
            if (file == null) {
                filePath = parentFile.getAbsolutePath().concat(File.separator + "unnamed");
            } else {
                filePath = parentFile.getAbsolutePath().concat(File.separator + file.getName());
            }
        } else {
            if (file == null) {
                filePath = defaultPath.getAbsolutePath().concat(File.separator + "unnamed");
            } else {
                filePath = defaultPath.getAbsolutePath().concat(File.separator + file.getName());
            }
        }

        file = new File(filePath);
        file = FileHelper.validateFileName(file, filter);

        JFileChooser chooser = new JFileChooser(file.getParentFile());
        chooser.setSelectedFile(file);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);

        // exec
        int result;
        result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File target = chooser.getSelectedFile();
        if (target == null) {
            return null;
        }

        target = FileHelper.validateFileName(target, filter);
        if (!FileHelper.askForOverwrite(target, null)) {
            return null;
        }

        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser;
    }

    public void flightplanToACP(CFlightplan plan, File parentFile, JFileChooser chooser) throws IOException {

        // ADD jpg - Extract picture Flightplan
        // zip file to: *.ACP

        // map.jpg
        // project.json
        // zipped together to: *.acp
        this.plan = plan;
        this.chooser = chooser;
        if (chooser == null) {
            if (FileHelper.validateFileName(getTarget(), MFileFilter.anpFPExportFilter).equals(getTarget())) {
                writerType = MFileFilter.anpFPExportFilter;
            } else {
                writerType = MFileFilter.acpFPExportFilter;
            }
        } else {
            writerType = (MFileFilter)chooser.getFileFilter();
        }

        this.target = null;
        ready = false;
        isRunning = false;
        writeFlightplanACP(parentFile);
    }

    public void writeFileAddACPLayers() throws IOException {
        if (target != null && !ready) {
            ready = true;

            if (writerType.equals(MFileFilter.anpFPExportFilter) || writerType.equals(MFileFilter.acpFPExportFilter)) {
                writeACP();
            } else {
                writeJpg();
            }
            // addACPLayers();
        }

        if (ready && target != null) {
            executeAscTecNavigator();
        }
    }

    private BufferedImage captureImage(WMSTiledImageLayer layer, Sector sector, int minSize) throws Exception {
        System.out.println("sector:" + sector);
        int[] size = this.adjustSize(sector, minSize);
        int width = size[0], height = size[1];
        String mimeType = layer.getDefaultImageFormat();
        // "image/dds", "image/png", "image/jpeg"
        mimeType = "image/png";

        return layer.composeImageForSector(sector, width, height, 1d, -1, mimeType, true, screenshot, 30000);
    }

    private int[] adjustSize(Sector sector, int desiredSize) {
        int[] size = new int[] {desiredSize, desiredSize};

        if (null != sector && desiredSize > 0) {
            LatLon centroid = sector.getCentroid();
            Angle dLat =
                LatLon.greatCircleDistance(
                    new LatLon(sector.getMinLatitude(), sector.getMinLongitude()),
                    new LatLon(sector.getMaxLatitude(), sector.getMinLongitude()));
            Angle dLon =
                LatLon.greatCircleDistance(
                    new LatLon(centroid.getLatitude(), sector.getMinLongitude()),
                    new LatLon(centroid.getLatitude(), sector.getMaxLongitude()));

            double max = Math.max(dLat.radians, dLon.radians);
            double min = Math.min(dLat.radians, dLon.radians);

            int minSize = (int)((min == 0d) ? desiredSize : ((double)desiredSize * min / max));

            if (dLon.radians > dLat.radians) {
                size[0] = desiredSize; // width
                size[1] = minSize; // height
            } else {
                size[0] = minSize; // width
                size[1] = desiredSize; // height
            }
        }

        return size;
    }

    static class JsonFormate implements Indenter {
        final String indent;
        final String newline;

        public JsonFormate() {
            this("\t", System.getProperty("line.separator"));
        }

        public JsonFormate(String indent, String newline) {
            this.indent = indent;
            this.newline = newline;
        }

        @Override
        public boolean isInline() {
            return false;
        }

        @Override
        public void writeIndentation(JsonGenerator jg, int level) throws IOException, JsonGenerationException {
            jg.writeRaw(newline);
            for (int i = 0; i < level; i++) {
                jg.writeRaw(indent);
            }
        }
    }

    public void writeFlightplanACP(File parentFile) throws IOException {
        if (plan == null) {
            return;
        }

        this.parentFile = parentFile;

        files = new Vector<File>();

        // WRITE json-File:
        targetJson = new File(parentFile, "project.json");

        writeJson(plan, targetJson);

        files.add(targetJson);

        // WRITE jpg-File:
        // openJpg();
        inputTarget();
        if (target == null) {
            return;
        }

        writeFileAddACPLayers();
    }

    public void writeFlightplanJpg(File parentFile, boolean createMap, boolean createCsv) throws IOException {
        if (plan == null) {
            return;
        }

        this.parentFile = parentFile;

        if (createCsv) {
            createCSV();
            if (targetCSV == null) {
                return;
            }
        }

        // files = new Vector<File>();
        if (createMap) {
            // openJpg();
            // getPicture();
            inputTarget();

            if (target == null) {
                return;
            }

            Debug.getLog().log(Level.FINE, "take Screenshot after inputTarget");
        } else {
            ready = true;
        }

        writeFileAddACPLayers();
    }

    public File getTargetCsv() {
        File falcon8CsvPath =
            new File(StaticInjector.getInstance(FalconDataExportSettings.class).getFalcon8CsvExportPath());
        chooser = ACPWriter.getChooserFile(falcon8CsvPath, plan, parentFile, MFileFilter.csvAscTecFpExportFilter);
        if (chooser == null) {
            ready = true;
            if (progressMonitor != null) {
                progressMonitor.close();
            }

            return null;
        }

        return chooser.getSelectedFile();
    }

    public void createCSV(String xsl) throws Exception {
        targetCSV = getTargetCsv();
        if (targetCSV == null) {
            return;
        }

        targetCSV = FileHelper.validateFileName(targetCSV, MFileFilter.csvAscTecFpExportFilter);

        try (ByteArrayOutputStream outOrg = new ByteArrayOutputStream()) {
            FMLWriter writer = new FMLWriter();
            writer.writeFlightplan(plan, outOrg);

            try (InputStream orgIn = new ByteArrayInputStream(outOrg.toByteArray());
                InputStream isXSL = ClassLoader.getSystemResourceAsStream(xsl)) {
                Source xsltSource = new StreamSource(isXSL);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(true);
                factory.setFeature("http://xml.org/sax/features/namespaces", false);
                factory.setFeature("http://xml.org/sax/features/validation", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                // open up the xml document
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                Document doc = docbuilder.parse(orgIn);

                class ClasspathResourceURIResolver implements URIResolver {
                    @Override
                    public Source resolve(String href, String base) throws TransformerException {
                        try (InputStream isHREF = ClassLoader.getSystemResourceAsStream(href)) {
                            return new StreamSource(isHREF);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }

                TransformerFactory transformfactory = TransformerFactory.newInstance();
                transformfactory.setURIResolver(new ClasspathResourceURIResolver());
                Templates xslTemplate = transformfactory.newTemplates(xsltSource);
                Transformer transformer = xslTemplate.newTransformer();

                try (PrintStream out = new PrintStream(targetCSV, "UTF-8")) {
                    // now transform
                    transformer.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(out));
                }
            }

            // executeAscTecNavigator();
            // FileHelper.openFile(target);
        }
    }

    public void createCSV() throws IOException {
        targetCSV = getTargetCsv();
        if (targetCSV == null) {
            return;
        }

        targetCSV = FileHelper.validateFileName(targetCSV, MFileFilter.csvAscTecFpExportFilter);
        try (PrintStream out = new PrintStream(targetCSV, ProtocolTokens.encoding)) {
            try {
                // ((Flightplan)plan).getTakeoff().updateFromUAV(controller.getPlane());
            } catch (Exception e) {
                Debug.getLog()
                    .log(
                        Level.INFO,
                        "Can not send mission with absolute altitude while no start elevation of UAV is known",
                        e);
            }

            // write header
            // out.print(Application.getExportHeader());
            // out.print("#XYZ_WKT: " + srs.getWkt().replace("\n", "").replace("\r", "") + " \r\n");
            out.print("Type;Longitude;Latitude;Height;Heading;Camera pitch;Velocity;Comment\r\n");

            ACPTaskModel model = new ACPModelExtractor().extractModel(plan, 0, null);
            ACPTaskModel.ACPTask lastWP = null;

            boolean heading = false;

            PicArea area = null;

            IFlightplanContainer container = plan.getParent();
            if (container == null) {
                container = (IFlightplanContainer)plan;
            }
            // TODO if more than one AOIs he takes first AOI container and uses M/S according this AOI. Has to be
            // changed!
            for (IFlightplanStatement statement : container) {
                if (statement instanceof PicArea) {
                    area = (PicArea)statement;
                    break;
                }
            }

            if (area == null
                    || (area.getPlanType() != PlanType.TOWER
                        && area.getPlanType() != PlanType.FACADE
                        && area.getPlanType() != PlanType.WINDMILL
                        && area.getPlanType() != PlanType.BUILDING
                        && area.getPlanType() != PlanType.POINT_OF_INTEREST
                        && area.getPlanType() != PlanType.PANORAMA)) {
                heading = true; // according to eu/mavinci/core/xml/toAscTecCsvFollow.xsl
            } else {
                heading = false; // according to eu/mavinci/core/xml/toAscTecCsvSingle.xsl
            }

            for (ACPTaskModel.ACPTask wp : model.getTasks()) {
                /* height not in cm but in m!! */
                writePoint(out, wp, heading);
                lastWP = wp;
            }
            // landing point
            //  S;8.388382497660935;49.306663761801225;55;;;;2, Landingpoint, previous height: NaN
            // fly to landing spot / landing spot = same as start / if possible

        }
    }

    private void executeAscTecNavigator(File csv, File map) throws IOException {
        final File asctecNavExe = new File(StaticInjector.getInstance(FalconDataExportSettings.class).getFalcon8Path());
        // if(target==null ){
        final Process p;
        if (StaticInjector.getInstance(IVersionProvider.class).getSystem().isLinux()) {
            // is not running with wine
            // if(asctecNavExe==null || (csv==null)){
            if (csv != null) {
                FileHelper.openFile(csv);
            }

            if (map != null) {
                FileHelper.openFile(map);
            }

            return;
            // }
            // if(!taskIsRunning(asctecNavExe)){
            // p = ProcessHelper.exec(new String[]{"wine", Application.getFalcon8Path().getAbsolutePath()});
            // }else{
            // CDebug.getLog().log(Level.INFO, "Do not start program because it is already running:
            // "+asctecNavExe.getName());
            // }

        } else if (StaticInjector.getInstance(IVersionProvider.class).getSystem().isMac()) {
            // not implemented;
        } else {
            if (asctecNavExe == null || !asctecNavExe.exists() || (csv == null)) {
                if (csv != null) {
                    FileHelper.openFile(csv);
                }

                if (map != null) {
                    FileHelper.openFile(map);
                }

                return;
            }

            if (!taskIsRunning(asctecNavExe)) {
                p = ProcessHelper.exec(new String[] {asctecNavExe.getAbsolutePath()});
            } else {
                Debug.getLog()
                    .log(Level.INFO, "Do not start program because it is already running: " + asctecNavExe.getName());
            }
        }
        // TODO open AscTec Navigator with PARAMETER
    }

    private static boolean taskIsRunning(File asctecNavExe) throws IOException {
        String line;
        Process p;
        boolean isRunning = false;
        if (StaticInjector.getInstance(IVersionProvider.class).getSystem().isWindows()) {
            String fileNameInTasklist = asctecNavExe.getName().replace(" ", "");
            String exec = "tasklist /NH /FI \"imagename eq " + fileNameInTasklist + "\"";
            p = Runtime.getRuntime().exec(exec);
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    if (line.contains(fileNameInTasklist)) {
                        isRunning = true;
                    }
                }

                input.close();
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "ACPWriter input", e);
            }
        } else {
            return false;
        }

        return isRunning;
    }

    private void executeAscTecNavigator(File acp) throws IOException {
        final File asctecNavExe =
            new File(StaticInjector.getInstance(FalconDataExportSettings.class).getFalcon8PlusPath());
        if (asctecNavExe == null || !asctecNavExe.exists()) {
            FileHelper.openFile(acp);

            return;
        }
        // TODO
        final Process p;
        if (StaticInjector.getInstance(IVersionProvider.class).getSystem().isLinux()) {
            p = ProcessHelper.exec(new String[] {"wine", asctecNavExe.getAbsolutePath()});
        } else if (StaticInjector.getInstance(IVersionProvider.class).getSystem().isMac()) {
            // not implemented
        } else {
            p = ProcessHelper.exec(new String[] {asctecNavExe.getAbsolutePath()});
        }
    }

    private void executeAscTecNavigator() throws IOException {
        if (true) {
            // Debug.getLog().log(Level.WARNING, "Auto opening was disabled according to INMAV-2719");
            return;
        }

        if (!writerType.equals(MFileFilter.anpFPExportFilter)
                && !writerType.equals(MFileFilter.acpFPExportFilter)) // Falcon 8
        {
            // TODO move csv into this class, 2nd parameter
            executeAscTecNavigator(this.targetCSV, this.target);
        } else { // Falcon 8+
            executeAscTecNavigator(this.target);
        }
    }

    public void writeACP() throws IOException {
        targetJpg = new File(parentFile, "map.jpg");
        targetJpg = writeJpg(targetJpg, screenshot);
        files.add(targetJpg);

        // generate zip-File

        if (target != null) {
            FileHelper.zipManyFiles(target, files);

            if (StaticInjector.getInstance(GeneralSettings.class).getOperationLevel() != OperationLevel.DEBUG) {
                for (File f : files) {
                    f.delete();
                }
            } else {
                for (File f : files) {
                    FileHelper.openFile(f);
                }
            }

            // FileHelper.openFile(target);
        }
    }

    public void writeJpg() throws IOException {
        target = writeJpg(target, screenshot);
        // TODO copy jpg-file to AscTec Navigator path
        // FileHelper.openFile(target);

    }

    public void inputTarget() {
        target = getTarget();
        if (target == null) {
            return;
        }

        if (writerType.equals(MFileFilter.anpFPExportFilter) || writerType.equals(MFileFilter.acpFPExportFilter)) {
            target = FileHelper.validateFileName(target, writerType);
        } else {
            target = FileHelper.validateFileName(target, MFileFilter.jpegFilterAscTec);
        }
    }

    public File getTarget() {
        var settings = StaticInjector.getInstance(FalconDataExportSettings.class);

        if (writerType != null
                && (writerType.equals(MFileFilter.anpFPExportFilter)
                    || writerType.equals(MFileFilter.acpFPExportFilter))) {
            File acpFolder = new File(settings.getFalcon8PlusAcpExportPath());
            chooser = getChooserFile(acpFolder, plan, parentFile, writerType);
        } else {
            File mapFolder = new File(settings.getFalcon8MapExportPath());
            chooser = getChooserFile(mapFolder, plan, parentFile, MFileFilter.jpegFilterAscTec);
        }

        if (chooser == null) {
            ready = true;
            this.target = null;
            if (progressMonitor != null) {
                progressMonitor.close();
            }

            return null;
        }

        return chooser.getSelectedFile();
    }

    public File writeJpg(File targetJpg, BufferedImage bi) throws IOException {
        String suffix = WWIO.getSuffix(targetJpg.getPath());
        ImageIO.write(bi, suffix, targetJpg);
        if (jpgDescription != null) {
            Map<ExifTool.Tag, String> map = new TreeMap<ExifTool.Tag, String>();
            map.put(ExifTool.Tag.ASCTECCOMMENT, jpgDescription);
            ExifTool.instance.setImageMeta(targetJpg, map);
            Debug.getLog().log(Level.INFO, "Image description saved to file" + targetJpg.getPath());
        }

        Debug.getLog().log(Level.INFO, "Image saved to file" + targetJpg.getPath());

        return targetJpg;
    }

    public void writeJson(CFlightplan plan, File targetJson) throws IOException, JsonGenerationException {
        System.out.println("Generate file: " + targetJson.getAbsolutePath());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(new JsonFormate());
        prettyPrinter.indentObjectsWith(new JsonFormate());

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator gen = jsonFactory.createJsonGenerator(os, JsonEncoding.UTF8);
        gen.setPrettyPrinter(prettyPrinter);

        gen.writeStartObject();

        writeCamera(plan, gen);
        writeDescription(plan, gen);
        writeFlightPlanHash(plan, gen);
        writeTakeoff(plan, gen);
        writeReference(gen);
        writeModel(plan, gen);

        gen.writeEndObject();
        gen.flush();

        // System.out.println("TEST ACP File / Json Content:\n" + os.toString() + "\nENDE File\n");

        FileHelper.writeStringToFile(os.toString(), targetJson);

        System.out.println("Generated file: " + targetJson.getAbsolutePath());
    }

    private void writeFlightPlanHash(CFlightplan plan, JsonGenerator gen) throws IOException, JsonGenerationException {
        int hash = plan.hashCode();
        gen.writeNumberField("mission hash", hash);
    }

    private void writeTakeoff(CFlightplan plan, JsonGenerator gen) throws IOException, JsonGenerationException {
        Takeoff takeoff = plan.getTakeoff();

        if (takeoff != null && takeoff.isDefined()) {
            gen.writeFieldName("takeoff");
            gen.writeStartObject();

            gen.writeFieldName("position");
            gen.writeStartObject();

            double alt = plan.getTakeofftAltWgs84WithElevation();
            gen.writeNumberField("height", alt);
            gen.writeNumberField("latitude", takeoff.getLat());
            gen.writeNumberField("longitude", takeoff.getLon());

            gen.writeEndObject(); // takeoff position

            gen.writeNumberField("tolerance radius", DEFAULT_TAKEOFF_TOLERANCE_RADIUS);

            gen.writeEndObject(); // takeoff
        }
    }

    public void writeReference(JsonGenerator gen) throws IOException, JsonGenerationException {
        // "heightReference": "Relative",
        gen.writeStringField("heightReference", "Relative");
    }

    public void writeDescription(CFlightplan plan, JsonGenerator gen) throws IOException, JsonGenerationException {
        String winSeparator = "\r\n"; // cockpit
        StringBuilder warningsBuilder = new StringBuilder();
        List<ResolvableValidationMessage> warnings =
            validationService
                .planningValidationMessagesProperty()
                .stream()
                .filter((message) -> !message.getCategory().equals(ValidationMessageCategory.NOTICE))
                .collect(Collectors.toList());
        Iterator<ResolvableValidationMessage> it = warnings.iterator();
        while (it.hasNext()) {
            ResolvableValidationMessage message = it.next();

            warningsBuilder.append("-" + message.getMessage().replaceAll(Pattern.quote("\u202F"), " "));
            if (it.hasNext()) {
                warningsBuilder.append(", " + winSeparator);
            }
        }

        Date date;
        if (plan.isChanged()) {
            date = new Date(); // now
        } else {
            date = new Date(plan.getFile().lastModified());
        }

        SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.ENGLISH);
        String savedOn = "Saved on " + form.format(date);

        int warningsSize = warnings.size();
        gen.writeStringField(
            "description",
            savedOn
                + (warningsSize > 0
                    ? "," + winSeparator + warningsSize + " Warnings:" + winSeparator + warningsBuilder.toString()
                    : ""));
    }

    private void writeCamera(CFlightplan plan, JsonGenerator gen) throws IOException {

        //      "camera": {
        //        "delay": 100,
        //                "focalLength": 35,
        //                "imageSize": {
        //            "height": 4912,
        //                    "width": 7360
        //        },
        //        "name": "Sony Alpha 7R",
        //                "processTime": 1500,
        //                "sensorSize": {
        //            "height": 24,
        //                    "width": 35.899999999999999
        //        }
        //      },

        final IGenericCameraConfiguration cameraConfig =
            plan.getHardwareConfiguration().getPrimaryPayload(IGenericCameraConfiguration.class);
        final IGenericCameraDescription cameraDesc = cameraConfig.getDescription();
        final ILensDescription lensDescription = cameraConfig.getLens().getDescription();

        gen.writeStringField("@comment", StaticInjector.getInstance(ILicenceManager.class).getExportHeaderCore());
        gen.writeFieldName("camera");
        gen.writeStartObject();

        gen.writeNumberField("delay", cameraDesc.getCameraDelay().convertTo(Unit.MILLISECOND).getValue().doubleValue());
        gen.writeNumberField(
            "focalLength",
            cameraConfig
                .getLens()
                .getDescription()
                .getFocalLength()
                .convertTo(Unit.MILLIMETER)
                .getValue()
                .doubleValue());

        gen.writeFieldName("imageSize");
        gen.writeStartObject();

        gen.writeNumberField("height", cameraDesc.getCcdResY());
        gen.writeNumberField("width", cameraDesc.getCcdResX());

        gen.writeEndObject(); // imageSize

        gen.writeStringField("name", cameraDesc.getName());
        gen.writeNumberField(
            "processTime", lensDescription.getMinRepTime().convertTo(Unit.MILLISECOND).getValue().doubleValue());

        gen.writeFieldName("sensorSize");
        gen.writeStartObject();

        gen.writeNumberField("height", cameraDesc.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue());
        gen.writeNumberField("width", cameraDesc.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue());
        gen.writeEndObject(); // sensorSize

        gen.writeEndObject(); // camera
    }

    public void writeModel(CFlightplan plan, JsonGenerator gen) throws IOException, JsonGenerationException {
        // "model": {
        gen.writeFieldName("model");
        gen.writeStartObject();

        writeTasks(plan, gen);

        gen.writeEndObject(); // end model
    }

    public void writeTasks(CFlightplan plan, JsonGenerator gen) throws IOException, JsonGenerationException {
        /*try {
            ((Flightplan)plan).getTakeoff().updateFromUAV(controller.getPlane());
        } catch (Exception e) {
            Debug.getLog()
                .log(
                    Level.INFO,
                    "Can not send mission with abolute altitude while no start elevation of UAV is known",
                    e);
        }*/

        ACPTaskModel model = new ACPModelExtractor().extractModel(plan, 0, null);
        gen.writeFieldName("tasks");
        gen.writeStartArray();
        ACPTaskModel.ACPTask lastWP = null;

        // JOptionPane.showInputDialog(message)

        //        Object [] options = {"please input fix speed (m/s), 0 for calculated"};
        //        final String inputSpeed = JOptionPane.showInputDialog(null,
        //            options,
        //            0);
        //        Double speed;
        //        try {
        //            speed = new Double(inputSpeed);
        //        } catch (NumberFormatException e) {
        //            Debug.getLog().log(Level.INFO,"Speed invalid, use calculated");
        //            speed = 0.;
        //        }
        for (ACPTaskModel.ACPTask wp : model.getTasks()) {
            /* height not in cm but in m!! */
            // writePoint(gen, wp.roll, wp.pitch, wp.yaw, wp.speed, wp.height, wp.lat, wp.lon,
            // wp.waypointType.equals(WaypointType.MATRIX), wp.isEndOfMatrix);

            writePoint(gen, wp);
            lastWP = wp;
        }

        gen.writeEndArray();
    }

    private void writePoint(PrintStream out, ACPTaskModel.ACPTask wp, boolean heading) throws IOException {
        // if (wp.waypointType.equals(WaypointType.MATRIX) || heading) {
        //    out.print("M;");
        // } else { // default
        out.print("S;");
        // }

        out.print(wp.lon + ";");
        out.print(wp.lat + ";");
        out.print(wp.height + ";");
        // if (wp.waypointType.equals(WaypointType.MATRIX) || heading) {
        //    out.print("follow;");
        // } else { // default
        out.print(";");
        // }

        out.print(wp.pitch + ";"); // if NaN; ?
        out.print(wp.speed + ";"); // if NaN; ?
        out.print("\r\n"); // comment

        /* according to: toAscTecCsvFollow.xsl
            Type;Longitude;Latitude;Height;Heading;Camera pitch;Velocity;Comment
            M;8.642076143721797;49.24823674639258;429.89;follow;NaN;NaN;-65548, following height: 429.89, previous height: NaN, parent height: NaN
            M;8.642076158948985;49.24859270860253;429.89;follow;NaN;NaN;-131085, following height: 429.89, previous height: 429.89, parent height: NaN
            M;8.63783756724366;49.24939604980648;429.89;follow;NaN;NaN;-262149, following height: 429.89, previous height: 429.89, parent height: NaN
            S;8.388382497660935;49.306663761801225;55;;;;2, Landingpoint, previous height: NaN
        */

    }

    public void writePoint(JsonGenerator gen, ACPTaskModel.ACPTask wp) throws IOException, JsonGenerationException {
        gen.writeStartObject();

        gen.writeStringField("action", wp.triggerImage ? "Trigger" : "None");
        gen.writeNumberField("actionDelay", Math.round(wp.waitTimeEvent1));
        gen.writeNumberField("postActionDelay", Math.round(wp.waitTimeEvent2));
        gen.writeStringField("class", "SingleWaypoint");

        gen.writeArrayFieldStart("flags");
        // if (wp.waypointType.equals(WaypointType.MATRIX) && wp.isEndOfMatrix) {
        //    gen.writeString("EndOfMatrix");
        // }
        // all other flags will be generated by tablet SW based on the waypoint type
        gen.writeEndArray();

        gen.writeNumberField("maxSpeed", wp.speed);
        gen.writeNumberField("desiredAcceleration", wp.desiredAcceleration);

        gen.writeObjectFieldStart("orientation");
        gen.writeNumberField("pitch", wp.pitch);
        gen.writeNumberField("roll", wp.roll);
        gen.writeNumberField("yaw", wp.yaw);
        gen.writeEndObject();

        gen.writeObjectFieldStart("position");
        gen.writeNumberField("height", wp.height);
        gen.writeNumberField("latitude", wp.lat);
        gen.writeNumberField("longitude", wp.lon);
        gen.writeEndObject();

        // if (wp.waypointType.equals(WaypointType.MATRIX)) {
        //    gen.writeStringField("type", "Matrix");
        // } else { // default
        gen.writeStringField("type", "Single");
        // }

        gen.writeEndObject();
    }

    public void setProgressMonitor(IMProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
        // latch = new CountDownLatch(1);

    }
}
