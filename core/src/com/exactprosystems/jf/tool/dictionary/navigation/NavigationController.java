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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.dictionary.FindListView;
import com.exactprosystems.jf.tool.custom.shutter.DelayShutterButton;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class NavigationController implements Initializable, ContainingParent
{
	public Button btnFindDialog;
	public Button btnFindElement;

	public FindListView<IWindow> listViewWindow;
	public FindListView<BorderPaneAndControl> listViewElement;

	public ToggleGroup groupSection;
	public HBox hBoxElement;
	public BorderPane paneWindow;
	public BorderPane paneElement;
	public Button btnRenameWindow;
	public Button btnNewElement;
	public Button btnDeleteElement;
	public Button btnCopyElement;
	public Button btnPasteElement;
	public Button btnNewDialog;
	public Button btnDeleteDialog;
	public Button btnCopyDialog;
	public Button btnPasteDialog;
	public Button btnTestWindow;

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
		assert hBoxElement != null : "fx:id=\"hBoxElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert btnFindElement != null : "fx:id=\"btnFindElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert btnFindDialog != null : "fx:id=\"btnFindDialog\" was not injected: check your FXML file 'Navigation.fxml'.";
		this.listViewWindow = new FindListView<>((w, s) -> w.getName().toUpperCase().contains(s.toUpperCase()), true);
		this.listViewWindow.setCellFactory(param -> new CustomListCell<>(
				(w, s) -> Common.tryCatch(() -> this.model.dialogRename(w, s), "Error on rename"),
				IWindow::getName,
				(d,i) ->Common.tryCatch(() -> this.model.dialogMove(d,currentSection(), i), "Error on move")
		));
		this.paneWindow.setCenter(this.listViewWindow);
		this.listViewElement = new FindListView<>((e, s) -> (!Str.IsNullOrEmpty(e.control.getID()) && e.control.getID().toUpperCase().contains(s.toUpperCase()) || (e.control.getBindedClass().getClazz().toUpperCase()
				.contains(s.toUpperCase()))),
				false);
		this.listViewElement.setCellFactory(param -> new CustomListCell<>(
				(w, s) -> {},
				e -> e.control.toString(),
				(w, i) -> Common.tryCatch(() -> this.model.elementMove(currentWindow(), currentSection(), w.control,i), "Error on move element")
		));
		this.paneElement.setCenter(this.listViewElement);
		Platform.runLater(() -> {

			ScrollPane scrollPaneWindow = new ScrollPane(this.paneWindow);
			scrollPaneWindow.setFitToWidth(true);
			scrollPaneWindow.setFitToHeight(true);
			scrollPaneWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			scrollPaneWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			Node dialog = BorderWrapper.wrap(this.paneWindow).title("Dialog").color(Common.currentTheme().getReverseColor()).build();
			((Region) dialog).setMinWidth(400.0);
			((Region) dialog).setMaxWidth(400.0);
			((Region) dialog).setPrefWidth(400.0);
			((HBox)this.pane).getChildren().add(0,dialog);

			ScrollPane scrollPaneElement = new ScrollPane(this.paneElement);
			scrollPaneElement.setFitToWidth(true);
			scrollPaneElement.setFitToHeight(true);
			scrollPaneElement.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			scrollPaneElement.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			Node element = BorderWrapper.wrap(this.paneElement).title("Element").color(Common.currentTheme().getReverseColor()).build();
			((Region) element).setMinWidth(400.0);
			((Region) element).setMaxWidth(400.0);
			((Region) element).setPrefWidth(400.0);
			((HBox)this.pane).getChildren().add(element);

			Common.customizeLabeled(this.btnNewDialog, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_NEW);
			Common.customizeLabeled(this.btnDeleteDialog, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_DELETE);
			Common.customizeLabeled(this.btnNewElement, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_NEW);
			Common.customizeLabeled(this.btnDeleteElement, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_DELETE);
			Common.customizeLabeled(this.btnTestWindow, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_TEST);
			Common.customizeLabeled(this.btnCopyDialog, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_COPY);
			Common.customizeLabeled(this.btnCopyElement, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_COPY);
			Common.customizeLabeled(this.btnPasteDialog, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_PASTE);
			Common.customizeLabeled(this.btnPasteElement, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DICTIONARY_PASTE);
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
	// ------------------------------------------------------------------------------------------------------------------

	public void changeSection(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.sectionChanged(currentWindow(), currentSection()), "Error on change section");
	}

	// ------------------------------------------------------------------------------------------------------------------
	public void newElement(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.elementNew(currentWindow(), currentSection()), "Error on new element");
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

	public void testingDialog(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.dialogTest(currentWindow(), this.listViewElement.getItems().stream().map(b -> b.control).collect(Collectors.toList())), "Error on show testing dialog");
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
		this.model.switchToCurrent(currentElement());
	}

	public void doIt(Object obj) throws Exception
	{
		this.model.doIt(obj, currentElement(), currentWindow());
	}
	
	public void chooseXpath(IControl selectedOwner, String xpath) throws Exception
	{
		if (this.appConnection != null)
		{
			Locator owner = null;
			if (selectedOwner == null)
			{
				IControl selfControl = currentWindow().getSelfControl();
				if (selfControl != null && selfControl != currentElement())
				{
					owner = selfControl.locator();
				}
			}
			else
			{
				owner = selectedOwner.locator();
			}
			IRemoteApplication service = this.appConnection.getApplication().service();
			service.startNewDialog();
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


	public void checkNewId(String id) throws Exception
	{
		this.model.checkNewId(currentWindow(), id);
	}


	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		display(window, windows, this.listViewWindow, this.windowChangeListener);
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
		if (control == null)
		{
			controls = new ArrayList<>();
		}
		display(new BorderPaneAndControl(control), controls.stream().map(BorderPaneAndControl::new).collect(Collectors.toList()), this.listViewElement, this.elementChangeListener);
	}

	public void displayTestingControl(IControl control, String text, DictionaryFxController.Result result)
	{
		Platform.runLater(() ->
		{
			int i = this.listViewElement.getItems().indexOf(new BorderPaneAndControl(control));
			BorderPaneAndControl borderPaneAndControl = this.listViewElement.getItems().get(i);
			borderPaneAndControl.getCount().setText(text);
			borderPaneAndControl.getCount().setFill(result.getColor());
		});
	}

	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------
	private IWindow currentWindow()
	{
		return this.listViewWindow.getSelectedItem();
	}

	private IWindow.SectionKind currentSection()
	{
		RadioButton radioButton = (RadioButton) this.groupSection.getSelectedToggle();
		if (radioButton == null)
		{
			return IWindow.SectionKind.Run;
		}
		String name = radioButton.getId();

		return SectionKind.valueOf(name);
	}

	private IControl currentElement()
	{
		return this.listViewElement.getSelectedItem().control;
	}

	public void setChoiseBoxListeners()
	{
		this.listViewElement.addChangeListener(this.elementChangeListener);
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
		DelayShutterButton recOneButton = new DelayShutterButton("Record one component", CssVariables.Icons.DICTIONARY_RECORD_ONE, 10000);
		DelayShutterButton recManyButton = new DelayShutterButton("Record component until toggled", CssVariables.Icons.DICTIONARY_RECORD_INF, 10000);
		DelayShutterButton renewButton = new DelayShutterButton("Rerecord current component", CssVariables.Icons.DICTIONARY_RENEW, 10000);
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
			tryCatch(() -> this.model.elementRecord(x, y, currentWindow(), currentSection()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
			group.selectToggle(null);
		});

		recManyButton.setSwithconAction(h -> tryCatch(this.model::startGrabbing, ""));
		recManyButton.setSwithcoffAction(h -> tryCatch(this.model::endGrabbing, ""));
		recManyButton.setCompleteAction((x, y) -> {
			tryCatch(() -> this.model.elementRecord(x, y, currentWindow(), currentSection()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
		});

		renewButton.setSwithconAction(h -> tryCatch(this.model::startGrabbing, ""));
		renewButton.setSwithcoffAction(h -> tryCatch(this.model::endGrabbing, ""));
		renewButton.setCompleteAction((x, y) -> {
			tryCatch(() -> model.elementRenew(x, y, currentWindow(), currentSection(), currentElement()), "");
			Platform.runLater(() -> ((Stage) scene.getWindow()).setIconified(false));
			group.selectToggle(null);
		});
	}

	private <T> void display(T item, Collection<T> items, FindListView<T> listView, ChangeListener<T> listener)
	{
		Platform.runLater(() -> {
			listView.removeChangeListener(listener);
			if (items != null)
			{
				listView.setData(new ArrayList<>(items), true);
			}
			else
			{
				listView.setData(new ArrayList<>(), true);
			}
			listView.selectItem(item);
			listView.addChangeListener(listener);
		});
	}

	private ChangeListener<IWindow> windowChangeListener = (observable, oldValue, newValue) -> tryCatch(() -> model.windowChanged(newValue, currentSection()), "Error on select window.");

	private ChangeListener<BorderPaneAndControl> elementChangeListener = (observable, oldValue, newValue) ->
			tryCatch(() -> {if (newValue != null) model.elementChanged(currentWindow(), currentSection(), newValue.control);},"Error on select element");

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
			this.pane.setLeft(new Text(this.control != null ? this.control.toString() : ""));
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

	private class CustomListCell<T> extends ListCell<T>
	{
		private TextField textField;
		private IUpdater<T> updater;
		private Function<T, String> converter;
		private BiConsumer<T, Integer> biConsumer;

		public CustomListCell(IUpdater<T> updater, Function<T, String> converter, BiConsumer<T, Integer> biConsumer)
		{
			this.biConsumer = biConsumer;
			this.updater = updater;
			this.converter = converter;
			this.setOnDragDetected(event -> {
				if (this.getItem() != null)
				{
					Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
					ClipboardContent cc = new ClipboardContent();
					int moveIndex = this.getListView().getItems().indexOf(this.getItem());
					cc.putString(String.valueOf(moveIndex));
					db.setContent(cc);
				}
			});

			this.setOnDragOver(event -> {
				if (event.getGestureSource() != this && event.getDragboard().hasString())
				{
					event.acceptTransferModes(TransferMode.MOVE);
				}
				event.consume();
			});

			this.setOnDragDropped(event -> {
				if (getItem() == null)
				{
					return;
				}
				Dragboard db = event.getDragboard();
				if (db.hasString())
				{
					biConsumer.accept(getListView().getItems().get(Integer.parseInt(db.getString())), getIndex());
				}
			});
		}

		@Override
		protected void updateItem(T item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				if (item instanceof BorderPaneAndControl)
				{
					setGraphic(((BorderPaneAndControl) item).getPane());
				}
				else
				{
					setText(this.converter.apply(getItem()));
				}
			}
			else
			{
				setText(null);
				setGraphic(null);
			}
		}

		@Override
		public void startEdit()
		{
			super.startEdit();
			if (textField == null)
			{
				createTextField();
			}
			textField.setText(this.converter.apply(getItem()));
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			Platform.runLater(textField::requestFocus);
		}

		@Override
		public void cancelEdit()
		{
			this.textField = null;
			super.cancelEdit();
			updateItem(getItem(), false);
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		private void createTextField()
		{
			textField = new TextField(this.converter.apply(getItem()));
			textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(t -> {
				if (t.getCode() == KeyCode.ENTER)
				{
					this.updater.update(getItem(), textField.getText());
					commitEdit(getItem());
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
				else if (t.getCode() == KeyCode.TAB)
				{
					this.updater.update(getItem(), textField.getText());
					commitEdit(getItem());
				}
			});
			textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue && textField != null)
				{
					this.updater.update(getItem(), textField.getText());
					commitEdit(getItem());
				}
			});
		}
	}

	private interface IUpdater<T>
	{
		void update(T item, String s);
	}
}
