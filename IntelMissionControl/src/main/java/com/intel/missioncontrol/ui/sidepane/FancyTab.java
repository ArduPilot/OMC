package com.intel.missioncontrol.ui.sidepane;

import de.saxsys.mvvmfx.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class FancyTab<TabIdType extends Enum<TabIdType>> {

    static class Autowiring {
        private static final Stack<FancyTab<? extends Enum<?>>> stack = new Stack<>();
        private static final Map<FancyTab<? extends Enum<?>>, FancyTabView<? extends ViewModel>> map =
            new WeakHashMap<>();

        static FancyTab<? extends Enum<?>> queryCurrentTab(FancyTabView<? extends ViewModel> view) {
            map.put(stack.peek(), view);
            return stack.pop();
        }

        static boolean hasView(FancyTab<? extends Enum<?>> tab) {
            return map.get(tab) != null;
        }

        static FancyTabView<? extends ViewModel> getView(FancyTab<? extends Enum<?>> tab) {
            return map.get(tab);
        }
    }

    private final ObservableList<String> stylesheets = FXCollections.observableArrayList();

    private TabIdType tabId;
    private Node header;
    private Node content;
    private Node scrollingContent;
    private Node footer;

    public FancyTab() {
        Autowiring.stack.push(this);
        Autowiring.map.put(this, null);
        stylesheets.addListener(this::stylesheetsChanged);
    }

    public final ObservableList<String> getStylesheets() {
        return stylesheets;
    }

    public final TabIdType getTabId() {
        return tabId;
    }

    public void setTabId(TabIdType tabId) {
        this.tabId = tabId;
    }

    public Node getHeader() {
        return header;
    }

    public void setHeader(Node header) {
        if (header instanceof Parent) {
            updateStylesheets((Parent)header, getStylesheets());
        }

        this.header = header;
    }

    public Node getContent() {
        return content;
    }

    public void setContent(Node content) {
        if (scrollingContent != null) {
            throw new IllegalArgumentException(
                "Cannot use scrolling content and non-scrolling content at the same time.");
        }

        if (content == null) {
            content = new Pane();
        }

        if (content instanceof Parent) {
            updateStylesheets((Parent)content, getStylesheets());
        }

        this.content = content;
    }

    public Node getScrollingContent() {
        return scrollingContent;
    }

    public void setScrollingContent(Node scrollingContent) {
        if (content != null) {
            throw new IllegalArgumentException(
                "Cannot use scrolling content and non-scrolling content at the same time.");
        }

        if (scrollingContent == null) {
            scrollingContent = new Pane();
        }

        if (scrollingContent instanceof Parent) {
            updateStylesheets((Parent)scrollingContent, getStylesheets());
        }

        this.scrollingContent = scrollingContent;
    }

    public Node getFooter() {
        return footer;
    }

    public void setFooter(Node footer) {
        if (footer instanceof Parent) {
            updateStylesheets((Parent)footer, getStylesheets());
        }

        this.footer = footer;
    }

    private void stylesheetsChanged(ListChangeListener.Change<? extends String> change) {
        Node node = getHeader();
        if (node instanceof Parent) {
            updateStylesheets((Parent)node, change.getList());
        }

        node = getContent();
        if (node instanceof Parent) {
            updateStylesheets((Parent)node, change.getList());
        }

        node = getScrollingContent();
        if (node instanceof Parent) {
            updateStylesheets((Parent)node, change.getList());
        }

        node = getFooter();
        if (node instanceof Parent) {
            updateStylesheets((Parent)node, change.getList());
        }
    }

    private void updateStylesheets(Parent parent, List<? extends String> stylesheets) {
        ObservableList<String> parentStylesheets = parent.getStylesheets();
        List<String> existingStylesheets = new ArrayList<>(parentStylesheets);
        parentStylesheets.clear();
        parentStylesheets.addAll(stylesheets);
        parentStylesheets.addAll(existingStylesheets);
    }

}
