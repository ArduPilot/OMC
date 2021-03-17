/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.test.rules;

import eu.mavinci.desktop.bluetooth.BluetoothManager;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.helper.MavinciEnvInitializer;
import java.io.File;
import java.io.IOException;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MavinciInitializer extends ExternalResource {
    private MavinciEnvInitializer mavinciEnvInitializer;
    private File sessionFolder;

    @Override
    protected void before() throws Throwable {
        sessionFolder = createTemporaryFolder();
    }

    @Override
    protected void after() {
        recursiveDelete(sessionFolder);
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }

        file.delete();
    }

    private File createTemporaryFolder() throws IOException {
        File createdFolder = File.createTempFile("mavinci", "");
        createdFolder.delete();
        createdFolder.mkdir();
        return createdFolder;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        final Runnable runnable;
        if (description.isSuite()) {
            // Set of tests - need to create MavinciEnvInitializer instance
            runnable =
                () -> {
                    mavinciEnvInitializer = new MavinciEnvInitializer();
                    mavinciEnvInitializer.init();
                    BluetoothManager.init();
                };
        } else if (description.isTest()) {
            // Single test - cleanup session
            runnable =
                () -> {
                    mavinciEnvInitializer.initSession();
                };
        } else {
            // Do nothing
            runnable = () -> {};
        }

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    runnable.run();

                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    public MavinciEnvInitializer getMavinciEnvInitializer() {
        return mavinciEnvInitializer;
    }
}
