/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneEventActions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CEvent extends CAFlightplanstatement implements IFlightplanStatement, IMuteable {

    private static final AirplaneEventActions[] GPS_LOSS_POSSIBLE_ACTIONS = {
        AirplaneEventActions.positionHold, AirplaneEventActions.circleDown
    };
    private static final AirplaneEventActions[] SIGNAL_LOSS_POSSIBLE_ACTIONS = {
        AirplaneEventActions.ignore,
        AirplaneEventActions.returnToStart,
        AirplaneEventActions.jumpLanging,
        AirplaneEventActions.circleDown
    };
    private static final AirplaneEventActions[] BATTLOW_POSSIBLE_ACTIONS = {
        AirplaneEventActions.ignore,
        AirplaneEventActions.positionHold,
        AirplaneEventActions.returnToStart,
        AirplaneEventActions.jumpLanging,
        AirplaneEventActions.circleDown
    };

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 100;

    private final int maxDelay = 15 * 60;
    boolean recoveryDisableAble = true;
    boolean mute = false;
    private final String name;
    AirplaneEventActions action = AirplaneEventActions.ignore;
    protected int delay;
    boolean recover = true;
    int level = MIN_LEVEL;
    private List<AirplaneEventActions> possibleActions;
    private int minDelay = 0;
    boolean hasLevel = false;

    protected CEvent(
            CEventList parent,
            String name,
            AirplaneEventActions action,
            int delay,
            boolean hasLevel,
            int level,
            boolean recover) {
        this(parent, name);
        this.action = action;
        this.delay = delay;
        this.hasLevel = hasLevel;
        this.level = level;
        this.recover = recover;
    }

    protected CEvent(CEventList parent, String name) {
        super(parent);
        this.name = name;
        minDelay = 0;
        delay = 10;
        if (CEventList.NAME_GPSLOSS.equals(name)) {
            possibleActions = Arrays.asList(GPS_LOSS_POSSIBLE_ACTIONS);
            delay = 5;
            minDelay = 0;
            action = AirplaneEventActions.circleDown;
        } else if (CEventList.NAME_RCLOSS.equals(name)) {
            possibleActions = Arrays.asList(SIGNAL_LOSS_POSSIBLE_ACTIONS);
            delay = 10;
            minDelay = 5;
            recoveryDisableAble = false;
        } else if (CEventList.NAME_DATALOSS.equals(name)) {
            possibleActions = Arrays.asList(SIGNAL_LOSS_POSSIBLE_ACTIONS);
            delay = 30;
            minDelay = 20;
            recoveryDisableAble = false;
        } else if (CEventList.NAME_RCDATALOSS.equals(name)) {
            possibleActions = Arrays.asList(SIGNAL_LOSS_POSSIBLE_ACTIONS);
            delay = 30;
            minDelay = 20;
            recoveryDisableAble = false;
            action = AirplaneEventActions.returnToStart;
        } else { // BATTLOW
            possibleActions = Arrays.asList(BATTLOW_POSSIBLE_ACTIONS);
        }

        if (DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                != OperationLevel.DEBUG) {
            recoveryDisableAble = false;
        }

        if (parent == null) {
            minDelay = -1;
            delay = -1;
        }
    }

    @Override
    public String toString() {
        return "Event: " + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        CEvent other = (CEvent)obj;
        if (action != other.action) {
            return false;
        }

        if (delay != other.delay) {
            return false;
        }

        if (hasLevel != other.hasLevel) {
            return false;
        }

        if (level != other.level) {
            return false;
        }

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (recover != other.recover) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + delay;
        result = prime * result + (hasLevel ? 1231 : 1237);
        result = prime * result + level;
        result = prime * result + (recover ? 1231 : 1237);
        return result;
    }

    @Override
    public void informChangeListener() {
        if (mute) {
            return;
        }

        super.informChangeListener();
    }

    @Override
    public void setMute(boolean mute) {
        if (this.mute == mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            informChangeListener();
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public boolean isMute() {
        return mute;
    }

    public String getName() {
        return name;
    }

    public AirplaneEventActions[] getPossibleActions() {
        return possibleActions.toArray(new AirplaneEventActions[possibleActions.size()]);
    }

    public List<AirplaneEventActions> getActiveActions() {
        // System.out.println("activeActions From"+this);
        if (getParent() == null) {
            return possibleActions;
        }

        if (CEventList.NAME_GPSLOSS.equals(name)) {
            return possibleActions;
        } else if (CEventList.NAME_RCLOSS.equals(name) || CEventList.NAME_DATALOSS.equals(name)) {
            List<AirplaneEventActions> activeActions = new ArrayList<>();

            CEvent eventCombined = getParent().getEventByName(CEventList.NAME_RCDATALOSS);
            AirplaneEventActions actCombined = eventCombined == null ? null : eventCombined.getAction();
            // at most what is set for combined loss
            for (AirplaneEventActions act : possibleActions) {
                if (act == AirplaneEventActions.jumpLanging && !isLandingEnabled()) {
                    continue;
                }

                activeActions.add(act);
                if (act == actCombined) {
                    break;
                }
            }

            return activeActions;
        } else if (CEventList.NAME_RCDATALOSS.equals(name)) {
            // at least waht is set for each single loss
            List<AirplaneEventActions> activeActions = new ArrayList<>();

            CEvent eventRC = getParent().getEventByName(CEventList.NAME_RCLOSS);
            CEvent eventData = getParent().getEventByName(CEventList.NAME_DATALOSS);
            AirplaneEventActions actRC = eventRC != null ? eventRC.getAction() : null;
            AirplaneEventActions actData = eventData != null ? eventData.getAction() : null;
            // at most what is set for combined loss
            boolean foundRC = false;
            boolean foundData = false;
            for (AirplaneEventActions act : possibleActions) {
                if (act == actRC) {
                    foundRC = true;
                }

                if (act == actData) {
                    foundData = true;
                }

                if (act == AirplaneEventActions.jumpLanging && !isLandingEnabled()) {
                    continue;
                }

                if (foundData && foundRC) {
                    activeActions.add(act);
                }
            }

            return activeActions;
        } else { // BATTLOW
            // at most what is set for combined loss
            return possibleActions
                .stream()
                .filter(act -> act != AirplaneEventActions.jumpLanging || isLandingEnabled())
                .collect(Collectors.toList());
        }
    }

    private boolean isLandingEnabled() {
        CFlightplan ret = getFlightplan();
        Ensure.notNull(ret, "ret"); // getLandingPoint, getMode cannot be null
        return ret == null || ret.getLandingpoint().getMode().isAutoLanding();
    }

    public boolean isIgnore() {
        if (CEventList.NAME_GPSLOSS.equals(name)) {
            return action == AirplaneEventActions.positionHold || action == AirplaneEventActions.ignore;
        } else {
            return action == AirplaneEventActions.ignore;
        }
    }

    public AirplaneEventActions getAction() {
        return action;
    }

    public void setAction(AirplaneEventActions action) {
        List<AirplaneEventActions> activeActions = getActiveActions();

        // check if new is possible
        for (AirplaneEventActions other : activeActions) {
            // check Copter case
            if (action == other || action == AirplaneEventActions.returnToStartOnSafetyAltitude) {
                // check if current selection is ok
                if (action != this.action) {
                    this.action = action;
                    informChangeListener();
                }

                return; // everything is fine
            }
        }

        // restore some useful default
        this.action = activeActions.get(0);
        informChangeListener();
    }

    @Override
    public CEventList getParent() {
        return (CEventList)super.getParent();
    }

    public int getDelay() {
        return delay;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public boolean isRecoveryDisableable() {
        return recoveryDisableAble;
    }

    public void setDelay(int delay) {
        if (delay < minDelay) {
            delay = minDelay;
        } else if (delay > maxDelay) {
            delay = maxDelay;
        }

        if (delay == this.delay) {
            return;
        }

        this.delay = delay;
        informChangeListener();
    }

    public boolean isRecover() {
        return recover;
    }

    public void setRecover(boolean recover) {
        if (!recoveryDisableAble) {
            recover = true;
        }

        if (this.recover == recover) {
            return;
        }

        this.recover = recover;
        informChangeListener();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < MIN_LEVEL) {
            level = MIN_LEVEL;
        } else if (level > MAX_LEVEL) {
            level = MAX_LEVEL;
        }

        if (this.level == level) {
            return;
        }

        this.level = level;
        informChangeListener();
    }

    public boolean hasLevel() {
        return hasLevel;
    }

    public void setHasLevel(boolean hasLevel) {
        if (this.hasLevel == hasLevel) {
            return;
        }

        this.hasLevel = hasLevel;
        informChangeListener();
    }

    public void overwriteFromOther(CEvent other) {
        if (!other.name.equals(name)) {
            return;
        }

        setMute(true);

        action = other.action;
        delay = other.delay;
        hasLevel = other.hasLevel;
        level = other.level;
        recover = other.recover;

        setMute(false);
    }

    public boolean isAvaliable() {
        if (CEventList.NAME_BATLOW.equals(name)) {
            return DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG;
        } else {
            return true;
        }
    }

    public void fixIt() {
        setDelay(getDelay());
        if (hasLevel()) {
            setLevel(getLevel());
        }

        setRecover(isRecover());
        setAction(getAction());
    }
}
