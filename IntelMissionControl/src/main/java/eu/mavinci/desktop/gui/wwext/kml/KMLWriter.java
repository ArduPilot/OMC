/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.kml;

import com.intel.missioncontrol.StaticInjector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Position.PositionList;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLAbstractLatLonBoxType;
import gov.nasa.worldwind.ogc.kml.KMLAbstractStyleSelector;
import gov.nasa.worldwind.ogc.kml.KMLAbstractSubStyle;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLData;
import gov.nasa.worldwind.ogc.kml.KMLDocument;
import gov.nasa.worldwind.ogc.kml.KMLExtendedData;
import gov.nasa.worldwind.ogc.kml.KMLFolder;
import gov.nasa.worldwind.ogc.kml.KMLGroundOverlay;
import gov.nasa.worldwind.ogc.kml.KMLIcon;
import gov.nasa.worldwind.ogc.kml.KMLLookAt;
import gov.nasa.worldwind.ogc.kml.KMLPair;
import gov.nasa.worldwind.ogc.kml.KMLPhotoOverlay;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.KMLScreenOverlay;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.ogc.kml.KMLStyleMap;
import gov.nasa.worldwind.ogc.kml.KMLStyleUrl;
import gov.nasa.worldwind.ogc.kml.KMLVec2;
import gov.nasa.worldwind.ogc.kml.gx.GXConstants;
import gov.nasa.worldwind.ogc.kml.gx.GXLatLongQuad;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class KMLWriter {

    InsertableIntendingXMLStreamWriter writer;

    /**
     * Create a KML document using an OutputStream.
     *
     * @param stream Stream to receive KML output.
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    public KMLWriter(OutputStream stream) throws XMLStreamException {
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        writer =
            new InsertableIntendingXMLStreamWriter(xmlof.createXMLStreamWriter(stream)) {

                public void writeFileds(AVList fields) throws XMLStreamException {
                    if (fields == null) {
                        return;
                    }

                    for (Entry<String, Object> e : fields.getEntries()) {
                        Object value = e.getValue();
                        if (e.getKey().equals("name") && dontWriteName) {
                            continue;
                        }

                        if (value instanceof String) {
                            writeStartElement(e.getKey());
                            writeCharacters(value.toString());
                            writeEndElement();
                        } else if (value instanceof Boolean) {
                            boolean b = (Boolean)value;
                            writeStartElement(e.getKey());
                            writeCharacters(KMLExportUtil.kmlBoolean(b));
                            writeEndElement();
                        } else if (value instanceof Number) {
                            Number n = (Number)value;
                            writeStartElement(e.getKey());
                            writeCharacters(n.toString());
                            writeEndElement();
                        } else if (value instanceof KMLStyleUrl) {
                            KMLStyleUrl style = (KMLStyleUrl)value;
                            writeStartElement(e.getKey());
                            writeCharacters(style.getCharacters());
                            writeEndElement();
                        } else if (value instanceof KMLIcon) {
                            KMLIcon icon = (KMLIcon)value;
                            writeStartElement("Icon");
                            writeFileds(icon.getFields());
                            writeEndElement();
                        } else if (value instanceof KMLLookAt) {
                            KMLLookAt lookAt = (KMLLookAt)value;
                            writeStartElement("LookAt");
                            writeFileds(lookAt.getFields());
                            writeEndElement();
                        } else if (value instanceof KMLVec2) {
                            KMLVec2 vec = (KMLVec2)value;
                            writeEmptyElement(e.getKey());
                            writeAttribute("x", vec.getX().toString());
                            writeAttribute("y", vec.getY().toString());
                            writeAttribute("xunits", vec.getXunits());
                            writeAttribute("yunits", vec.getYunits());
                        } else if (value instanceof KMLAbstractLatLonBoxType) {
                            KMLAbstractLatLonBoxType box = (KMLAbstractLatLonBoxType)value;
                            writeStartElement(e.getKey());
                            writeFileds(box.getFields());
                            writeEndElement();
                        } else if (value instanceof GXLatLongQuad) {
                            GXLatLongQuad latLonQuad = (GXLatLongQuad)value;
                            writeStartElement("gx:LatLonQuad");
                            writeFileds(latLonQuad.getFields());
                            writeEndElement();
                        } else if (value instanceof PositionList) {
                            PositionList posList = (PositionList)value;
                            writeStartElement(e.getKey());
                            boolean first = true;
                            for (Position p : posList.list) {
                                if (!first) {
                                    writeCharacters(" ");
                                } else {
                                    first = false;
                                }

                                writeCharacters(p.getLongitude().degrees + "," + p.getLatitude().degrees);
                            }

                            writeEndElement();
                        } else if (value instanceof KMLExtendedData) {
                            writeStartElement(e.getKey());
                            for (KMLData data : ((KMLExtendedData)value).getData()) {
                                writeStartElement("Data");
                                writeAttribute("name", data.getName());
                                if (data.getDisplayName() != null) {
                                    writeStartElement("displayName");
                                    writeCharacters(data.getDisplayName());
                                    writeEndElement();
                                }

                                writeStartElement("value");
                                writeCharacters(data.getValue());
                                writeEndElement();

                                writeEndElement();
                            }

                            writeEndElement();
                        } else if (value instanceof AbstractXMLEventParser) {
                            AbstractXMLEventParser astract = (AbstractXMLEventParser)value;
                            Debug.getLog()
                                .log(
                                    Debug.WARNING,
                                    "unsupported value type - trying workaround!:" + value + "  " + value.getClass());
                            writeStartElement(e.getKey());
                            writeFileds(astract.getFields());
                            writeEndElement();
                        } else {
                            Debug.getLog()
                                .log(Debug.WARNING, "unsupported value type:" + value + "  " + value.getClass());
                        }
                    }
                }

            };
        this.startDocument();
    }

    public void writeStyles(List<KMLAbstractStyleSelector> styleSelectors) throws XMLStreamException {
        if (styleSelectors == null) {
            return;
        }

        for (KMLAbstractStyleSelector s : styleSelectors) {
            if (s instanceof KMLStyle) {
                KMLStyle style = (KMLStyle)s;
                writer.writeStartElement("Style");
                if (style.getId() != null) {
                    writer.writeAttribute("id", style.getId());
                }

                for (String key : allStyleKeys) {
                    Object o = style.getField(key);
                    if (o instanceof KMLAbstractSubStyle) {
                        KMLAbstractSubStyle styleElem = (KMLAbstractSubStyle)o;
                        writer.writeStartElement(key);
                        writer.writeFileds(styleElem.getFields());
                        writer.writeEndElement();
                    } else if (o != null) {
                        Debug.getLog()
                            .log(
                                Debug.WARNING,
                                "Could not store unsupported KML Style Element " + o + "  " + o.getClass());
                    }
                }

                writer.writeEndElement();
            } else if (s instanceof KMLStyleMap) {
                KMLStyleMap styleMap = (KMLStyleMap)s;
                writer.writeStartElement("StyleMap");
                writer.writeAttribute("id", styleMap.getId());
                for (KMLPair p : styleMap.getPairs()) {
                    writer.writeStartElement("Pair");
                    writer.writeFileds(p.getFields());
                    writer.writeEndElement();
                }

                writer.writeEndElement();
            } else {
                Debug.getLog()
                    .log(Debug.WARNING, "Could not store unsupported KML Style Element " + s + "  " + s.getClass());
            }
        }
    }

    /**
     * Start the KML document and write namespace declarations.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    protected void startDocument() throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeCharacters("\r\n");
        writer.writeComment(StaticInjector.getInstance(ILicenceManager.class).getExportHeaderCore());
        writer.writeStartElement("kml");
        writer.writeDefaultNamespace(KMLConstants.KML_NAMESPACE);
        writer.setPrefix("gx", GXConstants.GX_NAMESPACE);
        writer.writeNamespace("gx", GXConstants.GX_NAMESPACE);
        // writer.setPrefix("mavinci", MAVinciKMLextension);
        // writer.writeNamespace("mavinci", MAVinciKMLextension);

    }

    public static final String MAVinciKMLextension = "http://www.mavinci.de/xml";

    /**
     * End the KML document.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    protected void endDocument() throws XMLStreamException {

        // writer.writeStartElement(writtenByTag);
        // writer.writeCharacters(GlobalSettings.getHumanReadableVersion());
        // writer.writeEndElement();

        writer.writeEndElement(); // kml
        writer.writeEndDocument();

        writer.close();
    }

    /**
     * Close the document builder.
     *
     * @throws XMLStreamException If an error is encountered while writing KML.
     */
    public void close() throws XMLStreamException {
        this.endDocument();
        writer.close();
    }

    /**
     * Write an {@link Exportable} object to the document. If the object does not support export in KML format, it will
     * be ignored.
     *
     * @param exportable Object to export in KML.
     * @throws IOException If an error is encountered while writing KML.
     */
    public void writeObject(Exportable exportable) throws IOException {
        String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
        if (Exportable.FORMAT_SUPPORTED.equals(supported) || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported)) {
            exportable.export(KMLConstants.KML_MIME_TYPE, writer);
        }
    }

    /**
     * Write a list of {@link Exportable} objects to the document. If any objects do not support export in KML format,
     * they will be ignored.
     *
     * @param exportables List of objects to export in KML.
     * @throws IOException If an error is encountered while writing KML.
     */
    public void writeObjects(Exportable... exportables) throws IOException {
        for (Exportable exportable : exportables) {
            String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
            if (Exportable.FORMAT_SUPPORTED.equals(supported)
                    || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported)) {
                exportable.export(KMLConstants.KML_MIME_TYPE, writer);
            }
        }
    }

    public static final String[] allStyleKeys =
        new String[] {
            KMLConstants.BALOON_STYLE_FIELD,
            KMLConstants.ICON_STYLE_FIELD,
            KMLConstants.LABEL_STYLE_FIELD,
            KMLConstants.LINE_STYLE_FIELD,
            KMLConstants.LIST_STYLE_FIELD,
            KMLConstants.POLY_STYLE_FIELD
        };

    public static final String writtenByTag = "WrittenBy";

    public void writeToXML(KMLRoot root)
            throws XMLStreamException, IOException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, NoSuchMethodException, SecurityException {
        // root.setField(KMLWriter.writtenByTag,GlobalSettings.getHumanReadableVersion());
        writeToXML(root.getFeature());
        close();
    }

    @Deprecated
    public void writeToXML(KMLAbstractFeature curObj)
            throws XMLStreamException, IOException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, NoSuchMethodException, SecurityException {
        if (curObj instanceof KMLAbstractContainer) {
            if (curObj instanceof KMLDocument) {
                KMLDocument doc = (KMLDocument)curObj;
                MKMLHelper.setExtendedDataByName(
                    doc, writtenByTag, StaticInjector.getInstance(ILicenceManager.class).getExportHeaderCore());
                writer.writeStartElement("Document");
            } else if (curObj instanceof KMLFolder) {
                // KMLFolder folder = (KMLFolder) curObj;
                writer.writeStartElement("Folder");
            } else {
                // unsupported!
                Debug.getLog()
                    .log(Debug.WARNING, "Could not store unsupported KML Element " + curObj + "  " + curObj.getClass());
                return;
            }

            writer.writeFileds(curObj.getFields());
            writeStyles(curObj.getStyleSelectors());

            KMLAbstractContainer cont = (KMLAbstractContainer)curObj;

            for (KMLAbstractFeature f : cont.getFeatures()) {
                writeToXML(f);
            }

            writer.writeEndElement();
        } else if (curObj instanceof KMLPlacemark) {
            KMLPlacemark place = (KMLPlacemark)curObj;

            writer.writeFieldsAtNextLevel(place.getFields());

            List<KMLRenderable> renderables = place.getRenderables();
            if (renderables == null) {
                MKMLHelper.initGeometry(place);
                renderables = place.getRenderables();
            }

            if (renderables != null) {
                for (KMLRenderable r : renderables) {
                    if (r instanceof Exportable) {
                        Exportable exportable = (Exportable)r;
                        writeObject(exportable);
                    }
                }
            }

            writer.writeFieldsAtNextLevelDisable();
        } else if (curObj instanceof KMLGroundOverlay) {
            // KMLGroundOverlay groundOverlay = (KMLGroundOverlay) curObj;
            // System.out.println("unsupported KMLGroundOverlay:"+groundOverlay);
            writer.writeStartElement("GroundOverlay");
            writer.writeFileds(curObj.getFields());
            writer.writeEndElement();
        } else if (curObj instanceof KMLPhotoOverlay) {
            // KMLPhotoOverlay photoOverlay = (KMLPhotoOverlay) curObj;
            // System.out.println("unsupported KMLPhotoOverlay:"+photoOverlay);
            writer.writeStartElement("PhotoOverlay");
            writer.writeFileds(curObj.getFields());
            writer.writeEndElement();
        } else if (curObj instanceof KMLScreenOverlay) {
            // KMLScreenOverlay screenOverlay = (KMLScreenOverlay) curObj;
            // System.out.println("unsupported KMLScreenOverlay:"+screenOverlay);
            writer.writeStartElement("ScreenOverlay");
            writer.writeFileds(curObj.getFields());
            writer.writeEndElement();
        } else {
            // unsupported type
            Debug.getLog()
                .log(Debug.WARNING, "Could not store unsupported KML Element " + curObj + "  " + curObj.getClass());
            return;
        }
    }

}
