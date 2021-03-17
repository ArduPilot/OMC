/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.licence;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.licence.AllowedUser;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class Voucher {
    private static final int MIN_LENGTH_VOUCHER = 8;
    private static final int MAX_LENGTH_VOUCHER = 22;
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    private static final BigInteger INT_97 = new BigInteger("97");
    private static final BigInteger INT_98 = new BigInteger("98");

    public static String KEY = "eu.mavinci.licence.voucher";
    static String VoucherSerialNumber;

    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public Voucher() {}

    public void inputAndSendVoucher() {
        inputVoucher();
        sendVoucher();
    }

    public void inputVoucher() {
        do {
            VoucherSerialNumber =
                JOptionPane.showInputDialog(
                    languageHelper.getString(KEY + ".inputVoucher"), languageHelper.getString(KEY + ".inputVoucherSN"));
            if (VoucherSerialNumber == null || VoucherSerialNumber.equals("")) {
                return;
            }

            if (!checkVoucher()) {
                JOptionPane.showMessageDialog(
                    null,
                    languageHelper.getString(KEY + ".msg.notvalidVoucher"),
                    languageHelper.getString(KEY + ".msg.notvalidVoucher.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } while (VoucherSerialNumber == null);
    }

    public boolean sendVoucher() {
        return sendVoucher("-");
    }

    public boolean sendVoucher(String comment) {
        /*
         * Sends Serial Number from Voucher via FileHelper.sendEMail (eMail or native eMail)
         */

        if (VoucherSerialNumber == null || VoucherSerialNumber.equals("")) {
            return false;
        }

        boolean defEclipse = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).isEclipseLaunched();
        Licence licence = DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getActiveLicence();
        AllowedUser user = DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getAllowedUser();

        String defSource =
            "OldInstallation: SN:"
                + (licence == null ? null : licence.getLicenceId())
                + " Version:"
                + DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getHumanReadableVersion();
        String defCompany = licence != null ? licence.getCompany() : "null";
        String defEMail = user != null ? user.getEmail() : "null";
        String defName = user != null ? user.getName() : "null";

        try {
            String toEmails = Licence.toLicenceRequestEmails;

            if (defEclipse) {
                // TODO clst FOR TEST
                // toEmails="clst@mavinci.de";
            }

            String url;
            url =
                "email="
                    + defEMail.trim()
                    + "\nname="
                    + defName.trim()
                    + "\ncompany="
                    + defCompany.trim()
                    + "\nsource="
                    + defSource.trim()
                    + "\nvoucher="
                    + VoucherSerialNumber
                    + "\ncomment="
                    + comment.trim();

            if (FileHelper.sendEMail(
                    toEmails,
                    "Open Mission Control Licence Voucher - " + defName.trim() + ", " + defCompany.trim(),
                    url)) {
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    languageHelper.getString(KEY + ".msg.sendErr", "unknown system"),
                    languageHelper.getString(KEY + ".msg.sendErr.title"),
                    JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            if (FileHelper.LAST_SENT == FileHelper.LAST_SENT_URL) {
                JOptionPane.showMessageDialog(
                    null,
                    languageHelper.getString(KEY + ".msg.sendOK"),
                    languageHelper.getString(KEY + ".msg.sendOK.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    languageHelper.getString(KEY + ".msg.sendEmail"),
                    languageHelper.getString(KEY + ".msg.sendEmail.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e2) {
            Debug.getLog().log(Level.WARNING, "could not send eMail", e2);
            JOptionPane.showMessageDialog(
                null,
                languageHelper.getString(KEY + ".msg.sendErr", e2.toString()),
                languageHelper.getString(KEY + ".msg.sendErr.title"),
                JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    public static boolean checkVoucher() {
        // VoucherSerialNumber //

        // Blanks entfernen
        Matcher matcher = SPACE_PATTERN.matcher(VoucherSerialNumber);
        if (matcher.find()) {
            VoucherSerialNumber = matcher.replaceAll("");
        }

        if (VoucherSerialNumber.length() < Voucher.MIN_LENGTH_VOUCHER
                || VoucherSerialNumber.length() > Voucher.MAX_LENGTH_VOUCHER) {
            VoucherSerialNumber = null;
            return false;
        }

        // Prüfziffern-Check; Settings startCheckNumber, lengthCheckNumber
        Integer startCheckNumber = VoucherSerialNumber.length() - 2;
        Integer lengthCheckNumber = 2;

        String left = VoucherSerialNumber.substring(0, startCheckNumber);
        String right =
            VoucherSerialNumber.substring(startCheckNumber + lengthCheckNumber, VoucherSerialNumber.length());
        try {
            String checkString = new String(left + right).toString();

            // Konvertieren nicht numerischer Einträge:
            // „+ 9“ ersetzt (z. B. A = 10, B = 11, usw., Z = 35).
            checkString = checkString.toUpperCase();

            // Replace each letter in the string with two digits, thereby expanding the string, where A = 10, B = 11,
            // ..., Z = 35.
            StringBuilder numericAccountNumber = new StringBuilder();
            for (int i = 0; i < checkString.length(); i++) {
                if (Character.getNumericValue(checkString.charAt(i)) != -1) {
                    numericAccountNumber.append(Character.getNumericValue(checkString.charAt(i)));
                }
            }

            BigInteger checkNumber = new BigInteger(numericAccountNumber.toString());

            String checkNumberIs =
                VoucherSerialNumber.substring(startCheckNumber, startCheckNumber + lengthCheckNumber);

            checkNumber = checkNumber.remainder(INT_97);

            checkNumber = INT_98.subtract(checkNumber);

            // TODO clst FOR TEST
            // System.out.println(checkString.toString());
            // System.out.println(numericAccountNumber.toString());
            // System.out.println(checkNumber);
            // checkNumber = INT_98 - Integer.remainderUnsigned(checkNumber, INT_97);
            String newCheckNumber = String.format("%02d", checkNumber);
            if (newCheckNumber.equals(checkNumberIs)) {
                return true;
            } else {
                VoucherSerialNumber = null;
                return false;
            }

        } catch (Exception e2) {
            VoucherSerialNumber = null;
            return false;
        }
    }

    public String getString() {
        return VoucherSerialNumber;
    }

    public static boolean checkVoucher(String text) {
        VoucherSerialNumber = text;
        if (VoucherSerialNumber == null || VoucherSerialNumber.equals("")) {
            return true;
        }

        if (!checkVoucher()) {
            JOptionPane.showMessageDialog(
                null,
                languageHelper.getString(KEY + ".msg.notvalidVoucher"),
                languageHelper.getString(KEY + ".msg.notvalidVoucher.title"),
                JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    public void setVoucher(String text) {
        VoucherSerialNumber = text;
    }
}
