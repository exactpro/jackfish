////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.browser;

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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;

import java.io.File;

public class Browser extends BorderPane
{
	static final String EVENT_CLICK = "click";
	private TabPane tabPane;
	private File reportFile;

	public Browser(File reportFile)
	{
		this.reportFile = reportFile;
		initTabPane();
		initToolbar();
	}

	private void initToolbar()
	{
		ToolBar toolBar = new ToolBar();

		Button reload = new Button("Reload");
		reload.setOnAction(e -> ((CustomTab) this.tabPane.getSelectionModel().getSelectedItem()).reload());
		toolBar.getItems().add(reload);

		Button back = new Button("Back");
		back.setOnAction(e -> ((CustomTab) this.tabPane.getSelectionModel().getSelectedItem()).back());
		toolBar.getItems().add(back);

		Button forward = new Button("Forward");
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
					NodeList list = this.engine.getDocument().getElementsByTagName("a");
					EventListener listener = ev -> {
						String domEventType = ev.getType();
						if (domEventType.equals(EVENT_CLICK))
						{
							String href = ((Element) ev.getTarget()).getAttribute("href");
							if (ev instanceof MouseEvent)
							{
								MouseEvent mouseEvent = ((MouseEvent) ev);
								if (mouseEvent.getButton() == 1)
								{
									CustomTab customTab = new CustomTab();
									this.getTabPane().getTabs().add(customTab);
									this.getTabPane().getSelectionModel().select(customTab);

									//TODO think about how to open
									customTab.load(this.engine.getLocation() + href);
								}
							}
						}
					};
					for (int i = 0; i < list.getLength(); i++)
					{
						((EventTarget) list.item(i)).addEventListener(EVENT_CLICK, listener, false);
					}
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

		public void load(String link)
		{
			this.engine.load(link);
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
