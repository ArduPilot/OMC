/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

public interface InfoHandler {

    void handle(Linter linter, String message);

}
