/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.common.Optional;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import java.net.URL;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCombination;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a node in a menu. A hierarchy of MenuModel classes is used to create a UI representation of the menu. */
public class MenuModel {

    private static class CommandData {
        final Command command;
        final BooleanProperty canExecute = new SimpleBooleanProperty(true);
        final ObjectProperty<Runnable> runnable = new SimpleObjectProperty<>();

        CommandData() {
            this.command = new DelegateCommand(() -> runnable.get().run(), canExecute.and(runnable.isNotNull()));
        }
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item() {
        return new MenuModel(null, false, null, null, null, null);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text) {
        return new MenuModel(null, false, text, null, null, null);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, String statusText) {
        return new MenuModel(null, false, text, statusText, null, null);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text) {
        return new MenuModel(id, false, text, null, null, null);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, String statusText) {
        return new MenuModel(id, false, text, statusText, null, null);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, URL imageSource) {
        return new MenuModel(null, false, text, null, imageSource, null);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, String statusText, URL imageSource) {
        return new MenuModel(null, false, text, statusText, imageSource, null);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, URL imageSource) {
        return new MenuModel(id, false, text, null, imageSource, null);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, String statusText, URL imageSource) {
        return new MenuModel(id, false, text, statusText, imageSource, null);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, KeyCombination accelerator) {
        return new MenuModel(null, false, text, null, null, accelerator);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, String statusText, KeyCombination accelerator) {
        return new MenuModel(null, false, text, statusText, null, accelerator);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, KeyCombination accelerator) {
        return new MenuModel(id, false, text, null, null, accelerator);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, String statusText, KeyCombination accelerator) {
        return new MenuModel(id, false, text, statusText, null, accelerator);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(null, false, text, null, imageSource, accelerator);
    }

    /** Creates a new menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(String text, String statusText, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(null, false, text, statusText, imageSource, accelerator);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(Enum<?> id, String text, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(id, false, text, null, imageSource, accelerator);
    }

    /** Creates a new menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel item(
            Enum<?> id, String text, String statusText, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(id, false, text, statusText, imageSource, accelerator);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem() {
        return new MenuModel(null, true, null, null, null, null);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text) {
        return new MenuModel(null, true, text, null, null, null);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, String statusText) {
        return new MenuModel(null, true, text, statusText, null, null);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text) {
        return new MenuModel(id, true, text, null, null, null);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, String statusText) {
        return new MenuModel(id, true, text, statusText, null, null);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, URL imageSource) {
        return new MenuModel(null, true, text, null, imageSource, null);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, String statusText, URL imageSource) {
        return new MenuModel(null, true, text, statusText, imageSource, null);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, URL imageSource) {
        return new MenuModel(id, true, text, null, imageSource, null);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, String statusText, URL imageSource) {
        return new MenuModel(id, true, text, statusText, imageSource, null);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, KeyCombination accelerator) {
        return new MenuModel(null, true, text, null, null, accelerator);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, String statusText, KeyCombination accelerator) {
        return new MenuModel(null, true, text, statusText, null, accelerator);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, KeyCombination accelerator) {
        return new MenuModel(id, true, text, null, null, accelerator);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, String statusText, KeyCombination accelerator) {
        return new MenuModel(id, true, text, statusText, null, accelerator);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(null, true, text, null, imageSource, accelerator);
    }

    /** Creates a new checkable menu item, identified by an enum value, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(String text, String statusText, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(null, true, text, statusText, imageSource, accelerator);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(Enum<?> id, String text, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(id, true, text, null, imageSource, accelerator);
    }

    /** Creates a new checkable menu item, which is a leaf node in a menu hierarchy. */
    public static MenuModel checkItem(
            Enum<?> id, String text, String statusText, URL imageSource, KeyCombination accelerator) {
        return new MenuModel(id, true, text, statusText, imageSource, accelerator);
    }

    /**
     * Establishes a check group for the specified menu items. If the menu items are checkable menu items, only one of
     * the menu items can be checked at any time.
     */
    public static MenuModel checkGroup(MenuModel... items) {
        MenuModel model = build(items);
        model.isGroup = false;
        model.isCheckGroup = true;
        return model;
    }

    /**
     * Establishes a check group for the specified menu items. If the menu items are checkable menu items, only one of
     * the menu items can be checked at any time.
     */
    public static MenuModel checkGroup(Enum<?> id, MenuModel... items) {
        MenuModel model = build(id, items);
        model.isGroup = false;
        model.isCheckGroup = true;
        return model;
    }

    /**
     * Groups the specified menu items. A UI representation might use horizontal separators to visually distinguish
     * between different groups of menu items.
     */
    public static MenuModel group(MenuModel... items) {
        MenuModel model = build(items);
        model.isGroup = true;
        return model;
    }

    /**
     * Groups the specified menu items. A UI representation might use horizontal separators to visually distinguish
     * between different groups of menu items.
     */
    public static MenuModel group(Enum<?> id, MenuModel... items) {
        MenuModel model = build(id, items);
        model.isGroup = true;
        return model;
    }

    /** Creates a new menu hierarchy. This might also be a sub-menu of an existing menu. */
    public static MenuModel menu(String text, MenuModel... items) {
        return menu(null, text, items);
    }

    /** Creates a new menu hierarchy, identified by an enum value. This might also be a sub-menu of an existing menu. */
    public static MenuModel menu(Enum<?> menuItem, String text, MenuModel... items) {
        MenuModel model = new MenuModel(menuItem, false, text, null, null, null);

        for (MenuModel item : items) {
            model.children.add(item);
            item.parent = model;
        }

        return model;
    }

    /** Creates a new menu hierarchy from a set of top-level menus. */
    public static MenuModel build(MenuModel... items) {
        return build(null, items);
    }

    /** Creates a new menu hierarchy from a set of top-level menus. */
    public static MenuModel build(Enum<?> id, MenuModel... items) {
        MenuModel model = new MenuModel(id, false, null, null, null, null);

        for (MenuModel item : items) {
            model.children.add(item);
            item.parent = model;
        }

        return model;
    }

    private MenuModel parent;
    private boolean isGroup;
    private boolean isCheckGroup;
    private boolean isCheckable;
    private Command command;
    private Optional<Object> commandParameter;

    private final Enum<?> id;
    private final CommandData commandData = new CommandData();
    private final StringProperty text = new SimpleStringProperty();
    private final StringProperty statusText = new SimpleStringProperty();
    private final ObjectProperty<Object> userData = new SimpleObjectProperty<>();
    private final BooleanProperty mnemonicParsing = new SimpleBooleanProperty(true);
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final ObjectProperty<KeyCombination> accelerator = new SimpleObjectProperty<>();
    private final ObjectProperty<URL> imageSource = new SimpleObjectProperty<>();

    private final ListProperty<MenuModel> children =
        new SimpleListProperty<>(FXCollections.observableArrayList()) {
            @Override
            protected void invalidated() {
                super.invalidated();

                for (MenuModel child : get()) {
                    child.parent = MenuModel.this;

                    Object checkedItem = getCheckedItem();
                    if (isCheckGroup && checkedItem != null) {
                        if (!child.isCheckable) {
                            continue;
                        }

                        if (child.getUserData() == checkedItem) {
                            child.checked.set(true);
                            break;
                        }
                    }
                }
            }
        };

    private final BooleanProperty checked =
        new SimpleBooleanProperty() {
            @Override
            protected void invalidated() {
                super.invalidated();

                if (get() && parent != null && parent.isCheckGroup) {
                    for (MenuModel sibling : parent.getChildren()) {
                        if (sibling != MenuModel.this) {
                            sibling.checked.set(false);
                        }
                    }

                    parent.checkedItem.set(userData.get());
                }
            }
        };

    private final ObjectProperty<Object> checkedItem =
        new SimpleObjectProperty<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                Object checkedItem = get();

                if (!isCheckGroup) {
                    throw new IllegalArgumentException("The current menu item is not a check group.");
                }

                for (MenuModel child : children) {
                    child.checked.set(child.getUserData() == checkedItem);
                }
            }
        };

    protected MenuModel(
            @Nullable Enum<?> id,
            boolean checkable,
            @Nullable String text,
            @Nullable String statusText,
            @Nullable URL imageSource,
            @Nullable KeyCombination accelerator) {
        this.id = id;
        this.parent = null;
        this.isCheckable = checkable;
        this.text.set(text);
        this.statusText.set(statusText);
        this.imageSource.set(imageSource);
        this.accelerator.set(accelerator);
        this.children.addListener(this::childrenChanged);
    }

    public ObjectProperty<Object> userDataProperty() {
        return userData;
    }

    public BooleanProperty mnemonicParsingProperty() {
        return mnemonicParsing;
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public StringProperty textProperty() {
        return text;
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }

    public BooleanProperty checkedProperty() {
        return checked;
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectProperty<T> checkedItemProperty() {
        return (ObjectProperty<T>)checkedItem;
    }

    public ObjectProperty<KeyCombination> acceleratorProperty() {
        return accelerator;
    }

    public ObjectProperty<URL> imageSourceProperty() {
        return imageSource;
    }

    public ListProperty<MenuModel> childrenProperty() {
        return children;
    }

    public ObservableList<MenuModel> getChildren() {
        return children;
    }

    public void setChildren(ObservableList<MenuModel> children) {
        this.children.set(children);
    }

    public Command getCommand() {
        if (command != null) {
            return command;
        }

        return commandData.command;
    }

    public Optional<Object> getCommandParameter() {
        return commandParameter;
    }

    /**
     * Set the action handler for this menu item. If no action handler is set, a UI representation might choose to
     * disable the menu item.
     */
    public void setActionHandler(Runnable action) {
        commandData.runnable.set(action);
    }

    /**
     * Set the action handler for this menu item and specifies whether the action can be executed. If no action handler
     * is set, a UI representation might choose to disable the menu item.
     */
    public void setActionHandler(Runnable action, ObservableValue<Boolean> canExecute) {
        commandData.runnable.set(action);
        commandData.canExecute.bind(canExecute);
    }

    public void setCommandHandler(Command command) {
        this.command = command;
        this.commandParameter = null;
    }

    public void setCommandHandler(Command command, Object parameter) {
        this.command = command;
        this.commandParameter = Optional.of(parameter);
    }

    public MenuModel find(Enum<?> id) {
        MenuModel model = findInternal(id);
        if (model == null) {
            throw new IllegalArgumentException();
        }

        return model;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isCheckGroup() {
        return isCheckGroup;
    }

    public Object getUserData() {
        return userData.get();
    }

    public void setUserData(Object userData) {
        this.userData.set(userData);
    }

    public boolean isMnemonicParsing() {
        return mnemonicParsing.get();
    }

    public void setMnemonicParsing(boolean mnemonicParsing) {
        this.mnemonicParsing.set(mnemonicParsing);
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public boolean isCheckable() {
        return isCheckable;
    }

    public boolean isChecked() {
        return checked.get();
    }

    public Object getCheckedItem() {
        return checkedItem.get();
    }

    public void setCheckedItem(Object checkedItem) {
        this.checkedItem.set(checkedItem);
    }

    void setCheckedItem(MenuModel itemModel) {
        Object userData = itemModel.getUserData();
        this.checkedItem.set(userData);
        itemModel.checked.set(true);
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getStatusText() {
        return statusText.get();
    }

    public void setStatusText(String statusText) {
        this.statusText.set(statusText);
    }

    private void childrenChanged(ListChangeListener.Change<? extends MenuModel> change) {
        for (MenuModel child : change.getList()) {
            child.parent = this;
        }
    }

    private @Nullable MenuModel findInternal(Enum<?> id) {
        if (id != null && this.id == id) {
            return this;
        }

        for (MenuModel model : children) {
            MenuModel found = model.findInternal(id);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

}
