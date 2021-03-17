/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.intel.missioncontrol.geo.Convert;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.proxy.ProxyManager;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.Sector;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.MathHelper;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirmapLaancService {

    public class LaancApprovalQr {
        private final BufferedImage qrCodeImage;
        private final URI link;

        public LaancApprovalQr(BufferedImage qrCodeImage, URI link) {
            this.qrCodeImage = qrCodeImage;
            this.link = link;
        }

        public BufferedImage getQrCodeImage() {
            return qrCodeImage;
        }

        public URI getLink() {
            return link;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AirmapLaancService.class);
    private final INetworkInformation networkInformation;
    private final ProxyManager proxyManager;

    @Inject
    public AirmapLaancService(INetworkInformation networkInformation, ProxyManager proxyManager) {
        this.networkInformation = networkInformation;
        this.proxyManager = proxyManager;
    }

    @Nullable
    public LaancApprovalQr airmapLaancApprove(Mission currentMission, IElevationModel elevationModel) {
        if (!networkInformation.isNetworkAvailable() || !proxyManager.proxyInitializedProperty().get()) {
            return null;
        }

        Sector s = currentMission.getSector();
        String pointGeoJSON = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[";
        boolean first = true;
        List<Position> corners = s.getCorners();
        corners.add(corners.get(0));
        for (Position latLon : corners) {
            if (!first) {
                pointGeoJSON += ",";
            }

            first = false;
            pointGeoJSON +=
                "["
                    + MathHelper.round(latLon.getLongitude(), 5)
                    + ","
                    + MathHelper.round(latLon.getLatitude(), 5)
                    + "]"; // 5 digits are apprx. 1m resolution
        }

        pointGeoJSON += "]]}}";
        MinMaxPair minMaxElev = elevationModel.getMaxElevation(Convert.toWWSector(s));
        double bufferToSectorInMeter = 20;
        /*
        Position takeoff = currentFlightplan.getLegacyFlightplan().getTakeoffPosition();
        LocalDateTime dateStart = LocalDateTime.now();
        LocalDateTime dateEnd = LocalDateTime.now().plus(Duration.ofHours(1));
        try {
            dateEnd =
                dateEnd.plus(
                    Duration.ofSeconds(
                        Math.round(currentFlightplan.getLegacyFlightplan().getFPsim().getSimResult().flightTime)));
        } catch (Exception e) {
            logger.warn("cant add estimated flight duration to landing time", e);
        }

        String start = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateStart);
        String end = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateEnd);
        String pilot = "IMC_Pilot";

        try {
            pilot +=
                ":"
                    + DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .fullNameInSupportProperty()
                        .get();
        } catch (Exception e) {
            logger.info("cant extract pilot name from settings", e);
        }
        */

        try {
            URI deepLink =
                new URIBuilder()
                    .setScheme("https")
                    .setHost("www.airmap.com")
                    .setPath("create_flight/v1/")
                    .addParameter("geometry", pointGeoJSON)
                    // .addParameter("takeoff_latitude", "" + takeoff.latitude.degrees) // not applicable for poylgone
                    // missions
                    // .addParameter("takeoff_longitude", "" + takeoff.longitude.degrees) // not applicable for poylgone
                    // missions
                    .addParameter("altitude", "" + (currentMission.getMaxElev().getAsDouble() - minMaxElev.min))
                    // .addParameter("pilot_id", pilot) //set by the app
                    // .addParameter("start_time", start) //not configureable
                    // .addParameter("end_time", end)//not configureable
                    .addParameter("buffer", "" + bufferToSectorInMeter) // only used for points, not for poylgones
                    .build();
            URI dynamicLink =
                new URIBuilder(
                        URI.create(
                            "https://xjy5t.app.goo.gl/?apn=com.airmap.airmap&isi=1042824733&ibi=com.airmap.AirMap&efr=1&ofl=https://www.airmap.com/airspace-authorization/&utm_source=partner&utm_medium=deeplink&utm_campaign=laanc"))
                    .addParameter("link", deepLink.toString())
                    .build();

            logger.info(
                "height: "
                    + (currentMission.getMaxElev().getAsDouble() - minMaxElev.min)
                    + " => "
                    + currentMission.getMaxElev().getAsDouble()
                    + "-"
                    + minMaxElev.min);
            logger.info("deepLink: " + deepLink);
            logger.info("dynamicLink: " + dynamicLink);

            LaancApprovalQr qr = generateQrCode(dynamicLink);
            return qr;
        } catch (Exception e) {
            logger.error("Error generating LAANC approval URL", e);
        }

        return null;
    }

    private LaancApprovalQr generateQrCode(URI dynamicLink) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(dynamicLink.toString(), BarcodeFormat.QR_CODE, 500, 500);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return new LaancApprovalQr(image, dynamicLink);
        } catch (WriterException e) {
            logger.error("Error generating LAANC approval URL", e);
        }

        return null;
    }

}
