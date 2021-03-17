/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

enum LockedCollectionType {
    WRITABLE,
    EXPLICIT_READONLY,
    NESTED_READONLY;

    private static final String NO_NESTED_MODIFICATION =
        "The collection cannot be modified: a write lock was acquired in an outer scope.";

    private static final String NO_EXPLICIT_MODIFICATION = "The collection cannot be modified.";

    boolean isReadOnly() {
        return this == EXPLICIT_READONLY || this == NESTED_READONLY;
    }

    String getNoModificationReason() {
        if (this == EXPLICIT_READONLY) {
            return NO_EXPLICIT_MODIFICATION;
        }

        if (this == NESTED_READONLY) {
            return NO_NESTED_MODIFICATION;
        }

        throw new IllegalStateException();
    }
}
