package com.intel.dronekitsample.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.function.Function;

public class UiUtils {

    public static <T> StringBinding defaultStringBind(ObjectProperty<T> property, String emptyString, Function<T, String> formatFun) {
        return Bindings.createStringBinding(() -> property.isNull().get() ? emptyString : formatFun.apply(property.get()), property);

    }

}
