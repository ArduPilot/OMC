/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.SuppressFBWarnings;
import com.intel.missioncontrol.ui.accessibility.IShortcutAware;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class ActivityToggleSwitch extends ToggleSwitch implements IShortcutAware {

    private static final int ICON_SIZE = 16;
    private final StringProperty shortcut = new SimpleStringProperty(this, "shortcut");
    private final ObjectProperty<Boolean> commandParameter = new SimpleObjectProperty<>();
    private final ObjectProperty<ParameterizedCommand<Boolean>> command =
            new SimpleObjectProperty<>(this, "command") {
                @Override
                protected void invalidated() {
                    Command command = get();
                    if (command != null) {
                        disableProperty().bind(command.notExecutableProperty());
                    } else {
                        disableProperty().unbind();
                    }
                }
            };

    private final BooleanProperty isBusy =
            new SimpleBooleanProperty() {
                private List<String> iconStyles = new ArrayList<>();
                private RotateTransition rotateTransition;

                @Override
                protected void invalidated() {
                    super.invalidated();

                    Node graphic = getGraphic();
                    if (graphic == null) {
                        String url =
                                getStyleClass().contains("inverted")
                                        ? "/com/intel/missioncontrol/icons/icon_progress(fill=theme-primary-button-text-color).svg"
                                        : "/com/intel/missioncontrol/icons/icon_progress.svg";
                        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                        ImageView imageView = new ImageView(contextClassLoader.getResource(url).toExternalForm());
                        imageView.setFitWidth(ICON_SIZE);
                        imageView.setFitHeight(ICON_SIZE);
                        graphic = imageView;
                        setGraphic(imageView);
                    }

                    if (get()) {
                        ListIterator<String> it = getStyleClass().listIterator();
                        while (it.hasNext()) {
                            String styleClass = it.next();
                            if (styleClass.startsWith("icon-")) {
                                iconStyles.add(styleClass);
                                it.remove();
                            }
                        }

                        setPrefWidth(getWidth());
                        rotateTransition = new RotateTransition(Duration.millis(1500), graphic);
                        rotateTransition.setInterpolator(Interpolator.LINEAR);
                        rotateTransition.setFromAngle(0);
                        rotateTransition.setToAngle(360);
                        rotateTransition.setCycleCount(Animation.INDEFINITE);
                        rotateTransition.play();
                        setStyle("-fx-content-display: graphic-only");
                        graphic.setVisible(true);
                        graphic.setManaged(true);

                        if (!disableProperty().isBound()) {
                            setDisable(true);
                        }
                    } else {
                        getStyleClass().addAll(iconStyles);
                        iconStyles.clear();

                        if (rotateTransition != null) {
                            rotateTransition.stop();
                            rotateTransition = null;
                        }

                        setStyle("-fx-content-display: left");
                        graphic.setVisible(false);
                        graphic.setManaged(false);

                        if (!disableProperty().isBound()) {
                            setDisable(false);
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return ActivityToggleSwitch.this;
                }

                @Override
                public String getName() {
                    return "isBusy";
                }
            };

    public ActivityToggleSwitch() {
        super();
        this.setOnMouseClicked(mouseEvent -> setToggleState());
    }

    public ActivityToggleSwitch(String label) {
        super(label);
        this.setOnMouseClicked(mouseEvent -> setToggleState());
        this.setOnTouchPressed(mouseEvent -> setToggleState());
        this.setOnKeyPressed(mouseEvent -> setToggleState());
    }

    public StringProperty shortcutProperty() {
        return shortcut;
    }

    public @Nullable String getShortcut() {
        return shortcut.get();
    }

    public void setShortcut(@Nullable String shortcut) {
        this.shortcut.set(shortcut);
    }

    public ObjectProperty<Boolean> commandParameterProperty() {
        return commandParameter;
    }

    public @Nullable boolean getCommandParameter() {
        return commandParameter.get();
    }

    public void setCommandParameter(Boolean parameter) {
        this.commandParameter.set(parameter);
    }

    public BooleanProperty isBusyProperty() {
        return isBusy;
    }

    public ObjectProperty<ParameterizedCommand<Boolean>> commandProperty() {
        return command;
    }

    public @Nullable ParameterizedCommand<Boolean> getCommand() {
        return command.get();
    }

    public void setCommand(ParameterizedCommand<Boolean> command) {
        this.command.set(command);
    }

    private void setToggleState() {
        ParameterizedCommand<Boolean> command = getCommand();
        if (command != null) {
            setCommandParameter(this.isSelected());
            command.execute(getCommandParameter());
        }
    }
}

