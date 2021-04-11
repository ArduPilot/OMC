/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016:
 * rending of own 3d modells added
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import eu.mavinci.desktop.gui.wwext.sun.SunPositionProviderSingleton;
import eu.mavinci.desktop.gui.wwext.sunlight.SunPositionProvider;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import eu.mavinci.desktop.gui.wwext.sun.SunPositionProviderSingleton;
import eu.mavinci.desktop.gui.wwext.sunlight.SunPositionProvider;
import eu.mavinci.desktop.main.debug.Debug;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;

import com.jogamp.opengl.GL2;
import java.util.logging.Level;

/** @author RodgersGB, Shawn Gano, Marco Möller, Peter Schauss */
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class WWModel3D implements Renderable {
    protected Position position = null;
    protected Model model = null;

    /** latlon where the sun is perpendicular above ground */
    protected LatLon sunPos;

    protected double yawDeg = 0; // in degrees
    protected double pitchDeg = 0; // in degrees
    protected double rollDeg = 0; // in degrees

    protected boolean maitainConstantSize = true; // default true
    protected double size = 1;

    // test rotation
    public double angle = 0;
    public double xAxis = 0;
    public double yAxis = 0;
    public double zAxis = 0;

    double angle2Rad = 0;

    protected DisplayListRenderer renderer = new DisplayListRenderer();

    // STK - model - Nadir Alignment with ECF velocity constraint

    /** Creates a new instance of WWModel3D_new */
    public WWModel3D() {
        renderer.debug(false);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    public double getYawDeg() {
        return yawDeg;
    }

    public void setYawDeg(double yawDeg) {
        this.yawDeg = yawDeg;
    }

    public double getPitchDeg() {
        return pitchDeg;
    }

    public void setPitchDeg(double pitchDeg) {
        this.pitchDeg = pitchDeg;
    }

    public double getRollDeg() {
        return rollDeg;
    }

    public void setRollDeg(double rollDeg) {
        this.rollDeg = rollDeg;
    }

    public boolean isConstantSize() {
        return maitainConstantSize;
    }

    /**
     * in case of true, the size in METER of the modell will be constant in case of false the size in PIXEL will be
     * constant
     *
     * @param maitainConstantSize
     */
    public void setMaitainConstantSize(boolean maitainConstantSize) {
        this.maitainConstantSize = maitainConstantSize;
    }

    public double getSize() {
        return size;
    }

    /**
     * sets the size of the airplane: in case of maitainConstantSize==true: the unit is meter in case of
     * maitainConstantSize==false: its a relative factor to the internal size of the 3d modell
     *
     * @param size
     */
    public void setSize(double size) {
        this.size = size;
    }

    public float[] computeLightPosition(Vec4 loc) {
        // set light from above
        Vec4 lightPos = loc.multiply3(1.2);
        return new float[] {(float)lightPos.x, (float)lightPos.y, (float)lightPos.z, (float)lightPos.w};
    }

    // Rendering routines so the object can render itself ------
    // ===============================================================
    // old doRender
    public void render(DrawContext dc) {
        if (position == null) {
            return;
        }

        if (model == null) {
            return;
        }

        Position pos = this.getPosition();
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);

        double localScale = this.computeSize(dc, loc);

        // increase height such that model is above globe
        // Bounds b = this.getModel().getBounds();
        // float offset = this.getModel().getCenterPoint().z - b.min.z;
        // float locLen = (float)Math.sqrt(loc.dot3(loc));
        // loc = loc.multiply3((localScale*offset+locLen)/locLen);

        if (sunPos == null) {
            SunPositionProvider spp = SunPositionProviderSingleton.getInstance();
            sunPos = spp.getPosition();
        }

        Vec4 sun = dc.getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
        float[] lightPos = new float[] {(float)sun.x, (float)sun.y, (float)sun.z, 0.f}; // computeLightPosition(loc);
        // System.out.format("sun: %f,$%f,%f,%f",(float)light.x,(float)light.y,(float)light.z,(float)light.w);
        try {
            beginDraw(dc, lightPos);

            draw(dc, loc, localScale, pos.getLongitude(), pos.getLatitude());
        }
        // handle any exceptions
        catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "error rendering plane model", e);
        }
        // we must end drawing so that opengl
        // states do not leak through.
        finally {
            endDraw(dc);
        }
    }

    /*
     * Update the light source properties
     */
    private void updateLightSource(GL2 gl, float[] lightPosition) {

        /** Ambient light array */
        float[] lightAmbient = {0.4f, 0.4f, 0.4f};
        /** Diffuse light array */
        float[] lightDiffuse = {0.25f, 0.25f, 0.25f, 1.f};
        /** Specular light array */
        float[] lightSpecular = {1.f, 1.f, 1.f, 1.f};

        float[] model_ambient = {0.2f, 0.2f, 0.2f, 1.f};

        // Experimental: trying everything with light0 instead of light1
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

        // gl.glDisable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);

        // experimental lines:
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        // gl.glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, constantAttenuation);
        // gl.glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, linearAttenuation);
        // gl.glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, quadraticAttenuation);

    }

    // draw this layer
    protected void draw(DrawContext dc, Vec4 loc, double localScale, Angle lon, Angle lat) {
        GL2 gl = dc.getGL().getGL2();

        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            // MAYBE REPLACE "PUSH REFERENCE CENTER" - with gl. move to new center... (maybe not)
            // gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Full Brightness. 50% Alpha (new )

            // Set The Blending Function For Translucency (new )
            // gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);

            dc.getView().pushReferenceCenter(dc, loc);
            if (!(dc.getView().getGlobe() instanceof FlatGlobe)) {
                gl.glRotated(lon.degrees, 0, 1, 0);
                gl.glRotated(-lat.degrees, 1, 0, 0);
            }

            gl.glScaled(localScale, localScale, localScale); // / can change the scale of the model here!!

            // MM: TODO diese rotationen sind eher empirisch!
            // man sollte auch auf die reihenfolge der roation achten, stimmt die?
            // PM: Reihenfolge stimmt, siehe Tait-Bryan-Winkel.
            // Die 90° sind 3d-Modell-bedingt. WW verwendet offenbar nicht das TB-System (Z-Achse in den Boden hinein)
            // Das neue 3d-Modell ist TB-konform.
            /*
             * drehungen für piper-modell gl.glRotated(90-model.getYawDeg(), 0,0,1); gl.glRotated(-model.getPitchDeg(), 0,1,0);
             * gl.glRotated(180-model.getRollDeg(), 1,0,0); //stimmt der roll winkel? PM: Jetzt ja!
             */
            // roll pitch yaw / x y z
            gl.glRotated(-getYawDeg(), 0, 0, 1);
            gl.glRotated(getPitchDeg(), 1, 0, 0);
            gl.glRotated(-getRollDeg(), 0, 1, 0);

            // gl.glRotated(getRollDeg(), 1,0,0);
            // gl.glRotated(getPitchDeg(), 0,1,0);
            // gl.glRotated(-getYawDeg(), 0,0,1);

            // fix coordinate system differences between opengl and obj-file format
            // coordinates system is official rollpitchyaw system in blender
            // and exported with rotate fix on
            // this rotation brings the plane in direction roll/pitch/yaw = 0/0/0
            gl.glRotated(90, 0, 0, 1);
            gl.glRotated(0, 0, 1, 0);
            gl.glRotated(-90, 1, 0, 0);

            /* das sind die drehungen für das alte redplane modell */
            // gl.glRotated(270-getYawDeg(), 0,0,1);
            // gl.glRotated(getPitchDeg(), 0,1,0);
            // gl.glRotated(getRollDeg(), 1,0,0); //stimmt der roll winkel?

            // miniplane.3ds
            // gl.glRotated(90-model.getYawDeg(), 0,0,1);
            // gl.glRotated(-model.getPitchDeg(), 0,1,0);
            // gl.glRotated(90-model.getRollDeg(), 1,0,0); //stimmt der roll winkel?

            gl.glEnable(GL2.GL_CULL_FACE);
            gl.glShadeModel(GL2.GL_SMOOTH); // gl.glShadeModel(GL.GL_FLAT);

            gl.glEnable(GL2.GL_DEPTH_TEST);
            gl.glDepthFunc(GL2.GL_LEQUAL);
            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

            // Get an instance of the display list renderer
            // iModel3DRenderer renderer = DisplayListRenderer.getInstance();
            renderer.render(gl, getModel());
            dc.getView().popReferenceCenter(dc);
        }
    }

    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc, float[] lightPosition) {
        GL2 gl = dc.getGL().getGL2();

        gl.glPushAttrib(
            GL2.GL_TEXTURE_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_HINT_BIT
                | GL2.GL_POLYGON_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_CURRENT_BIT
                | GL2.GL_LIGHTING_BIT
                | GL2.GL_TRANSFORM_BIT);

        updateLightSource(gl, lightPosition);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.maitainConstantSize) {
            return size;
        }

        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }

        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double currentSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (currentSize < 2) {
            currentSize = 2;
        }

        return currentSize * size;
    }

}
