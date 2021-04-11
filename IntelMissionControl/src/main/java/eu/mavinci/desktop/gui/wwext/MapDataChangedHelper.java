package eu.mavinci.desktop.gui.wwext;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.WWElevationModel;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.ReferencePoint;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.asyncfx.concurrent.Dispatcher;

/** Helps to update loaded missions when the map data changes */
public class MapDataChangedHelper implements IElevationModelUpdateHelper {

    private static final int MAX_TRIES_CNT = 20;

    private final ListProperty<FlightPlan> flightPlans = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IElevationModel elevationModel;

    @Inject
    public MapDataChangedHelper(
            IApplicationContext applicationContext,
            AirspacesProvidersSettings airspacesProvidersSettings,
            IElevationModel elevationModel,
            WWElevationModel wwElevationModel) {
        this.elevationModel = elevationModel;
        wwElevationModel.wwjElevationModelProperty().addListener(observable -> updateElevationsAndRecalculate());
        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightPlans.unbind();
                    if (newValue != null) {
                        flightPlans.bind(newValue.flightPlansProperty());
                    }
                });

        airspacesProvidersSettings
            .useAirspaceDataForPlanningProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightPlans
                        .stream()
                        .forEach(
                            (flightPlan -> {
                                if (flightPlan.recalculateOnEveryChangeProperty().get()) {
                                    flightPlan.doFlightplanCalculation();
                                } else {
                                    flightPlan.getLegacyFlightplan().getFPsim().tryStartRecomp();
                                }
                            }));
                });
    }

    @Override
    public void updateElevationsAndRecalculate() {
        flightPlans
            .stream()
            .forEach(
                flightPlan -> {
                    if (flightPlan != null) {
                        Flightplan flightplan = flightPlan.getLegacyFlightplan();
                        ReferencePoint refPoint = flightplan.getRefPoint();
                        if (refPoint.isDefined()) {
                            Dispatcher.background()
                                .runLaterAsync(
                                    () -> {
                                        double wgsAltitude = refPoint.getAltitudeWgs84();
                                        double newWgsAltitude = wgsAltitude;
                                        int counter = 0;
                                        while (newWgsAltitude == wgsAltitude && counter < MAX_TRIES_CNT) {
                                            newWgsAltitude =
                                                elevationModel.getElevationAsGoodAsPossible(refPoint.getLatLon());
                                            counter++;
                                        }
                                    })
                                .whenDone(
                                    () -> {
                                        if (flightPlan.recalculateOnEveryChangeProperty().get()) {
                                            flightplan.getRefPoint().updateAltitudeWgs84();
                                            flightplan.getTakeoff().updateAltitudeWgs84();
                                            flightplan.doFlightplanCalculation();
                                        }
                                    },
                                    Dispatcher.platform()::run);
                        }
                    }
                });
    }
}
