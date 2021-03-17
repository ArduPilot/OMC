package com.intel.dronekitsample.model;

import android.net.Uri;
import com.intel.dronekitsample.anp.AnpLoader;
import com.intel.dronekitsample.anp.AnpMissionUtil;
import com.intel.dronekitsample.ui.MissionMapView;
import com.intel.dronekitsample.ui.MissionView;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.State;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.droidplanner.services.android.impl.utils.MissionUtils;

import javax.swing.*;
import java.io.File;

public class MissionController {
    private final DroneModel drone;
    private final MissionView view;
    private final MissionMapView mapView;

    private ObjectProperty<Mission> missionProperty = new SimpleObjectProperty<>();

    public MissionController(DroneModel dm, MissionView view, MissionMapView missionMapView) {
        this.drone = dm;
        this.view = view;
        this.mapView = missionMapView;
        init();
    }

    private void init() {
        view.setActionsCallback(actions);

        view.aircraftConnectedProperty().bind(Bindings.createBooleanBinding(() -> drone.getAttributes().getState().isConnected(),
                drone.getAttributes().stateProperty()));

        drone.getAttributes().stateProperty().addListener((i) -> {
            State state = drone.getAttributes().stateProperty().get();

            System.out.println("state: "+state);
        });
        drone.getAttributes().missionProperty().addListener((i) -> {
            Mission mission = drone.getAttributes().missionProperty().get();

            System.out.println("mission: "+mission);

        });
    }

    private final MissionView.Actions actions = new MissionView.Actions() {

        @Override
        public void onPickerSelected(File selectedFile) {

            if (selectedFile != null) {
                view.fileProperty().set(selectedFile);
                view.labelProperty().set("Selected");

                Task<Mission> task = new Task<Mission>() {
                    @Override
                    protected Mission call() throws Exception {
                        boolean anpFile = selectedFile.getName().toLowerCase().matches(".*[.](?:zip|anp|json)");
                        if (anpFile) {
                            return AnpMissionUtil.convertFromAnp(AnpLoader.decode(selectedFile));
                        } else {
                            return MissionUtils.loadMission(drone.getContext(), Uri.fromFile(selectedFile));
                        }
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();

                        final Mission mission = getValue();
                        System.err.println("loaded mission: " + mission);
                        Platform.runLater(() -> {
                            view.labelProperty().set("W " + mission.getMissionItems().size());
                            view.missionProperty().set(mission);
                            missionProperty.set(mission);

                            if (mapView == null) return;

                            SwingUtilities.invokeLater(() -> {

                                mapView.updateMission(mission);
                            });
                        });

                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        System.err.println("error loading: " + getException().getMessage());
                        Platform.runLater(() -> {
                            view.labelProperty().set("E" + getException().getMessage());
                        });

                    }
                };
                drone.service.submit(task);

            }
        }

        @Override
        public void onPauseToggled() {
            ObjectProperty<MissionView.State> state = view.stateProperty();

            switch (state.get()) {
                case EXECUTING:
                    state.set(MissionView.State.PAUSED);
                    break;
                case PAUSED:
                    state.set(MissionView.State.NONE);
                    break;
            }
        }

        @Override
        public void onStartStopToggled() {
            ObjectProperty<MissionView.State> state = view.stateProperty();

            switch (state.get()) {
                case READY:
                case PAUSED:
                    startMission();
                    state.set(MissionView.State.EXECUTING);
                    break;

                case EXECUTING:
                    state.set(MissionView.State.PAUSED);
                    break;
            }

        }
    };

    private void startMission() {
        Mission mission = missionProperty.get();
        if (mission == null) {
            System.err.println("can't start mission");
            return;
        }

        drone.loadMission(mission)
                .thenCompose((f) -> drone.startMission())
                .thenRun(() -> System.out.println(">>> mission started"));
    }


}
