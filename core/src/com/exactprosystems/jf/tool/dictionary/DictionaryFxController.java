////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.dictionary.actions.ActionsController;
import com.exactprosystems.jf.tool.dictionary.info.element.ElementInfoController;
import com.exactprosystems.jf.tool.dictionary.info.owner.OwnerInfoController;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class DictionaryFxController implements Initializable, ContainingParent
{
	public enum Result 
	{ 
		PASSED (Color.GREEN), FAILED (Color.RED), NOT_ALLOWED (Color.DARKGRAY);
		
		private Result(Color color)
		{
			this.color = color;
		}
		
		public Color getColor()
		{
			return this.color;
		}
		
		private Color color;
	} 
	
	

	
	public CustomListView<String>	listView;
	public SplitPane				splitPane;
	public GridPane					mainGridPane;
	private Parent					pane;
	private CustomTab				tab;
	
	private ActionsController		actionsController;
	private ElementInfoController	elementInfoController;
	private OwnerInfoController		ownerInfoController;
	private NavigationController	navigationController;
	private Settings settings;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		listView = new CustomListView<>(true);
		splitPane.getItems().add(listView);
		listView.setPrefHeight(150);
		listView.setMaxHeight(400);
		listView.setMinHeight(100);
		splitPane.setDividerPosition(1, 0.85);
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void close() throws Exception
	{
		this.tab.close();
		this.navigationController.close();
		Common.getTabPane().getTabs().remove(this.tab);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void init(final DictionaryFx model, Context context) throws Exception
	{
		this.settings = context.getConfiguration().getSettings();
		this.tab = Common.createTab(model);
		this.tab.setContent(pane);

		this.navigationController = Common.loadController(NavigationController.class.getResource("Navigation.fxml"));
		this.navigationController.init(model, this.mainGridPane, this.settings, this.tab);

		Settings.SettingsValue themePath = this.settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.THEME, Theme.WHITE.name());
		this.elementInfoController = Common.loadController(ElementInfoController.class.getResource("ElementInfo.fxml"));
		this.elementInfoController.init(model, context, this.mainGridPane, this.navigationController, Theme.valueOf(themePath.getValue()).getPath());

		this.ownerInfoController = Common.loadController(OwnerInfoController.class.getResource("OwnerInfo.fxml"));
		this.ownerInfoController.init(this.mainGridPane);

		this.actionsController = Common.loadController(ActionsController.class.getResource("Actions.fxml"));
		this.actionsController.init(model, this.mainGridPane, context.getEvaluator(), this.navigationController, this.elementInfoController);

		Common.getTabPane().getTabs().add(this.tab);
		Common.getTabPane().getSelectionModel().select(this.tab);
	}

	public void println(String str)
	{
		Platform.runLater(() ->this.listView.getItems().add(ConsoleText.defaultText(str)));
	}


	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	private ListView<BorderPaneAndControl> borderPaneListView;

	private class BorderPaneAndControl
	{
		private IControl control;
		private BorderPane pane;

		private Text count;

		public BorderPaneAndControl(IControl control)
		{
			this.control = control;
			this.pane = new BorderPane();
			this.count = new Text();
			this.pane.setLeft(new Text(this.control.toString()));
			this.pane.setRight(count);
		}

		public BorderPane getPane()
		{
			return pane;
		}

		public Text getCount()
		{
			return count;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			BorderPaneAndControl that = (BorderPaneAndControl) o;

			return !(control != null ? !control.equals(that.control) : that.control != null);

		}

		@Override
		public int hashCode()
		{
			return control != null ? control.hashCode() : 0;
		}
	}

	public void displayTestingControls(Collection<IControl> controls)
	{
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.getDialogPane().getStylesheets().add(Common.currentTheme().getPath());
		BorderPane pane = new BorderPane();
		alert.getDialogPane().setContent(pane);
		alert.setTitle("Test");
		alert.setHeaderText("Testing all controls from section Run");
		borderPaneListView = new ListView<>();
		borderPaneListView.setCellFactory(param -> new ListCell<BorderPaneAndControl>(){
			@Override
			protected void updateItem(BorderPaneAndControl item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setGraphic(null);
				}
				else
				{
					setGraphic(item.getPane());
				}
			}
		});
		pane.setCenter(borderPaneListView);
		for (IControl control : controls)
		{
			BorderPaneAndControl borderPane = new BorderPaneAndControl(control);
			borderPaneListView.getItems().add(borderPane);
		}
		alert.show();
		alert.setOnHidden(event -> this.borderPaneListView = null);
	}

	public void displayTestingControl(IControl control, String text, Result result)
	{
		Platform.runLater(() -> 
		{
			if (this.borderPaneListView != null)
			{
				int i = this.borderPaneListView.getItems().indexOf(new BorderPaneAndControl(control));
				BorderPaneAndControl borderPaneAndControl = this.borderPaneListView.getItems().get(i);
				borderPaneAndControl.getCount().setText(text);
				((Text) borderPaneAndControl.getPane().getLeft()).setFill(result.getColor());
			}
		});
	}

	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		this.navigationController.displayDialog(window, windows);
	}

	public void displaySection(SectionKind sectionKind)
	{
		this.navigationController.displaySection(sectionKind);
	}

	public void displayElement(Collection<IControl> controls, IControl control, Collection<IControl> owners, IControl owner, 
			Collection<IControl> rows, IControl row, IControl header)
	{
		this.navigationController.displayElement(control, controls);
		this.ownerInfoController.displayInfo(owner);
		this.elementInfoController.displayInfo(control, owners, owner, rows, row, header);
	}

	public void displayImage(ImageWrapper imageWrapper) throws Exception
	{
		this.actionsController.displayImage(imageWrapper);
	}

	public void displayApplicationStatus(ApplicationStatus status, Throwable throwable, AppConnection appConnection)
	{
		this.navigationController.setAppConnection(appConnection);
		this.actionsController.displayApplicationStatus(status, throwable);
	}

	public void displayActionControl(Collection<String> entries, String entry, Collection<String> titles, String title)
	{
		this.actionsController.displayActionControl(entries, entry, titles, title);
	}


	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------
}
