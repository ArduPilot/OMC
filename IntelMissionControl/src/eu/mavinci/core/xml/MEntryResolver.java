/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MEntryResolver implements EntityResolver {

    public static final MEntryResolver resolver = new MEntryResolver();
    private InputStream is;

    private MEntryResolver() {}

    public InputSource resolveEntity(String publicId, String systemId) throws FileNotFoundException {
        if (systemId.startsWith("http://www.mavinci.eu/fml/")) {
            // return a local input source
            String newSystemId = systemId.replaceFirst("http://www.mavinci.eu/fml/", "eu/mavinci/core/xml/");
            this.is = ClassLoader.getSystemResourceAsStream(newSystemId);
            return new InputSource(this.is);
        } else if (systemId.startsWith("http://www.mavinci.eu/xml/")) {
            // return a local input source
            String newSystemId = systemId.replaceFirst("http://www.mavinci.eu/xml/", "eu/mavinci/core/xml/");
            this.is = ClassLoader.getSystemResourceAsStream(newSystemId);
            return new InputSource(this.is);
        } else {
            // use the default behaviour
            return null;
        }
    }

    public void closeResource() { // have using party close the stream
        if (this.is != null) {
            try {
                is.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
}
