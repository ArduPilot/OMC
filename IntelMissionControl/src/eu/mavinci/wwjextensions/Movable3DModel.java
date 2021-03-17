/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.wwjextensions;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.GL2;

import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.iModel3DRenderer;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;

/**
 * @author R.Wathelet, most of the code is from RodgersGB Model3DLayer class
 * see https://joglutils.dev.java.net/
 * modified by eterna2
 * modified by R.Wathelet adding the Adjustable
 */
public class Movable3DModel implements Renderable, Movable, Adjustable {

    private Position position = null;
    private Model model = null;

    private double yaw = 0.0;
    private double roll = 0.0;
    private double pitch = 0.0;

    private double yawModel = 0.0;
    private double rollModel = 0.0;
    private double pitchModel = 0.0;

    private boolean keepConstantSize = true;
    private Vec4 referenceCenterPoint;
    private Globe globe;
    private double size = 1;

    private LatLon sunPosition = null;

    iModel3DRenderer renderer;

    protected float polygonOffsetFactor = 0;
    protected float polygonOffsetUnits = 0;

    public Movable3DModel( iModel3DRenderer renderer) {
    	this.renderer = renderer;
    }

    public Movable3DModel( iModel3DRenderer renderer,Model model, Position pos) {
    	this(renderer);
        this.model = model;
        this.setPosition(pos);
    }

    public Movable3DModel( iModel3DRenderer renderer,String path, Position pos) {
    	this(renderer);
        try {
            this.model = ModelFactory.createModel(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setPosition(pos);
    }

    public Movable3DModel( DisplayListRenderer renderer, String path, Position pos, double size) {
    	this(renderer,path, pos);
        this.setSize(size);
    }

    public void render(DrawContext dc) {
    	if (position == null) return;
    	Model model = getModel();
    	if (model == null) return;
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        try {
            beginDraw(dc);
            if (dc.isPickingMode()) {
                model.setRenderPicker(true);
            } else {
                model.setRenderPicker(false);
            }
            draw(dc,model);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endDraw(dc);
        }
    }

    protected void draw(DrawContext dc) {
    	draw(dc, getModel());
    }

    protected void draw(DrawContext dc, Model model) {
    	GL2 gl = dc.getGL().getGL2();
        this.globe = dc.getGlobe();
        Vec4 loc = dc.getGlobe().computePointFromPosition(this.getPosition());
        double localSize = this.computeSize(dc, loc);
        this.computeReferenceCenter(dc);
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            dc.getView().pushReferenceCenter(dc, loc);
            if (!(dc.getView().getGlobe() instanceof FlatGlobe)) {
	            gl.glRotated(position.getLongitude().degrees, 0,1,0);
	            gl.glRotated(-position.getLatitude().degrees, 1,0,0);
            }
            gl.glRotated(yaw, 0, 0, 1);
            gl.glRotated(pitch, 1, 0, 0);
            gl.glRotated(roll, 0, 1, 0);

            gl.glRotated(yawModel, 0,0,1);//FIXME,maybe adjust everthing to make reall roll pitch yaw from this
            gl.glRotated(pitchModel, 0,1,0);
            gl.glRotated(rollModel, 1,0,0);

            gl.glScaled(localSize, localSize, localSize);

            gl.glEnable(GL2.GL_CULL_FACE);
            gl.glShadeModel(GL2.GL_SMOOTH); // gl.glShadeModel(GL2.GL_FLAT);

            gl.glDepthFunc(GL2.GL_LEQUAL);
            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

            renderer.render(gl, model);
            dc.getView().popReferenceCenter(dc);
        }
    }

    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
    	GL2 gl = dc.getGL().getGL2();

        Vec4 lightDirect;
        LatLon sunPos = getSunPosition();
        float[] lightPosition;
        if (sunPos != null){
        	lightDirect = dc.getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
        	lightPosition = new float[]{(float)lightDirect.x,(float)lightDirect.y,(float)lightDirect.z,0.f};
        } else {
        	lightDirect = dc.getView().getEyePoint();
            lightPosition = new float[]{(float) (lightDirect.x + 1000), (float) (lightDirect.y + 1000), (float) (lightDirect.z + 1000), 1.0f};
        }

        gl.glPushAttrib(
        		GL2.GL_TEXTURE_BIT |
        		GL2.GL_COLOR_BUFFER_BIT |
        		GL2.GL_HINT_BIT |
        		GL2.GL_POLYGON_BIT |
        		GL2.GL_ENABLE_BIT |
        		GL2.GL_CURRENT_BIT |
                GL2.GL_LIGHTING_BIT |
                GL2.GL_TRANSFORM_BIT |
                GL2.GL_DEPTH_BUFFER_BIT);

        //float[] lightPosition = {0F, 100000000f, 0f, 0f};
        /** Ambient light array */
        float[] lightAmbient = {0.4f, 0.4f, 0.4f, 0.4f};
        /** Diffuse light array */
        float[] lightDiffuse = {0.25f, 0.25f, 0.25f, 1.f};
        /** Specular light array */
        float[] lightSpecular = {1.f, 1.f, 1.f, 1.f};
        float[] model_ambient = {0.4f, 0.4f, 0.4f, 1.f};
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);
        gl.glDisable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        if (polygonOffsetFactor != 0 || polygonOffsetUnits != 0) {
        	gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        	gl.glPolygonOffset(polygonOffsetFactor, polygonOffsetUnits);
        }
    }

    // resets opengl state
    protected void endDraw(DrawContext dc) {
    	GL2 gl = dc.getGL().getGL2();
        gl.glMatrixMode(com.jogamp.opengl.GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopAttrib();
    	gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
    }

    public Position getReferencePosition() {
        return this.getPosition();
    }

    public void move(Position delta) {
        if (delta == null) {
            String msg = Logging.getMessage("nullValue.PositionDeltaIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.moveTo(this.getReferencePosition().add(delta));
    }

    public void moveTo(Position position) {
        if (position == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Vec4 newRef = this.globe.computePointFromPosition(position);
        Angle distance = LatLon.greatCircleDistance(this.getPosition(), position);
        Vec4 axis = this.referenceCenterPoint.cross3(newRef).normalize3();
        Vec4 p = this.globe.computePointFromPosition(this.getPosition());
        p = p.transformBy3(Quaternion.fromAxisAngle(distance, axis));
        this.position = this.globe.computePositionFromPoint(p);
    }

    private void computeReferenceCenter(DrawContext dc) {
        this.referenceCenterPoint = this.computeTerrainPoint(dc,
                this.getPosition().getLatitude(), this.getPosition().getLongitude());
    }

    private Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon) {
        Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon);
        if (p == null) {
            p = dc.getGlobe().computePointFromPosition(lat, lon,
                    dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
        }
        return p;
    }

    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.keepConstantSize) {
            return size;
        }
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double newSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (newSize < 2) {
            newSize = 2;
        }
        return newSize;
    }

    public boolean isConstantSize() {
        return keepConstantSize;
    }

    public void setKeepConstantSize(boolean val) {
        this.keepConstantSize = val;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model){
    	this.model = model;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double val) {
        this.yaw = val;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double val) {
        this.roll = val;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double val) {
        this.pitch = val;
    }


    public double getYawModel() {
        return yawModel;
    }

    public void setYawModel(double val) {
        this.yawModel = val;
    }

    public double getRollModel() {
        return rollModel;
    }

    public void setRollModel(double val) {
        this.rollModel = val;
    }

    public double getPitchModel() {
        return pitchModel;
    }

    public void setPitchModel(double val) {
        this.pitchModel = val;
    }

    public LatLon getSunPosition(){
    	return sunPosition;
    }

    /**
     * if called with null, the position of the camera is used as lightsource (also the default)
     * @param sunPosition
     */
    public void setSunPosition(LatLon sunPosition){
    	this.sunPosition = sunPosition;
    }
}
