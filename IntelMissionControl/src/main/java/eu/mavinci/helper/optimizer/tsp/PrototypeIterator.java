/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * (a).2 Bietet die möglichkeit zyklisch durch alle benachbarten Lösungen durchzuiterieren
 *
 * @author Marco Moeller
 */
public interface PrototypeIterator {
    /**
     * Liefert zyklisch das nächste Element zurück
     *
     * @return naechster Nachbar
     */
    PrototypeSolution nextElement();

    /**
     * Lässt sich abfragen um zu erfahren ob man einmal durch ist
     *
     * @return true == es sind noch Nachbarn vorhanden
     */
    boolean hasMoreElements();
}
