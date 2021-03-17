/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class FilteratorTest {

    static class A {}

    static class Aa extends A {}

    static class B {}

    static class C {}

    @Test
    void InstanceofFilterator() {
        var list = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            list.add(new A());
            list.add(new Aa());
            list.add(new B());
            list.add(new C());
        }

        int count = 0;
        for (var it = new InstanceofFilterator<>(list, A.class); it.hasNext(); ++count) {
            it.next();
        }

        assertEquals(6, count);
    }

    @Test
    void ClassFilterator() {
        var list = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            list.add(new A());
            list.add(new Aa());
            list.add(new B());
            list.add(new C());
        }

        int count = 0;
        for (var it = new ClassFilterator<>(list, A.class); it.hasNext(); ++count) {
            it.next();
        }

        assertEquals(3, count);
    }

}
