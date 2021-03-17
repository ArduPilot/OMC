/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Benachrichtigungsmoeglichkeit bei neuen Loesungen
 *
 * @author Marco Moeller
 */
public interface AlgoCallBack {
    /**
     * Benachrichtigen, das neue Loesung vorhanden
     *
     * @param next naechse Loesung
     */
    void NewSolution(Object next);
}
