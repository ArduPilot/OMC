/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.intel.missioncontrol.helper.ILanguageHelper;
import java.lang.ref.WeakReference;
import javafx.beans.WeakListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;

public class StreamComboBoxHelper {
    static <T> void setupComboBox(ComboBox<T> comboBox, ILanguageHelper languageHelper) {
        StringBinding promptText =
            Bindings.createStringBinding(
                () -> {
                    if (comboBox.getItems().size() == 0)
                        return languageHelper.getString(StreamComboBoxHelper.class, "NoStream");
                    else return languageHelper.getString(StreamComboBoxHelper.class, "SelectStream");
                },
                comboBox.getItems());
        comboBox.setButtonCell(
            new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(promptText.get());
                    } else {
                        setText(item.toString());
                    }
                }
            });

        comboBox.promptTextProperty().bind(promptText);
    }

    static private class BidirectionalSingleSelectionModelBinding<T> implements ChangeListener<T>, WeakListener {
        private final WeakReference<Property<T>> refSelected;
        private final WeakReference<SingleSelectionModel<T>> refSelectionModel;
        private boolean updating = false;

        private BidirectionalSingleSelectionModelBinding(
                Property<T> refSelected, SingleSelectionModel<T> refSelectionModel) {
            this.refSelected = new WeakReference<>(refSelected);
            this.refSelectionModel = new WeakReference<>(refSelectionModel);
        }

        @Override
        public boolean wasGarbageCollected() {
            return (refSelected.get() == null || refSelectionModel.get() == null);
        }

        @Override
        public void changed(ObservableValue<? extends T> source, T oldValue, T newValue) {
            if (!updating) {
                final Property<T> selected = refSelected.get();
                final SingleSelectionModel<T> selectionModel = refSelectionModel.get();

                if (selected == null || selectionModel == null) {
                    if (selected != null) {
                        selected.removeListener(this);
                    }

                    if (selectionModel != null) {
                        selectionModel.selectedItemProperty().removeListener(this);
                    }
                } else {
                    try {
                        updating = true;
                        if (selected == source) {
                            selectionModel.select(newValue);
                        } else {
                            selected.setValue(newValue);
                        }
                    } catch (RuntimeException e) {
                        // We don't add any additional handling here, as it wouldn't help much
                        throw new RuntimeException(
                            "Bidirectional bind between Property "
                                + selected
                                + " and SingleSelectionModel "
                                + selectionModel
                                + " failed.");
                    } finally{
                        updating = false;
                    }
                }
            }
        }

        public void unbind() {
            final Property<T> selected = refSelected.get();
            final SingleSelectionModel<T> selectionModel = refSelectionModel.get();

            if (selected != null) selected.removeListener(this);
            if (selectionModel != null) selectionModel.selectedItemProperty().removeListener(this);
        }
    }

    static private class BidirectionalBinding<T> {
        private BidirectionalSingleSelectionModelBinding<T> binding;

        BidirectionalBinding(BidirectionalSingleSelectionModelBinding<T> binding) {
            this.binding = binding;
        }

        public void unbind() {
            if (binding != null) {
                binding.unbind();
                binding = null;
            }
        }
    }

    static <T> BidirectionalBinding<T> bindBidirectional(SingleSelectionModel<T> selectionModel, Property<T> selected) {
        BidirectionalSingleSelectionModelBinding<T> binding = new BidirectionalSingleSelectionModelBinding<>(selected, selectionModel);
        selectionModel.select(selected.getValue());
        selected.addListener(binding);
        selectionModel.selectedItemProperty().addListener(binding);
        return new BidirectionalBinding<>(binding);
    }



}
