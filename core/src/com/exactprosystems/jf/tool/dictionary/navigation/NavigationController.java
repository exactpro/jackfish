////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.navigation;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.shutter.DelayShutterButton;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class NavigationController implements Initializable, ContainingParent
{
	public Button btnFindDialog;
	public Button btnFindElement;

	public ChoiceBox<IWindow> choiceBoxWindow;
	public ChoiceBox<IControl> choiceBoxElement;

	public ToggleGroup groupSection;
	public HBox hBoxElement;
	public CheckBox checkBoxUseSelfAsOwner;
	public BorderPane borderPaneWindow;
	public BorderPane paneWindow;
	public GridPane dialogGridPane;
	public Button btnRenameWindow;
	public GridPane elementGridPane;
	private Parent pane;

	private DictionaryFx model;
	private boolean fullScreen = false;
	private String themePath;
	private AppConnection appConnection;
	
	public void setAppConnection(AppConnection appConnection)
	{
		this.appConnection = appConnection;
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert groupSection != null : "fx:id=\"buttonGroup\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert choiceBoxWindow != null : "fx:id=\"comboBoxWindow\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert choiceBoxElement != null : "fx:id=\"comboBoxElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert checkBoxUseSelfAsOwner != null : "fx:id=\"checkBoxUseSelfAsOwner\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert hBoxElement != null : "fx:id=\"hBoxElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert btnFindElement != null : "fx:id=\"btnFindElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert btnFindDialog != null : "fx:id=\"btnFindDialog\" was not injected: check your FXML file 'Navigation.fxml'.";
		Platform.runLater(() -> {
			((GridPane) this.pane).add(BorderWrapper.wrap(this.dialogGridPane).title("Dialog").color(Common.currentTheme().getReverseColor()).build(), 0, 0);
			((GridPane) this.pane).add(BorderWrapper.wrap(this.elementGridPane).title("Element").color(Common.currentTheme().getReverseColor()).build(), 2, 0);
		});
	}

	public void init(DictionaryFx model, GridPane gridPane, Settings settings, CustomTab owner)
	{
		this.model = model;
		this.fullScreen = Boolean.parseBoolean(settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, "useFullScreenXpath", "false").getValue());
		Settings.SettingsValue theme = settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.THEME, Theme.WHITE.name());
		this.themePath = Theme.valueOf(theme.getValue()).getPath();
		
		boolean compactMode = Boolean.parseBoolean(settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.USE_SMALL_WINDOW, "false").getValue());
		setChoiseBoxListeners();
		createShutters(this.hBoxElement, owner, compactMode);

		gridPane.add(this.pane, 0, 0);
		GridPane.setColumnSpan(this.pane, 2);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// Event handlers
	// ------------------------------------------------------------------------------------------------------------------
	public void newWindow(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogNew(currentSection()), "Error on add new window");
	}

	public void deleteWindow(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogDelete(currentWindow(), currentSection()), "Error on delete window");
	}

	public void copyDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogCopy(currentWindow()), "Error on copy dialog");
	}

	public void pasteDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogPaste(currentSection()), "Error on paste dialog");
	}

	public void findDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> {
			ArrayList<IWindow> windows = new ArrayList<>(this.choiceBoxWindow.getItems());
			showFindPanel(windows, this.choiceBoxWindow, "Find dialogs");
		}, "Error on find dialog");
	}

	public void renameWindow(ActionEvent actionEvent)
	{
		tryCatch(() -> {
			final IWindow window = currentWindow();
			final String oldName = window.getName();
			final TextArea textField = new TextArea();
			textField.setPrefHeight(25);
			textField.setMinHeight(25);
			textField.setMaxHeight(25);
			btnFindDialog.setDisable(true);
			borderPaneWindow.setCenter(textField);
			textField.setText(oldName);
			textField.requestFocus();
			textField.positionCaret(oldName.length());
			textField.setOnKeyPressed(keyEvent -> {
				if (keyEvent.getCode() == KeyCode.ENTER)
				{
					tryCatch(() -> model.dialogRename(window, textField.getText()), "Error on rename");
					comboBoxToFront();
				}
				if (keyEvent.getCode() == KeyCode.ESCAPE)
				{
					comboBoxToFront();
				}
			});
		}, "Error on rename window");
	}

	// ------------------------------------------------------------------------------------------------------------------

	public void changeSection(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.sectionChanged(currentWindow(), currentSection()), "Error on change section");
	}

	// ------------------------------------------------------------------------------------------------------------------
	public void newElement(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.elementNew(currentWindow(), currentSection(), useSelfAsOwner()), "Error on new element");
	}

	public void deleteElement(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.elementDelete(currentWindow(), currentSection(), currentElement()), "Error on delete element");
	}

	public void copyElement(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.elementCopy(currentWindow(), currentSection(), currentElement()), "Error on copy element");
	}

	public void pasteElement(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.elementPaste(currentWindow(), currentSection()), "Error on paste element");
	}

	public void findElement(ActionEvent actionEvent)
	{
		tryCatch(() -> {
			ArrayList<IControl> controls = new ArrayList<>(this.choiceBoxElement.getItems());
			showFindPanel(controls, this.choiceBoxElement, "Find elements");
		}, "Error on find element");
	}

	public void testingDialog(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.dialogTest(currentWindow()), "Error on show testing dialog");

	}

	// ------------------------------------------------------------------------------------------------------------------
	// pass-throw methods
	// ------------------------------------------------------------------------------------------------------------------
	public void parameterSetControlKind(ControlKind controlKind) throws Exception
	{
		this.model.parameterSetControlKind(currentWindow(), currentSection(), currentElement(), controlKind);
	}

	public void parameterSetId(Object value) throws Exception
	{
		this.model.parameterSetId(currentWindow(), currentSection(), currentElement(), value);
	}

	public void parameterGoToOwner(IControl control) throws Exception
	{
		this.model.parameterGoToOwner(currentWindow(), control);
	}

	public void parameterSetOwner(String ownerId) throws Exception
	{
		this.model.parameterSetOwner(currentWindow(), currentSection(), currentElement(), ownerId);
	}

	public void parameterSet(String parameter, Object value) throws Exception
	{
		this.model.parameterSet(currentWindow(), currentSection(), currentElement(), parameter, value);
	}


	public void sendKeys(String text) throws Exception
	{
		this.model.sendKeys(text, currentElement(), currentWindow());
	}

	public void click() throws Exception
	{
		this.model.click(currentElement(), currentWindow());
	}

	public void find() throws Exception
	{
		this.model.find(currentElement(), currentWindow());
	}

	public void switchToCurrent() throws Exception
	{
		this.model.switchToCurrent(currentWindow());
	}

	public void doIt(Object obj) throws Exception
	{
		this.model.doIt(obj, currentElement(), currentWindow());
	}
	
	public void chooseXpath(IControl selectedOwner, String xpath) throws Exception
	{
		if (this.appConnection != null)
		{
			Locator owner = selectedOwner == null ? null : selectedOwner.locator();
			IRemoteApplication service = this.appConnection.getApplication().service();
			Document document = service.getTree(owner);
			if (document != null)
			{
				XpathViewer viewer = new XpathViewer(owner, document, service);
				String id = currentElement().getID();
	
				String result = viewer.show(xpath, "Xpath for " + (id == null ? "empty" : id), this.themePath, this.fullScreen);
				this.model.parameterSetXpath(currentWindow(), currentSection(), currentElement(), result);
			}
		}
	}


	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		display(window, windows, this.choiceBoxWindow, this.windowChangeListener);
	}

	public void displaySection(SectionKind sectionKind)
	{
		Platform.runLater(() -> {
			String kindName = String.valueOf(sectionKind);

			for (Toggle toggle : this.groupSection.getToggles())
			{
				RadioButton rb = (RadioButton) toggle;
				if (rb.getId().equals(kindName))
				{
					rb.fire();
					rb.setSelected(true);
					return;
				}
			}
		});
	}

	public void displayElement(IControl control, Collection<IControl> controls)
	{
		display(control, controls, this.choiceBoxElement, this.elementChangeListener);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------
	private IWindow currentWindow()
	{
		return this.choiceBoxWindow.getSelectionModel().getSelectedItem();
	}

	private IWindow.SectionKind currentSection()
	{
		RadioButton radioButton = (RadioButton) this.groupSection.getSelectedToggle();
		if (radioButton == null)
		{
			return IWindow.SectionKind.Run;
		}
		String name = radioButton.getId();
		IWindow.SectionKind kind = IWindow.SectionKind.valueOf(name);

		return kind;
	}

	private IControl currentElement()
	{
		return this.choiceBoxElement.getSelectionModel().getSelectedItem();
	}

	private boolean useSelfAsOwner()
	{
		return this.checkBoxUseSelfAsOwner.isSelected();
	}

	public void setChoiseBoxListeners()
	{
		this.choiceBoxWindow.getSelectionModel().selectedItemProperty().addListener(this.windowChangeListener);
		this.choiceBoxElement.getSelectionModel().selectedItemProperty().addListener(this.elementChangeListener);
	}

	private EventHandler<KeyEvent> pressHandler;
	private EventHandler<KeyEvent> releaseHandler;

	public void close()
	{
		Scene scene = Common.getTabPane().getScene();
		scene.removeEventFilter(KeyEvent.KEY_PRESSED, pressHandler);
		scene.removeEventFilter(KeyEvent.KEY_RELEASED, releaseHandler);
	}

	private void createShutters(HBox hBox, CustomTab owner, boolean compactMode)
	{
		DelayShutterButton recOneButton = new DelayShutterButton("R 1", 50.0, 10000);
		DelayShutterButton recManyButton = new DelayShutterButton("R ∞", 50.0, 10000);
		DelayShutterButton renewButton = new DelayShutterButton("Renew", 70.0, 10000);
		HBox.setMargin(recOneButton, new Insets(0, 0, 0, 5));
		ToggleGroup group = new ToggleGroup();
		recOneButton.setToggleGroup(group);
		recManyButton.setToggleGroup(group);
		renewButton.setToggleGroup(group);

		hBox.getChildren().add(0, recOneButton);
		hBox.getChildren().add(1, recManyButton);
		hBox.getChildren().add(2, renewButton);
		Scene scene = Common.getTabPane().getScene();
		pressHandler = keyEvent -> {
			if (keyEvent.getCode() == KeyCode.CONTROL && owner != null && owner.isSelected())
			{
				recOneButton.start();
				recManyButton.start();
				renewButton.start();
			}
		};
		releaseHandler = keyEvent -> {
			if (keyEvent.getCode() == KeyCode.CONTROL && owner != null && owner.isSelected())
			{
				if (recOneButton.isSelected() || recManyButton.isSelected() || renewButton.isSelected())
				{
					if (compactMode)
					{
						((Stage) scene.getWindow()).setIconified(true);
					}
				}
				recOneButton.stop();
				recManyButton.stop();
				renewButton.stop();
			}
		};
		scene.addEventHandler(KeyEvent.KEY_PRESSED, pressHandler);
		scene.addEventHandler(KeyEvent.KEY_RELEASED, releaseHandler);

		recOneButton.setSwithconAction(h -> tryCatch(this.model::startGrabbing, ""));
		recOneButton.setSwithcoffAction(h -> tryCatch(this.model::endGrabbing, ""));
		recOneButton.setCompleteAction((x, y) -> {
			tryCatch(() -> this.model.elementRecord(x, y, useSelfAsOwner(), currentWindow(), currentSection()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
			group.selectToggle(null);
		});

		recManyButton.setSwithconAction(h -> tryCatch(this.model::startGrabbing, ""));
		recManyButton.setSwithcoffAction(h -> tryCatch(this.model::endGrabbing, ""));
		recManyButton.setCompleteAction((x, y) -> {
			tryCatch(() -> this.model.elementRecord(x, y, useSelfAsOwner(), currentWindow(), currentSection()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
		});

		renewButton.setSwithconAction(h -> tryCatch(this.model::startGrabbing, ""));
		renewButton.setSwithcoffAction(h -> tryCatch(this.model::endGrabbing, ""));
		renewButton.setCompleteAction((x, y) -> {
			tryCatch(() -> model.elementRenew(x, y, useSelfAsOwner(), currentWindow(), currentSection(), currentElement()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
			group.selectToggle(null);
		});
	}

	private <T> void showFindPanel(final List<T> list, final ChoiceBox<T> cb, String title)
	{
		DialogsHelper.showFindListView(list, title, t -> cb.getSelectionModel().select(t));
	}

	private <T> void display(T item, Collection<T> items, ChoiceBox<T> choiceBox, ChangeListener<T> listener)
	{
		Platform.runLater(() -> {
			choiceBox.getSelectionModel().selectedItemProperty().removeListener(listener);

			choiceBox.getItems().clear();
			if (items != null)
			{
				choiceBox.setItems(FXCollections.observableArrayList(items));
			}
			else
			{
				choiceBox.setItems(FXCollections.observableArrayList());
			}
			choiceBox.getSelectionModel().select(item);

			choiceBox.getSelectionModel().selectedItemProperty().addListener(listener);
		});
	}

	private void comboBoxToFront()
	{
		this.borderPaneWindow.setCenter(paneWindow);
		this.paneWindow.toFront();
		this.btnFindDialog.setDisable(false);
	}

	private ChangeListener<IWindow> windowChangeListener = (observable, oldValue, newValue) -> tryCatch(() -> model.windowChanged(newValue, currentSection()), "Error on select window.");

	private ChangeListener<IControl> elementChangeListener = (observable, oldValue, newValue) -> tryCatch(() -> model.elementChanged(currentWindow(), currentSection(), newValue), "Error on select element");
}
