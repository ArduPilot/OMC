/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.FMLWriter;
import eu.mavinci.flightplan.Flightplan;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

public class DefaultFlightplanExporter implements IFlightplanExporter {

    private String xsl;
    private MFileFilter fileFilter;

    protected DefaultFlightplanExporter(String xsl, MFileFilter fileFilter) {
        this.xsl = xsl;
        this.fileFilter = fileFilter;
    }

    @Override
    public void export(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        if (target == null) {
            throw new IllegalArgumentException();
        }

        File newTarget = FileHelper.validateFileName(target, fileFilter);

        try (PrintStream out = new PrintStream(newTarget, "UTF-8");
            ByteArrayOutputStream outOrg = new ByteArrayOutputStream()) {
            FMLWriter writer = new FMLWriter();
            writer.writeFlightplan(flightplan, outOrg);
            InputStream orgIn = new ByteArrayInputStream(outOrg.toByteArray());

            try (InputStream inputXsl = ClassLoader.getSystemResourceAsStream(xsl)) {
                Source xsltSource = new StreamSource(inputXsl);
                DocumentBuilder docbuilder = createDocumentBuilder();
                Document doc = docbuilder.parse(orgIn);

                Transformer transformer = createTransformer(xsltSource);

                // now transform
                transformer.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(out));
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "DefaultFlightPlan xsl ", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Transformer createTransformer(Source xsltSource) throws TransformerConfigurationException {
        TransformerFactory transformfactory = TransformerFactory.newInstance();
        transformfactory.setURIResolver(new ClasspathResourceURIResolver());
        Templates xslTemplate = transformfactory.newTemplates(xsltSource);
        return xslTemplate.newTransformer();
    }

    private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // open up the xml document
        return factory.newDocumentBuilder();
    }

    private class ClasspathResourceURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            return new StreamSource(ClassLoader.getSystemResourceAsStream(href));
        }
    }

}
