/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.update;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.licence.Reseller;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class UpdateURL {

    public static final String DEV_EMAIL_MAVINCI = "imc-support@intel.com";
    public static final String ENTERPRISE_EMAIL = "imc-support@intel.com";

    public static String getSupportEmail() {
        Licence licence = StaticInjector.getInstance(ILicenceManager.class).getActiveLicence();
        return Reseller.getSupportEMailByKey(licence == null ? "null" : licence.getResellerID());
    }

    public static String getAllSupportEmails() {
        String email = getSupportEmail();
        if (email.equals(ENTERPRISE_EMAIL)) {
            return ENTERPRISE_EMAIL;
        }

        return email + "," + ENTERPRISE_EMAIL;
    }

    public static String SUPPORT_SUBJECT_PREFIX = "MAVinci Support Request - ";

    public static String getBaseString() throws UnsupportedEncodingException {
        // the time argument prevent new update stuff from getting stucked in proxy servers
        // adding /index.php, to make the URL correct handable by proxy's
        String str = "https://u.mavinci.de/index.php?time=" + System.currentTimeMillis();

        Licence licence = StaticInjector.getInstance(ILicenceManager.class).getActiveLicence();
        String myID = licence == null ? null : licence.getLicenceId();
        if (myID != null) {
            str += "&myID=" + URLEncoder.encode(myID, "UTF-8");
        }
        // System.out.println("upadteBaseString="+str);
        return str;
    }

    private static String getBaseString(String serialNo, EnumUpdateTargets updateTarget)
            throws UnsupportedEncodingException {
        // the time argument prevent new update stuff from getting stucked in proxy servers
        // adding /index.php, to make the URL correct handable by proxy's
        String str =
            getBaseString()
                + "&serialNumber="
                + URLEncoder.encode(serialNo != null ? serialNo : "", "UTF-8")
                + "&type="
                + updateTarget.ordinal()
                + "&osType="
                + StaticInjector.getInstance(IVersionProvider.class).getSystem();
        // System.out.println("updateBaseString="+str);
        return str;
    }

    public static URL getUpdateURLRevision(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, long currentVersion)
            throws Exception {
        return new URL(getBaseString(serialNo, updateTarget, currentMajor, currentVersion) + "&revision");
    }

    private static String getBaseString(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, long currentVersion)
            throws UnsupportedEncodingException {
        String str = getBaseString(serialNo, updateTarget);
        str += "&myMajor=" + URLEncoder.encode(StringUtils.trimToEmpty(currentMajor), "UTF-8");
        str += "&myVersion=" + currentVersion;

        return str;
    }

    public static URL getUpdateURLMajorVersion(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, long currentVersion)
            throws Exception {
        return new URL(getBaseString(serialNo, updateTarget, currentMajor, currentVersion) + "&major");
    }

    public static URL getUpdateURLFile(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, long currentVersion)
            throws Exception {
        return new URL(getBaseString(serialNo, updateTarget, currentMajor, currentVersion) + "&file");
    }

    public static URL getUpdateURLFile(String serialNo, EnumUpdateTargets updateTarget, File oldFile) throws Exception {
        return new URL(
            getBaseString(serialNo, updateTarget)
                + "&file&supportsDRPM&diffTo="
                + URLEncoder.encode(oldFile.getName(), "UTF-8"));
    }

    public static URL getUpdateURLbml(String serialNo, EnumUpdateTargets updateTarget) throws Exception {
        return new URL(getBaseString(serialNo, updateTarget) + "&bml");
    }

    public static String getURLData(URI uri) throws IOException {
        return getURLData(uri.toString());
    }

    public static String getURLData(String uri) throws IOException {
        return getURLData(new URL(uri));
    }

    /**
     * directly load data from url do not use this for large files! this is blocking! better use some progress dialog,
     * as internet access can be very slow
     *
     * @param url
     * @return the data from the url or null on error
     * @throws IOException
     */
    public static String getURLData(URL url) throws IOException {
        return getURLData(url, 3000);
    }

    public static String getURLData(URL url, int timeout) throws IOException {
        HttpURLConnection httpConn = null;
        int contentLength;
        try {
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setConnectTimeout(timeout);
            httpConn.setReadTimeout(timeout);
            contentLength = httpConn.getContentLength();
            try (InputStream is = httpConn.getInputStream()) {
                byte[] buffer = new byte[contentLength];
                is.read(buffer);

                return new String(buffer);
            }

        } finally {
            try {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }

    public static class ExpireInf {
        public static final ExpireInf defNowExpired = new ExpireInf(true);

        public static final ExpireInf defNeverExpired = new ExpireInf(false);

        private ExpireInf(boolean shouldExpire) {
            isExpired = shouldExpire;
            if (shouldExpire) {
                expireDate = "unknown";
            } else {
                expireDate = "never";
            }
        }

        public ExpireInf(String s) throws Exception {
            String[] t = s.split(Pattern.quote("\n"));
            expireDate = t[0];
            if (t[1].equals("true")) {
                isExpired = true;
            } else {
                isExpired = false;
            }

            chellangeRet = t[2];
        }

        @Override
        public String toString() {
            return "isExpired=" + isExpired + "  expireDate=" + expireDate + " chellangeRet=" + chellangeRet;
        }

        public boolean isExpired;
        public String expireDate;
        public String chellangeRet;

        public boolean willExpire() {
            return this != defNeverExpired;
        }
    }

    public static ExpireInf isExpired(String serialNo, EnumUpdateTargets updateTarget, String chellange)
            throws IOException {
        String uri = getBaseString(serialNo, updateTarget) + "&isExpired&chellange=" + chellange;
        // System.out.println(uri);
        String ret = getURLData(uri).toLowerCase();
        // System.out.println(ret);
        try {
            return new ExpireInf(ret);
        } catch (Exception e) {
            return ExpireInf.defNowExpired;
        }
    }

    /**
     * @param serialNo
     * @param updateTarget
     * @param currentVersion
     * @param currentMajor
     * @return the version number and -1 on error
     */
    public static long getRevision(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, long currentVersion) {
        try {
            URL url = getUpdateURLRevision(serialNo, updateTarget, currentMajor, currentVersion);
            String s = getURLData(url);
            if (s == null) {
                return -1;
            }

            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static long getRevision(String serialNo, EnumUpdateTargets updateTarget) {
        return getRevision(serialNo, updateTarget, null, 0);
    }

    public static String getMajorVersion(
            String serialNo, EnumUpdateTargets updateTarget, String currentMajor, int currentVersion) throws Exception {
        URL url = getUpdateURLMajorVersion(serialNo, updateTarget, currentMajor, currentVersion);
        return getURLData(url).trim();
    }

    public static String getMajorVersion(String serialNo, EnumUpdateTargets updateTarget) throws Exception {
        return getMajorVersion(serialNo, updateTarget, null, 0);
    }

    public static String getHumanReadableMajor(String majorVersion) {
        if (majorVersion == null) {
            return "";
        }

        return majorVersion;
    }

    public static String getHumanReadableVersion(String majorVersion, long revision) {
        String myRevision;
        if (revision < 999999999) {
            myRevision = String.valueOf(revision); // for licenses not a timestmp, its svn commit
        } else {
            try {
                Integer.parseInt(String.valueOf(revision));
                ZoneId zoneId = ZoneId.of("UTC");
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(revision * 1000), zoneId);
                final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                myRevision = FORMATTER.format(dateTime);
            } catch (NumberFormatException e) {
                myRevision = String.valueOf(revision);
            }
        }

        return getHumanReadableMajor(majorVersion) + "." + myRevision;
    }

    /**
     * let MAVinci Server send an email for this GUI
     *
     * @param toAddress
     * @param subject
     * @param body
     * @return true if the sending succeeded
     */
    public static boolean sendEMail(String toAddress, String subject, String body) {
        return sendEMail(toAddress, subject, body, Level.WARNING);
    }

    /**
     * let MAVinci Server send an email for this GUI
     *
     * @param toAddress
     * @param subject
     * @param body
     * @param level
     * @return true if the sending succeeded
     */
    public static boolean sendEMail(String toAddress, String subject, String body, Level level) {
        return sendEMail(toAddress, null, subject, body, level);
    }

    /**
     * let MAVinci Server send an email for this GUI
     *
     * @param toAddress
     * @param ccAddress
     * @param subject
     * @param body
     * @param level
     * @return true if the sending succeeded
     */
    public static boolean sendEMail(String toAddress, String ccAddress, String subject, String body, Level level) {
        try {
            String fromAddress = "";
            if (ccAddress == null) {
                ccAddress = "";
            }

            String[] customerAddress = ccAddress.split(",");
            if (customerAddress.length > 0) {
                fromAddress = customerAddress[0];
            }

            Licence licence = StaticInjector.getInstance(ILicenceManager.class).getActiveLicence();
            String myID = licence == null ? null : licence.getLicenceId();

            String url =
                getBaseString(myID, EnumUpdateTargets.GUI)
                    + "&sendMail"
                    + "&from="
                    + URLEncoder.encode(fromAddress, "UTF-8")
                    + "&to="
                    + URLEncoder.encode(toAddress, "UTF-8")
                    + "&cc="
                    + URLEncoder.encode(ccAddress != null ? ccAddress : "", "UTF-8")
                    + "&subject="
                    + URLEncoder.encode(subject, "UTF-8")
                    + "&body="
                    + URLEncoder.encode(body, "UTF-8");
            if (url.length() > 2000) {
                url = url.substring(0, 1997) + "...";
            }

            String ret = getURLData(url).toLowerCase();
            // System.out.println("ret:" + ret);
            boolean ok = "true".equals(ret);
            if (!ok) {
                Debug.getLog().log(Level.WARNING, "could not send eMail, server replys=" + ret);
            }

            return ok;
        } catch (Exception e) {
            Debug.getLog().log(level, "could not send eMail", e);
            return false;
        }
    }

}
