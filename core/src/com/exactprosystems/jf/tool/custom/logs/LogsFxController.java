////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.logs;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LogsFxController implements Initializable, ContainingParent
{
	public CustomListView<String> listView;
	public Button btnRefresh;
	public BorderPane borderPane;
	public BorderPane ownerFindPanel;
	private LogsFx model;
	private Dialog dialog;
	private FindPanel<ConsoleText<String>> findPanel;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.findPanel = new FindPanel<>();
		this.ownerFindPanel.setCenter(this.findPanel);
		this.listView = new CustomListView<>(false);
		this.borderPane.setCenter(listView);
		BorderPane.setMargin(this.listView, new Insets(10, 0, 0, 0));
		Platform.runLater(() -> {
			btnRefresh.setTooltip(new Tooltip("Refresh\nF5"));
			btnRefresh.getScene().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> refresh(null));
			Common.customizeLabeled(btnRefresh, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.REFRESH);
		});
	}

	@Override
	public void setParent(Parent parent)
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().setPrefWidth(600);
		this.dialog.getDialogPane().setPrefHeight(600);
		this.dialog.setHeaderText("Main.log");
		this.dialog.getDialogPane().setContent(parent);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
	}

	public void init(LogsFx model)
	{
		this.model = model;
		findPanel.setListener(new IFind<ConsoleText<String>>()
		{
			@Override
			public void find(ConsoleText<String> stringConsoleText)
			{
				model.find(stringConsoleText);
			}

			@Override
			public List<ConsoleText<String>> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				return model.findItem(what, matchCase, wholeWord);
			}
		});
	}

	public void show()
	{
		this.dialog.show();
	}

	public void close()
	{
		this.dialog.hide();
	}

	public void setTextToList(ArrayList<ConsoleText<String>> list)
	{
		listView.getItems().addAll(list);
	}

	public void clearListView()
	{
		this.listView.getItems().clear();
	}

	//============================================================
	// events methods
	//============================================================
	public void refresh(ActionEvent actionEvent)
	{
		Common.tryCatch(model::refresh, "Error on refresh");
	}

	public boolean isShow()
	{
		return this.dialog != null && this.dialog.isShowing();
	}

	public void clearAndSelect(int index)
	{
		listView.getSelectionModel().clearAndSelect(index);
		listView.scrollTo(index);
	}
}
