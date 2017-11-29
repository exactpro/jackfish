////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.navigation;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController;
import com.exactprosystems.jf.tool.dictionary.FindListView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.wizard.WizardButton;
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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class NavigationController implements Initializable, ContainingParent
{
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
    public WizardButton btnWindowWizardManager;
    public WizardButton btnElementWizardManager;

	private Parent pane;

	private Node dialog;
	private Node element;

	private DictionaryFxController mainController;
	private DictionaryFx model;

	@Deprecated
	private AppConnection appConnection;

	@Deprecated
	public void setAppConnection(AppConnection appConnection)
	{
		this.appConnection = appConnection;
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
		Common.runLater(() ->
		{
			((HBox)this.pane).getChildren().add(0,this.dialog);
			((HBox)this.pane).getChildren().add(this.element);
		});
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.listViewWindow = new FindListView<>((w, s) -> w.getName().toUpperCase().contains(s.toUpperCase()), true);
		this.listViewWindow.setId("findListWindow");
		this.listViewWindow.setCellFactory(param -> new CustomListCell<>(
		        (w, s) -> this.model.checkDialogName(s),
				(w, s) -> Common.tryCatch(() -> this.model.dialogRename(w, s), "Error on rename"),
				IWindow::getName,
				(d,i) -> Common.tryCatch(() -> this.model.dialogMove(d,currentSection(), i), "Error on move")
		));
		
		this.vBoxWindow.getChildren().add(0, this.listViewWindow);

		this.btnWindowWizardManager = WizardButton.normalButton();
        this.hBoxWindow.getChildren().add(this.btnWindowWizardManager);

		this.btnElementWizardManager = WizardButton.normalButton();
		this.hBoxElement.getChildren().add(this.btnElementWizardManager);
		
		this.listViewElement = new FindListView<>((e, s) -> (!Str.IsNullOrEmpty(e.control.getID()) && e.control.getID().toUpperCase().contains(s.toUpperCase()) || (e.control.getBindedClass().getClazz().toUpperCase()
				.contains(s.toUpperCase()))),
				false);
		this.listViewElement.setId("findListElement");
		this.listViewElement.setCellFactory(param -> new CustomListCell<>(
		        (w, s) -> this.model.checkNewId(w.control.getSection().getWindow(), w.control, s),
				(w, s) -> {},
				e -> e.control.toString(),
				(w, i) -> Common.tryCatch(() -> this.model.elementMove(currentWindow(), currentSection(), w.control,i), "Error on move element")
		));

		this.vBoxElement.getChildren().add(0, this.listViewElement);
		
		ScrollPane scrollPaneWindow = new ScrollPane(this.vBoxWindow);
		scrollPaneWindow.setFitToWidth(true);
		scrollPaneWindow.setFitToHeight(true);
		scrollPaneWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPaneWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.dialog = BorderWrapper.wrap(this.vBoxWindow).title("Dialog").color(Theme.currentTheme().getReverseColor()).build();
		double width = 350.0;
		((Region) this.dialog).setMinWidth(width);
		((Region) this.dialog).setMaxWidth(width);
		((Region) this.dialog).setPrefWidth(width);

		HBox.setHgrow(this.dialog, Priority.ALWAYS);

		ScrollPane scrollPaneElement = new ScrollPane(this.vBoxElement);
		scrollPaneElement.setFitToWidth(true);
		scrollPaneElement.setFitToHeight(true);
		scrollPaneElement.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPaneElement.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.element = BorderWrapper.wrap(this.vBoxElement).title("Element").color(Theme.currentTheme().getReverseColor()).build();
		double widthForElement = 316;
		((Region) this.element).setMinWidth(widthForElement);
		((Region) this.element).setMaxWidth(widthForElement);
		((Region) this.element).setPrefWidth(widthForElement);
		HBox.setHgrow(this.element, Priority.ALWAYS);
	}

	public void init(DictionaryFx model, DictionaryFxController mainController, GridPane gridPane)
	{
		this.mainController = mainController;
		this.model = model;

		this.listViewWindow.addChangeListener((observable, oldValue, newValue) -> this.model.setCurrentWindow(newValue));
		this.groupSection.selectedToggleProperty().addListener((observable, oldValue, newValue) -> this.model.setCurrentSection(this.currentSection()));
		this.listViewElement.addChangeListener((observable, oldValue, newValue) -> this.model.setCurrentElement(newValue == null ? null : newValue.control));

		Context context = model.getFactory().createContext();
		WizardManager manager = model.getFactory().getWizardManager();
		
		this.btnWindowWizardManager.initButton(context, manager, 
		        () -> new Object[] { model, currentWindow() }, 
		        () -> new Object[] { this.appConnection });
		this.btnElementWizardManager.initButton(context, manager, 
		        () -> new Object[] { model, currentWindow(), currentSection(), elementForWizard() },
		        () -> new Object[] { this.appConnection });
		
		gridPane.add(this.pane, 0, 0);
	}

	//region dialog event handlers
	public void newDialog(ActionEvent actionEvent)
	{
		this.model.createNewDialog();
	}

	public void removeDialog(ActionEvent actionEvent)
	{
		this.model.removeCurrentDialog();
	}

	public void copyDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogCopy(), "Error on copy dialog");
	}

	public void pasteDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogPaste(), "Error on paste dialog");
	}
	//endregion

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
		this.model.switchToCurrent(currentElement(), currentWindow());
	}

	public void doIt(Object obj) throws Exception
	{
		this.model.doIt(obj, currentElement(), currentWindow());
	}

	public boolean checkNewId(String id)
	{
		return this.model.checkNewId(currentWindow(), currentElement(), id);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayDialogs(Collection<IWindow> dialogs)
	{
		this.listViewWindow.setData(new ArrayList<>(dialogs), true);
	}

	public void addDialog(int index, IWindow dialog)
	{
		this.listViewWindow.addItem(index, dialog);
	}

	public void removeDialog(IWindow dialog)
	{
		this.listViewWindow.removeItem(dialog);
	}

	public void displayDialog(IWindow window)
	{
		this.listViewWindow.selectItem(window);
	}

	public void dialogChangeName(String newName)
	{
		this.listViewWindow.refresh();
	}

	public void displaySection(SectionKind sectionKind)
	{
		String kindName = String.valueOf(sectionKind);
		this.groupSection.getToggles()
				.stream()
				.map(toggle -> (RadioButton) toggle)
				.filter(rb -> rb.getId().equals(kindName))
				.findFirst()
				.ifPresent(rb ->
				{
					rb.setSelected(true);
					rb.fire();
				});
	}

	public void displayElement(IControl control)
	{
		if (control != null)
		{
			this.listViewElement.selectItem(new BorderPaneAndControl(control));
		}
	}

	public void displayElements(Collection<? extends IControl> collection)
	{
		List<BorderPaneAndControl> collect = collection.stream().map(BorderPaneAndControl::new).collect(Collectors.toList());
		this.listViewElement.setData(collect, true);
	}

	public void clearElements()
	{
		this.listViewElement.setData(Collections.emptyList(), true);
	}


	@Deprecated
	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		display(window, windows, this.listViewWindow, this.windowChangeListener);
	}

	@Deprecated
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
		Common.runLater(() ->
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


	public IWindow currentWindow()
	{
		return this.listViewWindow.getSelectedItem();
	}

	private IControl elementForWizard()
	{
		if(currentElement() != null)
		{
			return currentElement();
		}
		else
		{
			try
			{
				SectionKind sc = currentSection();
				AbstractControl element = AbstractControl.create(ControlKind.Any);
				if(sc == SectionKind.Self)
				{
					element.set(AbstractControl.idName, "self");
				}
				currentWindow().getSection(sc).addControl(element);
				return element;

			}
			catch (Exception e)
			{

			}
			return null;
		}
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

	@Deprecated
	private <T> void display(T item, Collection<T> items, FindListView<T> listView, ChangeListener<T> listener)
	{
		Common.runLater(() -> {
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
			Common.runLater(textField::requestFocus);
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
