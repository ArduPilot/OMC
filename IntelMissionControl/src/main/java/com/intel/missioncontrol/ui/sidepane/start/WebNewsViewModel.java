/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.intel.missioncontrol.EnvironmentOptions;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class WebNewsViewModel extends DialogViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebNewsViewModel.class);

    private final UIAsyncBooleanProperty showing = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty shouldShow = new UIAsyncBooleanProperty(this);

    private final UIAsyncStringProperty uri = new UIAsyncStringProperty(this);

    private final UIAsyncObjectProperty<Document> document =
        new UIAsyncObjectProperty<>(this) {
            @Override
            protected void invalidated() {
                updateDocumentHash(get());
            }
        };

    private final ParameterizedCommand<String> followLinkCommand;

    private final DisplaySettings displaySettings;
    private final ILicenceManager licenceManager;

    @Inject
    WebNewsViewModel(
            DisplaySettings displaySettings,
            ILicenceManager licenceManager,
            InternetConnectivitySettings internetConnectivitySettings) {
        this.displaySettings = displaySettings;
        this.licenceManager = licenceManager;
        followLinkCommand = new ParameterizedDelegateCommand<>(this::followLink);
        shouldShow.bind(displaySettings.showNewsProperty().and(internetConnectivitySettings.isIntelProxyProperty()));
        showing.bind(shouldShow.and(document.isNotNull()));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        Licence licence = licenceManager.getActiveLicence();
        if (licence.isFalconEdition() || licence.isGrayHawkEdition()) {
            uri.set(EnvironmentOptions.NEWS_URI);
        } else if (licence.isDJIEdition()) {
            uri.set(EnvironmentOptions.NEWS_URI_DJI);
        } else {
            uri.set(EnvironmentOptions.NEWS_URI);
        }
    }

    ParameterizedCommand<String> getFollowLinkCommand() {
        return followLinkCommand;
    }

    ReadOnlyProperty<Boolean> showingProperty() {
        return showing;
    }

    Property<String> uriProperty() {
        return uri;
    }

    public UIAsyncBooleanProperty shouldShowProperty() {
        return shouldShow;
    }

    Property<Document> documentProperty() {
        return document;
    }

    @Override
    protected void onClosing() {
        displaySettings.showNewsProperty().set(false);
    }

    private void followLink(String uri) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (URISyntaxException e) {
                LOGGER.warn("Cannot open link: the URI is invalid.", e);
            } catch (IOException ignored) {
                LOGGER.warn("Cannot open link.");
            }
        }
    }

    private void updateDocumentHash(Document document) {
        try {
            String text = getDocumentText(document);
            if (text == null) {
                return;
            }

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(text.getBytes());
            String hash = Hex.encodeHexString(messageDigest.digest());

            if (!hash.equals(displaySettings.newsHashProperty().get())) {
                displaySettings.newsHashProperty().set(hash);
                displaySettings.showNewsProperty().set(true);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    private String getDocumentText(Document doc) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(output, "UTF-8")));
            return output.toString(Charsets.UTF_8);
        } catch (TransformerException | IOException e) {
            return null;
        }
    }

}
