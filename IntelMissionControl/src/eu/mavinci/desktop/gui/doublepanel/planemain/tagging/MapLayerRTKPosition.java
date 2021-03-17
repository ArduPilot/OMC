/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerWW;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.geo.IPositionReferenced;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

public class MapLayerRTKPosition extends MapLayer implements IPositionReferenced, IMapLayerWW, IMatchingRelated {

    MapLayerMatching matching;
    IconLayerCentered wwLayer = new IconLayerCentered();

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerRTKPosition";

    IElevationModel elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    IMapLayerListener listener =
        new IMapLayerListener() {

            @Override
            public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {}

            @Override
            public void mapLayerValuesChanged(IMapLayer layer) {
                if (layer != matching) {
                    return;
                }

                if (icon != null) {
                    fixIcon();
                    wwLayer.firePropertyChange(AVKey.LAYER, null, wwLayer);
                }
            }

            @Override
            public void mapLayerStructureChanged(IMapLayer layer) {}

            @Override
            public void childMapLayerRemoved(int i, IMapLayer layer) {}

            @Override
            public void childMapLayerInserted(int i, IMapLayer layer) {}
        };

    public static final String iconRes = "com/intel/missioncontrol/gfx/icon_rtk_base.svg";

    UserFacingIconWithUserData icon;

    public MapLayerRTKPosition(MapLayerMatching mapLayerMatching) {
        super(true);
        this.matching = mapLayerMatching;

        wwLayer.setPickEnabled(true);
        wwLayer.setAlwaysUseAbsoluteElevation(false);
        wwLayer.setRenderAlwaysOverGround(true);

        icon = new UserFacingIconWithUserData(iconRes, matching.getRealPosition(), this);
        icon.setHighlightScale(1.5);
        icon.setToolTipTextColor(java.awt.Color.YELLOW);
        icon.setDraggable(false);

        icon.setPopupTriggering(false);
        icon.setSelectable(true);
        fixIcon();

        wwLayer.addIcon(icon);
        matching.addMapListener(listener); // if I would do the listenering by this object, I would get a call loop!!
    }

    @Override
    public LatLon getLatLon() {
        return matching.getRealPosition();
    }

    @Override
    public Layer getWWLayer() {
        return wwLayer;
    }

    @Override
    public Position getPosition() {
        return matching.getRealPosition();
    }

    @Override
    public AMapLayerMatching getMatching() {
        return matching;
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        super.mapLayerValuesChanged(layer);
    }

    private void fixIcon() {
        if (icon == null) {
            return;
        }

        icon.setToolTipText(languageHelper.getString(KEY + ".icon", matching.getName()));
        icon.setPosition(elevationModel.getPositionOverGroundRelativeToGround(matching.getRealPosition()));
    }

}
