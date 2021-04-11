package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags;

import eu.mavinci.core.obfuscation.IKeepAll;

public class Gtag implements IKeepAll {
    public Attitude attitude_ned;
    public double cam_angle_pitch_rad;
    public double cam_angle_roll_rad;
    public double cam_angle_yaw_rad;
    public double gps_height_m;
    public Header header;
    public double horizontal_accuracy_m;
    public double last_raw_time_of_week;
    public double last_raw_week;
    public double latitude_deg;
    public double latitude_reference_deg;
    public double longitude_deg;
    public double longitude_reference_deg;
    public String path_name;
    public double pressure_height_m;
    public double speed_accuracy_m_per_s;
    public Vec3 speed_m_per_s;
    public int time_of_week_ms;
    public int time_since_last_raw_timestamp_ms;
    public int trigger_count;
    public int trigger_source;
    public double vertical_accuracy_m;
    public int week;
    public double x_relative_to_reference_m;
    public double y_relative_to_reference_m;

    public long getTimetampMs() {
        return towWeek2UtcTimestamp(week, time_of_week_ms);
    }

    static long towWeek2GpsTimestamp(int week, int tow) {
        return (long)tow + (long)week * 7 * 24 * 60 * 60 * 1000;
    }

    static long gpsTimeStampToUtcTimeStamp(long t) {
        return t + 315964800000L; // /> \todo add leapseconds to the equation
    }

    static long towWeek2UtcTimestamp(int week, int tow) {
        return gpsTimeStampToUtcTimeStamp(towWeek2GpsTimestamp(week, tow));
    }
}
