/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

public class CountryDetector implements ICountryDetector {

    public static final String MTR = "MTR";

    public static final String[] countriesRestricted =
        new String[] {
            "AF",
            "IQ",
            "IR",
            "LB",
            "LY",
            "MM",
            "KP",
            "PK",
            "RU",
            "SD",
            "SY",
            // "SS", //SüdSudan ist 2011 vom sudan abgeteilt worden.... so neu
            // sind meine polygone aber nicht, so das es reicht das ebenfalls
            // restrictete Sudan hier zu berücksichtigen
            "SO",
            "CU"
        };

    public static final String[] countriesCEadd =
        new String[] {
            "LI",
            "NO",
            "SJ",
            "IS",
            "CH",
            "BV",
            "TR", // Turkey agreed R&TTE directive
            // (relevant part for radio
            // regulation in CE)
            "AN",
            "SH",
            "AW",
            "BM",
            "BV",
            "IO",
            "VG",
            "KY",
            "FK",
            "FO",
            "GF",
            "PF",
            "GI",
            "GL",
            "GP",
            "SJ",
            "MQ",
            "YT",
            "MS",
            "PN",
            "RE",
            "PM",
            "TC",
            "VG",
            "WF"
        };

    public static final String[] countriesEU =
        new String[] {
            "BE", "BG", "DK", "DE", "EE", "FI", "FR", "GR", "IE", "IT", "HR", "LV", "LT", "LU", "MT", "NL", "AT", "PL",
            "PT", "RO", "SE", "SK", "SI", "ES", "CZ", "HU", "GB", "CY", MTR,
        };
    public static final String[] countriesFCCIC =
        new String[] {"US", "CA", "AS", "GU", "UM", "MH", "FM", "MP", "PA", "PR", "VI"};

    public enum RadioRegulation {
        other, // ==0
        ce, // =1
        fccic, // =2
        malasia // =3
    }

    public final TreeMap<String, Country> countries;

    public CountryDetector() {
        this(loadData());
    }

    public CountryDetector(TreeMap<String, Country> countries) {
        this.countries = countries;
        setupRegulations();
    }

    private static TreeMap<String, Country> loadData() {
        TreeMap<String, Country> countries = new TreeMap<String, Country>();
        try (InputStream is = ClassLoader.getSystemResourceAsStream("eu/mavinci/other/WorldCountryBoundaries.txt"); ) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                Country c = Country.fromInternal(line);
                countries.put(c.iso2, c);
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "could not load country mapping", e);
        }

        try (InputStream is = ClassLoader.getSystemResourceAsStream("eu/mavinci/other/WorldCountryBoundaries.dat"); ) {
            GeoFencing.readFile(is);
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "could not load country mapping", e);
        }

        return countries;
    }

    private void setupRegulations() {
        for (String iso2 : countriesRestricted) {
            Country c = countries.get(iso2);
            if (c == null) {
                Debug.getLog().severe("Country not found: " + iso2);
                continue;
                // System.exit(-1);
            }

            c.isRestricted = true;
        }

        for (String iso2 : countriesEU) {
            Country c = countries.get(iso2);
            if (c == null) {
                Debug.getLog().severe("Country not found: " + iso2);
                continue;
                // System.exit(-1);
            }

            c.isEU = true;
            c.radioRegulation = RadioRegulation.ce;
        }

        for (String iso2 : countriesCEadd) {
            Country c = countries.get(iso2);
            if (c == null) {
                Debug.getLog().severe("Country not found: " + iso2);
                continue;
                // System.exit(-1);
            }

            c.isEU = false;
            c.radioRegulation = RadioRegulation.ce;
        }

        for (String iso2 : countriesFCCIC) {
            Country c = countries.get(iso2);
            if (c == null) {
                Debug.getLog().severe("Country not found: " + iso2);
                continue;
                // System.exit(-1);
            }

            c.isEU = false;
            c.radioRegulation = RadioRegulation.fccic;
        }
        // special stuff
        // countries.get("MY").radioRegulation=RadioRegulation.malasia;
        Country myCountry = countries.get("MY");
        Ensure.notNull(myCountry, "myCountry");
        myCountry.radioRegulation = RadioRegulation.ce; // TODO fixme
    }

    public static final String KEY = "eu.mavinci.geo.CountryDetector";

    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public String getRestrictedI18N() {
        return languageHelper.getString(KEY + ".restricted");
    }

    private void showWarning(Country c) {
        DependencyInjector.getInstance()
            .getInstanceOf(IApplicationContext.class)
            .addToast(
                Toast.of(ToastType.ALERT)
                    .setText(languageHelper.getString(KEY + ".msg", c == null ? "UNDEFINED" : c.name))
                    .setShowIcon(true)
                    .create());
    }

    public boolean allowProceed(LatLon latLon) {
        return allowProceed(latLon, false);
    }

    @Override
    public boolean allowProceed(LatLon latLon, boolean silent) {
        if (latLon == null) {
            return true;
        }

        if (DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG) {
            return true;
        }
        // if (Licence.activeLicence != null && Licence.activeLicence.isOverwritingRestrictions()) return true;
        if (!DependencyInjector.getInstance().getInstanceOf(IGeoFenceDetector.class).isGeoFencingRestrictionOn()) {
            return true;
        }

        if (!GeoFencing.isRestricted(latLon)) {
            return true;
        }

        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(latLon)) {
                if (c.isRestricted) {
                    if (!silent) {
                        showWarning(c);
                    }

                    return false;
                }
            }
        }

        if (!silent) {
            showWarning(null);
        }

        return false;
    }

    @Override
    public boolean allowProceed(Sector sector) {
        return allowProceed(sector, false);
    }

    @Override
    public boolean allowProceed(Sector sector, boolean silent) {
        if (sector == null) {
            return true;
        }

        if (DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG) {
            return true;
        }
        // if (Licence.activeLicence != null && Licence.activeLicence.isOverwritingRestrictions()) return true;
        if (!DependencyInjector.getInstance().getInstanceOf(IGeoFenceDetector.class).isGeoFencingRestrictionOn()) {
            return true;
        }

        boolean ok = !GeoFencing.isRestricted(sector.getCentroid());
        // System.out.println("center "+sector.getCentroid()+" restricted:"+ok);
        if (ok) {
            for (LatLon corner : sector.getCorners()) {
                if (GeoFencing.isRestricted(corner)) {
                    // System.out.println("this corner "+corner+" restricted " + corner);
                    ok = false;
                    break;
                    // } else {
                    // System.out.println("this corner "+corner+" UNrestricted " );
                }
            }
        }

        if (ok) {
            return true;
        }

        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(sector)) {
                if (c.isRestricted) {
                    if (!silent) {
                        showWarning(c);
                    }

                    return false;
                }
            }
        }

        if (!silent) {
            showWarning(null);
        }

        return false;
    }

    @Override
    public Country getFirstCountry(LatLon latLon) {
        if (latLon == null) {
            return null;
        }

        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(latLon)) {
                return c;
            }
        }

        return null;
    }

    @Override
    public Country getFirstCountry(Sector sector) {
        if (sector == null) {
            return null;
        }

        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(sector)) {
                return c;
            }
        }

        return null;
    }

    public List<Country> getAllCountries(LatLon latLon, double distance) {
        if (latLon == null) {
            return null;
        }

        ArrayList<Country> l = new ArrayList<Country>();
        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(latLon, distance)) {
                l.add(c);
            }
        }

        return l;
    }

    public List<Country> getAllCountries(LatLon latLon) {
        if (latLon == null) {
            return null;
        }

        ArrayList<Country> l = new ArrayList<Country>();
        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(latLon)) {
                l.add(c);
            }
        }

        return l;
    }

    public List<Country> getAllCountries(Sector sector) {
        if (sector == null) {
            return null;
        }

        ArrayList<Country> l = new ArrayList<Country>();
        for (Country c : countries.values()) {
            if (c.withinSafetyDistance(sector)) {
                l.add(c);
            }
        }

        return l;
    }

    public List<Country> mergeCountries() {
        LinkedList<Country> mergedList = new LinkedList<Country>();
        for (Country c : countries.values()) {
            boolean found = false;
            for (Country cMerged : mergedList) {
                if (cMerged.isSimilarRegulated(c) && cMerged.dataQuality == c.dataQuality) {
                    // System.out.println(c +" isSimTo " + cMerged);
                    // inject into that coutntry
                    for (ArrayList<LatLon> border : c.borders) {
                        cMerged.borders.add(border);
                    }

                    found = true;
                    break;
                }
            }

            if (found) {
                continue;
            }
            // System.out.println("new class " + c);
            mergedList.add(c.clone());
        }

        return mergedList;
    }

    public List<Country> splitCountries() {
        LinkedList<Country> splittedList = new LinkedList<Country>();
        for (Country c : countries.values()) {
            for (ArrayList<LatLon> border : c.borders) {
                Country cSplit = c.clone();
                cSplit.borders.clear();
                cSplit.borders.add(border);
                splittedList.add(cSplit);
            }
        }

        return splittedList;
    }

}
