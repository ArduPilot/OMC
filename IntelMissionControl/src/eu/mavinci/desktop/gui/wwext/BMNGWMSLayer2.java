/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016: restored this file from old sources
 *
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: BMNGWMSLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class BMNGWMSLayer2 extends WMSTiledImageLayer {
    public BMNGWMSLayer2() {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument() {
        return WWXML.openDocumentFile("config/Earth/BMNGWMSLayer2.xml", null);
    }
}
