/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.rtk.IRTKStation;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import org.asyncfx.beans.property.PropertyPathStore;

public class BackendLayer extends IconLayerCentered {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.BackendLayer";

    private static final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
    private ILinkBoxConnectionService linkBoxConnectionService;
    private PropertyPathStore path = new PropertyPathStore();

    public BackendLayer(ILinkBoxConnectionService service) {
        this.linkBoxConnectionService = service;

        setName("BackendLayerName");
        setEnabled(false);

        setAlwaysUseAbsoluteElevation(false);
        setRenderAlwaysOverGround(true);
        setPickEnabled(false);
        path.from(linkBoxConnectionService.getRTKStation())
            .selectReadOnlyAsyncObject(IRTKStation::getRTKStationPosition)
            .addListener((observable, oldValue, newValue) -> update());
        update();
    }

    private void update() {
        if (linkBoxConnectionService.getRTKStation().get() != null
                && linkBoxConnectionService.getRTKStation().get().getRTKStationPosition().get() != null) {
            UserFacingIconWithUserData icon =
                UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                    "com/intel/missioncontrol/gfx/icon_rtk_base.svg",
                    new Position(linkBoxConnectionService.getRTKStation().get().getRTKStationPosition().get(), -10000),
                    this);
            icon.setSelectable(true);
            icon.setDraggable(false);
            icon.setHighlightScale(FlightplanLayer.HIGHLIGHT_SCALE);
            icon.setToolTipText("RTK Station");
            icon.setToolTipTextColor(java.awt.Color.YELLOW);

            removeAllIcons();
            addIcon(icon);

            firePropertyChange(AVKey.LAYER, null, this);
        }
    }
}
