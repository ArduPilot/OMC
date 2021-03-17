/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import java.nio.file.Path;
import java.nio.file.Paths;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class Converters {

    private Converters() {}

    public static <T, U> BidirectionalValueConverter<U, T> invert(BidirectionalValueConverter<T, U> converter) {
        return new BidirectionalValueConverter<U, T>() {
            @Override
            public U convertBack(T value) {
                return converter.convert(value);
            }

            @Override
            public T convert(U value) {
                return converter.convertBack(value);
            }
        };
    }

    public static BidirectionalValueConverter<Boolean, Boolean> not() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Boolean convert(Boolean value) {
                return value != null ? !value : null;
            }

            @Override
            public Boolean convertBack(Boolean value) {
                return value != null ? !value : null;
            }
        };
    }

    public static BidirectionalValueConverter<Integer, Number> intToNumber() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Integer value) {
                return value;
            }

            @Override
            public Integer convertBack(Number value) {
                return value != null ? value.intValue() : null;
            }
        };
    }

    public static BidirectionalValueConverter<Number, Integer> numberToInt() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Integer convert(Number value) {
                return value != null ? value.intValue() : null;
            }

            @Override
            public Number convertBack(Integer value) {
                return value;
            }
        };
    }

    public static BidirectionalValueConverter<Long, Number> longToNumber() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Long value) {
                return value;
            }

            @Override
            public Long convertBack(Number value) {
                return value != null ? value.longValue() : null;
            }
        };
    }

    public static BidirectionalValueConverter<Number, Long> numberToLong() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Long convert(Number value) {
                return value != null ? value.longValue() : null;
            }

            @Override
            public Number convertBack(Long value) {
                return value;
            }
        };
    }

    public static BidirectionalValueConverter<Double, Number> doubleToNumber() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Double value) {
                return value;
            }

            @Override
            public Double convertBack(Number value) {
                return value != null ? value.doubleValue() : null;
            }
        };
    }

    public static BidirectionalValueConverter<Number, Double> numberToDouble() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Double convert(Number value) {
                return value != null ? value.doubleValue() : null;
            }

            @Override
            public Number convertBack(Double value) {
                return value;
            }
        };
    }

    public static BidirectionalValueConverter<Float, Number> floatToNumber() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Float value) {
                return value;
            }

            @Override
            public Float convertBack(Number value) {
                return value != null ? value.floatValue() : null;
            }
        };
    }

    public static BidirectionalValueConverter<Number, Float> numberToFloat() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Float convert(Number value) {
                return value != null ? value.floatValue() : null;
            }

            @Override
            public Number convertBack(Float value) {
                return value;
            }
        };
    }

    public static <Q extends Quantity<Q>> BidirectionalValueConverter<Number, Quantity<Q>> numberToQuantity(
            Unit<Q> unit) {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convertBack(Quantity<Q> value) {
                return value.convertTo(unit).getValue();
            }

            @Override
            public Quantity<Q> convert(Number value) {
                return Quantity.of(value.doubleValue(), unit);
            }
        };
    }

    public static <Q extends Quantity<Q>> BidirectionalValueConverter<Quantity<Q>, Number> quantityToNumber(
            Unit<Q> unit) {
        return new BidirectionalValueConverter<>() {
            @Override
            public Number convert(Quantity<Q> value) {
                return value.convertTo(unit).getValue();
            }

            @Override
            public Quantity<Q> convertBack(Number value) {
                return Quantity.of(value, unit);
            }
        };
    }

    public static BidirectionalValueConverter<Path, String> pathToString() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Path convertBack(String value) {
                return value != null ? Paths.get(value) : null;
            }

            @Override
            public String convert(Path value) {
                return value != null ? value.toString() : null;
            }
        };
    }

    public static BidirectionalValueConverter<String, Path> stringToPath() {
        return new BidirectionalValueConverter<>() {
            @Override
            public Path convert(String value) {
                return value != null ? Paths.get(value) : null;
            }

            @Override
            public String convertBack(Path value) {
                return value != null ? value.toString() : null;
            }
        };
    }

}
