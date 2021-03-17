/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import static eu.mavinci.airspace.AirspaceTypes.ExpirationPossibility.CANNOT_BE_EXPIRED;
import static eu.mavinci.airspace.AirspaceTypes.ExpirationPossibility.CAN_BE_EXPIRED;
import static eu.mavinci.airspace.AirspaceTypes.MAVFightAllowance.FLIGHT_NOT_ALLOWED;
import static eu.mavinci.airspace.AirspaceTypes.MAVFightAllowance.FLIGTH_ALLOWED;

public enum AirspaceTypes {
    Restricted(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "restricted"),
    Danger(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "danger"),
    Prohibited(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "prohibited"),
    ClassA(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_a"),
    ClassB(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_b"),
    ClassC(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_c"),
    ClassD(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_d"),
    ClassE(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_e"),
    ClassF(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "class_f"),
    ClassG(FLIGTH_ALLOWED, CANNOT_BE_EXPIRED, "class_g"),
    GliderProhibited(FLIGTH_ALLOWED, CAN_BE_EXPIRED, "glider_prohibited"),
    CTR(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "ctr"),
    WaveWindow(FLIGTH_ALLOWED, CAN_BE_EXPIRED, "wave_window"),
    TMZ(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "tmz"),
    AWY(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "awy"),
    UKN(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "ukn"),
    RMZ(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "rmz"), // unknown ?
    Airport(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "airport"),
    Heliport(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "heliport"),
    ControlledAirspace(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "controlled_airspace"),
    SpecialUseAirspace(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "special_use_airspace"),
    TFR(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "tfr"),
    Wildfires(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "wildfires"),
    Fires(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "fires"),
    PowerPlant(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "power_plant"),
    Prison(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "prison"),
    School(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "school"),
    Hospital(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "hospital"),
    Emergencies(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "emergencies"),
    DenselyInhabitDistrict(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "densely_inhabit_district"),
    Park(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "park"),
    University(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "university"),
    HazardArea(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "hazard_area"),
    RecreationalArea(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "recreational_area"),
    Custom(FLIGHT_NOT_ALLOWED, CAN_BE_EXPIRED, "custom"),
    UNKNOWN(FLIGHT_NOT_ALLOWED, CANNOT_BE_EXPIRED, "unknown");

    private final MAVFightAllowance mavFightAllowance;
    private final ExpirationPossibility expirationPossibility;
    private final String l10nKey;

    AirspaceTypes(MAVFightAllowance mavFightAllowance, ExpirationPossibility expirationPossibility, String l10nKey) {
        this.mavFightAllowance = mavFightAllowance;
        this.expirationPossibility = expirationPossibility;
        this.l10nKey = l10nKey;
    }

    enum MAVFightAllowance {
        FLIGTH_ALLOWED, FLIGHT_NOT_ALLOWED;
    }

    enum ExpirationPossibility {
        CAN_BE_EXPIRED, CANNOT_BE_EXPIRED;
    }

    /**
     * @return if a mav is allowed to fly through such an airspace without registration or authorization
     */
    public boolean isMAVAllowed() {
        return mavFightAllowance == FLIGTH_ALLOWED;
    }

    /**
     * @return if there is a possibility that airspace of the type can disappear or be changed in nearest feature
     */
    public boolean canBeExpired() {
        return expirationPossibility == CAN_BE_EXPIRED;
    }

    /**
     * @return L10N key of airspace type name
     */
    public String getLocalizationKey() {
        return "com.intel.missioncontrol.airspaces.types." + l10nKey;
    }
}
