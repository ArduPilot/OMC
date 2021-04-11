/*
 * Copyright (c) 2012, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// source: http://docs.oracle.com/javafx/2/swing/SimpleSwingBrowser.java.htm

package eu.mavinci.desktop.gui.widgets;

import static javafx.concurrent.Worker.State.FAILED;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.common.IPathProvider;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.asyncfx.concurrent.Dispatcher;

public class BrowserWidget extends JPanel {
    private static final long serialVersionUID = -231218075655439897L;
    private JFXPanel jfxPanel;
    private WebEngine engine;

    public JLabel lblStatus = new JLabel();

    private JButton btnGo;
    private JTextField txtURL = new JTextField();
    private JProgressBar progressBar = new JProgressBar();

    public JPanel topBar;
    public JPanel statusBar;

    public WebEngine getEngine() {
        return engine;
    }

    public static final WeakListenerList<BrowserWidget> browsers = new WeakListenerList<>("all browsers");

    protected void initComponents() {
        setLayout(new BorderLayout());
        jfxPanel = new JFXPanel();
        setPreferredSize(new Dimension(900, 600));
        createScene();
        btnGo = new JButton("go");

        ActionListener al =
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadURL(txtURL.getText());
                }
            };

        btnGo.addActionListener(al);
        txtURL.addActionListener(al);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

        statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(jfxPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * overwrite this, to extract title changes
     *
     * @param title
     */
    public void setTitle(String title) {}

    private void createScene() {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    ///// set context classloader for javafx
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                    // .getContextClassLoader()

                    WebView view = new WebView();
                    engine = view.getEngine();

                    engine.titleProperty()
                        .addListener(
                            new ChangeListener<String>() {
                                @Override
                                public void changed(
                                        ObservableValue<? extends String> observable,
                                        String oldValue,
                                        final String newValue) {
                                    SwingUtilities.invokeLater(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                setTitle(newValue);
                                            }
                                        });
                                }
                            });

                    engine.setOnStatusChanged(
                        new EventHandler<WebEvent<String>>() {
                            @Override
                            public void handle(final WebEvent<String> event) {
                                SwingUtilities.invokeLater(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            lblStatus.setText(event.getData());
                                        }
                                    });
                            }
                        });

                    engine.locationProperty()
                        .addListener(
                            new ChangeListener<String>() {
                                @Override
                                public void changed(
                                        ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                                    SwingUtilities.invokeLater(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                txtURL.setText(newValue);
                                            }
                                        });
                                }
                            });

                    engine.getLoadWorker()
                        .workDoneProperty()
                        .addListener(
                            new ChangeListener<Number>() {
                                @Override
                                public void changed(
                                        ObservableValue<? extends Number> observableValue,
                                        Number oldValue,
                                        final Number newValue) {
                                    SwingUtilities.invokeLater(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setValue(newValue.intValue());
                                            }
                                        });
                                }
                            });

                    engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(
                            new ChangeListener<Throwable>() {

                                public void changed(
                                        ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                    if (engine.getLoadWorker().getState() == FAILED) {
                                        SwingUtilities.invokeLater(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    JOptionPane.showMessageDialog(
                                                        BrowserWidget.this,
                                                        (value != null)
                                                            ? engine.getLocation() + "\n" + value.getMessage()
                                                            : engine.getLocation() + "\nUnexpected error.",
                                                        "Loading error...",
                                                        JOptionPane.ERROR_MESSAGE);
                                                }
                                            });
                                    }
                                }
                            });

                    jfxPanel.setScene(new Scene(view));
                }
            });
    }

    public void loadURL(final String url) {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    if (url == null) {
                        return;
                    }

                    String tmp = toURL(url);

                    if (tmp == null) {
                        tmp = toURL("http://" + url);
                    }

                    if (tmp == null) {
                        return;
                    }

                    engine.load(tmp);
                }
            });
    }

    protected static String toURL(String str) {
        if (str == null) {
            return null;
        }

        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    public static final String KEY = "eu.mavinci.desktop.gui.widgets.SimpleSwingBrowser";

    public BrowserWidget(String startURL) {
        initComponents();
        browsers.add(this);
        loadURL(startURL);
    }

    static long lastDeactivation;

    public static void stopAllBrowserForSomeWhile() {
        final long thisDeactivation = System.currentTimeMillis();
        lastDeactivation = thisDeactivation;

        // disable all webinterface requests to backend, since we had in earier backend version a use after free bug in
        // this interface!!
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    for (BrowserWidget browser : BrowserWidget.browsers) {
                        // browser.loadURL("http://127.0.0.1/");
                        try {
                            WebEngine webEngine = browser.getEngine();
                            webEngine.setUserDataDirectory(
                                StaticInjector.getInstance(IPathProvider.class).getWebviewCacheFolder().toFile());
                            webEngine.setJavaScriptEnabled(false);
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "could not disable browser:" + browser, e);
                        }
                    }
                }
            });

        try {
            Thread.sleep(1000); // make sure that disabling is really applied, and all ongoing loadings are done
        } catch (InterruptedException e1) {
        }

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLater(
            new Runnable() {

                @Override
                public void run() {
                    Platform.runLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (lastDeactivation > thisDeactivation) {
                                    return;
                                }

                                for (BrowserWidget browser : BrowserWidget.browsers) {
                                    // browser.loadURL("http://127.0.0.1/");
                                    try {
                                        WebEngine webEngine = browser.getEngine();
                                        webEngine.setJavaScriptEnabled(true);
                                    } catch (Exception e) {
                                        Debug.getLog().log(Level.WARNING, "could not enable browser:" + browser, e);
                                    }
                                }
                            }
                        });
                }
            },
            Duration.ofMillis(30 * 1000));
    }
}
