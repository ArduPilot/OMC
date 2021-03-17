/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.rtk.IRTKStation;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyListProperty;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class LinkBoxConnectionService implements ILinkBoxConnectionService {

    private final AsyncObjectProperty<ILinkBox> linkBox = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IRTKStation> rtkStation = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<LinkBoxStatus> linkBoxStatus = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty linkBoxOnline = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty linkBoxAuthenticated = new SimpleAsyncBooleanProperty(this);

    @Inject
    public LinkBoxConnectionService(
            INetworkInformation networkInformation,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext) {
        LinkBox linkBoxInt = new LinkBox(networkInformation, languageHelper, applicationContext);

        linkBox.setValue(linkBoxInt);
        rtkStation.setValue(linkBoxInt);
        linkBoxOnline.bind(linkBox.get().linkBoxOnlineProperty());
        linkBoxAuthenticated.bind(linkBox.get().linkBoxAuthenticatedProperty());

        linkBoxStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    if (linkBoxOnline.getValue()) {
                        return linkBoxAuthenticated.getValue() ?
                        LinkBoxStatus.AUTHENTICATED
                        : LinkBoxStatus.UNAUTHENTICATED;
                    } else {
                        return LinkBoxStatus.OFFLINE;
                    }
                },
                linkBoxOnline,
                linkBoxAuthenticated));
    }

    @Override
    public ReadOnlyAsyncObjectProperty<LinkBoxStatus> linkBoxStatusProperty() {
        return linkBoxStatus;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<ILinkBox> getLinkBox() {
        return linkBox;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IRTKStation> getRTKStation() {
        return rtkStation;
    }

    @Override
    public void requestLinkBoxAuthentication() {
        linkBox.get().requestLinkBoxAuthentication();
    }

    @Override
    public ReadOnlyListProperty<ResolvableValidationMessage> linkBoxResolvableMessagesProperty() {
        return linkBox.get().resolvableLinkBoxMessagesProperty();
    }

}
