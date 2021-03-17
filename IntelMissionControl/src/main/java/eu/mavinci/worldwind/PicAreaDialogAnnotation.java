/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.AnnotationFlowLayout;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwindx.examples.util.ProgressAnnotation;

import java.awt.Color;

/** @author Sérgio */
public class PicAreaDialogAnnotation extends MDialogAnnotation {
    protected static final String PLAY_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-play.png";
    protected static final String PAUSE_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-pause.png";

    protected static final String PLAY_TOOLTIP_TEXT = "Play audio clip";
    protected static final String PAUSE_TOOLTIP_TEXT = "Pause audio clip";

    protected long position;
    protected long length;
    protected Annotation titleLabel;
    protected Annotation positionLabel;
    protected Annotation lengthLabel;
    protected ProgressAnnotation progress;
    private static String surface;
    private static String time;

    public PicAreaDialogAnnotation(Position position) {
        super(position);
    }

    public long getClipPosition() {
        return this.position;
    }

    public void setPlayButtonState(String state) {
        if (state == AVKey.PLAY) {
        } else if (state == AVKey.PAUSE) {
            // this.playButton.setImageSource(PAUSE_IMAGE_PATH);
            // this.playButton.setToolTipText(PAUSE_TOOLTIP_TEXT);
        }
    }

    protected void initComponents() {
        super.initComponents();

        this.titleLabel = new ScreenAnnotation("", new java.awt.Point());

        // this.positionLabel = new ScreenAnnotation("", new java.awt.Point());

        this.setupTitle(this.titleLabel);
        if (surface == null) {
            surface = "0";
        }

        if (time == null) {
            time = "0";
        }

        setText("<b>Building</b><br>Surface: " + surface + "<br>Est. flight time: " + time + "<br>");

        // this.playButton.setActionCommand(AVKey.PLAY);

        // this.playButton.addActionListener(this);

        // this.playButton.setToolTipText(PLAY_TOOLTIP_TEXT);

    }

    protected void layoutComponents() {
        super.layoutComponents();

        // Annotation controlsContainer = new ScreenAnnotation("", new java.awt.Point());
        // {
        // this.setupContainer(controlsContainer);
        // controlsContainer.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 4, 0)); // hgap,
        // // vgap
        // // controlsContainer.addChild(this.playButton);
        //
        // //java.awt.Insets insets = this.positionLabel.getAttributes().getInsets();
        //// this.positionLabel.getAttributes()
        //// .setInsets(new java.awt.Insets(insets.top, insets.left + 4, insets.bottom, insets.right));
        // }

        AnnotationAttributes attributes = this.getAttributes();
        attributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        attributes.setTextColor(Color.WHITE);
        Annotation contentContainer = new ScreenAnnotation("", new java.awt.Point(), attributes);
        {
            this.setupContainer(contentContainer);
            contentContainer.setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.CENTER, 0, 16)); // hgap,
            // vgap
            contentContainer.addChild(this.titleLabel);
            // contentContainer.addChild(controlsContainer);
        }

        this.addChild(contentContainer);
    }

    protected void setupTitle(Annotation annotation) {
        this.setupLabel(annotation);
    }

    public void updateTextLabel() {
        super.setText("<b>Building</b><br>Surface: " + surface + "<br>Est. flight time: " + time + "<br>");
    }

    /** @param surface the surface to set */
    public static void setSurface(String surface) {
        PicAreaDialogAnnotation.surface = surface;
    }

    /** @param time the time to set */
    public static void setTime(String time) {
        PicAreaDialogAnnotation.time = time;
    }
}
