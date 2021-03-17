/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.manualcontrol;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import net.java.games.input.Controller;

public class ControllerConnection {

    Controller controller;

    public ControllerConnection(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    public boolean isVirtual() {
        return controller == null;
    }

    @Override
    public String toString() {
        final String KEY = "eu.mavinci.desktop.gui.doublepanel.manualcontrol.ManualControlVisInput";
        return controller != null
            ? controller.getName()
            : DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY + ".virtualInput");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controller == null) ? 0 : controller.hashCode());
        return result;
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

        ControllerConnection other = (ControllerConnection)obj;
        if (controller == null) {
            if (other.controller != null) {
                return false;
            }
        } else if (!controller.equals(other.controller)) {
            return false;
        }

        return true;
    }

}
