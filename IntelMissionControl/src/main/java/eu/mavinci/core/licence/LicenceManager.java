/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.core.update.EnumUpdateTargets;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MACaddrSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class LicenceManager implements ILicenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenceManager.class);

    private final ObjectProperty<Licence> activeLicence = new SimpleObjectProperty<>();
    private final ObjectProperty<OperationLevel> maxOperationLevel = new SimpleObjectProperty<>();
    private final ObjectProperty<AllowedUser> allowedUser = new SimpleObjectProperty<>();
    private final BooleanProperty isDJIEdition = new SimpleBooleanProperty();
    private final BooleanProperty isFalconEdition = new SimpleBooleanProperty();
    private final BooleanProperty isGrayHawkEdition = new SimpleBooleanProperty();

    private final IPathProvider pathProvider;
    private final IVersionProvider versionProvider;

    @Inject
    public LicenceManager(IPathProvider pathProvider, IVersionProvider versionProvider) {
        this.pathProvider = pathProvider;
        this.versionProvider = versionProvider;
        activeLicence.addListener((observable, oldValue, newValue) -> validateLicence());
        try {
            reloadLicence();
        } catch (Exception e) {
            // logger may not be loaded at this point in time
            LOGGER.warn("cant load licence", e);
            Licence licence = new Licence();
            licence.release = "123456789";
            licence.setLicenceId("DEBUG_REPLACEMENT");
            licence.isBuildInLicence = true;
            this.activeLicence.set(licence);
            LOGGER.info("License Version: " + licence.getHumanReadableVersion());
        }

        LOGGER.info("Software Version: " + getExportHeaderCore());
    }

    private void validateLicence() {
        Licence licence = activeLicence.get();
        maxOperationLevel.set(
            licence == null || licence.getMaxOperationLevel() == null
                ? OperationLevel.USER
                : licence.getMaxOperationLevel());
        Set<String> localMacs = MACaddrSource.getMACs();
        LOGGER.debug("localMAC-Addresses:" + localMacs);

        AllowedUser user = licence.detectMatchingUser(localMacs);
        allowedUser.set(user);
        isDJIEdition.set(licence.isDJIEdition());
        isFalconEdition.set(licence.isFalconEdition());
        isGrayHawkEdition.set(licence.isGrayHawkEdition());
    }

    @Override
    public void registerLicence(File file) throws Exception {
        Licence lic = new Licence(file);
        String tmp = lic.getLicenceId();
        String myID = lic.getLicenceId();
        tmp += "\n";
        tmp += file.getName();
        File target = new File(createTargetDir(EnumUpdateTargets.LICENCE, myID), file.getName());
        if (!target.equals(file)) {
            FileHelper.copyFile(file, target, false);
        }

        File licenceSettingsFile = pathProvider.getLicenseSettingsFile().toFile();
        File tmpFile = new File(licenceSettingsFile.getAbsolutePath() + "~");
        FileHelper.writeStringToFile(tmp, tmpFile);
        licenceSettingsFile.delete();
        tmpFile.renameTo(licenceSettingsFile);
        activeLicence.set(lic);
        LOGGER.info("License Version: " + lic.getHumanReadableVersion());
    }

    @Override
    public ReadOnlyObjectProperty<Licence> activeLicenceProperty() {
        return activeLicence;
    }

    @Override
    public Licence getActiveLicence() {
        return activeLicence.get();
    }

    @Override
    public ReadOnlyObjectProperty<OperationLevel> maxOperationLevelProperty() {
        return maxOperationLevel;
    }

    @Override
    public OperationLevel getMaxOperationLevel() {
        return maxOperationLevel.get();
    }

    @Override
    public ReadOnlyObjectProperty<AllowedUser> allowedUserProperty() {
        return allowedUser;
    }

    @Override
    public AllowedUser getAllowedUser() {
        return allowedUser.get();
    }

    private void initLicence(File licenceFile)
            throws IOException, GeneralSecurityException, SAXException, ParserConfigurationException {
        activeLicence.set(new Licence(licenceFile));
        LOGGER.info("License Version: " + activeLicence.get().getHumanReadableVersion());
    }

    private void initDefaultLicence()
            throws IOException, GeneralSecurityException, SAXException, ParserConfigurationException {
        try (InputStream cLoader = ClassLoader.getSystemResourceAsStream("eu/mavinci/default.mlf")) {
            Licence licence = new Licence(Licence.getResourcesAsBytes(cLoader));
            licence.isBuildInLicence = true;
            activeLicence.set(licence);
            LOGGER.info("License Version: " + licence.getHumanReadableVersion());
        }
    }

    @Override
    public void resetToDefaultLicence() {
        pathProvider.getLicenseSettingsFile().toFile().delete();
        reloadLicence();
    }

    private void reloadLicence() {
        reloadLicence(pathProvider.getLicenseSettingsFile().toFile());
    }

    private File createTargetDir(EnumUpdateTargets updateKind) {
        File f = new File(pathProvider.getUpdatesDirectory().toFile().getAbsolutePath(), updateKind.toString());
        f.mkdirs();
        return f;
    }

    private File createTargetDir(EnumUpdateTargets updateKind, String ownID) {
        File f = createTargetDir(updateKind);
        if (ownID != null) {
            f = new File(f.getAbsolutePath(), FileHelper.urlToFileName(ownID));
        }

        f.mkdirs();
        return f;
    }

    private void reloadLicence(File licenseSettingsFile) {
        try {
            File licenceFile = null;
            String myID = null;
            if (licenseSettingsFile == null || !licenseSettingsFile.exists()) {
                initDefaultLicence();
            } else {
                try {
                    String tmp = FileHelper.readFileAsString(licenseSettingsFile).trim();
                    int pos = tmp.indexOf("\n");
                    if (pos < 0) {
                        initDefaultLicence();
                    } else {
                        myID = tmp.substring(0, pos);
                        licenceFile =
                            new File(createTargetDir(EnumUpdateTargets.LICENCE, myID), tmp.substring(pos + 1));
                        initLicence(licenceFile);
                    }
                } catch (Exception e) {
                    LOGGER.warn(
                        "problems loading licence file "
                            + licenseSettingsFile
                            + "  file:"
                            + licenceFile
                            + "  id:"
                            + myID,
                        e);
                    initDefaultLicence();
                }
            }

        } catch (Exception e) {
            isLicenceOK = false;
            LOGGER.warn("problems loading licence file " + licenseSettingsFile, e);
        }
    }

    private boolean isLicenceOK = false;

    private boolean isDemoGUI = false;

    private boolean checkExpiring = false;

    public boolean isLicenceOK() {
        return isLicenceOK;
    }

    public boolean isMacOK() {
        Licence licence = activeLicence.get();
        if (licence == null) {
            return false;
        }

        return licence.isMacOK();
    }

    public void checkExpiring() {
        Licence licence = activeLicence.get();
        if (licence == null || licence.isDemoGui() || !licence.isMacOK()) {
            if (licence == null || licence.isDemoGui()) {
                isDemoGUI = true;
            }

            checkExpiring = true;

            // TODO CHECK IF EXPIRED
        }
    }

    @Override
    public String getExportHeader() {
        return "# " + getExportHeaderCore() + "\r\n" + "# Encoding:" + ProtocolTokens.encoding + "\r\n";
    }

    @Override
    public String getExportHeaderCore() {
        Date date = new Date();
        SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.ENGLISH);
        date.setTime(Calendar.getInstance().getTimeInMillis());
        Licence licence = activeLicence.get();
        AllowedUser user = allowedUser.get();
        String header =
            "created with "
                + versionProvider.getApplicationName()
                + " for "
                + versionProvider.getSystem().name()
                + " (SN: "
                + (licence == null ? null : licence.getLicenceId())
                + ", "
                + (user == null ? null : user.name)
                + ", "
                + (licence == null ? null : licence.company)
                + ", "
                + (user == null ? null : user.email)
                + ") "
                + versionProvider.getHumanReadableVersion()
                + " at "
                + form.format(date);
        return header.replaceAll("[^\\x00-\\x7F]", "_");
    }

    @Override
    public boolean isCompatibleService(String licenceReleaseVersion) {
        LOGGER.debug("licence release: " + licenceReleaseVersion);
        LOGGER.debug("gui release: " + versionProvider.getAppMajorVersion());
        Licence licence = activeLicence.get();
        if (licence == null) {
            return false;
        }

        LOGGER.debug("licence servicePayedUntil: " + licence.getServicePayedUntil());
        LOGGER.debug("licence branch: " + licence.getBranch());
        LOGGER.debug("betatester: " + licence.isBetaTesting());

        if (licence.isInternalGui()) {
            return true;
        }

        if (licenceReleaseVersion.equals(versionProvider.getAppMajorVersion())) {
            return true;
        }

        try {
            if (Double.parseDouble(licenceReleaseVersion) >= Double.parseDouble(versionProvider.getAppMajorVersion())) {
                return true;
            }
        } catch (NumberFormatException e) {
            if (Double.parseDouble(licenceReleaseVersion.replaceFirst("\\.", ""))
                    >= Double.parseDouble(versionProvider.getAppMajorVersion().replaceFirst("\\.", ""))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BooleanProperty isDJIEditionProperty() {
        return isDJIEdition;
    }

    @Override
    public BooleanProperty isFalconEditionProperty() {
        return isFalconEdition;
    }

    @Override
    public BooleanProperty isGrayHawkEditionProperty() {
        return isGrayHawkEdition;
    }
}
