/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import java.util.Collection;
import java.util.IllegalFormatException;
import org.jetbrains.annotations.Nullable;

/**
 * Provides methods to test for static correctness. This is an intentional carbon copy of the Ensure class found in the
 * mavincidesktopbase package. The copy is needed, so klocwork can pick up the source code of the nonNull function.
 *
 * @author mstrauss
 */
public class Expect {

    /**
     * Verifies that a condition is true.
     *
     * @param condition The condition to be tested.
     * @param paramName The name of the parameter.
     * @throws IllegalArgumentException If the condition is false.
     */
    public static void isTrue(boolean condition, @Nullable String paramName) {
        if (!condition) {
            if (paramName == null) {
                paramName = "<unknown>";
            }

            throw new IllegalArgumentException("Value is not valid. Parameter name: " + paramName);
        }
    }

    /**
     * Verifies that a condition is true.
     *
     * @param condition The condition to be tested.
     * @param paramName The name of the parameter.
     * @param message The error message.
     * @throws IllegalArgumentException If the condition is false.
     */
    public static void isTrue(boolean condition, @Nullable String paramName, @Nullable String message) {
        if (!condition) {
            if (paramName == null) {
                paramName = "<unknown>";
            }

            if (message == null) {
                throw new IllegalArgumentException("Value is not valid. Parameter name: " + paramName);
            }

            if (message.endsWith(".")) {
                throw new IllegalArgumentException(message + " Parameter name: " + paramName);
            }

            throw new IllegalArgumentException(message + ". Parameter name: " + paramName);
        }
    }

    /**
     * Verifies that a condition is true.
     *
     * @param condition The condition to be tested.
     * @param paramName The name of the parameter.
     * @param message The error message.
     * @param messageParams Parameters for the error message, as if called by {@code String.format(message, params)}.
     * @throws IllegalArgumentException If the condition is false.
     */
    public static void isTrue(
            boolean condition,
            @Nullable String paramName,
            @Nullable String message,
            @Nullable Object... messageParams) {
        if (!condition) {
            if (paramName == null) {
                paramName = "<unknown>";
            }

            if (message == null) {
                throw new IllegalArgumentException("Value is not valid. Parameter name: " + paramName);
            }

            String formattedMessage;

            try {
                formattedMessage = String.format(message, messageParams);
            } catch (IllegalFormatException ex) {
                formattedMessage = message;
            }

            if (formattedMessage.endsWith(".")) {
                throw new IllegalArgumentException(formattedMessage + " Parameter name: " + paramName);
            }

            throw new IllegalArgumentException(formattedMessage + ". Parameter name: " + paramName);
        }
    }

    public static <T> void notNullOrEmpty(@Nullable T[] obj0, String name0) {
        if (obj0 == null) {
            throw new IllegalStateException("Reference cannot be null: " + name0);
        }

        if (obj0.length == 0) {
            throw new IllegalStateException("Array cannot be empty: " + name0);
        }
    }

    public static <T> void notNullOrEmpty(@Nullable Collection<T> obj0, String name0) {
        if (obj0 == null) {
            throw new IllegalStateException("Reference cannot be null: " + name0);
        }

        if (obj0.isEmpty()) {
            throw new IllegalStateException("Collection cannot be empty: " + name0);
        }
    }

    public static void notNull(@Nullable Object obj0, String name0) {
        if (obj0 == null) {
            throw new IllegalStateException("Reference cannot be null: " + name0);
        }
    }

    public static void notNull(@Nullable Object obj0, String name0, @Nullable Object obj1, String name1) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9,
            @Nullable Object obj10,
            String name10) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (obj10 == null) {
            names = names == null ? name10 : names + ", " + name10;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9,
            @Nullable Object obj10,
            String name10,
            @Nullable Object obj11,
            String name11) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (obj10 == null) {
            names = names == null ? name10 : names + ", " + name10;
        }

        if (obj11 == null) {
            names = names == null ? name11 : names + ", " + name11;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9,
            @Nullable Object obj10,
            String name10,
            @Nullable Object obj11,
            String name11,
            @Nullable Object obj12,
            String name12) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (obj10 == null) {
            names = names == null ? name10 : names + ", " + name10;
        }

        if (obj11 == null) {
            names = names == null ? name11 : names + ", " + name11;
        }

        if (obj12 == null) {
            names = names == null ? name12 : names + ", " + name12;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9,
            @Nullable Object obj10,
            String name10,
            @Nullable Object obj11,
            String name11,
            @Nullable Object obj12,
            String name12,
            @Nullable Object obj13,
            String name13) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (obj10 == null) {
            names = names == null ? name10 : names + ", " + name10;
        }

        if (obj11 == null) {
            names = names == null ? name11 : names + ", " + name11;
        }

        if (obj12 == null) {
            names = names == null ? name12 : names + ", " + name12;
        }

        if (obj13 == null) {
            names = names == null ? name13 : names + ", " + name13;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            String name0,
            @Nullable Object obj1,
            String name1,
            @Nullable Object obj2,
            String name2,
            @Nullable Object obj3,
            String name3,
            @Nullable Object obj4,
            String name4,
            @Nullable Object obj5,
            String name5,
            @Nullable Object obj6,
            String name6,
            @Nullable Object obj7,
            String name7,
            @Nullable Object obj8,
            String name8,
            @Nullable Object obj9,
            String name9,
            @Nullable Object obj10,
            String name10,
            @Nullable Object obj11,
            String name11,
            @Nullable Object obj12,
            String name12,
            @Nullable Object obj13,
            String name13,
            @Nullable Object obj14,
            String name14) {
        String names = null;

        if (obj0 == null) {
            names = names == null ? name0 : names + ", " + name0;
        }

        if (obj1 == null) {
            names = names == null ? name1 : names + ", " + name1;
        }

        if (obj2 == null) {
            names = names == null ? name2 : names + ", " + name2;
        }

        if (obj3 == null) {
            names = names == null ? name3 : names + ", " + name3;
        }

        if (obj4 == null) {
            names = names == null ? name4 : names + ", " + name4;
        }

        if (obj5 == null) {
            names = names == null ? name5 : names + ", " + name5;
        }

        if (obj6 == null) {
            names = names == null ? name6 : names + ", " + name6;
        }

        if (obj7 == null) {
            names = names == null ? name7 : names + ", " + name7;
        }

        if (obj8 == null) {
            names = names == null ? name8 : names + ", " + name8;
        }

        if (obj9 == null) {
            names = names == null ? name9 : names + ", " + name9;
        }

        if (obj10 == null) {
            names = names == null ? name10 : names + ", " + name10;
        }

        if (obj11 == null) {
            names = names == null ? name11 : names + ", " + name11;
        }

        if (obj12 == null) {
            names = names == null ? name12 : names + ", " + name12;
        }

        if (obj13 == null) {
            names = names == null ? name13 : names + ", " + name13;
        }

        if (obj14 == null) {
            names = names == null ? name14 : names + ", " + name14;
        }

        if (names != null) {
            throw new IllegalStateException("Reference cannot be null: " + names);
        }
    }

    public static void notNull(@Nullable Object obj0) {
        if (obj0 == null) {
            throw new IllegalStateException("Value 0 cannot be null.");
        }
    }

    public static void notNull(@Nullable Object obj0, @Nullable Object obj1) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(@Nullable Object obj0, @Nullable Object obj1, @Nullable Object obj2) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0, @Nullable Object obj1, @Nullable Object obj2, @Nullable Object obj3) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10,
            @Nullable Object obj11) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (obj11 == null) {
            mask |= 2048;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10,
            @Nullable Object obj11,
            @Nullable Object obj12) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (obj11 == null) {
            mask |= 2048;
        }

        if (obj12 == null) {
            mask |= 4096;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10,
            @Nullable Object obj11,
            @Nullable Object obj12,
            @Nullable Object obj13) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (obj11 == null) {
            mask |= 2048;
        }

        if (obj12 == null) {
            mask |= 4096;
        }

        if (obj13 == null) {
            mask |= 8192;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10,
            @Nullable Object obj11,
            @Nullable Object obj12,
            @Nullable Object obj13,
            @Nullable Object obj14) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (obj11 == null) {
            mask |= 2048;
        }

        if (obj12 == null) {
            mask |= 4096;
        }

        if (obj13 == null) {
            mask |= 8192;
        }

        if (obj14 == null) {
            mask |= 16384;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

    public static void notNull(
            @Nullable Object obj0,
            @Nullable Object obj1,
            @Nullable Object obj2,
            @Nullable Object obj3,
            @Nullable Object obj4,
            @Nullable Object obj5,
            @Nullable Object obj6,
            @Nullable Object obj7,
            @Nullable Object obj8,
            @Nullable Object obj9,
            @Nullable Object obj10,
            @Nullable Object obj11,
            @Nullable Object obj12,
            @Nullable Object obj13,
            @Nullable Object obj14,
            @Nullable Object obj15) {
        int mask = 0;

        if (obj0 == null) {
            mask |= 1;
        }

        if (obj1 == null) {
            mask |= 2;
        }

        if (obj2 == null) {
            mask |= 4;
        }

        if (obj3 == null) {
            mask |= 8;
        }

        if (obj4 == null) {
            mask |= 16;
        }

        if (obj5 == null) {
            mask |= 32;
        }

        if (obj6 == null) {
            mask |= 64;
        }

        if (obj7 == null) {
            mask |= 128;
        }

        if (obj8 == null) {
            mask |= 256;
        }

        if (obj9 == null) {
            mask |= 512;
        }

        if (obj10 == null) {
            mask |= 1024;
        }

        if (obj11 == null) {
            mask |= 2048;
        }

        if (obj12 == null) {
            mask |= 4096;
        }

        if (obj13 == null) {
            mask |= 8192;
        }

        if (obj14 == null) {
            mask |= 16384;
        }

        if (obj15 == null) {
            mask |= 32768;
        }

        if (mask > 0) {
            String indices = "";

            for (int i = 0; i < 32; ++i) {
                if ((mask & (1 << i)) > 0) {
                    indices += indices.isEmpty() ? i : (", " + i);
                }
            }

            if (indices.contains(",")) {
                throw new IllegalStateException("Values " + indices + " cannot be null.");
            } else {
                throw new IllegalStateException("Value " + indices + " cannot be null");
            }
        }
    }

}
