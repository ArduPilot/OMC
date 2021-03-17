/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.annotation;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.sidepane.flight.AlertLevel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.util.ImageAnnotation;
import java.awt.Color;
import java.awt.Font;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ImageAndLabelAnnotation extends ScreenAnnotation {

    private String id;

    private ImageAnnotation imageAnnotation;
    private Annotation leftAnnotation;
    private Annotation rightAnnotation;

    private Font valueFontNormal;
    private Font valueFontBold;

    private final Property<AlertLevel> alertProperty = new SimpleObjectProperty<>(AlertLevel.GREEN);

    public ImageAndLabelAnnotation() {
        super("", new java.awt.Point());

        initAnnotation();
    }

    public ImageAndLabelAnnotation(String id) {
        this();

        this.id = id;
    }

    private void initAnnotation() {
        setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 1, 5));
        setPickEnabled(false);

        AnnotationAttributes attributes = createAttributes();
        getAttributes().setDefaults(attributes);

        buildAnnotation();

        alertProperty.addListener((observable, oldValue, newValue) -> setAlert(newValue));
    }

    private void buildAnnotation() {
        removeAllChildren();

        imageAnnotation = createImageAnnotation();
        leftAnnotation = createScreenAnnotation();
        rightAnnotation = createScreenAnnotation();

        valueFontNormal =
            new Font(rightAnnotation.getAttributes().getFont().getName(), Font.PLAIN, (int)ScaleHelper.emsToPixels(1));
        valueFontBold = new Font(valueFontNormal.getName(), Font.BOLD, valueFontNormal.getSize());

        leftAnnotation.getAttributes().setFont(valueFontNormal);
        rightAnnotation.getAttributes().setFont(valueFontBold);

        addChild(imageAnnotation);
        addChild(leftAnnotation);
        addChild(rightAnnotation);
    }

    private ImageAnnotation createImageAnnotation() {
        ImageAnnotation imageAnnotation = new ImageAnnotation();

        imageAnnotation.setFitSizeToImage(false);
        imageAnnotation.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT_OF_CENTER, 4, 4));
        imageAnnotation.setUseImageAspectRatio(true);
        imageAnnotation.setPickEnabled(false);
        imageAnnotation.getAttributes().setSize(ScaleHelper.scaleDimension(new java.awt.Dimension(16, 16)));
        imageAnnotation.getAttributes().setInsets(new java.awt.Insets(0, 0, 0, 0));

        return imageAnnotation;
    }

    private Annotation createScreenAnnotation() {
        Annotation annotation = new ScreenAnnotation("", new java.awt.Point());
        annotation.setPickEnabled(false);
        annotation.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.LEFT, 5, 5));

        AnnotationAttributes attributes = createAttributes();
        annotation.getAttributes().setDefaults(attributes);
        annotation.getAttributes().setSize(new java.awt.Dimension(0, 0));
        annotation.getAttributes().setInsets(new java.awt.Insets(1, 5, 1, 1));

        return annotation;
    }

    private AnnotationAttributes createAttributes() {
        AnnotationAttributes attributes = new AnnotationAttributes();

        // TODO need design
        // Color transparentBlack = new Color(0f, 0f, 0f, 0f);
        // attributes.setBackgroundColor(transparentBlack);
        // attributes.setTextColor(Color.WHITE);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        // attributes.setSize(new java.awt.Dimension(40, 0));
        // attributes.setBorderColor(transparentBlack);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        // attributes.setHighlightScale(1);
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setEffect(AVKey.TEXT_EFFECT_NONE);

        return attributes;
    }

    public void setLeftAnnotationText(String text) {
        setAnnotationText(leftAnnotation, text);
    }

    public void setRightAnnotationText(String text) {
        setAnnotationText(rightAnnotation, text);
    }

    private void setAnnotationText(Annotation annotation, String text) {
        if (text == null) {
            text = "";
        }

        if (!text.equals(annotation.getText())) {
            annotation.setText(text);
        }

        if (text.isEmpty()) {
            annotation.getAttributes().setSize(new java.awt.Dimension(0, 0));
        } else {
            annotation.getAttributes().setSize(new java.awt.Dimension(Integer.MAX_VALUE, 0));
        }
    }

    public void setLeftAnnotationBold(boolean isBold) {
        setAnnotationBold(leftAnnotation, isBold);
    }

    public void setRightAnnotationBold(boolean isBold) {
        setAnnotationBold(rightAnnotation, isBold);
    }

    private void setAnnotationBold(Annotation annotation, boolean isBold) {
        if (isBold) {
            annotation.getAttributes().setFont(valueFontBold);
        } else {
            annotation.getAttributes().setFont(valueFontNormal);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageSource(String imagePath) {
        if (imagePath == null) {
            imagePath = "";
        }

        if (!imagePath.equals(imageAnnotation.getImageSource())) {
            imageAnnotation.setImageSource(imagePath);
        }
    }

    private void setAlert(AlertLevel alert) {
        if ((alert == null) || (alert == AlertLevel.GREEN)) {
            leftAnnotation.getAttributes().setTextColor(Color.BLACK);
            rightAnnotation.getAttributes().setTextColor(Color.BLACK);

            return;
        }

        if (alert == AlertLevel.YELLOW) {
            leftAnnotation.getAttributes().setTextColor(Color.ORANGE);
            rightAnnotation.getAttributes().setTextColor(Color.ORANGE);

            return;
        }

        leftAnnotation.getAttributes().setTextColor(Color.RED);
        rightAnnotation.getAttributes().setTextColor(Color.RED);
    }

    public Property<AlertLevel> alertProperty() {
        return alertProperty;
    }

}
