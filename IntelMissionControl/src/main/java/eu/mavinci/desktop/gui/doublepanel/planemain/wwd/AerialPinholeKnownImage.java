/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPics;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.gui.wwext.IWWPickableAdvancedTooltip;
import eu.mavinci.desktop.gui.wwext.PolygonWithUserData;
import eu.mavinci.desktop.gui.wwext.SurfaceImageCompressing;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AerialPinholeKnownImage extends AerialPinholeImage
        implements Renderable, Highlightable, IWWPickableAdvancedTooltip {

    private SurfacePolygon surfacePol;
    private PolygonWithUserData frustumPolygon;
    private PolygonWithUserData imagePolygon;
    private SurfaceImageCompressing surfaceImg;
    private MapLayerMatch match;
    private boolean showLocation = false;
    private boolean showOutlines = false;
    private boolean showImage = false;
    private boolean showFrustum = false;
    private boolean isHighlighted = false;
    private boolean projectingToGround;

    final IElevationModel elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    final ISelectionManager selectionManager = DependencyInjector.getInstance().getInstanceOf(ISelectionManager.class);

    public AerialPinholeKnownImage(MapLayerMatch match) {
        super(match);
        this.match = match;
        setPopupTriggering(true);
    }

    public void setHighlighted(boolean isHighlighted) {
        if (this.isHighlighted == isHighlighted) {
            return;
        }

        if (projectingToGround && isHighlighted && thumpFileExists()) {
            if (surfaceImg == null) {
                surfaceImg = new SurfaceImageCompressing();
            }

            ComputeCornerData computeCornerData = this.getComputeCornerData();
            if (computeCornerData != null && computeCornerData.getGroundProjectedCorners() != null) {
                surfaceImg.setImageSource(getThumpFile(), computeCornerData.getGroundProjectedCorners(), null);
            }
        }

        this.isHighlighted = isHighlighted;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    @Override
    protected void setCorners(ComputeCornerData computeCornerData) {
        super.setCorners(computeCornerData);
        resetPolygon();
    }

    boolean isSelected;

    @Override
    public void preRenderDetail(DrawContext dc) {
        boolean resetPolygon = false;

        isSelected = false;
        MapLayerPics picsLayer = null;
        MapLayerMatch match = getMatch();

        if (match != null) {
            picsLayer = (MapLayerPics)match.getParentLayer();
            if (picsLayer != null) {
                if (projectingToGround != picsLayer.isProjectingToGround()) {
                    projectingToGround = picsLayer.isProjectingToGround();
                    resetPolygon = true;
                }
            }

            if (selectionManager.getSelection() == match) {
                isSelected = true;
            }
        }

        if (surfacePol == null && imagePolygon == null) {
            resetPolygon = true;
        }

        if (resetPolygon) {
            resetPolygon();
        }

        showLocation = isHighlighted && !isSelected;
        showOutlines = isHighlighted && !isSelected;
        showFrustum = isHighlighted && !isSelected;
        showImage = isHighlighted;

        if (picsLayer != null) {
            showLocation |= picsLayer.isShowImageLocations();
            showImage |= !projectingToGround && !picsLayer.isShowOnlyOutlines();
        }

        if (projectingToGround) {
            if (surfaceImg != null && showImage) {
                surfaceImg.preRender(dc);
            }

            if (surfacePol != null && showOutlines) {
                surfacePol.preRender(dc);
            }
        } else {
            if (imagePolygon != null && showImage) {
                imagePolygon.preRender(dc);
            }
        }

        if (frustumPolygon != null && showFrustum) {
            frustumPolygon.preRender(dc);
        }

        if (showLocation) {
            markerLayer.preRender(dc);
        }
    }

    @Override
    public void render(DrawContext dc) {
        if (!shouldRender()) return;

        if (projectingToGround) {
            if (surfaceImg != null && showImage) {
                surfaceImg.render(dc);
            }

            if (surfacePol != null && showOutlines) {
                surfacePol.render(dc);
            }
        } else {
            if (imagePolygon != null && showImage) {
                imagePolygon.render(dc);
            }
        }

        if (frustumPolygon != null && showFrustum) {
            frustumPolygon.render(dc);
        }

        if (showLocation) {
            markerLayer.render(dc);
        }
    }

    @Override
    public String getName() {
        return getFile().getName() + super.getName();
    }

    @Override
    public String getTooltip(Position position, Point point) {
        // basically the Tooltip interface is used as marker only, to make tooltip works even with composed images
        return toString();
    }

    public PhotoFile getPhotoFile() {
        return match.getCurPhotoFile();
    }

    private void resetPolygon() {
        ComputeCornerData computeCornerData = this.getComputeCornerData();
        if (computeCornerData == null) {
            return;
        }

        ArrayList<LatLon> groundProjectedCorners = computeCornerData.getGroundProjectedCorners();
        ArrayList<Position> idealCorners = computeCornerData.getIdealCorners();

        if (groundProjectedCorners == null && idealCorners == null) {
            return;
        }

        Position shiftedPosOnLevel = computeCornerData.getShiftedPosOnLevel();

        if (projectingToGround) {
            if (groundProjectedCorners == null) {
                surfaceImg = null;
                imagePolygon = null;
                // imagePolygonFullRes = null;
                surfacePol = null;
                frustumPolygon = null;
                return;
            }

            if (surfaceImg == null) {
                surfaceImg = new SurfaceImageCompressing();
                surfaceImg.setImageSource(getThumpFile(), groundProjectedCorners, null);
                surfaceImg.setUserData(match);
                surfaceImg.setSelectable(false);
                surfaceImg.setHasTooltip(true);
                surfaceImg.setPopupTriggering(true);
            }

            surfaceImg.setCorners(groundProjectedCorners);
            imagePolygon = null;
            // imagePolygonFullRes = null;
            surfacePol = AerialPinholeUnknownImage.makeLine(groundProjectedCorners, Color.WHITE, 1, this);

            frustumPolygon =
                new PolygonWithUserData(
                    Arrays.asList(
                        new Position[] {
                            new Position(
                                    groundProjectedCorners.get(0),
                                    elevationModel.getElevationAsGoodAsPossible(groundProjectedCorners.get(0))),
                                shiftedPosOnLevel,
                            new Position(
                                    groundProjectedCorners.get(1),
                                    elevationModel.getElevationAsGoodAsPossible(groundProjectedCorners.get(1))),
                                shiftedPosOnLevel,
                            new Position(
                                    groundProjectedCorners.get(2),
                                    elevationModel.getElevationAsGoodAsPossible(groundProjectedCorners.get(2))),
                                shiftedPosOnLevel,
                            new Position(
                                    groundProjectedCorners.get(3),
                                    elevationModel.getElevationAsGoodAsPossible(groundProjectedCorners.get(3))),
                                shiftedPosOnLevel
                        }));
            frustumPolygon.setUserData(match);
            frustumPolygon.setSelectable(false);
            frustumPolygon.setHasTooltip(true);
            frustumPolygon.setPopupTriggering(true);
        } else {
            surfaceImg = null;
            surfacePol = null;

            File thumpFile = getThumpFile();
            if (thumpFile != null) {
                PolygonWithUserData imagePolygonTmp =
                    new PolygonWithUserData(
                        Arrays.asList(
                            new Position[] {
                                idealCorners.get(0), idealCorners.get(1), idealCorners.get(2), idealCorners.get(3)
                            }));
                imagePolygonTmp.setUserData(match);
                imagePolygonTmp.setTextureImageSource(thumpFile, new float[] {0, 0, 1, 0, 1, 1, 0, 1}, 4);
                imagePolygonTmp.setSelectable(true);
                imagePolygonTmp.setHasTooltip(true);
                imagePolygonTmp.setPopupTriggering(true);
                imagePolygon = imagePolygonTmp;
            } else {
                imagePolygon = null;
            }

            frustumPolygon =
                new PolygonWithUserData(
                    Arrays.asList(
                        new Position[] {
                            idealCorners.get(0), shiftedPosOnLevel,
                            idealCorners.get(1), shiftedPosOnLevel,
                            idealCorners.get(2), shiftedPosOnLevel,
                            idealCorners.get(3), shiftedPosOnLevel
                        }));

            frustumPolygon.setUserData(match);
            frustumPolygon.setSelectable(false);
            frustumPolygon.setHasTooltip(true);
            frustumPolygon.setPopupTriggering(true);
        }

        ShapeAttributes attr = new BasicShapeAttributes();
        attr.setDrawInterior(false);
        attr.setDrawOutline(true);
        attr.setOutlineWidth(1.0);
        attr.setOutlineMaterial(new Material(Color.WHITE));
        frustumPolygon.setAttributes(attr);

        resetMarker();
    }

    private File getFile() {
        PhotoFile photoFile = getPhotoFile();
        return photoFile == null ? null : photoFile.getFile();
    }

    boolean thumpFileExists() {
        PhotoFile photoFile = getPhotoFile();
        return photoFile == null ? null : photoFile.thumpFileExists();
    }

    File getThumpFile() {
        PhotoFile photoFile = getPhotoFile();
        return photoFile == null ? null : photoFile.getThumpFile();
    }
}
