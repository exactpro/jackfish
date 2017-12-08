////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.guidic.navigation;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.FindListView;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFx;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFxController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.wizard.WizardButton;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class NavigationController implements Initializable, ContainingParent
{
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

	private FindListView<IWindow> listViewWindow;
	private FindListView<BorderPaneAndControl> listViewElement;

	private WizardButton btnWindowWizardManager;
	private WizardButton btnElementWizardManager;

	private Parent pane;

	private Node dialog;
	private Node element;

	private DictionaryFx model;

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
		this.vBoxWindow.getChildren().add(0, this.listViewWindow);

		this.listViewElement = new FindListView<>((e, s) -> (!Str.IsNullOrEmpty(e.control.getID()) && e.control.getID().toUpperCase().contains(s.toUpperCase()) || (e.control.getBindedClass().getClazz().toUpperCase()
				.contains(s.toUpperCase()))),
				false);
		this.listViewElement.setId("findListElement");
		this.vBoxElement.getChildren().add(0, this.listViewElement);

		this.btnWindowWizardManager = WizardButton.normalButton();
		this.hBoxWindow.getChildren().add(this.btnWindowWizardManager);

		this.btnElementWizardManager = WizardButton.normalButton();
		this.hBoxElement.getChildren().add(this.btnElementWizardManager);
		
		ScrollPane scrollPaneWindow = new ScrollPane(this.vBoxWindow);
		scrollPaneWindow.setFitToWidth(true);
		scrollPaneWindow.setFitToHeight(true);
		scrollPaneWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPaneWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.dialog = BorderWrapper.wrap(this.vBoxWindow).title(R.COMMON_DIALOG.get()).color(Theme.currentTheme().getReverseColor()).build();
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
		this.element = BorderWrapper.wrap(this.vBoxElement).title(R.COMMON_ELEMENT.get()).color(Theme.currentTheme().getReverseColor()).build();
		double widthForElement = 316;
		((Region) this.element).setMinWidth(widthForElement);
		((Region) this.element).setMaxWidth(widthForElement);
		((Region) this.element).setPrefWidth(widthForElement);
		HBox.setHgrow(this.element, Priority.ALWAYS);
	}

	public void init(DictionaryFx model, DictionaryFxController controller, Consumer<Parent> consumer)
	{
		this.model = model;

		this.listViewWindow.setCellFactory(param -> new CustomListCell<>(
				(w, s) -> this.model.checkDialogName(s),
				this.model::dialogRename,
				IWindow::getName,
				this.model::dialogMove
		));
		this.listViewElement.setCellFactory(param -> new CustomListCell<>(
				(w, s) -> false,
				(w, s) -> {},
				e -> e.control.toString(),
				(w, i) -> this.model.elementMove(w.control, i)
		));

		this.listViewWindow.addChangeListener((observable, oldValue, newValue) -> this.model.setCurrentWindow(newValue));
		this.groupSection.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
		{
			SectionKind newSection = this.sectionFromToggle(newValue);
			SectionKind oldSection = this.sectionFromToggle(oldValue);
//			//change if needed
			if (newSection != oldSection)
			{
				this.model.setCurrentSection(newSection);
			}
		});
		this.listViewElement.addChangeListener((observable, oldValue, newValue) -> controller.elementChanged(newValue == null ? null : newValue.control));

		Context context = model.getFactory().createContext();
		WizardManager manager = model.getFactory().getWizardManager();
		
		this.btnWindowWizardManager.initButton(context, manager, 
		        () -> new Object[] { this.model, this.model.getCurrentWindow() },
		        () -> new Object[] { this.model.getApp() });
		this.btnElementWizardManager.initButton(context, manager, 
		        () -> new Object[] { this.model, this.model.getCurrentWindow(), this.model.getCurrentSection(), elementForWizard() },
		        () -> new Object[] { this.model.getApp() });
		consumer.accept(this.pane);
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
		tryCatch(() -> this.model.dialogCopy(), R.NAVIGATION_CONTROLLER_COPY_DIALOG.get());
	}

	public void pasteDialog(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.dialogPaste(), R.NAVIGATION_CONTROLLER_PASTE_DIALOG.get());
	}
	//endregion

	//region element event handlers
	public void newElement(ActionEvent actionEvent)
	{
		tryCatch(this.model::createNewElement, R.NAVIGATION_CONTROLLER_NEW_ELEMENT.get());
	}

	public void deleteElement(ActionEvent actionEvent)
	{
		this.model.removeCurrentElement();
	}

	public void copyElement(ActionEvent actionEvent)
	{
		tryCatch(this.model::elementCopy, R.NAVIGATION_CONTROLLER_COPY_ELEMENT.get());
	}

	public void pasteElement(ActionEvent actionEvent)
	{
		tryCatch(this.model::elementPaste, R.NAVIGATION_CONTROLLER_PASTE_ELEMENT.get());
	}

	public void testingDialog(ActionEvent actionEvent)
	{
		this.model.testingAllElements();
	}

	//endregion

	//region display* methods
	public void displayDialogs(Collection<IWindow> dialogs)
	{
		this.listViewWindow.setData(new ArrayList<>(dialogs), true);
	}

	public void addDialog(int index, IWindow dialog)
	{
		this.listViewWindow.addItem(index, dialog);
		this.listViewWindow.select(index);
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
				.ifPresent(RadioButton::fire);
	}

	public void removeElement(IControl control)
	{
		this.listViewElement.removeItem(new BorderPaneAndControl(control));
	}

	public void addElement(int index, IControl control)
	{
		this.listViewElement.addItem(index, new BorderPaneAndControl(control));
		this.listViewElement.select(index);
	}

	public void displayElement(IControl control)
	{
		if (control != null)
		{
			this.listViewElement.refresh();
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

	public void displayTestingControls(DictionaryFx.ControlWithState elementWithState)
	{
		Common.runLater(() -> this.listViewElement.getItems().stream()
				.filter(item -> item.control.equals(elementWithState.getControl()))
				.findFirst()
				.ifPresent(bpWithControl ->
				{
					bpWithControl.getText().setText(elementWithState.getText());
					bpWithControl.getText().setFill(elementWithState.getResult().getColor());
				}));
	}

	//endregion

	//region private
	private IControl elementForWizard()
	{
		IControl currentElement = this.model.getCurrentElement();
		if(currentElement != null)
		{
			return currentElement;
		}
		else
		{
			try
			{
				this.model.createNewElement();
				IControl newElement = this.model.getCurrentElement();
				SectionKind sc = this.model.getCurrentSection();
				if(sc == SectionKind.Self)
				{
					((AbstractControl) newElement).set(AbstractControl.idName, "self");
				}
				return newElement;

			}
			catch (Exception e)
			{

			}
			return null;
		}
	}

	private IWindow.SectionKind sectionFromToggle(Toggle toggle)
	{
		RadioButton radioButton = (RadioButton) toggle;
		if (radioButton == null)
		{
			return IWindow.SectionKind.Run;
		}
		String name = radioButton.getId();

		return SectionKind.valueOf(name);
	}

	private class BorderPaneAndControl
	{
		private IControl control;
		private BorderPane pane;

		private Text text;

		private BorderPaneAndControl(IControl control)
		{
			this.control = control;
			this.pane = new BorderPane();
			this.text = new Text();
			this.updatePane();
		}

		public BorderPane getPane()
		{
			this.updatePane();
			return pane;
		}

		public Text getText()
		{
			return text;
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

		private void updatePane()
		{
			this.pane.setLeft(null);
			this.pane.setRight(null);

			this.pane.setLeft(new Text(this.control != null ? this.control.toString() : ""));
			this.pane.setRight(this.text);
		}

		@Override
		public String toString()
		{
			return String.valueOf(this.control);
		}
	}

	private class CustomListCell<T> extends ListCell<T>
	{
		private TextField textField;
		private BiConsumer<T, String> updater;
		private Function<T, String> converter;
        private BiFunction<T, String, Boolean> checker;

		private CustomListCell(BiFunction<T, String, Boolean> checker, BiConsumer<T, String> updater, Function<T, String> converter, BiConsumer<T, Integer> biConsumer)
		{
		    this.checker = checker;
			this.updater = updater;
			this.converter = converter;
			this.setOnDragDetected(event -> {
				if (this.getItem() != null)
				{
					Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
					ClipboardContent cc = new ClipboardContent();
					Text text = new Text(this.toString());
					db.setDragView(text.snapshot(new SnapshotParameters(), null));
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

		@Override
		public String toString()
		{
			return String.valueOf(this.getItem());
		}
	}

	//endregion
}
