/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence;

import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(Throwable throwable) {
        super(throwable);
    }

    public PersistenceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public static PersistenceException fromException(Exception exception) {
        if (exception instanceof FileAlreadyExistsException) {
            return new PersistenceException("The file already exists.", exception);
        } else if (exception instanceof NoSuchFileException) {
            return new PersistenceException("The file cannot be found.", exception);
        } else if (exception instanceof AccessDeniedException) {
            return new PersistenceException("Access to the file was denied.", exception);
        }

        return new PersistenceException(exception);
    }

}
