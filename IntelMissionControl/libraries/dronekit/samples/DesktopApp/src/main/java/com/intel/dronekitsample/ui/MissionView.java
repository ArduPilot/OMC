package com.intel.dronekitsample.ui;

import com.intel.dronekitsample.AppController;
import com.intel.dronekitsample.DroneTestApp;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intel.dronekitsample.ui.UiUtils.defaultStringBind;

public class MissionView extends ViewController {
    @FXML Hyperlink missionFile;
    @FXML Label statusLabel;
    @FXML Button startButton;
    @FXML Button pauseButton;
    @FXML ToggleButton infoToggle;

    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.NONE);
    private final StringProperty label = new SimpleStringProperty();
    private final ObjectProperty<Mission> mission = new SimpleObjectProperty<>();
    private final BooleanProperty aircraftConnected = new SimpleBooleanProperty(false);

    Stage missionStage;

    private Actions actions = new Actions();

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public State getState() {
        return state.get();
    }

    public ObjectProperty<State> stateProperty() {
        return state;
    }

    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public ObjectProperty<Mission> missionProperty() {
        return mission;
    }

    public boolean isAircraftConnected() {
        return aircraftConnected.get();
    }

    public BooleanProperty aircraftConnectedProperty() {
        return aircraftConnected;
    }

    public static class Actions {
        public void onPauseToggled() {}
        public void onStartStopToggled() {}
        public void onPickerSelected(File selectedFile) {}
    }

    public enum State {
        NONE,
        READY,
        PAUSED,
        EXECUTING
    }

    public void setActionsCallback(Actions actions) {
        this.actions = actions;
    }

    @Override
    public void doInitialize() {
        missionFile.textProperty().bind(defaultStringBind(file, "select file", File::getName));

        infoToggle.disableProperty().bind(state.isEqualTo(State.NONE));

        pauseButton.textProperty().set("Pause");
        pauseButton.disableProperty().bind(state.isNotEqualTo(State.EXECUTING).and(aircraftConnected.not()));
        startButton.disableProperty().bind(state.isEqualTo(State.NONE).and(aircraftConnected.not()));

        missionFile.disableProperty().bind(Bindings.createBooleanBinding(
                () -> state.get() == State.EXECUTING || state.get() == State.PAUSED, state));
        statusLabel.textProperty().bind(label);

        startButton.textProperty().bind(Bindings.createStringBinding(() -> {
            switch (state.get()) {
                case EXECUTING:
                    return "Stop";
                default:
                    return "Start";
            }
        }, state));

        file.addListener(
                (observable, oldValue, newValue) -> { if (newValue != null) state.set(State.READY);});

        infoToggle.disableProperty().bind(mission.isNull());
    }

    public void pauseMission(ActionEvent actionEvent) {
        actions.onPauseToggled();
    }

    public void startStopMission(ActionEvent actionEvent) {
        actions.onStartStopToggled();
    }

    public void pickMission(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File last = file.get();
        if (last != null) {
            fileChooser.setInitialDirectory(last.getParentFile());
        }
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Waypoint Files", "*.txt", "*.wpl"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(AppController.primaryWindow());
        actions.onPickerSelected(selectedFile);
    }

    public void toggleMissionWindow(ActionEvent actionEvent) {
        if (missionStage == null || !missionStage.isShowing()) {
            // open
            showMission(mission.get());
            infoToggle.setSelected(true);
        } else {
            // close
            if (missionStage == null) return;
            missionStage.close();
            missionStage = null;
        }
    }


    // mission popup

    public static String missionItemPosition(MissionItem missionItem) {
        if (missionItem instanceof MissionItem.SpatialItem) {
            LatLongAlt c = ((MissionItem.SpatialItem) missionItem).getCoordinate();
            return String.format("%3.1f, %3.1f / %3.1f m", c.getLatitude(), c.getLongitude(), c.getAltitude());
        } else {
            return "";
        }
    }

    public void showMission(Mission mission) {
        if (missionStage == null) {
            missionStage = new Stage();
            Window window = infoToggle.getScene().getWindow();
            missionStage.setX(window.getX() - 600);
            missionStage.setY(window.getY());
        }
        missionStage.setTitle("Mission " + ((file.get() == null) ? file.get().getName() : ""));

        Scene scene = new Scene(createTableView(mission), 600, 400);
        missionStage.setScene(scene);
        missionStage.show();
        missionStage.setOnCloseRequest((e) -> {
            infoToggle.setSelected(false);
            System.out.println("closing window");
        });
    }

    public static class MissionViewItem {
        final MissionItem item;
        final IntegerProperty order;

        public MissionViewItem(MissionItem missionItem, int order) {
            this.item = missionItem.clone();
            this.order = new SimpleIntegerProperty(order);
        }

        public IntegerProperty orderProperty() {
            return order;
        }
    }

    private static String strip(String str) {
        Pattern p = Pattern.compile("[^{]+\\{(.*)}\\s*");
        Matcher m = p.matcher(str);
        if (m.matches()) {
            return m.group(1);
        } else {
            return str;
        }
    }

    private TableView createTableView(Mission mission) {
        List<MissionItem> missionItems = mission.getMissionItems();
        List<MissionViewItem> viewItems = new ArrayList<>(missionItems.size());

        int i = 0;
        for (MissionItem missionItem : missionItems) {
            viewItems.add(new MissionViewItem(missionItem, i++));
        }

        final ObservableList<MissionViewItem> data = FXCollections.observableArrayList(viewItems);

        TableColumn<MissionViewItem, Integer> orderCol = new TableColumn<>();
        orderCol.setText("#");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));

        TableColumn<MissionViewItem, String> typeCol = new TableColumn<>();
        typeCol.setText("type");
        typeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().item.getType().toString()));

        TableColumn<MissionViewItem, String> posCol = new TableColumn<>();
        posCol.setText("position");
        posCol.setCellValueFactory(param -> new SimpleStringProperty(missionItemPosition(param.getValue().item)));
        posCol.setMinWidth(80);
        posCol.setMinWidth(120);

        TableColumn<MissionViewItem, String>  strCol = new TableColumn<>();
        strCol.setText("string");
        strCol.setStyle("-fx-font-size: smaller;");
        strCol.setCellValueFactory(param -> new SimpleStringProperty(
                strip( param.getValue().item.toString()) ));
        strCol.setMinWidth(150);
        strCol.setPrefWidth(300);


        final TableView<MissionViewItem> tableView = new TableView<>();
        tableView.setItems(data);
        tableView.getColumns().addAll(orderCol, typeCol, posCol, strCol);
        return tableView;
    }


}
