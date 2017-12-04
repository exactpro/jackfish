////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.element;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.Visibility;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.exactprosystems.jf.tool.Common.get;
import static com.exactprosystems.jf.tool.Common.tryCatch;

public class ElementInfoController implements Initializable, ContainingParent
{
	public ComboBox<ControlKind> comboBoxControl;
	public ChoiceBox<Visibility> choiceBoxVisibility;
	public ChoiceBox<Addition>   choiceBoxAddition;
	public ChoiceBox<IControl>   choiceBoxHeader;
	public ChoiceBox<IControl>   choiceBoxRows;
	public ChoiceBox<IControl>   choiceBoxOwner;
	public ChoiceBox<IControl>   choiceBoxReference;
	public CheckBox              checkBoxWeak;
	public CustomFieldWithButton tfID;
	public CustomFieldWithButton tfUID;
	public CustomFieldWithButton tfXpath;
	public CustomFieldWithButton tfClass;
	public CustomFieldWithButton tfText;
	public CustomFieldWithButton tfName;
	public CustomFieldWithButton tfTooltip;
	public CustomFieldWithButton tfColumns;
	public CustomFieldWithButton tfAction;
	public CustomFieldWithButton tfTitle;
	public CustomFieldWithButton tfExpression;
	public TextField             tfTimeout;
	public GridPane              mainGrid;
	public Button                btnGoToOwner;
	public GridPane              fieldGrid;
	public GridPane              identifiersGrid;

	private Parent                      pane;
	private DictionaryFx                model;
	private String previousId = null;

	private TextField editedTextField = null;
	private List<Node> allNodes;
	private transient boolean disableAllListeners = false;

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
		Common.runLater(() -> ((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).color(Theme.currentTheme().getReverseColor()).title("Element info").build()));
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		Stream.of(tfID, tfUID, tfXpath, tfClass, tfText, tfName, tfTooltip, tfColumns, tfAction, tfTitle, tfExpression).forEach(tf -> {
			tf.prefWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).maxWidthProperty());
			tf.maxWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).maxWidthProperty());
			tf.minWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).minWidthProperty());
		});

		this.allNodes = Arrays.asList(
				this.comboBoxControl, this.choiceBoxVisibility, this.choiceBoxAddition, this.choiceBoxHeader, this.choiceBoxRows, this.choiceBoxOwner, this.choiceBoxReference, this.checkBoxWeak,
				this.tfID, this.tfUID, this.tfXpath, this.tfClass, this.tfText, this.tfName, this.tfTooltip, this.tfColumns, this.tfAction, this.tfTitle, this.tfExpression, this.tfTimeout, this.btnGoToOwner);
	}

	public void init(DictionaryFx model, Consumer<Parent> consumer)
	{
		this.model = model;
		this.setItems(this.choiceBoxAddition, Addition.values());
		this.setItems(this.choiceBoxVisibility, Visibility.values());

		this.comboBoxControl.getItems().setAll(ControlKind.values());

		consumer.accept(this.pane);

		this.addAllListeners();
	}

	public void displaySupportedControls(Collection<ControlKind> supportedControls)
	{
		try
		{
			this.disableAllListeners = true;
			ControlKind selectedItem = this.comboBoxControl.getSelectionModel().getSelectedItem();
			this.comboBoxControl.getItems().setAll(supportedControls);
			this.selectControlKind(supportedControls, selectedItem);
		}
		finally
		{
			this.disableAllListeners = false;
		}
	}

	public void displayElement(IControl control)
	{
		try
		{
			this.disableAllListeners = true;
			this.allNodes.forEach(node -> node.setDisable(false));
			this.pane.setDisable(control == null);

			List<IControl> allControlsWithNonEmptyId = this.allControlsWithNonEmptyId(control);

			this.choiceBoxOwner.getItems().setAll(allControlsWithNonEmptyId);
			this.choiceBoxOwner.getSelectionModel().select(control == null ? null : control.getSection().getWindow().getOwnerControl(control));

			this.tfID.setText(get(control, "", IControl::getID));

			ControlKind selectedKind = control == null ? null : control.getBindedClass();
			this.selectControlKind(this.comboBoxControl.getItems(), selectedKind);

			this.choiceBoxAddition.getSelectionModel().select(control == null ? null : control.getAddition());

			this.choiceBoxReference.getItems().setAll(allControlsWithNonEmptyId);
			this.choiceBoxReference.getSelectionModel().select(control == null ? null : control.getSection().getWindow().getControlForName(null, control.getRefID()));

			this.tfTimeout.setText(get(control, "0", IControl::getTimeout));

			this.choiceBoxVisibility.getSelectionModel().select(control == null ? null : control.getVisibility());

			this.choiceBoxHeader.getItems().setAll(allControlsWithNonEmptyId);
			this.choiceBoxHeader.getSelectionModel().select(control == null ? null : control.getSection().getWindow().getHeaderControl(control));

			this.choiceBoxRows.getItems().setAll(allControlsWithNonEmptyId);
			this.choiceBoxRows.getSelectionModel().select(control == null ? null : control.getSection().getWindow().getRowsControl(control));

			this.checkBoxWeak.setSelected(control != null && control.isWeak());

			this.tfXpath.setText(get(control, "", IControl::getXpath));
			this.tfUID.setText(get(control, "", IControl::getUID));
			this.tfClass.setText(get(control, "", IControl::getClazz));
			this.tfName.setText(get(control, "", IControl::getName));
			this.tfTitle.setText(get(control, "", IControl::getTitle));
			this.tfAction.setText(get(control, "", IControl::getAction));
			this.tfText.setText(get(control, "", IControl::getText));
			this.tfTooltip.setText(get(control, "", IControl::getTooltip));
			this.tfColumns.setText(get(control, "", IControl::getColumns));
			this.tfExpression.setText(get(control, "", IControl::getExpression));

			if (control != null)
			{
				this.choiceBoxReference.setDisable(control.getBindedClass() != ControlKind.Wait);
				this.tfTimeout.setDisable(control.getBindedClass() != ControlKind.Wait);

				this.choiceBoxHeader.setDisable(control.getBindedClass() != ControlKind.Table);
				this.choiceBoxRows.setDisable(control.getBindedClass() != ControlKind.Table);
				this.tfColumns.setDisable(control.getBindedClass() != ControlKind.Table);

				if (Str.IsNullOrEmpty(control.getOwnerID()))
				{
					this.btnGoToOwner.setDisable(true);
				}
			}
		}
		finally
		{
			this.disableAllListeners = false;
		}
	}

	public void lostFocus()
	{
		if (this.editedTextField != null)
		{
			this.comboBoxControl.requestFocus();
			this.editedTextField = null;
		}
	}

	//region events methods
	public void goToOwner(ActionEvent actionEvent)
	{
		this.model.goToOwner();
	}

	//endregion

	//region private methods
	private void addAllListeners()
	{
		this.addListenersForTextFields();

		this.addListenerForSelections(this.choiceBoxOwner.getSelectionModel(), AbstractControl.ownerIdName, this::mapper, "Error on change owner");
		this.choiceBoxOwner.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.btnGoToOwner.setDisable(newValue == null));

		this.comboBoxControl.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
				this.executeIfListenerEnable(() -> this.model.changeControlKind(newValue), "Error on change control kind"));

		this.addListenerForSelections(this.choiceBoxAddition.getSelectionModel(), AbstractControl.additionName, null, "Error on change addition");
		this.addListenerForSelections(this.choiceBoxReference.getSelectionModel(), AbstractControl.refIdName, this::mapper, "Error on change reference");
		this.addListenerForSelections(this.choiceBoxVisibility.getSelectionModel(), AbstractControl.visibilityName, null, "Error on change visibility");
		this.addListenerForSelections(this.choiceBoxHeader.getSelectionModel(), AbstractControl.headerName, this::mapper, "Error on change header");
		this.addListenerForSelections(this.choiceBoxRows.getSelectionModel(), AbstractControl.rowsName, this::mapper, "Error on change rows");

		this.checkBoxWeak.selectedProperty().addListener((observable, oldValue, newValue) ->
				this.executeIfListenerEnable(() -> this.model.changeParameter(AbstractControl.weakName, this.checkBoxWeak.isSelected()),"Error on change weakness"));
	}

	private void addListenersForTextFields()
	{
		Stream.of(this.tfXpath, this.tfUID, this.tfClass, this.tfName, this.tfTitle, this.tfAction, this.tfText, this.tfTooltip, this.tfColumns, this.tfExpression).forEach(tf ->
		{
			tf.focusedProperty().addListener((observable, oldValue, newValue) ->
			{
				if (!oldValue && newValue)
				{
					this.editedTextField = tf;
				}
				if (oldValue && !newValue)
				{
					this.editedTextField = null;
					this.changeText(tf);
				}
			});
			tf.setHandler(event -> {
				tf.clear();
				this.changeText(tf);
			});
		});

		this.tfID.focusedProperty().addListener(((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				this.editedTextField = this.tfID;
				this.previousId = this.tfID.getText();
			}
			if (!newValue && oldValue)
			{
				String id = this.tfID.getText();
				if (this.model.isValidId(id))
				{
					this.changeText(this.tfID);
				}
				else
				{
					DialogsHelper.showError(String.format("Element with id %s already exists.", id));
					this.tfID.setText(this.previousId);
				}
				this.previousId = null;
				this.editedTextField = null;
			}
		}));
		this.tfID.setHandler(event ->
		{
			this.tfID.clear();
			this.changeText(this.tfID);
		});

		this.tfTimeout.focusedProperty().addListener(((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				this.editedTextField = this.tfTimeout;
			}
			if (oldValue && !newValue)
			{
				this.editedTextField = null;
				this.changeInt(this.tfTimeout);
			}
		}));
	}

	private void changeText(TextField textField)
	{
		this.executeIfListenerEnable(() -> this.model.changeParameter(textField.getId(), textField.getText()), String.format("Error on update field '%s'", textField.getId()));
	}

	private void changeInt(TextField textField)
	{
		this.executeIfListenerEnable(() -> this.model.changeParameter(textField.getId(), Integer.parseInt(textField.getText())), String.format("Error on update field '%s'", textField.getId()));
	}

	private <T> void addListenerForSelections(SingleSelectionModel<T> selectionModel, String propName, Function<T, String> mapper, String errorMsg)
	{
		selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> this.executeIfListenerEnable(() ->
		{
			if (mapper != null)
			{
				this.model.changeParameter(propName, mapper.apply(newValue));
			}
			else
			{
				this.model.changeParameter(propName, newValue);
			}
		}, errorMsg));
	}

	private String mapper(IControl control)
	{
		if (control == null)
		{
			return null;
		}
		return control.getID();
	}

	private void executeIfListenerEnable(Common.Function function, String errorMsg)
	{
		if (this.disableAllListeners)
		{
			return;
		}
		tryCatch(function, errorMsg);
	}

	private List<IControl> allControlsWithNonEmptyId(IControl element)
	{
		if (element != null)
		{
			List<IControl> list = element.getSection().getWindow().getControls(null)
					.stream()
					.filter(c -> !Objects.equals(c, element))
					.filter(c -> !Str.IsNullOrEmpty(c.getID()))
					.collect(Collectors.toList());
			list.add(0, null);
			return list;
		}
		return Collections.singletonList(null);
	}

	@SafeVarargs
	private final <T> void setItems(ChoiceBox<T> cb, T... values)
	{
		cb.getItems().add(null);
		cb.getItems().addAll(values);
	}

	private void selectControlKind(Collection<ControlKind> kinds, ControlKind kind)
	{
		if (!kinds.contains(kind) && this.model.getApp() != null)
		{
			DialogsHelper.showError(String.format("Plugin %s doesn't support control %s. Control replacing to Any", this.model.getApp().getId(), kind));
			this.comboBoxControl.getSelectionModel().select(ControlKind.Any);
		}
		else
		{
			this.comboBoxControl.getSelectionModel().select(kind);
		}
	}
	//endregion
}
