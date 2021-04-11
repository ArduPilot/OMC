/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package eu.mavinci.core.plane.protocol;

/** see NmeaTrackPoint in WWJ */
public class NmeaGGAPoint {
    public double latitude;
    public double longitude;
    public double altitude;
    public double geoidHeight;

    /**
     * @param words
     * @throws IllegalArgumentException if <code>words</code> is null or has length less than 1
     */
    public NmeaGGAPoint(String[] words) {
        if (words == null) {
            throw new NullPointerException();
        }

        if (words.length < 1) {
            throw new IllegalArgumentException("empty argument array");
        }

        if (words[0].equalsIgnoreCase("GPGGA")) {
            this.doGGA(words);
        } else if (words[0].equalsIgnoreCase("GPRMC")) {
            this.doRMC(words);
        }
    }

    /**
     * @param words
     * @throws IllegalArgumentException if <code>words</code> is null or has length less than 6
     */
    private void doGGA(String[] words) {
        // words won't be null, but it could be the wrong length
        if (words.length < 6) {
            throw new IllegalArgumentException("less than 6 words");
        }

        this.latitude = this.parseLatitude(words[2], words[3]);
        this.longitude = this.parseLongitude(words[4], words[5]);
        if (words.length >= 11) {
            this.altitude = this.parseElevation(words[9], words[10]);
        }

        if (words.length >= 13) {
            this.geoidHeight = this.parseElevation(words[11], words[12]);
        }
    }

    private void doRMC(String[] words) {}

    private double parseLatitude(String angle, String direction) {
        if (angle.length() == 0) {
            return 0;
        }

        double minutes = angle.length() > 2 ? Double.parseDouble(angle.substring(2, angle.length())) : 0d;
        double degrees = Double.parseDouble(angle.substring(0, 2)) + minutes / 60d;

        return direction.equalsIgnoreCase("S") ? -degrees : degrees;
    }

    private double parseLongitude(String angle, String direction) {
        if (angle.length() == 0) {
            return 0;
        }

        double minutes = angle.length() > 3 ? Double.parseDouble(angle.substring(3, angle.length())) : 0d;
        double degrees = Double.parseDouble(angle.substring(0, 3)) + minutes / 60d;

        return direction.equalsIgnoreCase("W") ? -degrees : degrees;
    }

    private double parseElevation(String height, String units) {
        if (height.length() == 0) {
            return 0;
        }

        return Double.parseDouble(height) * unitsToMeters(units);
    }

    private double unitsToMeters(String units) {
        double f;

        if (units.equals("M")) {
            f = 1d;
        } else if (units.equals("f")) {
            f = 3.2808399;
        } else if (units.equals("F")) {
            f = 0.5468066528;
        } else {
            f = 1d;
        }

        return f;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude
     * @throws IllegalArgumentException if <code>latitude</code> is less than -90 or greater than 90
     */
    public void setLatitude(double latitude) {
        if (latitude > 90 || latitude < -90) {
            throw new IllegalArgumentException("lat out of range" + latitude);
        }

        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude
     * @throws IllegalArgumentException if <code>longitude</code> is less than -180 or greater than 180
     */
    public void setLongitude(double longitude) {
        if (longitude > 180 || longitude < -180) {
            throw new IllegalArgumentException("longitude out of range" + longitude);
        }

        this.longitude = longitude;
    }

}
