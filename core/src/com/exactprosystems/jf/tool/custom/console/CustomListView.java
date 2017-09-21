////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.tool.Common;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;

import java.util.Objects;
import java.util.stream.Collectors;

public class CustomListView<T> extends ListView<ConsoleText<T>> {
    private OnDoubleClickListener<T> listener;

    public CustomListView(final boolean mayClear) {
        super();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.setCellFactory(cellConsoleFxListView -> new ColorCell<>());

        this.setOnMouseClicked(mouseEvent ->
        {
            if (mouseEvent.getClickCount() == 2)
            {
                ConsoleText<T> selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem != null && listener != null && selectedItem.getItem() != null)
                {
                    listener.onDoubleClick(selectedItem.getItem());
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        MenuItem clear = new MenuItem("Clear");
        MenuItem copy = new MenuItem("Copy");

        contextMenu.getItems().addAll(copy);
        if (mayClear)
        {
            contextMenu.getItems().add(clear);
        }

        setContextMenu(contextMenu);

        clear.setOnAction(event ->
        {
            this.getItems().forEach(i -> i.setText(""));
            this.getItems().clear();
        });

        copy.setOnAction(event -> Sys.copyToClipboard(
                this.getSelectionModel().getSelectedItems()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ConsoleText::toString)
                        .collect(Collectors.joining("\n"))
                )
        );
    }

    public CustomListView(OnDoubleClickListener<T> listener, boolean mayClear) {
        this(mayClear);
        this.listener = listener;
    }

    public void autoScroll(boolean flag) {
        if (flag)
        {
            this.getItems().addListener(changeListener);
        }
        else
        {
            this.getItems().removeListener(changeListener);
        }
    }

    public void setListener(OnDoubleClickListener<T> listener) {
        this.listener = listener;
    }

    private ListChangeListener<ConsoleText<T>> changeListener = c ->
    {
        int size = this.getItems().size();
        if (size > 0)
        {
            Common.runLater(() -> this.scrollTo(size - 1));
        }
    };
}
