/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Provides native interface to the Asctec SD card log parser.
 *
 * @author elena
 */
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public interface IFalconSDLog {

    void get_PhotoTags(String path, IntByReference numOfElements, PointerByReference structs, AntennaInformation info, IntByReference num);

    void free_struct(Pointer struct, int numElements);

}
