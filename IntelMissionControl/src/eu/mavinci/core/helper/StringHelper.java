/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    private static final QuantityFormat quantityFormat =
        new AdaptiveQuantityFormat(DependencyInjector.getInstance().getInstanceOf(IQuantityStyleProvider.class));

    public static final String[] unitNames = {
        "Y", "Z", "E", "P", "T", "G", "M", "k", "", "m", "μ", "n", "p", "f", "a", "z", "y"
    };

    /**
     * transform a number into a human readable SI-Representation
     *
     * <p>e.g. 1234 -> 1.23k 0.0012 -> 1.2m
     *
     * <p>attention, it asshures not that also the wished number of digits is in the string. ending zeros will be
     * truncated!
     *
     * @param num the number to transform
     * @param digits the number of digits where to round, if negative, its the number of significat digits
     * @return
     */
    public static String numberToIngName(double num, int digits, boolean addZeros) {
        return numberToIngName(num, digits, 1000, addZeros);
    }

    public static String bytesToIngName(double num, int digits, boolean addZeros) {
        return numberToIngName(num, digits, 1024, addZeros) + "B";
    }

    public static String areaToIngName(double m2, int digits, boolean addZeros) {
        synchronized (quantityFormat) {
            if (digits < 0) {
                quantityFormat.setSignificantDigits(-digits);
                quantityFormat.setMaximumFractionDigits(-digits);
            } else {
                quantityFormat.setSignificantDigits(10);
                quantityFormat.setMaximumFractionDigits(digits);
            }

            return quantityFormat.format(Quantity.of(m2, Unit.SQUARE_METER), UnitInfo.LOCALIZED_AREA);
        }
    }

    public static String speedToIngName(double MperSec, int digits, boolean addZeros) {
        synchronized (quantityFormat) {
            if (digits < 0) {
                quantityFormat.setSignificantDigits(-digits);
                quantityFormat.setMaximumFractionDigits(-digits);
            } else {
                quantityFormat.setSignificantDigits(10);
                quantityFormat.setMaximumFractionDigits(digits);
            }

            return quantityFormat.format(Quantity.of(MperSec, Unit.METER_PER_SECOND), UnitInfo.LOCALIZED_SPEED);
        }
    }

    public static String ratioToPercent(double ratio, int digits, boolean addZeros) {
        return round(ratio * 100, digits, addZeros) + "%";
    }

    // public static String lengthToIngName(double meter, int digits) {
    // return lengthToIngName(meter, digits,false);
    // }

    public static String lengthToIngName(double meter, int digits, boolean addZeros) {
        return lengthToIngName(meter, digits, addZeros, false);
    }

    public static String lengthToIngName(double meter, int digits, boolean addZeros, boolean isVertical) {
        synchronized (quantityFormat) {
            if (digits < 0) {
                quantityFormat.setSignificantDigits(-digits);
                quantityFormat.setMaximumFractionDigits(-digits);
            } else {
                quantityFormat.setSignificantDigits(10);
                quantityFormat.setMaximumFractionDigits(digits);
            }

            return quantityFormat.format(Quantity.of(meter, Unit.METER), UnitInfo.LOCALIZED_LENGTH);
        }
    }

    // public static String numberToIngName(double num, int digits, int basis) {
    // return numberToIngName(num,digits,basis,false);
    // }

    public static String numberToIngName(double num, int digits, int basis, boolean addZeros) {
        String unit = "";
        int correct = 24;
        for (int i = 0; i != unitNames.length; i++) {
            double shift = Math.pow(basis, correct / 3);

            if (Math.abs(num) >= shift) {
                unit = unitNames[i];
                num /= shift;
                break;
            }

            correct -= 3;
        }

        String str = round(num, digits, addZeros);
        if (str.equals("0")) {
            return str + " ";
        }

        return str + " " + unit;
    }

    //
    // public static String round(double num, int digits){
    // return round(num,digits,false);
    // }

    public static String round(double num, int digits, boolean addZeros) {
        num = CMathHelper.round(num, digits);
        if (addZeros && digits != 0) {
            // System.out.println("num:"+num+" dig:"+digits);
            if (digits < 0 && num != 0) {
                int length = (int)Math.floor(Math.log10(Math.abs(num)) + 1);
                // System.out.println("num:"+num+" dig:"+digits + " length:"+length);
                digits = -digits - length;
            }

            if (digits <= 0) {
                return ((long)num) + "";
            }
            // System.out.println("num:"+num+" dig:"+digits);
            return String.format("%." + digits + "f", num);
        } else {
            if (Math.round(num) == num) {
                return ((long)num) + "";
            } else {
                return num + "";
            }
        }
    }

    public static final long ONE_SECOND = 1;
    public static final long SECONDS = 60;

    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long MINUTES = 60;

    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long HOURS = 24;

    public static final long ONE_DAY = ONE_HOUR * 24;

    /**
     * make a human readable version of a duration converts time (in seconds) to human-readable format "
     * <dd:><hh:>mm:ss"
     *
     * @param duration in seconds
     * @return
     */
    public static String secToShortDHMS(double duration) {
        long d = Math.round(duration);
        String res = "";
        d /= ONE_SECOND;
        int seconds = (int)(d % SECONDS);
        d /= SECONDS;
        int minutes = (int)(d % MINUTES);
        d /= MINUTES;
        int hours = (int)(d % HOURS);
        int days = (int)(d / HOURS);
        if (days == 0) {
            if (hours == 0) {
                res = String.format("%d:%02d", minutes, seconds);
            } else {
                res = String.format("%d:%02d:%02d", hours, minutes, seconds);
            }
        } else {
            res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
        }

        return res;
    }

    /**
     * make a human readable version of a duration converts time (in seconds) to human-readable format "
     * <dd:>hh:mm", not rounded to minutes
     *
     * @param duration in seconds
     * @return
     */
    public static String secToShortHM(double duration) {
        long d = Math.round(duration);
        String res = "";
        d /= ONE_SECOND;
        // int seconds = (int) (d % SECONDS);
        d /= SECONDS;
        int minutes = (int)(d % MINUTES);
        d /= MINUTES;
        int hours = (int)(d % HOURS);
        int days = (int)(d / HOURS);
        if (days == 0) {
            res = String.format("%02d:%02d", hours, minutes);
        } else {
            res = String.format("%dd%02d:%02d", days, hours, minutes);
        }

        return res;
    }

    /**
     * extracts minutes from a duration time (in seconds) to format "%dd"
     *
     * @param duration in seconds
     * @return
     */
    public static String secGetM(double duration) {
        long d = Math.round(duration);
        String res = "";
        d /= ONE_SECOND;
        d /= SECONDS;
        int minutes = (int)(d % MINUTES);
        d /= MINUTES;
        res = String.format("%02d", minutes);
        return res;
    }

    /**
     * extracts minutes from a duration time (in seconds) to format "%dd", rounds the seconds to minutes
     *
     * @param duration in seconds
     * @return
     */
    public static String secGetRoundM(double duration) {
        long d = Math.round(duration / ONE_SECOND / SECONDS);
        String res = "";
        int minutes = (int)(d % MINUTES);
        d /= MINUTES;
        res = String.format("%02d", minutes);
        return res;
    }

    /**
     * extracts hours from a duration time (in seconds) to format "%hh"
     *
     * @param duration in seconds
     * @return
     */
    public static String secGetH(double duration) {
        long d = Math.round(duration);
        String res = "";
        d /= ONE_SECOND;
        d /= SECONDS;
        d /= MINUTES;
        int hours = (int)(d);
        res = String.format("%02d", hours);
        return res;
    }

    /**
     * extracts hours from a duration time (in seconds) to format "%hh", rounds the seconds to minutes
     *
     * @param duration in seconds
     * @return
     */
    public static String secGetRoundH(double duration) {
        long d = Math.round(duration / ONE_SECOND / SECONDS);
        String res = "";
        d /= MINUTES;
        int hours = (int)(d);
        res = String.format("%02d", hours);
        return res;
    }

    /**
     * Reads an entire input stream (with UTF-8) and returns is as string
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String readStreamUTF8(InputStream is) throws IOException {
        try {
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } finally {
            is.close();
        }
    }

    public static InputStream stringToInputStream(String str) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes("UTF-8"));
    }

    /** compatible md5-hex string with php / mysql */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            String result = hash.toString(16);
            if ((result.length() % 2) != 0) {
                result = "0" + result;
            }

            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String replaceAllCaseInsensitive(String string, String regex, String replaceWith) {
        Pattern myPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        string = myPattern.matcher(string).replaceAll(replaceWith);
        return string;
    }

    /**
     * computes a md5 hash of a files content but ignoring all xml comment fields
     *
     * @param xml
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getHashXML(String xml) {
        xml = xml.replaceAll("[\\s]*<!--.*?-->", Matcher.quoteReplacement(""));
        return StringHelper.md5(xml);
    }

    /**
     * shortening a string to make it better fitting into messages e.g. "aasdasdasdasd" -> "aa...sd"
     *
     * @param str
     * @param maxLen
     * @param prefixLen
     * @return
     */
    public static String shortening(String str, int maxLen, int prefixLen) {
        if (str.length() <= maxLen) {
            return str;
        }

        if (prefixLen > maxLen + 3) {
            throw new IllegalArgumentException("prefix Len has to be shorter than maxLen");
        }

        return str.substring(0, prefixLen) + "..." + str.substring(str.length() - prefixLen - 3);
    }

    /**
     * escape all java string elements like linebreaks to \n or similar
     *
     * @param str
     * @return
     */
    public static String escapeSpecialChars(String str) {
        str = str.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"));
        str = str.replaceAll(Pattern.quote("\b"), Matcher.quoteReplacement("\\b"));
        str = str.replaceAll(Pattern.quote("\t"), Matcher.quoteReplacement("\\t"));
        str = str.replaceAll(Pattern.quote("\n"), Matcher.quoteReplacement("\\n"));
        str = str.replaceAll(Pattern.quote("\f"), Matcher.quoteReplacement("\\f"));
        str = str.replaceAll(Pattern.quote("\r"), Matcher.quoteReplacement("\\r"));
        str = str.replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\""));
        return str;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static Double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer parseInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }
}
