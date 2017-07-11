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
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.ServiceLambdaBean;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.wizard.WizardButton;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController;
import com.exactprosystems.jf.tool.dictionary.FindListView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
	public VBox vBoxWindow;
    public HBox hBoxWindow;
	public VBox vBoxElement;
    public HBox hBoxElement;

	public Button btnNewElement;
	public Button btnDeleteElement;
	public Button btnCopyElement;
	public Button btnPasteElement;
	public Button btnNewDialog;
	public Button btnDeleteDialog;
	public Button btnCopyDialog;
	public Button btnPasteDialog;
	public Button btnTestWindow;
	public Button btnShowWizard;
    public WizardButton btnWindowWizardManager;
    public WizardButton btnElementWizardManager;

	private Parent pane;

	private DictionaryFx model;
	private boolean fullScreen = false;
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
		assert btnFindElement != null : "fx:id=\"btnFindElement\" was not injected: check your FXML file 'Navigation.fxml'.";
		assert btnFindDialog != null : "fx:id=\"btnFindDialog\" was not injected: check your FXML file 'Navigation.fxml'.";
		
		this.listViewWindow = new FindListView<>((w, s) -> w.getName().toUpperCase().contains(s.toUpperCase()), true);
		this.listViewWindow.setCellFactory(param -> new CustomListCell<>(
		        (w, s) -> this.model.checkDialogName(w, s),
				(w, s) -> Common.tryCatch(() -> this.model.dialogRename(w, s), "Error on rename"),
				IWindow::getName,
				(d,i) -> Common.tryCatch(() -> this.model.dialogMove(d,currentSection(), i), "Error on move")
		));
		
		this.vBoxWindow.getChildren().add(0, this.listViewWindow);

		this.btnWindowWizardManager = new WizardButton();
		this.btnWindowWizardManager.setVisible(VersionInfo.isDevVersion());
        this.hBoxWindow.getChildren().add(this.btnWindowWizardManager);
		
        this.btnElementWizardManager = new WizardButton();
        this.btnElementWizardManager.setVisible(VersionInfo.isDevVersion());
        this.hBoxElement.getChildren().add(this.btnElementWizardManager);
		
		this.listViewElement = new FindListView<>((e, s) -> (!Str.IsNullOrEmpty(e.control.getID()) && e.control.getID().toUpperCase().contains(s.toUpperCase()) || (e.control.getBindedClass().getClazz().toUpperCase()
				.contains(s.toUpperCase()))),
				false);
		this.listViewElement.setCellFactory(param -> new CustomListCell<>(
		        (w, s) -> this.model.checkNewId(w.control.getSection().getWindow(), w.control, s),
				(w, s) -> {},
				e -> e.control.toString(),
				(w, i) -> Common.tryCatch(() -> this.model.elementMove(currentWindow(), currentSection(), w.control,i), "Error on move element")
		));
		this.vBoxElement.getChildren().add(0, this.listViewElement);
		
		Platform.runLater(() -> 
		{
			ScrollPane scrollPaneWindow = new ScrollPane(this.vBoxWindow);
			scrollPaneWindow.setFitToWidth(true);
			scrollPaneWindow.setFitToHeight(true);
			scrollPaneWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			scrollPaneWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			Node dialog = BorderWrapper.wrap(this.vBoxWindow).title("Dialog").color(Common.currentTheme().getReverseColor()).build();
			double width = 350.0;
			((Region) dialog).setMinWidth(width);
			((Region) dialog).setMaxWidth(width);
			((Region) dialog).setPrefWidth(width);
			((HBox)this.pane).getChildren().add(0,dialog);
			HBox.setHgrow(dialog, Priority.ALWAYS);

			ScrollPane scrollPaneElement = new ScrollPane(this.vBoxElement);
			scrollPaneElement.setFitToWidth(true);
			scrollPaneElement.setFitToHeight(true);
			scrollPaneElement.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			scrollPaneElement.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			Node element = BorderWrapper.wrap(this.vBoxElement).title("Element").color(Common.currentTheme().getReverseColor()).build();
			double widthForElement = 316;
			((Region) element).setMinWidth(widthForElement);
			((Region) element).setMaxWidth(widthForElement);
			((Region) element).setPrefWidth(widthForElement);
			((HBox)this.pane).getChildren().add(element);
			HBox.setHgrow(element, Priority.ALWAYS);
		});
	}

	public void init(DictionaryFx model, GridPane gridPane, Settings settings, CustomTab owner)
	{
		this.model = model;
		//TODO this need move from here to model
		this.fullScreen = Boolean.parseBoolean(settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, "useFullScreenXpath", "false").getValue());
		setChoiseBoxListeners();

		Context context = model.getFactory().createContext();
		WizardManager manager = model.getFactory().getWizardManager();
		
		this.btnWindowWizardManager.initButton(context, manager, 
		        () -> new Object[] { model, currentWindow() }, 
		        () -> new Object[] { this.appConnection });
		this.btnElementWizardManager.initButton(context, manager, 
		        () -> new Object[] { model, currentWindow(), currentSection(), currentElement() }, 
		        () -> new Object[] { this.appConnection });
		
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

	public void openWizard(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.openDialogWizard(currentWindow()), "Error on open dialog wizard");
	}

	public void setDisableWizardButton(boolean flag)
	{
		this.btnShowWizard.setDisable(flag);
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

	public void parameterGoToOwner(IControl control) throws Exception
	{
		this.model.parameterGoToOwner(currentWindow(), control);
	}

	public void parameterSetOwner(String ownerId) throws Exception
	{
		this.model.parameterSet(currentWindow(), currentSection(), currentElement(), AbstractControl.ownerIdName, ownerId);
	}

	public void parameterSetRef(String refId) throws Exception
	{
		this.model.parameterSet(currentWindow(), currentSection(), currentElement(), AbstractControl.refIdName, refId);
	}

	public void parameterSet(String parameter, Object value) throws Exception
	{
		this.model.parameterSet(currentWindow(), currentSection(), currentElement(), parameter, value);
	}

	public void parameterSet(String parameter, Object value, IControl control) throws Exception
	{
		this.model.parameterSet(currentWindow(), currentSection(), control, parameter, value);
	}

	public void displayElementWithoutInfo(IWindow window) throws Exception
	{
		if(window==currentWindow())
		{
			this.model.displayElementWithoutInfo(window, currentSection(), currentElement());
		}
	}

	public void sendKeys(String text) throws Exception
	{
		this.model.sendKeys(text, currentElement(), currentWindow());
	}

	public void getValue() throws Exception
	{
		this.model.getValue(currentElement(), currentWindow());
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
			if (selectedOwner != null)
			{
	            owner = selectedOwner.locator();
			}
			IRemoteApplication service = this.appConnection.getApplication().service();
			service.startNewDialog();

			Locator finalOwner = owner;
			XpathViewer xpathViewer = new XpathViewer(owner, () -> {
				byte[] treeBytes = service.getTreeBytes(finalOwner);
				return Converter.convertByteArrayToXmlDocument(treeBytes);
			}, new ServiceLambdaBean(
					() -> service.getImage(null, finalOwner).getImage(),
					() -> service.getRectangle(null, finalOwner))
			);
			String id = currentElement().getID();
			String result = xpathViewer.show(xpath, "Xpath for " + (id == null ? "empty" : id), Common.currentThemesPaths(), this.fullScreen);
			this.model.parameterSet(currentWindow(), currentSection(), currentElement(), AbstractControl.xpathName, result);

			//			Document document = service.getTree(owner);
//			byte[] treeBytes = service.getTreeBytes(owner);
//			Document document = Converter.convertByteArrayToXmlDocument(treeBytes);
//			if (document != null)
//			{
//				XpathViewer viewer = new XpathViewer(owner, document, service);
//				String id = currentElement().getID();
//
//				String result = viewer.show(xpath, "Xpath for " + (id == null ? "empty" : id), Common.currentThemesPaths(), this.fullScreen);
//		        this.model.parameterSet(currentWindow(), currentSection(), currentElement(), AbstractControl.xpathName, result);
//			}
		}
	}


	public boolean checkNewId(String id)
	{
		return this.model.checkNewId(currentWindow(), currentElement(), id);
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
		if (controls == null)
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

	public void close()
	{
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
		private BiConsumer<T, String> updater;
		private Function<T, String> converter;
        private BiFunction<T, String, Boolean> checker;

		public CustomListCell(BiFunction<T, String, Boolean> checker, BiConsumer<T, String> updater, Function<T, String> converter, BiConsumer<T, Integer> biConsumer)
		{
		    this.checker = checker;
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
			textField.setMinWidth(225);
			textField.setOnKeyPressed(t -> {
			    KeyCode code = t.getCode();
				if (code == KeyCode.ENTER || code == KeyCode.TAB)
				{
				    checkAndRename();
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
			});
			
			textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue && textField != null)
				{
                    checkAndRename();
				}
			});
		}


		private void checkAndRename()
        {
            if (this.checker.apply(getItem(), textField.getText()))
            {
                this.updater.accept(getItem(), textField.getText());
                commitEdit(getItem());
            }
            else
            {
                DialogsHelper.showError("Dialog with name " + textField.getText() + " already exists.");   
                cancelEdit();
            }
        }
	}
}
