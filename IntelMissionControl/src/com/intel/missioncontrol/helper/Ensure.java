/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides methods to test for static correctness.
 *
 * @author mstrauss
 */
public class Ensure {

    @EnsuresNonNull("#1")
    public static void notNull(@Nullable Object obj0, String name0) {
        if (obj0 == null) {
            throw new IllegalStateException("Reference cannot be null: " + name0);
        }
    }

    @EnsuresNonNull({"#1", "#3"})
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

    @EnsuresNonNull({"#1", "#3", "#5"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19", "#21"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19", "#21", "#23"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19", "#21", "#23", "#25"})
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

    @EnsuresNonNull({"#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19", "#21", "#23", "#25", "#27"})
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

    @EnsuresNonNull({
        "#1", "#3", "#5", "#7", "#9", "#11", "#13", "#15", "#17", "#19", "#21", "#23", "#25", "#27", "#29"
    })
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

    @EnsuresNonNull("#1")
    public static void notNull(@Nullable Object obj0) {
        if (obj0 == null) {
            throw new IllegalStateException("Value 0 cannot be null.");
        }
    }

    @EnsuresNonNull({"#1", "#2"})
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

    @EnsuresNonNull({"#1", "#2", "#3"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11", "#12"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11", "#12", "#13"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11", "#12", "#13", "#14"})
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

    @EnsuresNonNull({"#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11", "#12", "#13", "#14", "#15"})
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

    @EnsuresNonNull({
        "#1", "#2", "#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10", "#11", "#12", "#13", "#14", "#15", "#16"
    })
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
