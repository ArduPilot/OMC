/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import java.util.function.Consumer;

class ExpectedDiagnosticHandler implements Consumer<String> {

    private final SuppressionInfo suppressionInfo;
    private final Consumer<String> handler;
    private boolean occurred;

    ExpectedDiagnosticHandler(SuppressionInfo suppressionInfo, Consumer<String> handler) {
        this.suppressionInfo = suppressionInfo;
        this.handler = handler;
    }

    @Override
    public void accept(String message) {
        occurred = true;

        if (!suppressionInfo.isLocalOrInherited()) {
            handler.accept(message);
        }
    }

    boolean hasOccurred() {
        return occurred;
    }

}
