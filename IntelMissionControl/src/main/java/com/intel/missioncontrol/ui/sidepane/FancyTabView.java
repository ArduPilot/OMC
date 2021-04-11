package com.intel.missioncontrol.ui.sidepane;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import de.saxsys.mvvmfx.utils.notifications.WeakNotificationObserver;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public abstract class FancyTabView<T extends ViewModel> implements FxmlView<T>, Initializable {

    protected abstract ViewModel getViewModel();

    protected final Node getRootNode() {
        Node content = tab.getContent();
        if (content != null) {
            return content;
        }

        return tab.getScrollingContent();
    }

    protected void initializeView() {
        superInitializeCalled = true;
    }

    private FancyTab<? extends Enum<?>> tab;
    private boolean superInitializeCalled;

    private final NotificationObserver getWindowObserver =
        (key, payload) -> {
            if (payload.length > 0 && payload[0] instanceof IDialogService.GetWindowRequest) {
                IDialogService.GetWindowRequest getWindowRequest = (IDialogService.GetWindowRequest)payload[0];
                getWindowRequest.setWindow(getRootNode().getScene().getWindow());
            }
        };

    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        tab = FancyTab.Autowiring.queryCurrentTab(this);
        if (tab.getContent() == null && tab.getScrollingContent() == null) {
            tab.setContent(new Pane());
        }

        Platform.runLater(
            () -> {
                ViewModel viewModel = getViewModel();
                Expect.notNull(viewModel, "viewModel");

                viewModel.subscribe(IDialogService.GET_WINDOW_REQUEST, new WeakNotificationObserver(getWindowObserver));

                initializeView();

                if (!superInitializeCalled) {
                    throw new IllegalStateException("Did you forget to call super.initializeView()?");
                }
            });
    }

}
