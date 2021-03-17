/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

class AsyncTest {

    private String formatError(Linter linter, String message) {
        return "[lint4gj::" + linter.getClass().getSimpleName() + "] " + message;
    }

    private void formatInfo(Linter linter, String message) {
        System.out.println("[lint4gj::" + linter.getClass().getSimpleName() + "] " + message);
    }

    @Test
    void AsyncMethod_Naming_Is_Consistent() throws IOException, URISyntaxException {
        String source = Files.readString(Paths.get(getClass().getResource("/AsyncTestClass.java").toURI()));

        int[] count = new int[1];
        ErrorHandler handler =
            (linter, msg) -> {
                count[0]++;
                System.err.println(formatError(linter, msg));
            };

        new CodeScanner(source, null, null).scan(handler, this::formatInfo);
        Assertions.assertEquals(2, count[0]);
    }

}
