/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.StringRes;
import java.io.Serializable;

@SuppressWarnings("unused")
public class MappingService extends BaseService {

    public enum AirMapLayerType implements Serializable {
        TFRS("tfrs", R.string.tile_layer_tfr_faa),
        Wildfires("wildfires", R.string.tile_layer_wildfires),
        Fires("fires", R.string.tile_layer_fires),
        Emergencies("emergencies", R.string.tile_layer_emergencies),
        Prohibited("sua_prohibited", R.string.tile_layer_prohibited),
        Restricted("sua_restricted", R.string.tile_layer_restricted_airspace),
        NationalParks("national_parks", R.string.tile_layer_national_parks),
        NOAA("noaa", R.string.tile_layer_noaa),
        Schools("schools", R.string.tile_layer_schools),
        Hospitals("hospitals", R.string.tile_layer_hospitals),
        Heliports("heliports", R.string.tile_layer_heliports),
        PowerPlants("power_plants", R.string.tile_layer_power_plants),
        AirportsCommercial("airports_commercial,airports_commercial_private", R.string.tile_layer_airports),
        AirportsRecreational("airports_recreational,airports_recreational_private", R.string.tile_layer_airports),
        AirportsCommercialPrivate("airports_commercial_private", R.string.tile_layer_private_airports),
        AirportsRecreationalPrivate("airports_recreational_private", R.string.tile_layer_private_airports),
        ClassB("class_b", R.string.tile_layer_class_b),
        ClassC("class_c", R.string.tile_layer_class_c),
        ClassD("class_d", R.string.tile_layer_class_d),
        ClassE("class_e0", R.string.tile_layer_class_e),
        ClassF("class_f", R.string.tile_layer_class_f),
        HazardAreas("hazard_areas", R.string.tile_layer_hazard_areas),
        AerialRecreationalAreas("aerial_recreational_areas", R.string.tile_layer_aerial_rec_areas),
        DenselyPopulatedRegion("densely_populated_region", R.string.tile_layer_did),
        Cities("cities", R.string.tile_layer_cities),
        Custom("custom", R.string.tile_layer_custom),
        Prisons("prisons", R.string.tile_layer_prisons),
        Universities("universities", R.string.tile_layer_universities),
        SeaplaneBase("seaplane_base", R.string.tile_layer_seaplane_base),
        Other(
            "aerial_recreational_areas,custom,hazard_areas,hospitals,power_plants,prisons,schools,universities,cities",
            R.string.tile_layer_other_cautionary_areas),
        Notam("notam", R.string.notams),
        AMA("ama_field", R.string.amas),
        Unknown("type_not_found", R.string.tile_layer_unknown);

        private final String text;
        private final int title;

        AirMapLayerType(String text, @StringRes int title) {
            this.text = text;
            this.title = title;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public static AirMapLayerType fromString(String text) {
            switch (text) {
            case "tfrs":
                return TFRS;
            case "wildfires":
                return Wildfires;
            case "fires":
                return Fires;
            case "emergencies":
                return Emergencies;
            case "sua_prohibited":
                return Prohibited;
            case "sua_restricted":
                return Restricted;
            case "national_parks":
                return NationalParks;
            case "noaa":
                return NOAA;
            case "schools":
                return Schools;
            case "hospitals":
                return Hospitals;
            case "heliports":
                return Heliports;
            case "power_plants":
                return PowerPlants;
            case "airports_commercial,airports_commercial_private":
            case "airports_commercial":
                return AirportsCommercial;
            case "airports_recreational":
            case "airports_recreational,airports_recreational_private":
                return AirportsRecreational;
            case "airports_commercial_private":
                return AirportsCommercialPrivate;
            case "airports_recreational_private":
                return AirportsRecreationalPrivate;
            case "class_b":
                return ClassB;
            case "class_c":
                return ClassC;
            case "class_d":
                return ClassD;
            case "class_e0":
                return ClassE;
            case "class_f":
                return ClassF;
            case "hazard_areas":
                return HazardAreas;
            case "aerial_recreational_areas":
                return AerialRecreationalAreas;
            case "densely_populated_region":
                return DenselyPopulatedRegion;
            case "cities":
                return Cities;
            case "custom":
                return Custom;
            case "prisons":
                return Prisons;
            case "universities":
                return Universities;
            case "seaplane_base":
                return SeaplaneBase;
            case "aerial_recreational_areas,custom,hazard_areas,hospitals,power_plants,prisons,schools,universities,cities":
                return Other;
            case "notam":
                return Notam;
            case "ama":
            case "ama_field":
                return AMA;
            default:
                return Unknown;
            }
        }

        public AirMapAirspaceType getAirspaceType() {
            switch (this) {
            case TFRS:
                return AirMapAirspaceType.TFR;
            case Wildfires:
                return AirMapAirspaceType.Wildfires;
            case Fires:
                return AirMapAirspaceType.Fires;
            case Emergencies:
                return AirMapAirspaceType.Emergencies;
            case Prohibited:
            case Restricted:
                return AirMapAirspaceType.SpecialUse;
            case NationalParks:
            case NOAA:
                return AirMapAirspaceType.Park;
            case Schools:
                return AirMapAirspaceType.School;
            case Hospitals:
                return AirMapAirspaceType.Hospitals;
            case PowerPlants:
                return AirMapAirspaceType.PowerPlant;
            case SeaplaneBase:
            case AirportsCommercial:
            case AirportsRecreational:
            case AirportsCommercialPrivate:
            case AirportsRecreationalPrivate:
                return AirMapAirspaceType.Airport;
            case Heliports:
            case ClassB:
            case ClassC:
            case ClassD:
            case ClassE:
            case ClassF:
                return AirMapAirspaceType.ControlledAirspace;
            case HazardAreas:
                return AirMapAirspaceType.HazardArea;
            case AerialRecreationalAreas:
                return AirMapAirspaceType.RecreationalArea;
            case DenselyPopulatedRegion:
            case Cities:
                return AirMapAirspaceType.City;
            case Custom:
                return AirMapAirspaceType.Custom;
            case Prisons:
                return AirMapAirspaceType.Prison;
            case Universities:
                return AirMapAirspaceType.University;
            case Notam:
                return AirMapAirspaceType.Notam;
            case AMA:
                return AirMapAirspaceType.AMA;
            case Unknown:
                return AirMapAirspaceType.Unknown;
            }

            return null;
        }
    }

    public enum AirMapAirspaceType {
        Airport("airport", R.string.airspace_type_airport, true, 4),
        Heliport("heliport", R.string.airspace_type_heliport, true, 5),
        Park("park", R.string.airspace_type_national_park, false, 3),
        PowerPlant("power_plant", R.string.airspace_type_power_plant, true, 6),
        ControlledAirspace("controlled_airspace", R.string.airspace_type_controlled, true, 3),
        School("school", R.string.airspace_type_school, true, 1),
        SpecialUse("special_use_airspace", R.string.airspace_type_special_use, true, 2),
        SeaplaneBase("seaplane_base", R.string.airspace_type_seaplane_base, false, 1),
        TFR("tfr", R.string.airspace_type_tfr_faa, true, 10),
        Wildfires("wildfire", R.string.airspace_type_wildfire, true, 10),
        Fires("fire", R.string.airspace_type_fire, false, 10),
        Emergencies("emergency", R.string.airspace_type_emergency, false, 10),
        Hospitals("hospital", R.string.airspace_type_hospital, true, 6),
        HazardArea("hazard_area", R.string.airspace_type_hazard_area, false, 10),
        RecreationalArea("recreational_area", R.string.airspace_type_aerial_rec_area, false, 1),
        City("city", R.string.airspace_type_city, false, 1),
        Custom("custom", R.string.airspace_type_custom, false, 1),
        Prison("prison", R.string.airspace_type_prison, true, 6),
        University("university", R.string.airspace_type_university, false, 1),
        Notam("notam", R.string.airspace_type_notam, true, 10),
        AMA("ama_field", R.string.airspace_type_ama, false, 1),
        County("county", R.string.county, false, 1),
        Country("country", R.string.country, false, 1),
        Embassy("embassy", R.string.embassy, false, 1),
        FIR("fir", R.string.fir, false, 1),
        FederalBuilding("federal_building", R.string.federal_building, false, 6),
        GliderPort("gliderport", R.string.glider_port, false, 1),
        Highway("highway", R.string.highway, false, 5),
        IndustrialProperty("industrial_property", R.string.industrial_property, false, 6),
        MilitaryProperty("military_property", R.string.military_property, false, 6),
        PoliceStation("police_station", R.string.police_station, false, 1),
        Powerline("powerline", R.string.powerline, false, 5),
        Railway("railway", R.string.railway, false, 5),
        ResidentialProperty("residential_property", R.string.residential_property, false, 1),
        Stadium("stadium", R.string.stadium, false, 1),
        State("state", R.string.state, false, 1),
        Subprefecture("subprefecture", R.string.subprefecture, false, 1),
        Supercity("supercity", R.string.supercity, false, 1),
        UlmField("ulm_field", R.string.ulm_field, false, 1),
        Waterway("waterway", R.string.waterway, false, 1),
        JapanBase("jpn_base", R.string.japan_base_admin, false, 1),
        Unknown("unknown", R.string.airspace_type_unknown, false, 1);

        private final String text;
        private final int title;
        private final boolean defaultEnabled;
        private final double zOrder;

        AirMapAirspaceType(String text, @StringRes int title, boolean defaultEnabled, double zOrder) {
            this.text = text;
            this.title = title;
            this.defaultEnabled = defaultEnabled;
            this.zOrder = zOrder;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public boolean isDefaultEnabled() {
            return defaultEnabled;
        }

        public double getzOrder() {
            return zOrder;
        }

        public static AirMapAirspaceType fromString(String text) {
            switch (text) {
            case "airport":
                return Airport;
            case "heliport":
                return Heliport;
            case "park":
                return Park;
            case "power_plant":
                return PowerPlant;
            case "controlled_airspace":
                return ControlledAirspace;
            case "school":
                return School;
            case "special_use_airspace":
                return SpecialUse;
            case "tfr":
                return TFR;
            case "wildfire":
                return Wildfires;
            case "fire":
                return Fires;
            case "emergency":
                return Emergencies;
            case "hospital":
                return Hospitals;
            case "hazard_area":
                return HazardArea;
            case "recreational_area":
                return RecreationalArea;
            case "city":
                return City;
            case "custom":
                return Custom;
            case "prison":
                return Prison;
            case "university":
                return University;
            case "seaplane_base":
                return SeaplaneBase;
            case "notam":
                return Notam;
            case "ama":
            case "ama_field":
                return AMA;
            case "country":
                return Country;
            case "county":
                return County;
            case "embassy":
                return Embassy;
            case "fir":
                return FIR;
            case "federal_building":
                return FederalBuilding;
            case "gliderport":
                return GliderPort;
            case "highway":
                return Highway;
            case "industrial_property":
                return IndustrialProperty;
            case "military_property":
                return MilitaryProperty;
            case "police_station":
                return PoliceStation;
            case "powerline":
                return Powerline;
            case "railway":
                return Railway;
            case "residential_property":
                return ResidentialProperty;
            case "stadium":
                return Stadium;
            case "state":
                return State;
            case "subprefecture":
                return Subprefecture;
            case "supercity":
                return Supercity;
            case "ulm_field":
                return UlmField;
            case "waterway":
                return Waterway;
            default:
                return Unknown;
            }
        }
    }

    public enum AirMapMapTheme {
        Standard("standard"),
        Dark("dark"),
        Light("light"),
        Satellite("satellite");

        private final String text;

        AirMapMapTheme(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static AirMapMapTheme fromString(String text) {
            switch (text) {
            case "standard":
            case "street":
                return Standard;
            case "light":
                return Light;
            case "dark":
                return Dark;
            case "satellite":
                return Satellite;
            }

            return null;
        }
    }

}
