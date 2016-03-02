////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.browser;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import java.io.File;

public class ReportBrowser extends BorderPane
{
	private TabPane tabPane;
	private File reportFile;

	public ReportBrowser(File reportFile)
	{
		this.getStyleClass().add(CssVariables.BROWSER);
		this.reportFile = reportFile;
		initTabPane();
		initToolbar();
	}

	public String getMatrix()
	{
		try
		{
			CustomTab selectedItem = (CustomTab) this.tabPane.getSelectionModel().getSelectedItem();
			Document document = selectedItem.engine.getDocument();
			return document.getElementsByTagName("pre").item(0).getTextContent();
		}
		catch (Exception e)
		{
			return "";
		}
	}

	private void initToolbar()
	{
		ToolBar toolBar = new ToolBar();

		Button reload = new Button();
		Common.customizeLabeled(reload, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.RELOAD);
		reload.setOnAction(e -> ((CustomTab) this.tabPane.getSelectionModel().getSelectedItem()).reload());
		toolBar.getItems().add(reload);

		Button back = new Button();
		Common.customizeLabeled(back, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.GO_BACK);
		back.setOnAction(e -> ((CustomTab) this.tabPane.getSelectionModel().getSelectedItem()).back());
		toolBar.getItems().add(back);

		Button forward = new Button();
		Common.customizeLabeled(forward, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.GO_FORWARD);
		forward.setOnAction(e -> ((CustomTab) this.tabPane.getSelectionModel().getSelectedItem()).forward());
		toolBar.getItems().add(forward);

		this.setTop(toolBar);
	}

	private void initTabPane()
	{
		this.tabPane = new TabPane();
		this.setCenter(this.tabPane);
		CustomTab mainTab = new CustomTab();
		mainTab.load(this.reportFile);
		mainTab.setClosable(false);
		this.tabPane.getTabs().add(mainTab);
	}

	private static class CustomTab extends Tab
	{
		private WebEngine engine;

		public CustomTab()
		{
			WebView view = new WebView();
			this.engine = view.getEngine();
			this.setContent(view);
			this.setText("New tab...");
			Worker<Void> loadWorker = this.engine.getLoadWorker();
			loadWorker.stateProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == Worker.State.SUCCEEDED)
				{
					this.setText(this.engine.getTitle());
				}
			});
			this.engine.setCreatePopupHandler(param -> {
				CustomTab customTab = new CustomTab();
				this.getTabPane().getTabs().add(customTab);
				this.getTabPane().getSelectionModel().select(customTab);
				return customTab.engine;
			});
		}

		public void load(File file)
		{
			this.engine.load(file.toURI().toASCIIString());
		}

		public void reload()
		{
			this.engine.reload();
		}

		public void back()
		{
			final WebHistory history = this.engine.getHistory();
			ObservableList<WebHistory.Entry> entryList = history.getEntries();
			int currentIndex = history.getCurrentIndex();
			if (currentIndex > 0)
			{
				Platform.runLater(() -> history.go(-1));
			}
			String url = entryList.get(currentIndex > 0 ? currentIndex - 1 : currentIndex).getUrl();
			this.engine.load(url);
		}

		public void forward()
		{
			final WebHistory history = this.engine.getHistory();
			ObservableList<WebHistory.Entry> entryList = history.getEntries();
			int currentIndex = history.getCurrentIndex();
			if (currentIndex + 1 < this.engine.getHistory().getEntries().size())
			{
				Platform.runLater(() -> history.go(1));
			}
			String url = entryList.get(currentIndex < entryList.size() - 1 ? currentIndex + 1 : currentIndex).getUrl();
			this.engine.load(url);
		}
	}

}