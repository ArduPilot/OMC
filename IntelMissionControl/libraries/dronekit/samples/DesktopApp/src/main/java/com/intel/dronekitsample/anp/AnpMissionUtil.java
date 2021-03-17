package com.intel.dronekitsample.anp;

import com.intel.dronekitsample.anp.AnpMission.Action;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import java.util.List;

public class AnpMissionUtil {
    private static final double FROM_MILLIS = 1.0/1000.0;
    private static final double DELTA = 0.001;

    public static boolean equals(double d1, double d2) {
        return d1 == d2 || (Math.abs(d1 - d2) < DELTA);
    }

    public static Mission convertFromAnp(AnpMission anp) {
        Mission mission = new Mission();

        double speed = 0.0;
        List<AnpMission.Task> tasks = anp.getWaypoints();
        for (AnpMission.Task task : tasks) {
            if ("Single".equals(task.type)) {

                // generate speed mission item if necessary
                if (equals(task.maxSpeed, speed)) {
                    ChangeSpeed changeSpeed = new ChangeSpeed();
                    changeSpeed.setSpeed(task.maxSpeed);
                    mission.addMissionItem(changeSpeed);
                    speed = task.maxSpeed;
                }

                // generate waypoint
                Waypoint waypoint = new Waypoint();
                waypoint.setCoordinate(task.position);
                waypoint.setAcceptanceRadius(1.0);
                waypoint.setDelay(FROM_MILLIS * task.actionDelay);
                mission.addMissionItem(waypoint);
                // todo: something with task.postActionDelay

                // generate camera trigger if necessary
                switch (task.action) {
                    case Action.NONE:
                        break;
                    case Action.TRIGGER:
                        CameraTrigger trigger = new CameraTrigger();
                        trigger.setTriggerDistance(0.0);
                        mission.addMissionItem(trigger);
                }
            }
        }

        return mission;
    }
}
