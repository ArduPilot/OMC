/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.manualcontrol;

import com.intel.missioncontrol.concurrent.Dispatcher;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

public class JoystickReader {

    public static enum RC_MODES {
        MODE_1,
        MODE_2,
        MODE_3,
        MODE_4,
        PLANE;

        public static RC_MODES[] typicalModes = new RC_MODES[] {MODE_1, MODE_2};
    }

    private RC_MODES currentmode = RC_MODES.MODE_1;

    public Controller controller;
    int[] overWrite;

    public JoystickReader() {}

    /**
     * @param mode is the remote mode, MODE_1 through 4 are standard and PLANE outputs to plane comprehensible
     *     controls
     */
    public void setMode(RC_MODES mode) {
        currentmode = mode;
    }

    public RC_MODES getMode() {
        return currentmode;
    }

    public Controller getController() {
        return controller;
    }

    /**
     * This method is designed to find all channels available on a certain USB controller and returns them as a string
     * array of channel names
     *
     * @return String[]
     */
    public String[] getChannels() {
        if (controller == null) {
            return new String[PlaneConstants.MANUAL_SERVO_COUNT];
        } else {
            Component[] components = controller.getComponents();
            String[] channels = new String[components.length];
            for (int count = 0; count < components.length; count++) {
                channels[count] = components[count].getName();
            }

            return channels;
        }
    }

    /**
     * This method returns the raw value of every channel available to the selected remote and is useful for debugging
     * mostly
     *
     * @return float[] of all raw values in order in an array of PlaneConstants.SERVO_THROTTLE-11
     */
    public float[] getRawValues() {
        if (controller == null) {
            return new float[PlaneConstants.MANUAL_SERVO_COUNT];
        } else {
            poll();
            Component[] components = controller.getComponents();
            float[] values = new float[components.length];
            for (int count = 0; count < components.length; count++) {
                try {
                    values[count] = components[count].getPollData();
                } catch (Exception e) {
                    values[count] = 0;
                }
            }

            return values;
        }
    }

    public boolean hasChannelInput(int channelNo) {
        if (controller == null) {
            return false;
        }

        switch (channelNo) {
        case PlaneConstants.SERVO_THROTTLE:
        case PlaneConstants.SERVO_AILERON:
        case PlaneConstants.SERVO_ELEVATOR:
        case PlaneConstants.SERVO_RUDDER:
            return true;
        case PlaneConstants.SERVO_SWITCH_LANDING:
            if (controller.getName().equalsIgnoreCase("FeiYing Model PengFei Simulator - XTR+G2+FMS Controller")) {
                return true;
            } else if (controller.getName().equalsIgnoreCase("Gam Gam")) {
                return false;
            } else {
                return false;
            }
        default:
            return false;
        }
    }

    /**
     * getValues returns a list of useful comprehensive values and maps them into an array of TAERG (throttle, aileron,
     * elevator, rudder, gear) This array is also recustomizeable through mapping adjustment
     *
     * @return
     */
    public int[] getValues() {
        int[] values = new int[PlaneConstants.MANUAL_SERVO_COUNT];
        if (controller != null) {
            float[] raws = getRawValues();
            int throttle, aileron, elevator, rudder, gear;
            // System.out.println("name:"+controller.getName() + " "+raws.length);
            // for (float f :raws){
            // System.out.print(":"+f);
            // }
            // System.out.println();
            // System.out.println(raws[0]+":"+raws[1]+":"+raws[2]+":"+raws[3]+":"+raws[4]+":"+raws[5]);
            if (controller.getName().equalsIgnoreCase("FeiYing Model PengFei Simulator - XTR+G2+FMS Controller")) {

                // System.out.println(raws[0]+":"+raws[1]+":"+raws[2]+":"+raws[3]+":"+raws[4]+":"+raws[5]+":"+raws[5]);
                elevator = (int)(((raws[5] + 1) - 2) * -128);
                rudder = (int)((raws[8] + 1) * 128);
                throttle = 255 - (int)(((raws[4] + 1) - 2) * -128);
                aileron = (int)(((raws[PlaneConstants.SERVO_SWITCH_LANDING] + 1) - 2) * -128);
                gear = (int)((raws[3]) * 256);

                // THIS dongle supports different HW-switchable modes, but non of them gives ALL channels I need at the
                // same time...
                // } else if(controller.getName().equalsIgnoreCase("FeiYing Model KMODEL Simulator - XTR+G2+FMS
                // Controller"))
                // {
                // System.out.println(raws[0]+":"+raws[1]+":"+raws[2]+":"+raws[3]+":"+raws[4]+":"+raws[5]);
                // throttle = (int) (((raws[4]+1)-2)*-128);
                // rudder = (int) ((raws[8]+1)*128);
                // elevator = (int) (((raws[2]+1)-2)*-128);
                // aileron = (int) (((raws[PlaneConstants.SERVO_SWITCH_LANDING]+1)-2)*-128);
                // gear = (int) ((raws[3])*256);
                //
            } else if (controller.getName().equalsIgnoreCase("Gam Gam")) {
                throttle = 255 - (int)((raws[3] - 1) * -128);
                rudder = (int)((raws[8] - 1) * -128);
                elevator = (int)((raws[5] - 1) * -128);
                aileron = (int)((raws[4] + 1) * 128);
                gear = PlaneConstants.SERVO_THROTTLE;
            } else {
                throttle = (int)((raws[1] + 1) * 128);
                aileron = (int)((raws[2] + 1) * 128);
                elevator = (int)((raws[3] + 1) * 128);
                rudder = (int)((raws[4] + 1) * 128);
                gear = PlaneConstants.SERVO_THROTTLE;
            }

            switch (currentmode) {
            case MODE_1: // mode 1 TREAG
                values[PlaneConstants.SERVO_THROTTLE] = throttle;
                values[PlaneConstants.SERVO_AILERON] = aileron;
                values[PlaneConstants.SERVO_ELEVATOR] = elevator;
                values[PlaneConstants.SERVO_RUDDER] = rudder;
                values[PlaneConstants.SERVO_SWITCH_LANDING] = gear;
                break;
            case MODE_2: // mode 2 ERTAG
                values[PlaneConstants.SERVO_THROTTLE] = elevator;
                values[PlaneConstants.SERVO_AILERON] = aileron;
                values[PlaneConstants.SERVO_ELEVATOR] = throttle;
                values[PlaneConstants.SERVO_RUDDER] = rudder;
                values[PlaneConstants.SERVO_SWITCH_LANDING] = gear;
                break;
            case MODE_3: // mode 2 TAERG
                values[PlaneConstants.SERVO_THROTTLE] = throttle;
                values[PlaneConstants.SERVO_AILERON] = rudder;
                values[PlaneConstants.SERVO_ELEVATOR] = elevator;
                values[PlaneConstants.SERVO_RUDDER] = aileron;
                values[PlaneConstants.SERVO_SWITCH_LANDING] = gear;
                break;
            case PLANE: // mode 2 TAERG
                values[PlaneConstants.SERVO_THROTTLE] = rudder;
                values[PlaneConstants.SERVO_AILERON] = elevator;
                values[PlaneConstants.SERVO_ELEVATOR] = throttle;
                values[PlaneConstants.SERVO_RUDDER] = aileron;
                values[PlaneConstants.SERVO_SWITCH_LANDING] = gear;
                break;
            case MODE_4: // mode 2 EATRG
                values[PlaneConstants.SERVO_THROTTLE] = elevator;
                values[PlaneConstants.SERVO_AILERON] = rudder;
                values[PlaneConstants.SERVO_ELEVATOR] = throttle;
                values[PlaneConstants.SERVO_RUDDER] = aileron;
                values[PlaneConstants.SERVO_SWITCH_LANDING] = gear;
                break;
            }
        }

        // apply overwrites
        for (int i = 0; i != PlaneConstants.MANUAL_SERVO_COUNT; i++) {
            if (overWrite[i] != Integer.MIN_VALUE) {
                values[i] = overWrite[i];
            }
        }

        return values;
    }

    /**
     * This method is used internally and adds controllers found to a list
     *
     * @return An arraylist of controllers attached
     */
    public static ArrayList<ControllerConnection> searchForControllers() {
        Controller[] controllers = createDefaultEnvironment().getControllers();
        ArrayList<ControllerConnection> readers = new ArrayList<ControllerConnection>();
        for (int i = 0; i < controllers.length; i++) {
            Controller controller = controllers[i];
            if (controller.getType() == Controller.Type.STICK) {
                readers.add(new ControllerConnection(controller));
            }
        }

        readers.add(new ControllerConnection(null));
        return readers;
    }

    /**
     * This is an internaly used method because it uses reflection to preform the hardware scan needed for
     * getControllers()
     *
     * @return ControllerEnviorment
     */
    @SuppressWarnings("unchecked")
    private static ControllerEnvironment createDefaultEnvironment() {
        try {
            // Find constructor (class is package private, so we can't access it directly)
            Constructor<ControllerEnvironment> constructor =
                (Constructor<ControllerEnvironment>)
                    Class.forName("net.java.games.input.DefaultControllerEnvironment").getDeclaredConstructors()[0];
            // Constructor is package private, so we have to deactivate access control checks
            constructor.setAccessible(true);

            return constructor.newInstance();
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not rescan controllers", e);
            return ControllerEnvironment.getDefaultEnvironment();
        }
    }

    /** You'll need to call this in order to properly get values from the remote for inexplicable reasons. */
    public Boolean poll() {
        if (controller != null && !controller.poll()) {
            System.out.println("There's been an error reaching the controller!");
            return false;
        } else {
            return true;
        }
    }

    boolean isConnected;

    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * connecting to a certain Controller object or to a dummy object with referres to virtual FULL mouse+keyboard
     * input!
     *
     * @param controller
     */
    public synchronized void connect(ControllerConnection controller) {
        System.out.println("setConnected:" + true);
        if (isConnected) {
            return;
        }

        this.controllerConnection = controller;
        this.controller = controller.getController();
        overWrite = new int[PlaneConstants.MANUAL_SERVO_COUNT];
        for (int i = PlaneConstants.SERVO_THROTTLE; i != PlaneConstants.MANUAL_SERVO_COUNT; i++) {
            overWrite[i] = Integer.MIN_VALUE;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_THROTTLE)) {
            overWrite[PlaneConstants.SERVO_THROTTLE] = PlaneConstants.SERVO_VAL_MAX;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_AILERON)) {
            overWrite[PlaneConstants.SERVO_AILERON] = PlaneConstants.SERVO_VAL_CENTER;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_ELEVATOR)) {
            overWrite[PlaneConstants.SERVO_ELEVATOR] = PlaneConstants.SERVO_VAL_CENTER;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_RUDDER)) {
            overWrite[PlaneConstants.SERVO_RUDDER] = PlaneConstants.SERVO_VAL_CENTER;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_SWITCH_LANDING)) {
            overWrite[PlaneConstants.SERVO_SWITCH_LANDING] = PlaneConstants.SERVO_VAL_LANDING_RELEASED;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_SWITCH_AP_MANUAL)) {
            overWrite[PlaneConstants.SERVO_SWITCH_AP_MANUAL] = PlaneConstants.SERVO_VAL_AUTOPILOT;
        }

        if (!hasChannelInput(PlaneConstants.SERVO_SWITCH_AUTO_ASSISTED)) {
            overWrite[PlaneConstants.SERVO_SWITCH_AUTO_ASSISTED] = PlaneConstants.SERVO_VAL_AUTOMATIC;
        }

        // workerThread=new Thread(workerRunnable);
        // workerThread.setPriority(Thread.MIN_PRIORITY);
        // workerThread.start();
        Dispatcher.post(workerRunnable);
    }

    ControllerConnection controllerConnection;

    public ControllerConnection getControllerConnection() {
        return controllerConnection;
    }

    public synchronized void disconnect() {
        isConnected = false;
    }

    // Thread workerThread;

    Runnable workerRunnable =
        new Runnable() {

            @Override
            public void run() {
                System.out.println("start reading");
                try {
                    setConnectionState(true);

                    while (isConnected && poll()) {
                        final int[] values = getValues();
                        try {
                            Dispatcher.runOnUI(
                                new Runnable() {
                                    public void run() {
                                        for (IJoystickReaderListener listener : listeners) {
                                            listener.newDataAvaliable(values);
                                        }

                                        IAirplane plane = JoystickReader.this.plane;
                                        if (plane != null && plane.isWriteable()) {
                                            Vector<Integer> vec = new Vector<Integer>();
                                            for (int i : values) {
                                                vec.add(i);
                                            }

                                            while (vec.size() < PlaneConstants.MANUAL_SERVO_COUNT) {
                                                vec.add(0);
                                            }

                                            while (vec.size() > PlaneConstants.MANUAL_SERVO_COUNT) {
                                                vec.remove(vec.size() - 1);
                                            }

                                            plane.setManualServos(vec);
                                        }
                                    }

                                });
                        } catch (Exception e1) {
                            Debug.getLog().log(Level.WARNING, "problem distributing new JoystickValues", e1);
                        }

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                        }
                    }

                } catch (Exception e1) {
                    Debug.getLog().log(Level.WARNING, "problem in JoystickReader thread", e1);
                } finally {
                    setConnectionState(false);
                }
            }

        };

    private void setConnectionState(final boolean newState) {
        try {
            Dispatcher.runOnUI(
                    new Runnable() {
                        public void run() {
                            synchronized (JoystickReader.this) {
                                isConnected = newState;
                                // if (newState==false)workerThread=null;
                                for (IJoystickReaderListener listener : listeners) {
                                    listener.connectionStateChanged(isConnected);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "problem in JoystickReader connection state change to:" + newState, e);
        }
    }

    WeakListenerList<IJoystickReaderListener> listeners =
        new WeakListenerList<IJoystickReaderListener>("JoystickReader");

    public void addListener(IJoystickReaderListener l) {
        listeners.add(l);
    }

    public void addListenerAtBegin(IJoystickReaderListener l) {
        listeners.addAtBegin(l);
    }

    public void removeListener(IJoystickReaderListener l) {
        listeners.remove(l);
    }

    @Override
    public String toString() {
        return controller == null ? "virtual" : controller.toString();
    }

    public void setChannelOverwrite(int channel, int i) {
        overWrite[channel] = i;
    }

    IAirplane plane;

    public void sendDataToPlane(IAirplane plane) {
        this.plane = plane;
    }

}
