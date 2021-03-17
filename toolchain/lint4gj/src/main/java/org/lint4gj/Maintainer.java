/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Maintainer {

    public static final Maintainer NONE = new Maintainer("none", null);

    private final String name;
    private final Optional<String> email;

    private Maintainer(String name, String email) {
        this.name = name;
        this.email = Optional.ofNullable(email);
    }

    static Maintainer parse(String text) {
        if (!text.contains("(")) {
            return new Maintainer(text.trim(), null);
        }

        text = text.trim();
        int idx = text.indexOf("(");
        String name = text.substring(0, idx);
        String email = text.substring(idx + 1, text.length() - 1);
        return new Maintainer(name.trim(), email.trim());
    }

    public String getName() {
        return name;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public boolean equalsName(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Maintainer)) {
            return false;
        }

        return name.equalsIgnoreCase(((Maintainer)obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return email.map(s -> name + "(" + s + ")").orElse(name);
    }

}
