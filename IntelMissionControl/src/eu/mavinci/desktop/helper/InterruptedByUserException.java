/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

public class InterruptedByUserException extends Exception {

    private static final long serialVersionUID = -19110308550131026L;

    public InterruptedByUserException() {
        super("Cancelt by user");
    }
}
