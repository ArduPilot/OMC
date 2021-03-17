/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.intel.insight.Test
import java.time.Instant
import java.time.format.DateTimeFormatter

class DateTest

fun main() {
println(    DateTimeFormatter.ISO_INSTANT.format(Instant.now()))

}
