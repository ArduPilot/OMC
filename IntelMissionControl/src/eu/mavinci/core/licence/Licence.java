/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.crypto.CryptoHelper;
import eu.mavinci.core.crypto.CryptoHelper.ZipData;
import eu.mavinci.core.helper.MProperties;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.core.xml.MEntryResolver;
import eu.mavinci.core.xml.XMLWriter;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Licence extends MProperties {

    static enum GuiLevels {
        USER,
        EXPERT,
        CONFIGURATION,
        TRANSLATOR,
        DEBUG
    }

    public static final String toLicenceRequestEmails = "Emea.gs.licencerequest@intel.com";

    public static final String KEY_OPTION_ALLOWCITYMODEL = "ALLOWCITYMODEL";
    public static final String KEY_OPTION_ALLOW27MM = "ALLOW27MM";

    public static final String KEY_FALCONEDITION = "FALCONEDITION";
    public static final String KEY_DJIEDITION = "DJIEDITION";

    // make map case independent
    @Override
    public synchronized boolean containsKey(Object key) {
        return super.containsKey(key.toString().toUpperCase());
    }

    public synchronized boolean containsKey(String key) {
        return super.containsKey(key.toUpperCase());
    }

    @Override
    public synchronized Object get(Object key) {
        return super.get(key.toString().toUpperCase());
    }

    @Override
    public String getProperty(String key) {
        return super.getProperty(key.toUpperCase());
    }

    @Override
    public synchronized String getProperty(String key, String defaultValue) {
        return super.getProperty(key.toUpperCase(), defaultValue);
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return super.setProperty(key.toUpperCase(), value);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return super.put(key.toString().toUpperCase(), value);
    }

    private static final long serialVersionUID = -5951519165998001908L;

    int type;
    String resellerID;
    int[] numbers;
    OsTypes[] platform;
    GuiLevels maxUserlevel;
    String licenceId;
    String branch;
    Locale language;
    String company;
    Date initDate;
    Date expireingDate;
    Date servicePayedUntil;

    String defaultCam;
    String SEEN_1;
    String SEEN_0;
    boolean isBetaTesting;
    boolean isMannedEdition;
    boolean isFixwingUAVedition;
    // TODO: temporary to support building AOI
    boolean isCopterUAVedition = true;
    private boolean isFalconEdition;
    private boolean isDJIEdition;

    public boolean isCopterUAVedition() {
        // return true;
        return isCopterUAVedition;
    }

    public void setIsCopterUAVedition(boolean value) {
        this.isCopterUAVedition = value;
    }

    public boolean isFixwingUAVedition() {
        // return true;
        return isFixwingUAVedition;
    }

    public void setIsFixwingUAVedition(boolean value) {
        this.isFixwingUAVedition = value;
    }

    public boolean isMannedEdition() {
        // return true;
        return isMannedEdition;
    }

    public void setIsMannedEdition(boolean value) {
        this.isMannedEdition = value;
    }

    public boolean isCityMappingOptionEnabled() {
        if (isDemoGui()) {
            return true;
        }

        return containsKey(KEY_OPTION_ALLOWCITYMODEL);
    }

    public boolean is27mmOptionEnabled() {
        if (isDemoGui()) {
            return true;
        }

        return containsKey(KEY_OPTION_ALLOW27MM);
    }

    public boolean isDemoGui() {
        // System.out.println("isDemo:" + (expireingDate!=null));
        return expireingDate != null;
    }

    ArrayList<AllowedUser> users = new ArrayList<AllowedUser>();

    private static byte[] decrypt(byte[] data) throws IOException, GeneralSecurityException {
        String pubKeyDerHexString =
            "308201b63082012b06072a8648ce3804013082011e02818100d215a9cf90be377e67f602721dd66ef63a9ebdaa9647cfb8eae4b43846d042b81d1b49c2c0046565c278abe3bf542905dfc3e8d0d3edb69e107c2cb551a250bec9e0ce58c99c1197d61d0d4167bfe34d01b9541db037624e01c333a512ca091468f4cfd85dd08b1218077bcda365a890d27a761112ea56cb4392fc525436ba61021500e6fa60e1b2254371aa4e09b40eca5d0908d3059502818031e46ea936cb0c1e66f95c25a3c27d4e10b9eef0138926394338e9ecf604ea6565bde1a1f024f68b6669599a4ae96158e3c9aebbe2e6c8e5b6da599760dff47d38fd6c9a574dce94edb83a58793f510fafaa6bb41c364e8b5a8ae9567e6dcbb32553ce4827e0de8cd5b8ae4b7224256d044f5f424aa0f9d9d75ce488b93fbda60381840002818000f1e09c7bbf8283c7dc473e94ee7dd69ca32527eaf98f3a94c1227d7ddcc03665dc0b413c862cde0c5e147adbcdbdaaa618cfaec194918265e517399aa99822bed45b855450b2d9df626357b996a632211ea9d2ac18ce842dfd1a53afca367aaf0b90fdd77ae02564c4d14ce7eaabbe041482a040dd6f9f525e456b56cbf182";

        // Encryption key (16 chars for AES-128) + initialization vector (16 chars):
        String cryptKey = "FSGMygOw/13Sfvvx";
        String iv = "fz7.gw4e77bq13es";

        CryptoHelper cryptoHelper = new CryptoHelper(pubKeyDerHexString, cryptKey, iv);

        return cryptoHelper.decryptDataAndCheckSignature(data);
    }

    static byte[] getResourcesAsBytes(InputStream is) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public int getType() {
        return type;
    }

    public boolean isBetaTesting() {
        return isBetaTesting;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getResellerID() {
        return resellerID;
    }

    public void setResellerID(String resellerID) {
        this.resellerID = resellerID;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumber(int[] numbers) {
        this.numbers = numbers;
    }

    public OsTypes[] getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = new OsTypes[] {OsTypes.valueOf(platform)};
    }

    public void setPlatform(OsTypes platform[]) {
        this.platform = platform;
    }

    public OperationLevel getMaxOperationLevel() {
        switch (maxUserlevel) {
        case USER:
        case EXPERT:
            return OperationLevel.USER;
        case CONFIGURATION:
        case TRANSLATOR:
            return OperationLevel.TECHNICIAN;
        case DEBUG:
            return OperationLevel.DEBUG;

        default:
            return OperationLevel.USER;
        }
    }

    public void setMaxUserlevel(String maxUserlevel) {
        this.maxUserlevel = GuiLevels.valueOf(maxUserlevel);
    }

    public void setMaxUserlevel(GuiLevels maxUserlevel) {
        this.maxUserlevel = maxUserlevel;
    }

    public String getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(String licenceId) {
        this.licenceId = licenceId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = new Locale(language);
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDefaultCam() {
        return defaultCam;
    }

    public void setDefaultCam(String defaultCam) {
        this.defaultCam = defaultCam;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(String initDate) throws ParseException {
        this.initDate = BmlXMLparser.df.parse(initDate);
    }

    public Date getExpireingDate() {
        return expireingDate;
    }

    public void setExpireingDate(String expireingDate) throws ParseException {
        this.expireingDate = BmlXMLparser.df.parse(expireingDate);
    }

    public Date getServicePayedUntil() {
        return servicePayedUntil;
    }

    public void setServicePayedUntil(String servicePayedUntil) throws ParseException {
        this.servicePayedUntil = BmlXMLparser.df.parse(servicePayedUntil);
    }

    public String getSEEN_1() {
        return SEEN_1;
    }

    public void setSEEN_1(String SEEN_1) {
        this.SEEN_1 = SEEN_1;
    }

    public String getSEEN_0() {
        return SEEN_0;
    }

    public void setSEEN_0(String SEEN_0) {
        this.SEEN_0 = SEEN_0;
    }

    public String[] getEditionList() {
        if (this.isInternalGui() || getMaxOperationLevel().compareTo(OperationLevel.DEBUG) >= 0) {
            return new String[] {""};
        } else {
            if (isFalconEdition() && isDJIEdition()) {
                return new String[] {"DJI", "Falcon 8+"};
            } else if (isDJIEdition()) {
                return new String[] {"DJI"};
            } else {
                return new String[] {"Falcon 8+"};
            }
        }
    }

    public void setIsFalconEdition(boolean isFalconEdition) {
        this.isFalconEdition = isFalconEdition;
    }

    public boolean isFalconEdition() {
        if (!isFalconEdition) {
            return !isDJIEdition();
        }

        return isFalconEdition;
    }

    public void setIsDJIEdition(boolean isDJIEdition) {
        this.isDJIEdition = isDJIEdition;
    }

    public boolean isDJIEdition() {
        return isDJIEdition;
    }

    public void setUsers(ArrayList<AllowedUser> users) {

        // modification required
        this.users = users;
    }

    public ArrayList<AllowedUser> getUsers() {
        // modification required
        return users;
    }

    // public ArrayList<AllowedUser> cloneUsers() {
    // // modification required
    // return (ArrayList<AllowedUser>) super.getUsers.clone();
    // }

    Date licenceIssuingDate;
    int svnRevision;
    String release;

    public Date getIssuingDate() {
        return licenceIssuingDate;
    }

    public int getSVNrevision() {
        return svnRevision;
    }

    public String getRelease() {
        return release;
    }

    Vector<ZipData> zipData;

    public Vector<ZipData> getZipFiles() {
        return zipData;
    }

    public byte[] getZipFile(String path) {
        if (zipData == null) {
            return null;
        }

        for (ZipData zd : zipData) {
            if (zd.name.equals(path)) {
                return zd.data;
            }
        }

        return null;
    }

    public boolean willExpire() {
        boolean defwillExpire = false;
        // Date expiringDate = getExpireingDate();
        if (getExpireingDate() == null) {
            return false;
        }

        defwillExpire = willExpire(getExpireingDate());
        if (defwillExpire) {
            Debug.getLog().fine("DemoVersion will expire on " + getExpireingDate());
        }

        return defwillExpire;
    }

    private boolean willExpire(Date expiringDate) {
        int days = 7;
        boolean defwillExpire = false;
        Date now = new Date();
        Date dateAfter7Days = new Date(now.getTime() + (1000 * 60 * 60 * 24 * days));
        // Date expiringDate = getExpireingDate();
        if (expiringDate != null) {
            if (dateAfter7Days.after(new Date(expiringDate.getTime() + 24 * 60 * 60 * 1000L))) {
                defwillExpire = true;
            }
        }

        return defwillExpire;
    }

    AllowedUser allowedUser;

    public boolean isMacOK() {
        if (allowedUser == null) {
            return false;
        }

        return true;
    }

    public AllowedUser getAllowedUser() {
        return allowedUser;
    }

    public AllowedUser detectMatchingUser(Set<String> localMacs) {
        // allowedUser=users.get(0);
        // return allowedUser;
        for (ListIterator<AllowedUser> iterator = users.listIterator(users.size()); iterator.hasPrevious(); ) {
            final AllowedUser user = iterator.previous();
            if (user.match(localMacs)) {
                allowedUser = user;
                Debug.getLog().fine("matching user: " + allowedUser);
                return allowedUser;
            }
        }

        if (containsKey("UNIVERSALBUILD")) {
            allowedUser = users.get(0);
            Debug.getLog().fine("universal user is matching. keep this as alias" + allowedUser);
            return allowedUser;
        }

        return null;
    }

    boolean isBuildInLicence;

    public boolean isBuildInLicence() {
        return isBuildInLicence;
    }

    public Licence() {
        svnRevision = 100000;
        licenceIssuingDate = new Date(1592982108 );
        release = "1.6.1";
        isDJIEdition = true;
        isFalconEdition = true;
        isFixwingUAVedition = true;
        setMaxUserlevel(GuiLevels.DEBUG);
    }

    @SuppressWarnings("deprecation")
    public Licence(File licenceFile)
            throws IOException, GeneralSecurityException, SAXException, ParserConfigurationException {
        this(Files.readAllBytes(licenceFile.toPath()));
    }

    public Licence(byte[] licence)
            throws IOException, GeneralSecurityException, SAXException, ParserConfigurationException {
        byte[] data = decrypt(licence);

        zipData = new Vector<ZipData>();
        MEntryResolver res = MEntryResolver.resolver;

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new ByteArrayInputStream(data)))) {
            ZipData tmp;
            while ((tmp = ZipData.readNextZipData(zis)) != null) {
                // System.out.println("found:"+tmp.name);
                zipData.add(tmp);
            }

            // Find bml
            byte[] dataBML = getZipFile("bml");
            BmlXMLparser handler = new BmlXMLparser(this);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            SAXParser saxParser = factory.newSAXParser();
            XMLReader xr = saxParser.getXMLReader();
            xr.setContentHandler(handler);
            xr.setEntityResolver(res);
            xr.setErrorHandler(handler);
            xr.setDTDHandler(handler); // not supported in android
            xr.parse(new InputSource(new ByteArrayInputStream(dataBML)));

            // parse metadata
            MProperties meta = new MProperties();
            byte[] dataMeta = getZipFile("meta.txt");
            meta.load(new ByteArrayInputStream(dataMeta));

            for (java.util.Map.Entry<Object, Object> entry : meta.entrySet()) {
                setProperty(META_PREFIX + entry.getKey(), entry.getValue().toString());
                if (entry.getKey().toString().equalsIgnoreCase("svnRevision")) {
                    svnRevision = Integer.parseInt(entry.getValue().toString());
                } else if (entry.getKey().toString().equalsIgnoreCase("buildAt")) {
                    licenceIssuingDate = new Date(1000 * Long.parseLong(entry.getValue().toString()));
                } else if (entry.getKey().toString().equalsIgnoreCase("release")) {
                    release = entry.getValue().toString();
                }
            }
        } finally {
            if (res != null) {
                res.closeResource();
            }
        }
    }

    public static final String META_PREFIX = "__meta.";

    public void toXML(File file) throws FileNotFoundException, UnsupportedEncodingException {
        // TODO add FalconEdition and DJIEdition
        PrintStream out = new PrintStream(file, "UTF-8");
        XMLWriter xml = new XMLWriter(new PrintWriter(out));
        xml.begin("", 2);
        xml.comment(DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getExportHeaderCore());
        xml.start("bml");
        xml.contentTag("type", "5"); // gui mlf (MAVinci Licence File)

        xml.contentTag(
            "platform", DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem().name());

        // xml.contentTag("jar", "false");

        if (getNumbers() != null) {
            String numbers = null;
            for (int number : getNumbers()) {
                if (numbers == null) {
                    numbers = "" + number;
                } else {
                    numbers += "," + number;
                }
            }

            xml.contentTag("number", numbers);
        }

        if (initDate != null) {
            xml.contentTag("initDate", BmlXMLparser.df.format(initDate));
        }

        if (expireingDate != null) {
            xml.contentTag("expire", BmlXMLparser.df.format(expireingDate));
        }

        xml.contentTag("maxUserLevel", maxUserlevel.ordinal() + "");
        xml.contentTag("hardwareId", licenceId);

        if (servicePayedUntil != null) {
            xml.contentTag("servicePayedUntil", BmlXMLparser.df.format(servicePayedUntil));
        }

        xml.contentTag("branch", branch);
        // xml.contentTag("buildOS", "openSUSE_12.1_x86_64");

        Ensure.notNull(language, "language");
        xml.contentTag("lang", getLanguage().getLanguage());
        xml.contentTag("company", company);
        xml.contentTag("reseller", getResellerID());

        xml.contentTag("default_cam", defaultCam);

        if (isMannedEdition) {
            xml.tagEmpty("MannedEdition");
        }

        if (isFixwingUAVedition) {
            xml.tagEmpty("FixwingUAVedition");
        }

        if (isCopterUAVedition) {
            xml.tagEmpty("CopterUAVedition");
        }

        if (SEEN_0 != null) {
            xml.contentTag("SEEN_0", SEEN_0);
        }

        if (SEEN_1 != null) {
            xml.contentTag("SEEN_1", SEEN_1);
        }

        xml.contentTag("beta", isBetaTesting + "");

        for (java.util.Map.Entry<Object, Object> entry : entrySet()) {
            if (entry.getKey().toString().startsWith(META_PREFIX)) {
                continue;
            }

            if (entry.getKey().toString().startsWith(MProperties.KEY_TESTCOMPLEATNESS)) {
                continue;
            }

            System.out.println("entry:" + entry);
            if (entry.getValue().toString().isEmpty()) {
                xml.tagEmpty(entry.getKey().toString());
            } else {
                xml.contentTag(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        for (AllowedUser user : getUsers()) {
            xml.start("user");
            String macs = null;
            for (String mac : user.getMacAddresses()) {
                if (macs == null) {
                    macs = mac;
                } else {
                    macs += "," + mac;
                }
            }

            xml.contentTag("macs", macs);
            if (user.pcName == null) {
                xml.contentTag("PCname", user.pcName);
            }

            xml.contentTag("displayName", user.displayName);
            xml.contentTag("email", user.email);
            xml.contentTag("name", user.name);

            for (java.util.Map.Entry<Object, Object> entry : user.entrySet()) {
                if (entry.getKey().toString().startsWith(MProperties.KEY_TESTCOMPLEATNESS)) {
                    continue;
                }

                if (entry.getValue().toString().isEmpty()) {
                    xml.tagEmpty(entry.getKey().toString());
                } else {
                    xml.contentTag(entry.getKey().toString(), entry.getValue().toString());
                }
            }

            xml.end(); // user
        }

        xml.end(); // bml
        xml.finish("");
        out.close();
    }

    public String getHumanReadableVersion() {
        return UpdateURL.getHumanReadableVersion(getRelease(), getSVNrevision());
    }

    public boolean isInternalGui() {
        return licenceId == null ? false : (licenceId.contains("Example") || licenceId.contains("Intern"));
    }
}
