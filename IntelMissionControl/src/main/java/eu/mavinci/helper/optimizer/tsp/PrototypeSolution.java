/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * (a).1 Repräsentiert zulässige Lösungen
 *
 * @author Marco Moeller
 */
public interface PrototypeSolution {

    /** @return andere Zulässige Lösung */
    Object OtherSolution();

    /** @return änderungsinformation / null entspricht keiner Änderung */
    Object ModificationInformation();

    /** @return Diese Lösung */
    Object ThisSolution();
}
