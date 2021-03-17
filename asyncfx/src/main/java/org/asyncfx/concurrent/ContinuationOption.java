/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

enum ContinuationOption {
    ALWAYS,
    SUCCESS,
    FAILURE,
    CANCELLED,
    NOT_CANCELLED;

    boolean continueOnSuccess() {
        return this == ALWAYS || this == SUCCESS || this == NOT_CANCELLED;
    }

    boolean continueOnFailure() {
        return this == ALWAYS || this == FAILURE || this == NOT_CANCELLED;
    }

    boolean continueOnCancelled() {
        return this == ALWAYS || this == CANCELLED;
    }

    boolean continuesWhen(ContinuationOption continuationOption) {
        return this == ALWAYS
            || continuationOption == SUCCESS && continueOnSuccess()
            || continuationOption == FAILURE && continueOnFailure()
            || continuationOption == CANCELLED && continueOnCancelled();
    }
}
