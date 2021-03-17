/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.geospatial.GeoMath;

public interface QuantityArithmetic {

    QuantityArithmetic DEFAULT =
        new QuantityArithmetic() {
            @Override
            public VariantQuantity add(VariantQuantity a, VariantQuantity b) {
                return a.add(b);
            }

            @Override
            public VariantQuantity subtract(VariantQuantity a, VariantQuantity b) {
                return a.subtract(b);
            }
        };

    QuantityArithmetic LATITUDE =
        new QuantityArithmetic() {
            @Override
            public VariantQuantity add(VariantQuantity a, VariantQuantity b) {
                return GeoMath.addLat(a, b);
            }

            @Override
            public VariantQuantity subtract(VariantQuantity a, VariantQuantity b) {
                return GeoMath.subtractLat(a, b);
            }
        };

    QuantityArithmetic LONGITUDE =
        new QuantityArithmetic() {
            @Override
            public VariantQuantity add(VariantQuantity a, VariantQuantity b) {
                return GeoMath.addLon(a, b);
            }

            @Override
            public VariantQuantity subtract(VariantQuantity a, VariantQuantity b) {
                return GeoMath.subtractLon(a, b);
            }
        };

    VariantQuantity add(VariantQuantity a, VariantQuantity b);

    VariantQuantity subtract(VariantQuantity a, VariantQuantity b);

}
