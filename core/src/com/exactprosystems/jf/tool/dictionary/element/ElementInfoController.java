////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.element;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.Visibility;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.get;
import static com.exactprosystems.jf.tool.Common.tryCatch;

public class ElementInfoController implements Initializable, ContainingParent
{
	public ComboBox<ControlKind> comboBoxControl;
	public ChoiceBox<Visibility> choiceBoxVisibility;
	public ChoiceBox<Addition> choiceBoxAddition;
	public ChoiceBox<IControl> choiceBoxHeader;
	public ChoiceBox<IControl> choiceBoxRows;
	public ChoiceBox<IControl> choiceBoxOwner;
	public CheckBox checkBoxWeak;
	public CheckBox checkBoxUseNumericHeader;
	public CustomFieldWithButton tfID;
	public CustomFieldWithButton tfUID;
	public CustomFieldWithButton tfXpath;
	public CustomFieldWithButton tfClass;
	public CustomFieldWithButton tfText;
	public CustomFieldWithButton tfName;
	public CustomFieldWithButton tfTooltip;
	public CustomFieldWithButton tfAction;
	public CustomFieldWithButton tfTitle;
	public CustomFieldWithButton tfExpression;
	public TextField tfTimeout;
	public Button xpathHelper;
	public CheckBox checkBoxAbsoluteXpath;
	public GridPane mainGrid;
	public Button btnGoToOwner;
	public GridPane fieldGrid;
	public GridPane identifiersGrid;


	private Parent pane;
	private NavigationController navigation;
	private DictionaryFx model;
	private Configuration configuration;
	private ObservableList<ControlKind> controls;

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfXpath != null : "fx:id=\"tfXpath\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfUID != null : "fx:id=\"tfUID\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfID != null : "fx:id=\"tfID\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfClass != null : "fx:id=\"tfClass\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert choiceBoxVisibility != null : "fx:id=\"choiceBoxVisibility\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfText != null : "fx:id=\"tfText\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfTimeout != null : "fx:id=\"tfTimeout\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfName != null : "fx:id=\"tfName\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert choiceBoxAddition != null : "fx:id=\"choiceBoxAddition\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfTooltip != null : "fx:id=\"tfTooltip\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert comboBoxControl != null : "fx:id=\"comboBoxControl\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert choiceBoxHeader != null : "fx:id=\"choiceBoxHeader\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert choiceBoxRows != null : "fx:id=\"choiceBoxRows\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert checkBoxWeak != null : "fx:id=\"checkBoxWeak\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert choiceBoxOwner != null : "fx:id=\"choiceBoxOwner\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfAction != null : "fx:id=\"tfAction\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfExpression != null : "fx:id=\"tfExpression\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert tfTitle != null : "fx:id=\"tfTitle\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		assert checkBoxUseNumericHeader != null : "fx:id=\"checkBoxUseNumericHeader\" was not injected: check your FXML file 'ElementInfo.fxml'.";
		Common.customizeLabeled(this.xpathHelper, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.XPATH_TREE);
		Platform.runLater(() -> {
			this.checkBoxAbsoluteXpath.setTooltip(new Tooltip("Absolute xpath"));
			((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).color(Common.currentTheme().getReverseColor()).title("Element info").build());
		});

		Arrays.asList(tfID, tfUID, tfXpath, tfClass, tfText, tfName, tfTooltip, tfAction, tfTitle, tfExpression).forEach(tf -> {
			tf.prefWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).maxWidthProperty());
			tf.maxWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).maxWidthProperty());
			tf.minWidthProperty().bind(this.fieldGrid.getColumnConstraints().get(1).minWidthProperty());
		});
		this.tfUID.prefWidthProperty().bind(this.identifiersGrid.getColumnConstraints().get(1).maxWidthProperty());
		this.tfUID.maxWidthProperty().bind(this.identifiersGrid.getColumnConstraints().get(1).maxWidthProperty());
		this.tfUID.minWidthProperty().bind(this.identifiersGrid.getColumnConstraints().get(1).minWidthProperty());
	}

	public void init(DictionaryFx model, Configuration configuration, GridPane gridPane, NavigationController navigation, String themePath)
	{
		this.navigation = navigation;
		this.model = model;
		this.configuration = configuration;

		setTextFieldListeners();
		
		setItems(this.choiceBoxAddition, Addition.values());
		setItems(this.choiceBoxVisibility, Visibility.values());
		
		this.controls = FXCollections.observableArrayList(ControlKind.values());
		this.comboBoxControl.setItems(this.controls);

		gridPane.add(this.pane, 1, 0);
	}

	public void setAppName(String id)
	{
		update(this.comboBoxControl.getSelectionModel().selectedItemProperty(),
				() ->
				{
					ControlKind temp = currentControlKind();
					if (id == null)
					{
						this.controls.clear();
						this.controls.addAll(ControlKind.values());
					} else
					{
						tryCatch(
								() ->
								{
									this.controls.clear();
									this.controls.addAll(this.configuration.getApplicationPool()
											.loadApplicationFactory(id)
											.supportedControlKinds());
								}, "Error on set supported controls");
					}
					this.comboBoxControl.setValue(temp);
				}, this.changeControlKindListener);
	}	
	
	//============================================================
	// events methods
	//============================================================
	public void goToOwner(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.parameterGoToOwner(currentOwner()), "Error on go to the owner");
	}

	public void changeWeak(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.parameterSet(AbstractControl.weakName, this.checkBoxWeak.isSelected()), "Error on changing weakness");
	}
	
	public void changeUseNumericHeader(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.parameterSet(AbstractControl.useNumericHeaderName, this.checkBoxUseNumericHeader.isSelected()), "Error on changing using numeric headers");
	}
	
	public void changeId(ActionEvent actionEvent)
	{
		tryCatch(() -> {
			TextField source = (TextField) actionEvent.getSource();
			this.navigation.parameterSetId(source.getText());
		}, "Error on changing " + actionEvent);
	}

	public void clear(ActionEvent actionEvent)
	{
		tryCatch(() -> {
			String id = ((Node) actionEvent.getSource()).getId();
			TextField tf = (TextField) this.pane.lookup("#" + id);
			tf.clear();
			this.navigation.parameterSet(id, null);
		}, "Error on clearing " + actionEvent); 
	}

	public void showTree(ActionEvent actionEvent)
	{
		tryCatch(() -> 
		{
			this.navigation.chooseXpath(currentOwner(), tfXpath.getText());
		}, "Error on show tree");
	}
	
	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayInfo(IControl control, Collection<IControl> owners, IControl owner, Collection<IControl> rows, IControl row, IControl header)
	{
    	Platform.runLater(() ->
		{
			this.pane.setDisable(control == null);

			update(
					this.choiceBoxOwner.getSelectionModel().selectedItemProperty(),
					() -> {
						this.choiceBoxOwner.getItems().clear();
						this.choiceBoxOwner.getItems().add(null);
						if (owners != null)
						{
							this.choiceBoxOwner.getItems().addAll(owners);
						}
						this.choiceBoxOwner.getSelectionModel().select(owner);
					},
					this.changeOwnerListener
			);
			update(
					this.choiceBoxHeader.getSelectionModel().selectedItemProperty(),
					() -> {
						this.choiceBoxHeader.getItems().clear();
						this.choiceBoxHeader.getItems().add(null);
						if (rows != null)
						{
							this.choiceBoxHeader.getItems().addAll(rows);
						}
						this.choiceBoxHeader.getSelectionModel().select(header);
					},
					this.changeHeaderListener
			);
			update(
					this.choiceBoxRows.getSelectionModel().selectedItemProperty(),
					() -> {
						this.choiceBoxRows.getItems().clear();
						this.choiceBoxRows.getItems().add(null);
						if (rows != null)
						{
							this.choiceBoxRows.getItems().addAll(rows);
						}
						this.choiceBoxRows.getSelectionModel().select(row);
					},
					this.changeRowsListener
			);
			update(
					this.comboBoxControl.getSelectionModel().selectedItemProperty(),
					() -> this.comboBoxControl.getSelectionModel().select(control == null ? null : control.getBindedClass()),
					this.changeControlKindListener
			);
			update(
					this.choiceBoxAddition.getSelectionModel().selectedItemProperty(),
					() -> this.choiceBoxAddition.getSelectionModel().select(control == null ? null : control.getAddition()),
					this.changeAdditionListener
			);

			update(
					this.choiceBoxVisibility.getSelectionModel().selectedItemProperty(),
					() -> this.choiceBoxVisibility.getSelectionModel().select(control == null ? null : control.getVisibility()),
					this.changeVisibilityListener
			);
			this.tfID.setText(get(control, "", IControl::getID));
			this.tfUID.setText(get(control, "", IControl::getUID));
			this.tfXpath.setText(get(control, "", IControl::getXpath));
			this.tfText.setText(get(control, "", IControl::getText));
			this.tfClass.setText(get(control, "", IControl::getClazz));
			this.tfName.setText(get(control, "", IControl::getName));
			this.tfTitle.setText(get(control, "", IControl::getTitle));
			this.tfAction.setText(get(control, "", IControl::getAction));
			this.tfTooltip.setText(get(control, "", IControl::getTooltip));
			this.tfExpression.setText(get(control, "", IControl::getExpression));
			this.tfTimeout.setText(get(control, "0", IControl::getTimeout));

			this.checkBoxWeak.setSelected(control != null && control.isWeak());
			this.checkBoxUseNumericHeader.setSelected(control != null && control.useNumericHeader());
			this.checkBoxAbsoluteXpath.setSelected(control != null && control.useAbsoluteXpath());
			this.previousValue = "";
		});
	}

	//============================================================
	// private methods
	//============================================================
	private void setTextFieldListeners()
	{
		Arrays.stream(new CustomFieldWithButton[] {this.tfUID, this.tfXpath, this.tfClass, this.tfText, this.tfName, this.tfTooltip, this.tfAction, this.tfTitle, this.tfExpression})
				.forEach(tf -> {
					tf.focusedProperty().addListener(textFocusListener(tf));
					tf.setHandler(event -> {
						tf.clear();
						changeText(tf, "");
					});
				});
		this.tfID.focusedProperty().addListener(textIdFocusListener(this.tfID));
		this.tfID.setHandler(event -> {
			this.tfID.clear();
			changeText(this.tfID, "");
		});
		this.tfTimeout.focusedProperty().addListener(numberFocusListener(this.tfTimeout));
		this.checkBoxAbsoluteXpath.selectedProperty().addListener((obs, prev, next) -> changeBoolean(this.checkBoxAbsoluteXpath, next));
	}

	private void changeText(TextField source, String value)
	{
		tryCatch(() -> {
			if (!Str.areEqual(value, previousValue))
			{
				this.navigation.parameterSet(source.getId(), value);
				previousValue = value;
			}
		}, "Error on changing " + source.getId());
	}

	private void changeBoolean(CheckBox source, Boolean value)
	{
		tryCatch(() -> {
					Arrays.asList(
							this.tfUID, this.tfClass,this.tfText,
							this.tfName, this.tfTooltip, this.tfAction,
							this.tfTitle).forEach(tf -> tf.setDisable(value));
					this.navigation.parameterSet(source.getId(), value);
				}, "Error on changing " + source.getId()
		);
	}

	private void changeNumber(TextField source, String value)
	{
		tryCatch(() -> {
			if (!Str.areEqual(value, previousValue))
			{
				this.navigation.parameterSet(source.getId(), Integer.parseInt(value));
				previousValue = value;
			}
		}, "Error on changing " + source.getId());
	}

	private static <T> void update(ObservableValue<T> property, Common.Function fn, ChangeListener<T> listener)
	{
		Common.tryCatch(() -> 
		{
			property.removeListener(listener);
			fn.call();
			property.addListener(listener);
		}, "Error on update listener");
	}

	private IControl currentOwner()
	{
		return this.choiceBoxOwner.getSelectionModel().getSelectedItem();
	}

	private ControlKind currentControlKind()
	{
		return this.comboBoxControl.getSelectionModel().getSelectedItem();
	}

	@SafeVarargs
	private final <T> void setItems(ChoiceBox<T> cb, T... values)
	{
		cb.getItems().add(null);
		cb.getItems().addAll(values);
	}

	//change listeners
	private ChangeListener<ControlKind> changeControlKindListener = (observable, oldValue, newValue) ->
			tryCatch(() -> navigation.parameterSetControlKind(newValue), "Error on changing control kind");

	private ChangeListener<IControl> changeOwnerListener = (observableValue, oldValue, newValue) ->
			tryCatch(() -> navigation.parameterSetOwner(newValue == null ? null : newValue.getID()), "Error on changing owner");

	private ChangeListener<IControl> changeHeaderListener = ((observable, oldValue, newValue) ->
			tryCatch(() -> this.navigation.parameterSet(AbstractControl.headerName, newValue == null ? null : newValue.getID()), "Error on changing header"));

	private ChangeListener<IControl> changeRowsListener = ((observable, oldValue, newValue) ->
			tryCatch(() -> this.navigation.parameterSet(AbstractControl.rowsName, newValue == null ? null : newValue.getID()), "Error on changing rows"));

	private ChangeListener<Addition> changeAdditionListener = ((observable, oldValue, newValue) ->
			tryCatch(() -> this.navigation.parameterSet(AbstractControl.additionName, newValue), "Error on changing addition") );

	private ChangeListener<Visibility> changeVisibilityListener = ((observable, oldValue, newValue) ->
			tryCatch(() -> this.navigation.parameterSet(AbstractControl.visibilityName, newValue), "Error on changing visibility") );

	private String previousValue = "";

	private ChangeListener<Boolean> textFocusListener(TextField tf)
	{
		return ((observable, oldValue, newValue) -> {
			if (!oldValue && newValue)
			{
				this.previousValue = tf.getText();
			}
			if (!newValue && oldValue)
			{
				changeText(tf, tf.getText());
			}
		});
	}

	private ChangeListener<Boolean> textIdFocusListener(TextField tf)
	{
		return ((observable, oldValue, newValue) -> {
			if (!oldValue && newValue)
			{
				this.previousValue = tf.getText();
			}
			if (!newValue && oldValue)
			{
				Common.tryCatch(() -> this.navigation.checkNewId(tf.getText()), "");
				changeText(tf, tf.getText());
			}
		});
	}

	private ChangeListener<Boolean> numberFocusListener(TextField tf)
	{
		return ((observable, oldValue, newValue) -> {
			if (!oldValue && newValue)
			{
				this.previousValue = tf.getText();
			}
			if (!newValue && oldValue)
			{
				changeNumber(tf, tf.getText());
			}
		});
	}

}
