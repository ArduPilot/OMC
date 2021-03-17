/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

class ViewModelTest {

    @Test
    void ViewModel_Fails_When_Public_Method_Returns_Unsupported_Type() throws IOException, URISyntaxException {
        String dir = Paths.get(getClass().getResource("/ViewModelTestClass.java").toURI()).getParent().toString();
        Main.scanDir(dir, true);
        Main.showEpilog();
    }

}
