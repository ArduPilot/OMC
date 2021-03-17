/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import eu.mavinci.core.helper.CMathHelper;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.desktop.main.debug.Debug;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BmlXMLparser extends DefaultHandler {

    static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

    public Licence bml;
    public AllowedUser user;

    protected StringBuffer sbuf = new StringBuffer();

    public BmlXMLparser(Licence bml) {
        super();
        this.bml = bml;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // first clear the character buffer
        sbuf.delete(0, sbuf.length());

        if (qName.equalsIgnoreCase("user")) {
            user = new AllowedUser();
            bml.users.add(user);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        // fix defaults for old BMLs
        if (!bml.isFixwingUAVedition && !bml.isMannedEdition && !bml.isCopterUAVedition) {
            bml.isFixwingUAVedition = true;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String tmp = sbuf.toString();
        // System.out.println("end of " + qName + " buf"+tmp);

        if (qName.equalsIgnoreCase("type")) {
            bml.type = Integer.parseInt(tmp);
        } else if (qName.equalsIgnoreCase("reseller")) {
            bml.resellerID = tmp;
        } else if (qName.equalsIgnoreCase("number")) {
            if (tmp.length() > 0) {
                String[] parts = tmp.split(Pattern.quote(","));
                bml.numbers = new int[parts.length];
                for (int i = 0; i != parts.length; i++) {
                    bml.numbers[i] = Integer.parseInt(parts[i]);
                }
            }
        } else if (qName.equalsIgnoreCase("beta")) {
            bml.isBetaTesting = Boolean.parseBoolean(tmp);
        } else if (qName.equalsIgnoreCase("platform")) {
            String[] parts = tmp.split(Pattern.quote(","));
            bml.platform = new OsTypes[parts.length];
            for (int i = 0; i != parts.length; i++) {
                bml.platform[i] = OsTypes.valueOf(parts[i]);
            }
        } else if (qName.equalsIgnoreCase("maxUserlevel")) {
            try {
                int i = Integer.parseInt(tmp);
                i = CMathHelper.intoRange(i, 0, Licence.GuiLevels.values().length - 1);
                bml.maxUserlevel = Licence.GuiLevels.values()[i];
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "unknown maximal user level: " + tmp, e);
                bml.maxUserlevel = Licence.GuiLevels.EXPERT;
            }
        } else if (qName.equalsIgnoreCase("hardwareId")) {
            bml.licenceId = tmp;
        } else if (qName.equalsIgnoreCase("branch")) {
            bml.branch = tmp;
        } else if (qName.equalsIgnoreCase("lang")) {
            bml.language = new Locale(tmp.toLowerCase());
        } else if (qName.equalsIgnoreCase("company")) {
            bml.company = tmp;
        } else if (qName.equalsIgnoreCase("initDate")) {
            try {
                bml.initDate = df.parse(tmp);
            } catch (ParseException e) {
                Debug.getLog().log(Level.WARNING, "could not parse init date", e);
            }
        } else if (qName.equalsIgnoreCase("expire")) {
            try {
                bml.expireingDate = df.parse(tmp);
            } catch (ParseException e) {
                Debug.getLog().log(Level.WARNING, "could not parse expireing date", e);
            }
        } else if (qName.equalsIgnoreCase("servicePayedUntil")) {
            try {
                bml.servicePayedUntil = df.parse(tmp);
            } catch (ParseException e) {
                Debug.getLog().log(Level.WARNING, "could not parse servicePayedUntil date", e);
            }
        } else if (qName.equalsIgnoreCase("default_cam")) {
            bml.defaultCam = tmp;
        } else if (qName.equalsIgnoreCase("SEEN_1")) {
            bml.SEEN_1 = tmp;
        } else if (qName.equalsIgnoreCase("SEEN_0")) {
            bml.SEEN_0 = tmp;
        } else if (qName.equalsIgnoreCase("user")) {
            user = null;
        } else if (qName.equalsIgnoreCase("bml")) {
            // nothing to do
        } else if (qName.equalsIgnoreCase("macs")) {
            startUserIfNessesary();
            user.macAddresses = new Vector<String>();
            for (String s : tmp.split(",|\\n")) {
                user.macAddresses.add(s.toUpperCase().trim());
            }
        } else if (qName.equalsIgnoreCase("displayName")) {
            startUserIfNessesary();
            user.displayName = tmp;
        } else if (qName.equalsIgnoreCase("email")) {
            startUserIfNessesary();
            user.email = tmp;
            return;
        } else if (qName.equalsIgnoreCase("name")) {
            startUserIfNessesary();
            user.name = tmp;
        } else if (qName.equalsIgnoreCase("MannedEdition")) {
            bml.isMannedEdition = true;
        } else if (qName.equalsIgnoreCase("FixwingUAVedition")) {
            bml.isFixwingUAVedition = true;
        } else if (qName.equalsIgnoreCase("CopterUAVedition")) {
            bml.isCopterUAVedition = true;
        } else if (qName.equalsIgnoreCase("FalconEdition")) {
            bml.setIsFalconEdition(Boolean.parseBoolean(tmp));
        } else if (qName.equalsIgnoreCase("GrayHawkEdition")) {
            bml.setIsGrayHawkEdition(Boolean.parseBoolean(tmp));
        } else if (qName.equalsIgnoreCase("DJIEdition")) {
            bml.setIsDJIEdition(Boolean.parseBoolean(tmp));
        } else if (qName.equalsIgnoreCase("PCname")) {
            startUserIfNessesary();
            user.pcName = tmp;
        } else {
            // DONT store everything also in the key value stores!!
            // this makes information redundant, and its not clear which value is the true one,especially when writing
            // changed BML
            qName = qName.toUpperCase(); // make stuff case insensitive
            if (user == null) {
                bml.setProperty(qName, tmp);
            } else {
                user.setProperty(qName, tmp);
            }
        }
    }

    void startUserIfNessesary() {
        if (user != null) {
            return;
        }

        user = new AllowedUser();
        bml.users.add(user);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        sbuf.append(ch, start, length);
    }

}
