/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.crypto;

import java.io.IOException;

public class CryptoHelperException extends IOException {
    private static final long serialVersionUID = 1L;

    public CryptoHelperException() {
        super();
    }

    public CryptoHelperException(String message) {
        super(message);
    }
}
