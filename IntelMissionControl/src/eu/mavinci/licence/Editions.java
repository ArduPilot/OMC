/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.licence;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import java.util.List;

/**
 * List of supported Editions, i.e., flying types for which a license is available. Current Editions are
 *
 * <p>
 *
 * <ul>
 *   <li>FixedWingUAV
 *   <li>CopterUAV
 *   <li>Manned
 * </ul>
 *
 * <p>Editions can be set visible by changing the content of {@link #isVisible()}
 */
public enum Editions {
    FixedWingUAV("Fixed Wing UAV Edition"),
    CopterUAV("Copter UAV Edition"),
    Manned("Manned Edition");

    String description;

    Editions(String description) {
        this.description = description;
    }

    /**
     * Check if the Edition matches the activeLicense in `Licence`
     *
     * @return true if active Licence matches edition
     */
    public boolean isEnabled() {
        Licence licence = DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getActiveLicence();
        if (this == Manned) {
            if (licence != null) {
                return licence.isMannedEdition();
            } else {
                return false;
            }
        } else if (this == CopterUAV) {
            if (licence != null) {
                return licence.isCopterUAVedition();
            } else {
                return false;
            }
        } else if (this == FixedWingUAV) {
            if (licence != null) {
                return licence.isFixwingUAVedition();
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * get list of enabled Editions
     *
     * @return Editions[] list of enabled editions
     */
    public static Editions[] getEnabledEditions() {
        Editions[] en = new Editions[getEnabledSize()];
        int j = 0;
        for (int i = 0; i < Editions.values().length; i++) {
            if (Editions.values()[i].isEnabled() && Editions.values()[i].isVisible()) {
                en[j] = Editions.values()[i];
                j++;
            }
        }

        return en;
    }

    /**
     * get list of visible Editions
     *
     * @return Editions[] list of visible editions
     */
    public static Editions[] getEditions() {
        Editions[] en = new Editions[getSize()];
        int j = 0;
        for (int i = 0; i < Editions.values().length; i++) {
            if (Editions.values()[i].isVisible()) {
                en[j] = Editions.values()[i];
                j++;
            }
        }

        return en;
    }

    private boolean isVisible() {
        // if(this==CopterUAV
        // && !Application.isEclipseLaunched()
        // ) return false;
        return true;
    }

    /** @return Number of Editions */
    public static int getSize() {
        int i = 0;
        for (int j = 0; j < Editions.values().length; j++) {
            if (Editions.values()[j].isVisible()) {
                i++;
            }
        }

        return i;
    }

    /**
     * get number of Editions that are enabled and are visible {@link #isVisible()}
     *
     * @return number of enabled Editions
     */
    public static int getEnabledSize() {
        int i = 0;
        for (int j = 0; j < Editions.values().length; j++) {
            if (Editions.values()[j].isEnabled() && Editions.values()[j].isVisible()) {
                i++;
            }
        }

        return i;
    }

    /**
     * get indices of enabled Editions
     *
     * @return int[] list of indices of enabled editions
     */
    public static int[] getEnabled() {
        int[] en = new int[getEnabledSize()];
        int j = 0;
        for (int i = 0; i < Editions.values().length; i++) {
            if (Editions.values()[i].isEnabled() && Editions.values()[i].isVisible()) {
                en[j] = i;
                j++;
            }
        }

        return en;
    }

    /**
     * get indices of enabled Editions, `editions`
     *
     * @param editions
     * @return int[] list of indices of enabled editions
     */
    public static int[] getEnabled(Editions[] editions) {
        int[] en = new int[getEnabledSize()];
        int j = 0;
        for (int i = 0; i < editions.length; i++) {
            if (editions[i].isEnabled() && editions[i].isVisible()) {
                en[j] = i;
                j++;
            }
        }

        return en;
    }
    // public String toStringI18N(){
    // return CLanguage.getString("Licence."+this.toString());
    // }

    public String toString() {
        return description;
    }

    public String value() {
        return super.toString();
    }

    /**
     * Update Licence
     *
     * @param selectedValuesList List of Editions to be added to Licence
     * @param licenceToSave Licence where Editions should be added to
     */
    public static void updateLicence(List selectedValuesList, Licence licenceToSave) {
        if (selectedValuesList.contains(Manned)) {
            licenceToSave.setIsMannedEdition(true);
        } else {
            licenceToSave.setIsMannedEdition(false);
        }

        if (selectedValuesList.contains(CopterUAV)) {
            licenceToSave.setIsCopterUAVedition(true);
        } else {
            licenceToSave.setIsCopterUAVedition(false);
        }

        if (selectedValuesList.contains(FixedWingUAV)) {
            licenceToSave.setIsFixwingUAVedition(true);
        } else {
            licenceToSave.setIsFixwingUAVedition(false);
        }
    }

}
