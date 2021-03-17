package com.intel.dronekitsample.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *  Handles view creation callbacks and
 */
public abstract class ViewController {
    private boolean initialized = false;
    protected InitializationListener initializeCallback;
    Parent root;

    @FXML protected ResourceBundle resources;

    /**
     * @return root of view hierarchy for FXML
     */
    public <T extends Parent> T getRoot() {
        return (T) root;
    }

    /**
     * inflates view and returns the View controller associated with FXML
     */
    public static <T extends ViewController> T create(URL resourceName) throws IOException {
        FXMLLoader loader = new FXMLLoader(resourceName);
        Parent node = loader.load();
        T t  = loader.getController();
        t.root = node;
        return t;
    }

    public interface InitializationListener {
        public void onControllerInitialized();
    }

    void setInitializeCallback(InitializationListener listener) {
        initializeCallback = listener;
        if (initialized) initializeCallback.onControllerInitialized();
    }

    /**
     * called after FXML is 'inflated'
     */
    @FXML void initialize() {
        System.out.println("Initializing ViewController for class: " + getClass());
        initialized = true;
        doInitialize();
        if (initializeCallback != null) {
            initializeCallback.onControllerInitialized();
        }
    }

    /**
     * perform view specific initialization here
     */
    public abstract void doInitialize();
}
