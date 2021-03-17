/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import eu.mavinci.core.update.UpdateURL;
import java.util.TreeSet;

public enum Reseller {
    // should be always at the start of the list:

    ChooseReseller("choose", "_1", "choose your reseller company", null, true),
    NewReseller("add", "_2", "add new Reseller...", null, true),
    // KEY, Name, EMail, translate

    Topcon("topcon", "zz1", "Topcon", UpdateURL.ENTERPRISE_EMAIL, false), // "techsupport.eu@topcon.com"
    ;

    public final String name;
    public final String key;
    public final String email;
    public final String iso2;
    public final boolean toTranslate;

    private Reseller(String key, String iso2, String name, String email, boolean toTranslate) {
        this.name = name;
        this.iso2 = iso2;
        this.key = key;
        this.email = email;
        this.toTranslate = toTranslate;
    }

    public static Reseller getResellerByKey(String key) {
        for (Reseller r : Reseller.values()) {
            if (r.key.equals(key)) {
                return r;
            }
        }

        return null;
    }

    public static String getSupportEMailByKey(String key) {
        if (key == null) {
            return UpdateURL.ENTERPRISE_EMAIL;
        }

        key = key.toLowerCase();
        if (key.equals("mavinci.test") || key.equals("imc.dev.team")) {
            return UpdateURL.DEV_EMAIL_MAVINCI;
        }

        while (true) {
            Reseller r = getResellerByKey(key);
            if (r != null && r.isRealReseller() && r.email != null && !r.email.isEmpty()) {
                return r.email;
            }

            int pos = key.lastIndexOf(".");
            if (pos < 0) {
                return UpdateURL.ENTERPRISE_EMAIL;
            }

            key = key.substring(0, pos - 1);
        }
    }

    public boolean isRealReseller() {
        return !this.toTranslate;
    }

    public static void main(String[] args) {
        System.out.println("All support emails:\n");
        TreeSet<String> all = new TreeSet<>();
        for (Reseller r : Reseller.values()) {
            if (r.email == null) {
                continue;
            }

            all.add(r.email);
        }

        boolean first = true;
        for (String s : all) {
            if (!first) {
                System.out.print(" , ");
            }

            System.out.print(s);
            first = false;
        }
    }
}
