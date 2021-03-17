/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightPlanTemplateService implements IFlightPlanTemplateService {

    private static Logger LOGGER = LoggerFactory.getLogger(FlightPlanTemplateService.class);

    private static final String FML = ".fml";
    private static final String CLONE_NAME_SUFFIX = "_copy";

    private static final String PREDEFINED_FLIGHT_TEMPLATES_LOCATION = "com/intel/missioncontrol/templates";

    @Inject
    private MavinciObjectFactory mavinciObjectFactory;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private IPathProvider pathProvider;

    private List<FlightPlanTemplate> loadedFlightPlanTemplates;
    private Set<Path> systemFlightPlanTemplatePaths;

    private Set<String> systemTemplateNames;

    private synchronized Set<String> getSystemTemplatesNames() {
        if (systemTemplateNames != null) {
            return systemTemplateNames;
        }

        try {
            systemTemplateNames =
                getSystemFlightPlanTemplatePaths()
                    .stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            systemTemplateNames = Collections.emptySet();
            LOGGER.error("Failed to load system template names", ex);
        }

        return systemTemplateNames;
    }

    private Flightplan createLegacyFlightPlan(File file) throws InvalidFlightPlanFileException {
        return mavinciObjectFactory.flightPlanFromTemplate(file);
    }

    @Override
    public synchronized List<FlightPlanTemplate> getFlightPlanTemplates() {
        if (loadedFlightPlanTemplates != null) {
            return loadedFlightPlanTemplates;
        }

        loadedFlightPlanTemplates = new ArrayList<>();
        Set<String> systemTemplatesNames = getSystemTemplatesNames();

        for (File flightTemplate : getFlightPlanFiles()) {
            Flightplan legacyFlightplan = null;
            try {
                legacyFlightplan = createLegacyFlightPlan(flightTemplate);
            } catch (InvalidFlightPlanFileException e) {
                e.printStackTrace();
                Debug.getLog().severe("Cannot open a template file " + flightTemplate + " : " + e.getCause());
                applicationContext.addToast(
                    Toast.of(ToastType.INFO)
                        .setText("Cannot open a template file " + flightTemplate)
                        .setTimeout(Duration.seconds(60))
                        .create());
                try {
                    FileUtils.forceDelete(flightTemplate);
                } catch (IOException e1) {
                    Debug.getLog().severe("Cannot delete a template file " + flightTemplate);
                }

                continue;
            }

            FlightPlanTemplate template =
                new FlightPlanTemplate(legacyFlightplan.getName(), new FlightPlan(legacyFlightplan, true));
            template.setSystem(systemTemplatesNames.contains(legacyFlightplan.getFile().getName()));
            loadedFlightPlanTemplates.add(template);
        }

        // templates are only now completely loaded, so we now have to update there cross dependencies..
        loadedFlightPlanTemplates.forEach(template -> template.getFlightPlan().updateBaseTemplate());

        return loadedFlightPlanTemplates;
    }

    @Override
    public void saveTemplate(FlightPlanTemplate flightPlanTemplate) {
        persistTemplate(flightPlanTemplate);
    }

    private void persistTemplate(FlightPlanTemplate template) {
        Flightplan legacyFlightplan = template.getFlightPlan().getLegacyFlightplan();
        legacyFlightplan.save(pathProvider.getTemplatesDirectory().toFile());
    }

    private FlightPlanTemplate persistTemplateAs(FlightPlanTemplate source, String templateName, File file)
            throws IOException, InvalidFlightPlanFileException {
        Flightplan legacyFlightPlan = createLegacyFlightPlan(source.getFlightPlan().getLegacyFlightplan().getFile());

        legacyFlightPlan.setFile(file);
        legacyFlightPlan.setName(templateName);
        FlightPlan flightPlan = new FlightPlan(legacyFlightPlan, true);
        flightPlan.rename(templateName);
        FlightPlanTemplate template = new FlightPlanTemplate(templateName, flightPlan);
        template.setSystem(false);
        persistTemplate(template);
        return template;
    }

    private String getUniqueName(final String nameBase, final Collection<String> availableNames) {
        boolean unique;
        int nameIndex = -1;
        String nameCandidate;
        do {
            nameIndex++;
            nameCandidate = nameBase.concat(nameIndex == 0 ? "" : String.valueOf(nameIndex));
            unique = availableNames.stream().anyMatch(nameCandidate::equalsIgnoreCase);
        } while (unique);
        return nameCandidate;
    }

    @Override
    public File generateTemplateFile(File candidate) {
        String nameBase = candidate == null ? "template" : FilenameUtils.removeExtension(candidate.getName());
        String targetFileName =
            getUniqueName(
                nameBase.toLowerCase(),
                Arrays.stream(getFlightPlanFiles())
                    .map(File::getName)
                    .map(String::toLowerCase)
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toList()));
        return new File(pathProvider.getTemplatesDirectory().toFile(), targetFileName.concat(FML));
    }

    @Override
    public synchronized String generateTemplateName(String nameBase) {
        return getUniqueName(
            nameBase, getFlightPlanTemplates().stream().map(FlightPlanTemplate::getName).collect(Collectors.toList()));
    }

    @Override
    public synchronized FlightPlanTemplate duplicate(FlightPlanTemplate source) throws IOException {
        if (source == null) {
            return null;
        }

        String targetName = source.getName();
        targetName =
            targetName.endsWith(FML) ? targetName.substring(0, targetName.length() - FML.length()) : targetName;
        if (!targetName.endsWith(CLONE_NAME_SUFFIX)) {
            targetName = targetName.concat(CLONE_NAME_SUFFIX);
        }

        targetName = generateTemplateName(targetName);
        File targetFile =
            generateTemplateFile(new File(pathProvider.getTemplatesDirectory().toFile(), targetName.concat(FML)));
        FlightPlanTemplate result = null;
        try {
            result = persistTemplateAs(source, targetName, targetFile);
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe(
                    "Cannot create a duplicate template from a file "
                        + source
                        + ", template will not be updated : "
                        + e.getCause());
            return null;
        }

        getFlightPlanTemplates().add(result);
        return result;
    }

    @Override
    public synchronized FlightPlanTemplate importFrom(File file) throws IOException {
        if (file == null || !(file.exists() && file.canRead())) {
            return null;
        }

        File fileImported;
        if (file.getParentFile().equals(pathProvider.getTemplatesDirectory().toFile())) {
            fileImported = file;
        } else {
            fileImported = importTemplateFile(Paths.get(file.toURI()), StandardCopyOption.COPY_ATTRIBUTES);
        }

        Flightplan legacyFP = null;
        try {
            legacyFP = createLegacyFlightPlan(fileImported);
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe(
                    "Cannot create a flightplan from a file "
                        + file
                        + ", template will not be updated : "
                        + e.getCause());
            return null;
        }

        legacyFP.setBasedOnTemplate(null);
        String templateName = legacyFP.getName();
        templateName =
            templateName.endsWith(FML) ? templateName.substring(0, templateName.length() - FML.length()) : templateName;
        legacyFP.save(pathProvider.getTemplatesDirectory().toFile());
        FlightPlan flightPlan = new FlightPlan(legacyFP, true);
        FlightPlanTemplate template = new FlightPlanTemplate(templateName, flightPlan);

        getFlightPlanTemplates().add(template);

        return template;
    }

    @Override
    public void updateTemplateWith(FlightPlanTemplate template, File file) {
        if (file == null || template == null) {
            return;
        }

        File templateFile = template.getFlightPlan().getLegacyFlightplan().getFile();
        Flightplan legacyFP = null;
        try {
            legacyFP = createLegacyFlightPlan(file);
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe(
                    "Cannot create a flightplan from a file "
                        + file
                        + ", template will not be updated : "
                        + e.getCause());
            return;
        }

        legacyFP.setBasedOnTemplate(null);
        legacyFP.setFile(templateFile);
        legacyFP.setName(template.getName());
        legacyFP.save(pathProvider.getTemplatesDirectory().toFile());
        template.reload(this::createLegacyFlightPlan);
    }

    @Override
    public void exportTo(FlightPlanTemplate template, File destination) throws IOException {
        if (template == null || destination == null) {
            return;
        }

        File templateFile = template.getFlightPlan().getLegacyFlightplan().getFile();
        File fileExportTo;
        if (destination.isDirectory()) {
            fileExportTo = new File(destination, templateFile.getName());
        } else {
            fileExportTo = destination;
        }

        FileUtils.copyFile(templateFile, fileExportTo);
    }

    @Override
    public synchronized boolean delete(FlightPlanTemplate template) throws IOException {
        if (template == null) {
            return false;
        }

        File fileTobeDeleted = template.getFlightPlan().getLegacyFlightplan().getFile();
        try {
            FileUtils.forceDelete(fileTobeDeleted);
        } catch (IOException ex) {
            FileUtils.forceDeleteOnExit(fileTobeDeleted);
        }

        return getFlightPlanTemplates().remove(template);
    }

    @Override
    public synchronized FlightPlanTemplate findByName(String templateName) {
        if (StringUtils.isEmpty(templateName)) {
            return null;
        }

        List<FlightPlanTemplate> list = getFlightPlanTemplates();
        if (list == null) {
            return null;
        }

        return list.stream().filter(fp -> templateName.equals(fp.getName())).findFirst().orElse(null);
    }

    @Override
    public synchronized FlightPlanTemplate saveAs(FlightPlanTemplate template, File targetFile)
            throws IOException, InvalidFlightPlanFileException {
        if (template == null || targetFile == null) {
            return null;
        }

        File targetDir = targetFile.getParentFile();
        if (!pathProvider.getTemplatesDirectory().toFile().equals(targetDir)) {
            exportTo(template, targetFile);
            return null;
        }

        String templateFileName = targetFile.getName();
        String templateName;
        if (templateFileName.endsWith(FML)) {
            templateName = templateFileName.substring(0, templateFileName.length() - FML.length());
        } else {
            templateName = templateFileName;
            templateFileName = templateFileName.concat(FML);
        }

        templateName = generateTemplateName(templateName);

        FlightPlanTemplate result =
            persistTemplateAs(
                template, templateName, new File(pathProvider.getTemplatesDirectory().toFile(), templateFileName));
        getFlightPlanTemplates().add(result);

        return result;
    }

    private String proposeTheFileNameFor(FlightPlanTemplate template, String anotherTemplateName) {
        Flightplan legacyFP = template.getFlightPlan().getLegacyFlightplan();
        AirplaneType airplaneType = legacyFP.getHardwareConfiguration().getPlatformDescription().getAirplaneType();
        IGenericCameraConfiguration cameraConfig =
            legacyFP.getHardwareConfiguration().getPrimaryPayload(IGenericCameraConfiguration.class);
        ILensDescription lensDescription = cameraConfig.getLens().getDescription();
        return String.format(
            "%s-%s-%s_%s",
            airplaneType.name(),
            lensDescription.getName(),
            cameraConfig.getDescription().getName(),
            StringUtils.isEmpty(anotherTemplateName) ? template.getName() : anotherTemplateName);
    }

    @Override
    public synchronized void rename(FlightPlanTemplate template, String newName) throws IOException {
        if (template == null || StringUtils.isEmpty(newName)) {
            return;
        }

        FlightPlan flightPlan = template.getFlightPlan();
        Flightplan legacyFP = flightPlan.getLegacyFlightplan();
        File fpFileOriginal = legacyFP.getFile();
        String targetName =
            getUniqueName(
                newName,
                getFlightPlanTemplates()
                    .stream()
                    .map(FlightPlanTemplate::getName)
                    .filter(newName::equals)
                    .collect(Collectors.toList()));
        flightPlan.nameProperty().set(targetName);
        String targetFileName =
            getUniqueName(
                proposeTheFileNameFor(template, newName),
                getFlightPlanTemplates()
                    .stream()
                    .map(FlightPlanTemplate::getFlightPlan)
                    .map(FlightPlan::getLegacyFlightplan)
                    .map(Flightplan::getFile)
                    .map(File::getName)
                    .filter(name -> fpFileOriginal.getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList()));
        File newFpFile = new File(pathProvider.getTemplatesDirectory().toFile(), targetFileName.concat(FML));
        legacyFP.setName(targetName);
        legacyFP.setFile(newFpFile);
        legacyFP.save(pathProvider.getTemplatesDirectory().toFile());
        try {
            FileUtils.forceDelete(fpFileOriginal);
        } catch (IOException ex) {
            try {
                FileUtils.forceDeleteOnExit(fpFileOriginal);
            } catch (IOException ignored) {
            }
        }

        template.reload(this::createLegacyFlightPlan);
        template.nameProperty().set(targetName);
    }

    @Override
    public boolean revert(FlightPlanTemplate template) throws IOException, URISyntaxException {
        boolean result = false;
        if (template != null) {
            getSystemFlightPlanTemplatePaths()
                .stream()
                .filter(
                    path -> {
                        String t1Name = path.getFileName().toString();
                        String t2Name = template.getFlightPlan().getLegacyFlightplan().getFile().getName();
                        return t1Name.equalsIgnoreCase(t2Name);
                    })
                .findFirst()
                .ifPresent(
                    path -> {
                        restoreSystemTemplateFile(path);
                        template.reload(this::createLegacyFlightPlan);
                    });
            result = true;
        }

        return result;
    }

    private File[] getFlightPlanFiles() throws IllegalStateException {
        File templatesFolder = pathProvider.getTemplatesDirectory().toFile();
        if (listTemplates(templatesFolder).length == 0) {
            bootstrapFlightPlanTemplates(templatesFolder);
        }

        File[] templateFiles = listTemplates(templatesFolder);
        Arrays.sort(templateFiles, Comparator.comparing(File::getName));
        return templateFiles;
    }

    private File[] listTemplates(File templatesFolder) throws IllegalStateException {
        File[] listOfFiles =
            templatesFolder.listFiles(
                (dir, name) -> {
                    return name.endsWith(".fml");
                });
        return listOfFiles == null ? new File[0] : listOfFiles;
    }

    private Set<Path> getSystemFlightPlanTemplatePaths() throws URISyntaxException, IOException {
        if (systemFlightPlanTemplatePaths == null) {
            URL resource = FlightPlanTemplate.class.getClassLoader().getResource(PREDEFINED_FLIGHT_TEMPLATES_LOCATION);
            if (resource == null) {
                throw new IllegalStateException("Unable to locate predefined flight plan templates");
            }

            Stream<Path> templatePaths;
            try {
                templatePaths = loadSystemFlightPlanTemplatePaths(resource);
            } catch (FileSystemNotFoundException ex) {
                addJarFs(resource);
                templatePaths = loadSystemFlightPlanTemplatePaths(resource);
            }

            systemFlightPlanTemplatePaths = templatePaths.collect(Collectors.toSet());
        }

        return systemFlightPlanTemplatePaths;
    }

    private Stream<Path> loadSystemFlightPlanTemplatePaths(URL resource) throws URISyntaxException, IOException {
        Path templatesRootPath = Paths.get(resource.toURI());
        return Files.list(templatesRootPath);
    }

    private File importTemplateFile(Path sourcePath, StandardCopyOption... copyOptions) throws IOException {
        File templatesFolder = pathProvider.getTemplatesDirectory().toFile();
        String templateFileName = sourcePath.getFileName().toString();
        return Files.copy(sourcePath, Paths.get(templatesFolder.toURI()).resolve(templateFileName), copyOptions)
            .toFile();
    }

    private void restoreSystemTemplateFile(Path templatePath) {
        try {
            importTemplateFile(templatePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to extract flight plan template file", e);
        }
    }

    private void bootstrapFlightPlanTemplates(File templatesFolder) {
        try {
            getSystemFlightPlanTemplatePaths().forEach(this::restoreSystemTemplateFile);
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unable to get flight plan template files", e);
        }
    }

    private void addJarFs(URL resource) throws IOException, URISyntaxException {
        try {
            FileSystems.newFileSystem(resource.toURI(), new HashMap<>());
        } catch (Exception e) {
            // ignore mounting problems
        }
    }
}
