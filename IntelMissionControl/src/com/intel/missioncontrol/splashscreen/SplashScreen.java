/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.splashscreen;

import static com.intel.missioncontrol.utils.VersionProvider.BUILD_TIME_FORMAT;
import static com.intel.missioncontrol.utils.VersionProvider.PROPERTIES_APP_BRANCH;
import static com.intel.missioncontrol.utils.VersionProvider.PROPERTIES_BUILD_COMMIT_TIME;
import static com.intel.missioncontrol.utils.VersionProvider.PROPERTIES_FILE_NAME;

import com.intel.missioncontrol.Main;
import com.sun.jna.IntegerType;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Application {

    private MailslotServer mailslotServer;

    @Override
    public void start(Stage primaryStage) throws IOException {
        ProcessHandle parentProcess = ProcessHandle.of(Long.parseLong(System.getProperty("pid"))).orElse(null);
        if (parentProcess == null) {
            System.exit(0);
        }

        parentProcess.onExit().thenRun(() -> Platform.runLater(primaryStage::close));

        FXMLLoader loader = new FXMLLoader(SplashScreenView.class.getResource("SplashScreenView.fxml"));
        Region view = loader.load();
        SplashScreenView controller = loader.getController();
        controller.setVersion(getVersionString());

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(1200 / 2);
        primaryStage.setHeight(750 / 2);
        primaryStage.setX((bounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((bounds.getHeight() - primaryStage.getHeight()) / 2);

        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initOwner(primaryStage);
        stage.setScene(new Scene(view));
        stage.setWidth(1200 / 2);
        stage.setHeight(750 / 2);
        stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((bounds.getHeight() - stage.getHeight()) / 2);

        primaryStage.setOnCloseRequest(Event::consume);
        stage.setOnCloseRequest(Event::consume);

        primaryStage.show();
        stage.show();

        mailslotServer =
            new MailslotServer(
                System.getProperty("mailslot"),
                message -> {
                    double progress = Double.parseDouble(message);
                    if (progress == 1.0) {
                        Platform.runLater(primaryStage::close);
                    } else {
                        Platform.runLater(() -> controller.setProgress(progress));
                    }
                },
                exception -> Platform.runLater(() -> controller.setVersion("err: " + exception.getMessage())));
    }

    @Override
    public void stop() throws Exception {
        mailslotServer.close();
        super.stop();
    }

    private String getVersionString() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8.name());
            Properties properties = new Properties();
            properties.load(reader);
            return properties.getProperty(PROPERTIES_APP_BRANCH) + ".b" + getBuildCommitTimeAsLong(properties);
        } catch (IOException e) {
            return "";
        }
    }

    private long getBuildCommitTimeAsLong(Properties properties) {
        ZonedDateTime dateTime;
        try {
            dateTime =
                ZonedDateTime.parse(getBuildCommitTime(properties), DateTimeFormatter.ofPattern(BUILD_TIME_FORMAT));
        } catch (Exception e) {
            dateTime = LocalDateTime.of(2017, 7, 18, 19, 00).atZone(ZoneId.systemDefault());
        }

        return dateTime.toInstant().getEpochSecond();
    }

    private String getBuildCommitTime(Properties properties) {
        return properties.getProperty(PROPERTIES_BUILD_COMMIT_TIME);
    }

    public static void show(String mailslotName) throws IOException, URISyntaxException {
        var jna = IntegerType.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        var jnaPlatform = Kernel32.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        var sourceDir = SplashScreen.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

        Optional<String> executable = ProcessHandle.current().info().command();
        if (!executable.isPresent()) {
            throw new FileNotFoundException("javaw");
        }

        String cmdLine =
            "javaw -Dmailslot="
                + mailslotName
                + " -Dpid="
                + ProcessHandle.current().pid()
                + " -classpath \".;"
                + jna
                + ";"
                + jnaPlatform
                + ";"
                + sourceDir
                + "\" "
                + Main.class.getName();

        final int CREATE_NO_WINDOW = 0x08000000;
        final int CREATE_UNICODE_ENVIRONMENT = 0x00000400;
        WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
        WinBase.PROCESS_INFORMATION procInfo = new WinBase.PROCESS_INFORMATION();

        Kernel32.INSTANCE.CreateProcess(
            executable.get(),
            cmdLine,
            null,
            null,
            false,
            new WinDef.DWORD(CREATE_UNICODE_ENVIRONMENT | CREATE_NO_WINDOW),
            null,
            null,
            startupInfo,
            procInfo);
    }

}
