package com.o3dr.android.client;

// from strings.xml
public class R {
    public static class integer {

        public static int core_lib_version = 0;
    };

    public enum string {
        error_no_error("No error!"),
        error_throttle_below_failsafe("Throttle below failsafe"),
        error_gyro_calibration_failed("Gyro calibration failed"),
        error_mode_not_armable("Mode not armable"),
        error_rotor_not_spinning("Rotor not spinning"),
        error_vehicle_leaning("Vehicle is leaning"),
        error_throttle_too_high("Throttle too high"),
        error_safety_switch("Safety switch"),
        error_compass_calibration_running("Compass calibration running"),
        error_rc_not_calibrated("RC not calibrated"),
        error_barometer_not_healthy("Barometer not healthy"),
        error_compass_not_healthy("Compass not healthy"),
        error_compass_not_calibrated("Compass not calibrated"),
        error_compass_offsets_too_high("Compass offsets too high"),
        error_check_magnetic_field("Check magnetic field"),
        error_inconsistent_compass("Inconsistent compass"),
        error_check_geo_fence("Check geo fence"),
        error_ins_not_calibrated("Inertial Navigation System not calibrated"),
        error_accelerometers_not_healthy("Accelerometers not healthy"),
        error_inconsistent_accelerometers("Inconsistent accelerometers"),
        error_gyros_not_healthy("Gyros not healthy"),
        error_inconsistent_gyros("Inconsistent gyros"),
        error_check_board_voltage("Check board voltage"),
        error_duplicate_aux_switch_options("Duplicate aux switch options"),
        error_check_failsafe_threshold("Check failsafe threshold"),
        error_check_angle_max("Check max angle"),
        error_acro_bal_roll_pitch("Acro bal roll/pitch"),
        error_need_gps_lock("Need GPS lock"),
        error_ekf_home_variance("EKF Home variance"),
        error_high_gps_hdop("High GPS hdop"),
        error_gps_glitch("GPS glitch"),
        error_waiting_for_navigation_alignment("Waiting for navigation alignment"),
        error_altitude_disparity("Altitude disparity"),
        error_low_battery("Low battery!"),
        error_auto_tune_failed("Auto-tune failed!"),
        error_crash("Vehicle crash"),
        error_parachute_too_low("Parachute too low"),
        error_ekf_variance("EKF Variance"),
        error_no_dataflash("No inserted dataflash"),
        error_rc_failsafe("RC Failsafe"),

        label_free_flight("Free Flight"),
        label_selfie("Selfie"),
        label_cable_cam("Cable Cam"),
        label_preset_cable_cam("Cable Cam"),
        label_preset_orbit("Orbit"),
        label_orbit("Orbit"),
        label_preset_selfie("Selfie"),
        label_follow("Follow"),
        label_inspect("Inspect"),
        label_survey("Survey"),
        label_scan("Scan"),
        label_pano("Pano"),
        label_zipline("Zipline"),
        label_rewind("Rewind"),
        label_return_home("Return Home"),
        label_transect("Transect"),

        //<!-- Shot type indicator -->
        copter_alt_hold_label("FLY: Manual"),
        copter_loiter_label("FLY"),
        copter_rtl_label("Return Home"),
        copter_guided_label("Take off"),
        copter_auto_tune_label("Auto-tune"),
        copter_pos_hold_label("Position Hold"),
        copter_auto_label("Mission"),

        label_tlog_title("Telemetry Log Files"),
        tlog_selector_label("Telemetry Logs File Selector"),
        loading_data("Loading data...");

        string(String s) {
            description = s;
        }

        public String description;
    }
}
