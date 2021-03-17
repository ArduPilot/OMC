/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.map;

import com.sun.javafx.scene.NodeHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

@DefaultProperty("items")
public class ColorLegend extends Control {

    private final StyleableObjectProperty<Font> font =
        new StyleableObjectProperty<Font>(Font.getDefault()) {
            private boolean fontSetByCss = false;

            public void applyStyle(StyleOrigin var1, Font var2) {
                try {
                    this.fontSetByCss = true;
                    super.applyStyle(var1, var2);
                } catch (Exception var7) {
                    throw var7;
                } finally {
                    this.fontSetByCss = false;
                }
            }

            public void set(Font var1) {
                Font var2 = (Font)this.get();
                if (var1 != null) {
                    if (var1.equals(var2)) {
                        return;
                    }
                } else if (var2 == null) {
                    return;
                }

                super.set(var1);
            }

            protected void invalidated() {
                if (!this.fontSetByCss) {
                    NodeHelper.reapplyCSS(ColorLegend.this);
                }
            }

            public CssMetaData<ColorLegend, Font> getCssMetaData() {
                return ColorLegend.StyleableProperties.FONT;
            }

            public Object getBean() {
                return ColorLegend.this;
            }

            public String getName() {
                return "font";
            }
        };

    private final StyleableObjectProperty<Paint> textFill =
        new StyleableObjectProperty<Paint>(Color.BLACK) {
            @Override
            public CssMetaData<ColorLegend, Paint> getCssMetaData() {
                return ColorLegend.StyleableProperties.TEXT_FILL;
            }

            @Override
            public Object getBean() {
                return ColorLegend.this;
            }

            @Override
            public String getName() {
                return "textFill";
            }
        };

    private final StringProperty caption = new SimpleStringProperty();
    private final ListProperty<ColorLegendItem> items = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ObjectProperty<Paint> textFillProperty() {
        return textFill;
    }

    public Paint getTextFill() {
        return textFill.get();
    }

    public void setTextFill(Paint textFill) {
        this.textFill.set(textFill);
    }

    public StyleableObjectProperty<Font> fontProperty() {
        return font;
    }

    public Font getFont() {
        return font.get();
    }

    public void setFont(Font font) {
        this.font.set(font);
    }

    public StringProperty captionProperty() {
        return caption;
    }

    public String getCaption() {
        return caption.get();
    }

    public void setCaption(String caption) {
        this.caption.set(caption);
    }

    public ListProperty<ColorLegendItem> itemsProperty() {
        return items;
    }

    public ObservableList<ColorLegendItem> getItems() {
        return items.get();
    }

    public void setItems(ObservableList<ColorLegendItem> list) {
        items.set(list);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColorLegendSkin(this);
    }

    private static class StyleableProperties {
        private static final FontCssMetaData<ColorLegend> FONT =
            new FontCssMetaData<ColorLegend>("-fx-font", Font.getDefault()) {

                @Override
                public boolean isSettable(ColorLegend n) {
                    return n.font == null || !n.font.isBound();
                }

                @Override
                public StyleableProperty<Font> getStyleableProperty(ColorLegend n) {
                    return n.fontProperty();
                }
            };

        private static final CssMetaData<ColorLegend, Paint> TEXT_FILL =
            new CssMetaData<ColorLegend, Paint>("-fx-text-fill", PaintConverter.getInstance(), Color.BLACK) {
                @Override
                public boolean isSettable(ColorLegend n) {
                    return n.textFill == null || !n.textFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(ColorLegend n) {
                    return n.textFill;
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, FONT, TEXT_FILL);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return ColorLegend.StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

}
