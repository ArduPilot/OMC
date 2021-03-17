/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;

import com.google.common.base.Charsets;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.sun.javafx.iio.ImageFormatDescription;
import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.common.ImageLoaderImpl;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javafx.scene.paint.Color;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("restriction")
public class SvgImageLoader extends ImageLoaderImpl {

    private static class XmlElement {
        private String nodeName;
        private boolean shortNode;
        private Map<String, String> attributes = new HashMap<>();

        public XmlElement(String element) {
            int currentPos = 0;
            while (Character.isWhitespace(element.charAt(currentPos))) {
                ++currentPos;
            }

            if (element.charAt(currentPos++) != '<') {
                throw new IllegalArgumentException("Invalid XML element.");
            }

            while (Character.isWhitespace(element.charAt(currentPos))) {
                ++currentPos;
            }

            int nodeStartPos = currentPos;
            while (Character.isLetter(element.charAt(currentPos))) {
                ++currentPos;
            }

            nodeName = element.substring(nodeStartPos, currentPos);

            boolean endBracketFound = false;
            for (int i = element.length() - 1; i > 0; --i) {
                if (Character.isWhitespace(element.charAt(i))) {
                    continue;
                }

                if (!endBracketFound) {
                    if (element.charAt(i) == '>') {
                        endBracketFound = true;
                    }
                } else {
                    if (element.charAt(i) == '/') {
                        shortNode = true;
                    }

                    break;
                }
            }

            while (currentPos < element.length()) {
                int equalsSignPos = element.indexOf("=", currentPos);
                if (equalsSignPos == -1) {
                    return;
                }

                int endPos = equalsSignPos - 1;
                while (Character.isWhitespace(element.charAt(endPos))) {
                    --endPos;
                }

                int beginPos = endPos;
                while (!Character.isWhitespace(element.charAt(beginPos))) {
                    --beginPos;
                }

                ++beginPos;
                String key = element.substring(beginPos, endPos + 1);

                beginPos = equalsSignPos + 1;
                while (element.charAt(beginPos) != '"') {
                    ++beginPos;
                }

                ++beginPos;
                endPos = beginPos;
                while (element.charAt(endPos) != '"') {
                    ++endPos;
                }

                --endPos;
                String value = element.substring(beginPos, endPos + 1);
                attributes.put(key, value);

                currentPos = endPos;
            }
        }

        public boolean hasAttribute(String key) {
            return attributes.containsKey(key);
        }

        public String getAttribute(String key) {
            return attributes.get(key);
        }

        public void setAttribute(String key, String value) {
            attributes.put(key, value);
        }

        public void removeAttribute(String key) {
            attributes.remove(key);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('<');
            stringBuilder.append(nodeName);

            for (Entry<String, String> entry : attributes.entrySet()) {
                stringBuilder.append(' ');
                stringBuilder.append(entry.getKey());
                stringBuilder.append('=');
                stringBuilder.append('"');
                stringBuilder.append(entry.getValue());
                stringBuilder.append('"');
            }

            if (shortNode) {
                stringBuilder.append('/');
            }

            stringBuilder.append('>');
            stringBuilder.append('\n');
            return stringBuilder.toString();
        }
    }

    private static final int BYTES_PER_PIXEL = 4; // RGBA

    private InputStream input;
    private int fileWidth;
    private int fileHeight;

    SvgImageLoader(ImageFormatDescription formatDescription) {
        super(formatDescription);
    }

    SvgImageLoader(InputStream input, ColorCache colorCache) throws IOException, IllegalArgumentException {
        this(SvgDescriptor.getInstance());
        Expect.notNull(input, "input");
        Expect.notNull(colorCache, "colorCache");

        String fillStyle = null;
        String strokeStyle = null;

        final var annotations =
            ((AnnotatedSvgClassLoader)Thread.currentThread().getContextClassLoader()).getCurrentAnnotations();
        for (var annotation : annotations) {
            if ("fill".equals(annotation.getKey())) {
                fillStyle = annotation.getValue();
            } else if ("stroke".equals(annotation.getKey())) {
                strokeStyle = annotation.getValue();
            }
        }

        initialize(input, colorCache, fillStyle, strokeStyle);
    }

    protected void initialize(
            @UnderInitialization SvgImageLoader this,
            InputStream input,
            ColorCache colorCache,
            @Nullable String fillStyle,
            @Nullable String strokeStyle)
            throws IOException, IllegalArgumentException {
        String document = getStringFromStream(input);
        int beginNode = document.indexOf("<svg");
        if (beginNode < 0) {
            throw new IllegalArgumentException("SVG document contains no root element of type <svg>.");
        }

        int endNode = document.indexOf(">", beginNode) + 1;
        XmlElement rootNode = new XmlElement(document.substring(beginNode, endNode));

        if (rootNode.hasAttribute("width")) {
            String widthString = rootNode.getAttribute("width").replaceFirst("px", "");
            this.fileWidth = Integer.parseInt(widthString);
        } else {
            throw new IllegalArgumentException("SVG file contains no valid width attribute.");
        }

        if (rootNode.hasAttribute("height")) {
            String heightString = rootNode.getAttribute("height").replaceFirst("px", "");
            this.fileHeight = Integer.parseInt(heightString);
        } else {
            throw new IllegalArgumentException("SVG file contains no valid height attribute.");
        }

        StringBuilder documentBuilder = new StringBuilder();
        documentBuilder.append(document.substring(0, beginNode));
        documentBuilder.append(rootNode.toString());

        while (true) {
            beginNode = document.indexOf("<", endNode);
            if (beginNode < 0) {
                break;
            }

            int tempBeginNode = beginNode + 1;
            while (Character.isWhitespace(document.charAt(tempBeginNode))) {
                ++tempBeginNode;
            }

            if (document.charAt(tempBeginNode++) == '/') {
                endNode = document.indexOf(">", tempBeginNode);
                documentBuilder.append(document.substring(beginNode, endNode + 1));
                continue;
            }

            endNode = document.indexOf(">", beginNode);
            if (endNode < 0) {
                break;
            }

            XmlElement node = new XmlElement(document.substring(beginNode, endNode + 1));
            resolveStyles(colorCache, node, fillStyle, strokeStyle);

            documentBuilder.append(node.toString());
            documentBuilder.append('\n');
        }

        this.input = new ByteArrayInputStream(documentBuilder.toString().getBytes(Charsets.UTF_8));
    }

    private String formatColor(Color color) {
        return String.format(
            "#%02X%02X%02X", (int)(color.getRed() * 255), (int)(color.getGreen() * 255), (int)(color.getBlue() * 255));
    }

    private void resolveStyles(
            ColorCache colorCache, XmlElement element, @Nullable String fillStyle, @Nullable String strokeStyle) {
        String style = element.getAttribute("style");
        if (style == null && fillStyle == null && strokeStyle == null) {
            return;
        }

        if (fillStyle != null) {
            element.setAttribute("fill", formatColor(colorCache.get(fillStyle.trim())));
        }

        if (strokeStyle != null) {
            element.setAttribute("stroke", formatColor(colorCache.get(strokeStyle.trim())));
        }

        if (style != null) {
            String[] properties = style.split(";");
            for (final String property : properties) {
                String[] kvp = property.split(":");
                if (kvp.length == 2) {
                    if (kvp[0].trim().equals("fill")) {
                        element.setAttribute("fill", formatColor(colorCache.get(kvp[1].trim())));
                    } else if (kvp[0].trim().equals("stroke")) {
                        element.setAttribute("stroke", formatColor(colorCache.get(kvp[1].trim())));
                    }
                }
            }

            element.removeAttribute("style");
        }
    }

    private String getStringFromStream(InputStream stream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder stringBuilder = new StringBuilder();

        Reader reader = new InputStreamReader(stream, "UTF-8");
        int bytesRead = 0;
        do {
            bytesRead = reader.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                stringBuilder.append(buffer, 0, bytesRead);
            }
        } while (bytesRead > 0);

        return stringBuilder.toString();
    }

    @Override
    @SuppressWarnings("nullness")
    public ImageFrame load(int imageIndex, int width, int height, boolean preserveAspectRatio, boolean smooth)
            throws IOException {
        if (0 != imageIndex) {
            return null;
        }

        if (width == 0) {
            width = this.fileWidth;
        }

        if (height == 0) {
            height = this.fileHeight;
        }

        try {
            return createImageFrame(width, height, (float)ScaleHelper.getScaleFactor());
        } catch (TranscoderException ex) {
            throw new IOException(ex);
        }
    }

    @SuppressWarnings("nullness")
    private ImageFrame createImageFrame(int width, int height, float pixelScale) throws TranscoderException {
        BufferedImage bufferedImage = getTranscodedImage(width * pixelScale, height * pixelScale);
        ByteBuffer imageData = getImageData(bufferedImage);

        return new FixedPixelDensityImageFrame(
            ImageStorage.ImageType.RGBA,
            imageData,
            bufferedImage.getWidth(),
            bufferedImage.getHeight(),
            getStride(bufferedImage),
            null,
            pixelScale,
            null);
    }

    @SuppressWarnings("nullness")
    private BufferedImage getTranscodedImage(float width, float height) throws TranscoderException {
        BufferedImageTranscoder trans = new BufferedImageTranscoder(BufferedImage.TYPE_INT_ARGB);

        if (width != 0) {
            trans.addTranscodingHint(KEY_WIDTH, width);
        }

        if (height != 0) {
            trans.addTranscodingHint(KEY_HEIGHT, height);
        }

        trans.transcode(new TranscoderInput(this.input), null);
        return trans.getBufferedImage();
    }

    private int getStride(BufferedImage bufferedImage) {
        return bufferedImage.getWidth() * BYTES_PER_PIXEL;
    }

    @SuppressWarnings("nullness")
    private ByteBuffer getImageData(BufferedImage bufferedImage) {
        int[] rgb =
            bufferedImage.getRGB(
                0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
        byte[] imageData = new byte[getStride(bufferedImage) * bufferedImage.getHeight()];
        copyColorToBytes(rgb, imageData);
        return ByteBuffer.wrap(imageData);
    }

    private void copyColorToBytes(int[] rgb, byte[] imageData) {
        if (rgb.length * BYTES_PER_PIXEL != imageData.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);

        for (int i = 0; i < rgb.length; i++) {
            byte[] bytes = byteBuffer.putInt(rgb[i]).array();

            int dataOffset = BYTES_PER_PIXEL * i;
            imageData[dataOffset] = bytes[1];
            imageData[dataOffset + 1] = bytes[2];
            imageData[dataOffset + 2] = bytes[3];
            imageData[dataOffset + 3] = bytes[0];

            byteBuffer.clear();
        }
    }

    @Override
    public void dispose() {
        try {
            input.close();
        } catch (IOException e) {
            // expected
        }
    }

}
