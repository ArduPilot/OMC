/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.flightplan.CFMLWriter;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.flightplan.CFMLWriter;
import eu.mavinci.core.flightplan.CFlightplan;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class FMLWriter extends CFMLWriter {
    public String flightplanToASM(CFlightplan plan) throws IOException {
        ByteArrayOutputStream outOrg = new ByteArrayOutputStream();

        writeFlightplan(plan, outOrg);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // open up the xml document
            Document doc = null;
            DocumentBuilder docbuilder = factory.newDocumentBuilder();
            try (InputStream orgIn = new ByteArrayInputStream(outOrg.toByteArray())) {
                doc = docbuilder.parse(orgIn);
            }

            TransformerFactory transformfactory = TransformerFactory.newInstance();
            Source xsltSource = null;
            try (InputStream flightplanmlInputStream =
                ClassLoader.getSystemResourceAsStream("eu/mavinci/core/xml/flightplanml.xsl")) {
                xsltSource = new StreamSource(flightplanmlInputStream);
            }

            Templates xslTemplate = transformfactory.newTemplates(xsltSource);
            Transformer transformer = xslTemplate.newTransformer();

            Ensure.notNull(doc, "doc");
            // now transform
            transformer.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(out));

        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Cant transform mission to ASM code", e);
        }

        return out.toString();
    }
}
