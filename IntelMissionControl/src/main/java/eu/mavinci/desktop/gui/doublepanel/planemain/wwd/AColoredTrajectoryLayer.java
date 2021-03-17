/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.wwext.PolylineWithUserData;
import eu.mavinci.desktop.helper.ColorHelper;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import org.asyncfx.concurrent.Dispatcher;

public abstract class AColoredTrajectoryLayer extends RenderableLayer {

    public static final double MIN_NEW_POINT_DISTANCE = 0.01;
    public static final double MIN_ACCEPTED_CTE = 0.03;
    public static final double IGNORE_DISTANCE = 0.1;
    public static final int MAX_DROPPING = 300;

    private ArrayList<Position> positions = new ArrayList<Position>();
    private EfficientPolyline polyline;
    private EfficientPolyline polylineShadow;

    private ArrayList<Renderable> nextRenderables = new ArrayList<>(MAX_DROPPING);

    private Position currentPosition = null;

    private ArrayList<Vec4> droppedVec = new ArrayList<Vec4>();

    private Vec4 lastVec = null;
    private Vec4 currentVec = null;

    private Color lastColor;

    private Object userData;
    private boolean withShadows;
    private final boolean doubleBuffered;

    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    private final Dispatcher dispatcher =
        DependencyInjector.getInstance().getInstanceOf(Dispatcher.class);

    public AColoredTrajectoryLayer(Object userData, String name, boolean withShadows, boolean doubleBuffered) {
        this.userData = userData;
        this.withShadows = withShadows;
        this.doubleBuffered = doubleBuffered;
        setName(name);
        setPickEnabled(false);
    }

    private long nextCut = -1;

    protected abstract long getCutIntervalInMs();

    protected void addPosition(Position pos, Color color) {
        if (doubleBuffered) {
            addPositionInt(pos, color);
        } else {
            dispatcher.runLater(
                () -> {
                    addPositionInt(pos, color);
                    notifyRedraw();
                });
        }
    }

    private void addPositionInt(Position pos, Color color) {
        // System.out.println("add pos:" + pos);
        long cutInterval = getCutIntervalInMs();

        if (!color.equals(lastColor) || (cutInterval > 0 && nextCut <= System.currentTimeMillis())) {
            // System.out.println("mode change" + lastMode + " -> " + p.flightmode);
            nextCut = System.currentTimeMillis() + cutInterval;
            // System.out.println("cut noW! nextcut:"+nextCut);
            lastColor = color;
            polyline = new EfficientPolyline(userData);
            if (color.getAlpha() > 0) {
                addRenderable(polyline);
            }

            if (withShadows) {
                polylineShadow = new EfficientPolyline(userData);
                if (color.getAlpha() > 0) {
                    polylineShadow.setFollowTerrain(true);
                    addRenderable(polylineShadow);
                }
            }

            positions.clear();
            droppedVec.clear();
            lastVec = currentVec;
            if (currentPosition != null) {
                // System.out.println("lastPos was not 0");
                positions.add(currentPosition);
                polyline.setPositions(positions);
                if (withShadows) {
                    polylineShadow.setPositions(positions);
                }
            }

            polyline.setColor(color);
            polyline.setHighlightColor(Color.WHITE);
            if (withShadows) {
                polylineShadow.setColor(ColorHelper.scaleAlphaToShadow(polyline.getColor()));
                polylineShadow.setHighlightColor(Color.WHITE);
            }
        }

        Vec4 vec = globe.computePointFromPosition(pos);

        if (currentPosition == null) {
            // start empty line!
            currentPosition = pos;
            currentVec = vec;
            lastVec = vec;
            positions.add(pos);
            // System.out.println("initial pos Adding:" + positions);
        } else {
            int i = positions.size() - 1;
            double dist = lastVec.distanceTo3(vec);
            // System.out.println("dist:"+dist);
            if (dist < IGNORE_DISTANCE) {
                return;
            } else if (dist < MIN_NEW_POINT_DISTANCE && lastVec != currentVec) {
                droppedVec.add(currentVec);
                positions.set(i, pos); // update current pos
                // System.out.println("update pos no:"+ i);
            } else if (MAX_DROPPING <= droppedVec.size()) {
                // if the dropped vector becomes longer, we have an very bad runtime on adding further points
                // worst case runtime behavior is on a very long perfectly linear line of points, since basically all of
                // them should be dropped....
                droppedVec.clear(); // could not be done while iterating -> concurrentMod.Exception
                // lastPosition = currentPosition;
                lastVec = currentVec;
                // System.out.println("stop DROPPING");
                positions.add(pos);
            } else {
                // System.out.println("lastVec="+lastVec );
                Line l = new Line(lastVec, vec.subtract3(lastVec));
                boolean dropping = true;
                for (Vec4 v : droppedVec) {
                    if (l.distanceTo(v) > MIN_ACCEPTED_CTE) {
                        dropping = false;
                        break;
                    }
                }

                if (dropping && lastVec != currentVec) {
                    // System.out.println("dropping + update pos no:"+ i + "
                    // MIN_ACCEPTED_CTE;"+MIN_ACCEPTED_CTE);
                    positions.set(i, pos); // update current pos
                    droppedVec.add(currentVec);
                } else {
                    droppedVec.clear(); // could not be done while iterating -> concurrentMod.Exception
                    // lastPosition = currentPosition;
                    lastVec = currentVec;
                    // System.out.println("stop DROPPING");
                    positions.add(pos);
                }
            }
        }

        // System.out.println("positions:" + positions);

        currentVec = vec;
        currentPosition = pos;

        polyline.setPositions(positions);
        if (withShadows) {
            polylineShadow.setPositions(positions);
        }

        // System.out.println("added new pos" + pos + " NUMREND"+this.getNumRenderables());
    }

    @Override
    public void addRenderable(Renderable renderable) {
        if (doubleBuffered) {
            nextRenderables.add(renderable);
        } else {
            dispatcher.runLater(
                () -> {
                    super.addRenderable(renderable);
                });
        }
    }

    private ArrayList<Renderable> latestToBeRenderedDoubleBuffered;

    protected void notifyRedraw() {
        if (doubleBuffered) {
            latestToBeRenderedDoubleBuffered = nextRenderables;
        }

        dispatcher.runLater(
            () -> {
                if (doubleBuffered) {
                    setRenderables(latestToBeRenderedDoubleBuffered);
                }

                firePropertyChange(AVKey.LAYER, null, this);
            });
    }

    public void clear() {
        if (doubleBuffered) {
            clearInt();
        } else {
            dispatcher.runLater(() -> clearInt());
        }
    }

    private void clearInt() {
        // System.out.println("clearTrack cache");
        positions.clear();
        lastColor = null;
        currentPosition = null;
        if (doubleBuffered) {
            nextRenderables = new ArrayList<>();
        } else {
            removeAllRenderables();
            notifyRedraw();
        }
    }

    @Override
    public void removeAllRenderables() {
        dispatcher.runLater(
            () -> {
                super.removeAllRenderables();
            });
    }

    public static class EfficientPolyline extends PolylineWithUserData {

        long timestamp = System.currentTimeMillis();

        public EfficientPolyline(Object userData) {
            super();
            setPathType(Polyline.LINEAR);
            setLineWidth(8);
            setUserData(userData);
            setSelectable(false);
            setHighlightableEvenWithoutSelectability(true);
            setPopupTriggering(true);
            setPathType(AVKey.LINEAR);
        }

        boolean shouldDraw(DrawContext dc) {
            return !(isFollowTerrain()
                && dc.getView().getGlobe() instanceof FlatGlobe); // dont render shadows in flat world
        }

        @Override
        protected void draw(DrawContext dc) {
            if (!shouldDraw(dc)) {
                return; // dont render shadows in flat world
            }

            super.draw(dc);
        }

        @Override
        public void render(DrawContext dc) {
            if (!shouldDraw(dc)) {
                return; // dont render shadows in flat world
            }

            super.render(dc);
        }

        @Override
        protected void drawOrderedRenderable(DrawContext dc) {
            if (!shouldDraw(dc)) {
                return; // dont render shadows in flat world
            }

            // Modify the projection transform to shift the depth values slightly toward the camera in order to
            // ensure the lines are rendered on top of planned flight plan lines
            dc.pushProjectionOffest(0.99);
            super.drawOrderedRenderable(dc);
            dc.popProjectionOffest();
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint) {
            if (!shouldDraw(dc)) {
                return; // dont render shadows in flat world
            }

            super.pick(dc, pickPoint);
        }

    }

    public void clearOldStuffAsync() {
        dispatcher.runLater(
            () -> {
                long cutInterval = getCutIntervalInMs();
                if (cutInterval < 0) {
                    clear();
                    return;
                }

                long expire = System.currentTimeMillis() - cutInterval; // dropping everything
                // System.out.println("dropping everythign what expires somewhere here: " + expire);
                LinkedList<EfficientPolyline> toDrop = new LinkedList<EfficientPolyline>();
                for (Renderable r : getRenderables()) {
                    if (r instanceof EfficientPolyline) {
                        EfficientPolyline eLine = (EfficientPolyline)r;
                        // System.out.println("this line expires:" + eLine.timestamp);
                        if (eLine.timestamp <= expire) {
                            toDrop.add(eLine);
                        }
                    }
                }

                if (toDrop.isEmpty()) {
                    return;
                }

                for (EfficientPolyline r : toDrop) {
                    removeRenderable(r);
                    // System.out.println("dropping old renderable:"+r.timestamp);
                }

                if (getNumRenderables() == 0) {
                    lastColor = null;
                    currentPosition = null;
                    // System.out.println("ALL renderables removed!");
                }

                if (!doubleBuffered) {
                    notifyRedraw();
                }
            });
    }

}
