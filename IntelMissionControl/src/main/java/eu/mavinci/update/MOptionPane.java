/*
 * Copyright (c) 2014-2016 MAVinci GmbH and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Modified at 7. April 2014
 *
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  MAVinci designates this
 * particular file as subject to the "Classpath" exception as provided
 * by MAVinci in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact MAVinci, Opelstraße 8a, 68789 St. Leon-Rot, Germany
 * or visit www.mavinci.com if you need additional information or have any
 * questions.
 */

package eu.mavinci.update;

import com.intel.missioncontrol.PublishSource;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * equivalent to original JOptionPane (but a light version), but it exposes the modality interface to you. This enables
 * document modailty (used by default), and by this, e.g. the update dialog is modal for each update kind, and not
 * application wide
 *
 * @deprecated Use IDialogService instead
 * @author Marco - MAVinci
 */
@Deprecated
@PublishSource(
        module = "openjfx",
        licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class MOptionPane {

    public static int showConfirmDialog(Window parentComponent, Object message, String title, int optionType)
            throws HeadlessException {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(
            Window parentComponent, Object message, String title, int optionType, int messageType)
            throws HeadlessException {
        return showConfirmDialog(parentComponent, message, title, optionType, messageType, null);
    }

    public static int showConfirmDialog(
            Window parentComponent, Object message, String title, int optionType, int messageType, Icon icon)
            throws HeadlessException {
        return showOptionDialog(
            parentComponent, message, title, optionType, messageType, icon, null, null, ModalityType.DOCUMENT_MODAL);
    }

    @SuppressWarnings("deprecation")
    public static int showOptionDialog(
            Window parentComponent,
            Object message,
            String title,
            int optionType,
            int messageType,
            Icon icon,
            Object[] options,
            Object initialValue,
            ModalityType modalityType)
            throws HeadlessException {
        JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);

        pane.setInitialValue(initialValue);
        pane.setComponentOrientation(
            ((parentComponent == null) ? JOptionPane.getRootFrame() : parentComponent).getComponentOrientation());

        int style = styleFromMessageType(messageType);
        JDialog dialog = createDialog(parentComponent, title, style, pane);

        pane.selectInitialValue();
        dialog.setModalityType(modalityType);
        dialog.show();
        dialog.dispose();

        Object selectedValue = pane.getValue();

        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }

        if (options == null) {
            if (selectedValue instanceof Integer) {
                return ((Integer)selectedValue).intValue();
            }

            return JOptionPane.CLOSED_OPTION;
        }

        for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
            if (options[counter].equals(selectedValue)) {
                return counter;
            }
        }

        return JOptionPane.CLOSED_OPTION;
    }

    private static int styleFromMessageType(int messageType) {
        switch (messageType) {
        case JOptionPane.ERROR_MESSAGE:
            return JRootPane.ERROR_DIALOG;
        case JOptionPane.QUESTION_MESSAGE:
            return JRootPane.QUESTION_DIALOG;
        case JOptionPane.WARNING_MESSAGE:
            return JRootPane.WARNING_DIALOG;
        case JOptionPane.INFORMATION_MESSAGE:
            return JRootPane.INFORMATION_DIALOG;
        case JOptionPane.PLAIN_MESSAGE:
        default:
            return JRootPane.PLAIN_DIALOG;
        }
    }

    private static JDialog createDialog(Window parentComponent, String title, int style, JOptionPane pane)
            throws HeadlessException {
        final JDialog dialog;

        Window window = parentComponent;
        if (window instanceof Frame) {
            dialog = new JDialog((Frame)window, title, true);
        } else {
            dialog = new JDialog((Dialog)window, title, true);
        }

        initDialog(dialog, style, parentComponent, pane);
        return dialog;
    }

    private static void initDialog(final JDialog dialog, int style, Component parentComponent, final JOptionPane pane) {
        dialog.setComponentOrientation(pane.getComponentOrientation());
        Container contentPane = dialog.getContentPane();

        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        dialog.setResizable(false);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.setUndecorated(true);
                pane.getRootPane().setWindowDecorationStyle(style);
            }
        }

        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);

        final PropertyChangeListener listener =
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    // Let the defaultCloseOperation handle the closing
                    // if the user closed the window without selecting a button
                    // (newValue = null in that case). Otherwise, close the dialog.
                    if (dialog.isVisible()
                            && event.getSource() == pane
                            && (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))
                            && event.getNewValue() != null
                            && event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                        dialog.setVisible(false);
                    }
                }
            };

        WindowAdapter adapter =
            new WindowAdapter() {
                private boolean gotFocus = false;

                public void windowClosing(WindowEvent we) {
                    pane.setValue(null);
                }

                public void windowClosed(WindowEvent e) {
                    pane.removePropertyChangeListener(listener);
                    dialog.getContentPane().removeAll();
                }

                public void windowGainedFocus(WindowEvent we) {
                    // Once window gets focus, set initial focus
                    if (!gotFocus) {
                        pane.selectInitialValue();
                        gotFocus = true;
                    }
                }
            };
        dialog.addWindowListener(adapter);
        dialog.addWindowFocusListener(adapter);
        dialog.addComponentListener(
            new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    // reset value to ensure closing works properly
                    pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                }
            });

        pane.addPropertyChangeListener(listener);
    }

}
