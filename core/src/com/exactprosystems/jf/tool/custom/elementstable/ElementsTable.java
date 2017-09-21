package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Visibility;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.sun.javafx.css.PseudoClassState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ElementsTable extends TableView<TableBean>
{
	private BiConsumer<AbstractControl, Node>   removeConsumer;
	private BiConsumer<AbstractControl, Node>   updateConsumer;
	private BiConsumer<AbstractControl, Node> editConsumer;

	public ElementsTable()
	{
		this.setEditable(true);
		this.setRowFactory(row -> new CustomRowFactory());
		TableColumn<TableBean, String> columnId = new TableColumn<>("Id");
		columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		columnId.setEditable(true);
		columnId.setCellFactory(e -> new TableCell<TableBean, String>()
		{
			private TextField textField;

			@Override
			public void startEdit()
			{
				super.startEdit();
				if (textField == null)
				{
					createTextField();
				}
				textField.setText(getString());
				setGraphic(textField);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Common.runLater(textField::requestFocus);
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(Str.asString(getItem()));
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			@Override
			protected void updateItem(String s, boolean b)
			{
				super.updateItem(s, b);
				if (b || s == null)
				{
					setText(null);
					setGraphic(null);
				}
				else
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}

			private String getString()
			{
				return Str.asString(getItem());
			}

			private void createTextField()
			{
				textField = new TextField(getString());
				textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
				textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
				textField.setOnKeyPressed(t ->
				{
					if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
					{
						commitEdit(textField.getText());
					}
					else if (t.getCode() == KeyCode.ESCAPE)
					{
						cancelEdit();
					}
				});
				textField.focusedProperty().addListener((observable, oldValue, newValue) ->
				{
					if (!newValue && textField != null)
					{
						commitEdit(textField.getText());
					}
				});
			}
		});

		columnId.setOnEditCommit(e ->
				Common.tryCatch(() -> e.getRowValue().getAbstractControl().set("id", e.getNewValue()), ""));

		columnId.setMinWidth(100.0);

		TableColumn<TableBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellValueFactory(new PropertyValueFactory<>("controlKind"));
		columnKind.setOnEditCommit(e ->
		{
			AbstractControl newControl = Common.tryCatch(() -> AbstractControl.createCopy(e.getRowValue().getAbstractControl(), e.getNewValue()),"",null);
			e.getRowValue().setAbstractControl(newControl);
		});
		columnKind.setCellFactory(e -> new TableCell<TableBean, ControlKind>()
		{
			ChoiceBox<ControlKind> comboBox;

			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				createCB();
				setGraphic(this.comboBox);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				this.comboBox.show();
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(getString());
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private void createCB()
			{
				if (this.comboBox == null)
				{
					this.comboBox = new ChoiceBox<>(FXCollections.observableArrayList(ControlKind.values()));
					this.comboBox.getSelectionModel().select(getItem());
					this.comboBox.setOnAction(e -> commitEdit(this.comboBox.getSelectionModel().getSelectedItem()));
					this.comboBox.showingProperty().addListener((observable, oldValue, newValue) ->
					{
						if (!newValue)
						{
							cancelEdit();
						}
					});
				}
			}

			private String getString()
			{
				return String.valueOf(getItem() == null ? "" : getItem().name());
			}
		});

		columnKind.setPrefWidth(135);
		columnKind.setMaxWidth(135);
		columnKind.setMinWidth(135);

		int value = 50;

		TableColumn<TableBean, Boolean> columnIsXpath = new TableColumn<>("Xpath");
		columnIsXpath.setCellValueFactory(new PropertyValueFactory<>("xpath"));
		columnIsXpath.setCellFactory(e -> new TableCell<TableBean, Boolean>()
		{
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					this.setAlignment(Pos.CENTER);
					setGraphic(item ? new ImageView(new javafx.scene.image.Image(CssVariables.Icons.MARK_ICON)) : null);
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		columnIsXpath.setPrefWidth(value);
		columnIsXpath.setMaxWidth(value);
		columnIsXpath.setMinWidth(value);

		TableColumn<TableBean, Boolean> columnIsNew = new TableColumn<>("New");
		columnIsNew.setCellValueFactory(new PropertyValueFactory<>("isNew"));
		columnIsNew.setCellFactory(e -> new TableCell<TableBean, Boolean>()
		{
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					this.setAlignment(Pos.CENTER);
					setGraphic(item ? new ImageView(new javafx.scene.image.Image(CssVariables.Icons.MARK_ICON)) : null);
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		columnIsNew.setPrefWidth(value);
		columnIsNew.setMaxWidth(value);
		columnIsNew.setMinWidth(value);

		TableColumn<TableBean, Integer> columnCount = new TableColumn<>("Count");
		columnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
		columnCount.setCellFactory(e -> new TableCell<TableBean, Integer>()
		{
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				this.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					setText(item.toString());
				}
				else
				{
					setText(null);
				}
			}
		});

		columnCount.setPrefWidth(value);
		columnCount.setMaxWidth(value);
		columnCount.setMinWidth(value);

		TableColumn<TableBean, TableBean> columnOption = new TableColumn<>("Option");
		columnOption.setCellValueFactory(new PropertyValueFactory<>("option"));
		columnOption.setPrefWidth(100);
		columnOption.setMaxWidth(100);
		columnOption.setMinWidth(100);
		columnOption.setCellFactory(e -> new TableCell<TableBean, TableBean>()
		{
			@Override
			protected void updateItem(TableBean item, boolean empty)
			{
				super.updateItem(item, empty);
				this.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER);

					Button btnEdit = new Button();
					btnEdit.setId("btnEdit");
					btnEdit.setTooltip(new Tooltip("Edit element"));
					btnEdit.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnEdit.setOnAction(e -> Common.tryCatch(() -> editElement(item), "Error on edit element"));

					Button btnRemove = new Button();
					btnRemove.setId("btnRemove");
					btnRemove.setTooltip(new Tooltip("Remove element"));
					btnRemove.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRemove.setOnAction(e -> {
						boolean needRemove = DialogsHelper.showQuestionDialog("Remove element", "Are you sure to remove this element?");
						if (needRemove)
						{
							Optional.ofNullable(removeConsumer).ifPresent(c -> c.accept(item.getAbstractControl(), item.getNode()));
							getItems().remove(item);
						}
					});

					Button btnRelation = new Button();
					btnRelation.setId("btnRelation");
					btnRelation.setTooltip(new Tooltip("Set relation"));
					btnRelation.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRelation.setOnAction(e -> Optional.ofNullable(updateConsumer).ifPresent(c -> c.accept(item.getAbstractControl(), item.getNode())));
					box.getChildren().addAll(btnEdit, btnRelation, btnRemove);
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		columnId.prefWidthProperty().bind(this.widthProperty().subtract(135 + value * 3 + 100 + 2 + 16));
		this.getColumns().addAll(columnId, columnKind, columnIsXpath, columnIsNew, columnCount, columnOption);
	}

	public void remove(BiConsumer<AbstractControl, Node> consumer)
	{
		this.removeConsumer = consumer;
	}

	public void update(BiConsumer<AbstractControl, Node> consumer)
	{
		this.updateConsumer = consumer;
	}

	public void edit(BiConsumer<AbstractControl, Node> consumer)
	{
		this.editConsumer = consumer;
	}

	public void updateElement(AbstractControl control, Node node, int count, String style, boolean isNew)
	{
		this.getItems().stream().filter(tb -> tb.getAbstractControl() == control).findFirst().ifPresent(tableBean -> {
			tableBean.setCount(count);
			tableBean.setIsNew(isNew);
			tableBean.setNode(node);
			tableBean.setStyle(style);
		});
		this.refresh();
	}

	public void updateControl(Node node, AbstractControl control)
	{
		this.getItems().stream().filter(tb -> tb.getNode() == node).findFirst().ifPresent(tb -> {
			tb.setAbstractControl(control);
			this.refresh();
		});
	}

	public void updateStyle(Node node, String style)
	{
		this.getItems().stream().filter(tb -> tb.getNode() == node).findFirst().ifPresent(tb -> {
			tb.setStyle(style);
			this.refresh();
		});
	}

	public void clearRelation(Node node)
	{
		this.getItems().stream().filter(tb -> tb.getNode() == node).findFirst().ifPresent(tb -> {
			tb.setNode(null);
			this.refresh();
		});
	}

	public AbstractControl controlByNode(Node node)
	{
		return this.getItems().stream().filter(tb -> tb.getNode() == node).findFirst().map(TableBean::getAbstractControl).orElse(null);
	}

	public List<AbstractControl> getControls()
	{
		return this.getItems().stream().map(TableBean::getAbstractControl).collect(Collectors.toList());
	}

	//region private methods
	private void editElement(TableBean bean) throws Exception
	{
		AbstractControl newControl = this.editElement(AbstractControl.createCopy(bean.getAbstractControl()));
		if (newControl != null)
		{
			bean.setAbstractControl(newControl);
			Optional.ofNullable(this.editConsumer).ifPresent(c -> c.accept(newControl, bean.getNode()));
			this.refresh();
		}

	}

	private AbstractControl editElement(AbstractControl abstractControl)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		alert.getDialogPane().setHeader(new Label());
		alert.setTitle("Change element");
		GridPane gridPane = new GridPane();
		gridPane.setPrefWidth(800);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		alert.getDialogPane().setContent(gridPane);
		gridPane.getStyleClass().addAll(CssVariables.HGAP_MIN, CssVariables.VGAP_MIN);

		//region create columns
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(15);
		c0.setHalignment(HPos.RIGHT);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setFillWidth(true);
		c1.setPercentWidth(35);
		c1.setHalignment(HPos.LEFT);


		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(15);
		c2.setHalignment(HPos.RIGHT);

		ColumnConstraints c3 = new ColumnConstraints();
		c3.setFillWidth(true);
		c3.setPercentWidth(35);
		c3.setHalignment(HPos.LEFT);

		gridPane.getColumnConstraints().addAll(c0, c1, c2, c3);
		//endregion

		int index = 0;

		addComboToLeftPane(gridPane, "Owner : ", abstractControl.getOwnerID(), newOwner -> Common.tryCatch(()->abstractControl.set(AbstractControl.ownerIdName, newOwner), "Error on set parameters"), index++, new ArrayList<>());
		addComboToLeftPane(gridPane, "Additional : ", abstractControl.getAddition(), newAdd -> Common.tryCatch(()->abstractControl.set(AbstractControl.additionName, newAdd), "Error on set parameters"), index++, Arrays.asList(
				Addition.values()));
		addComboToLeftPane(gridPane, "Ref : ", abstractControl.getRefID(), refId -> Common.tryCatch(()->abstractControl.set(AbstractControl.refIdName, refId), "Error on set parameters"), index++, new ArrayList<>());
		addToLeftPane(gridPane, "Timeout : ", String.valueOf(abstractControl.getTimeout()), newTimeout -> Common.tryCatch(()->abstractControl.set(AbstractControl.timeoutName, newTimeout), "Error on set parameters"), index++);
		addComboToLeftPane(gridPane, "Visibility : ", abstractControl.getVisibility(), newVis -> Common.tryCatch(()->abstractControl.set(AbstractControl.visibilityName, newVis), "Error on set parameters"),index++, Arrays.asList(
				Visibility.values()));
		addToLeftPane(gridPane, "Columns : ", abstractControl.getColumns(), newColumns -> Common.tryCatch(() -> abstractControl.set(AbstractControl.columnsName, newColumns), "Error on set new columns"),index++ );
		addCheckBoxToLeftPane(gridPane, "Weak", abstractControl.isWeak(), newWeak -> Common.tryCatch(() -> abstractControl.set(AbstractControl.weakName, newWeak), "Error on set new columns"),index++ );
		index = 1;

		addXpathToPane(gridPane, abstractControl.getXpath(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.xpathName, newId), "Error on set parameter"));
		addToRightPane(gridPane, "UID : ", abstractControl.getUID(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.uidName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Class : ", abstractControl.getClazz(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.clazzName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Name : ", abstractControl.getName(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.nameName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Title : ", abstractControl.getTitle(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.titleName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Action : ", abstractControl.getAction(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.actionName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Text : ", abstractControl.getText(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.textName, newId), "Error on set parameter"), index++);
		addToRightPane(gridPane, "Tooltip : ", abstractControl.getTooltip(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.tooltipName, newId), "Error on set parameter"), index++);

		Optional<ButtonType> buttonType = alert.showAndWait();
		if (buttonType.isPresent())
		{
			ButtonType type = buttonType.get();
			if (type == ButtonType.OK)
			{
				return abstractControl;
			}
		}
		return null;
	}

	private void addToRightPane(GridPane pane, String id, String value, Consumer<String> consumer, int index)
	{
		Label lbl = new Label(id);
		CustomFieldWithButton tf = new CustomFieldWithButton(value);
		tf.setMaxWidth(Double.MAX_VALUE);
		tf.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		GridPane.setFillWidth(tf, true);
		pane.add(lbl, 2, index);
		pane.add(tf, 3, index);
	}

	private void addToLeftPane(GridPane pane, String id, String value, Consumer<String> consumer, int index)
	{
		Label lbl = new Label(id);
		CustomFieldWithButton tf = new CustomFieldWithButton(value);
		tf.setMaxWidth(Double.MAX_VALUE);
		tf.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		GridPane.setFillWidth(tf, true);
		pane.add(lbl, 0, index);
		pane.add(tf, 1, index);
	}

	private <T> void addComboToLeftPane(GridPane pane, String id, T value, Consumer<T> consumer, int index, List<T> values)
	{
		ChoiceBox<T> cb = new ChoiceBox<>();
		cb.getItems().add(null);
		cb.getItems().addAll(values);
		cb.getSelectionModel().select(value);
		cb.setMaxWidth(Double.MAX_VALUE);
		cb.setOnAction(e -> consumer.accept(cb.getValue()));
		Label lbl = new Label(id);

		pane.add(lbl, 0, index);
		pane.add(cb, 1, index);
	}

	private void addCheckBoxToLeftPane(GridPane pane, String id, boolean initValue, Consumer<Boolean> consumer, int index)
	{
		CheckBox checkBox = new CheckBox();
		checkBox.setText(id);
		checkBox.setSelected(initValue);
		checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		pane.add(checkBox, 1, index);
	}

	private void addXpathToPane(GridPane pane, String value, Consumer<String> consumer)
	{
		Label lbl = new Label("Xpath : ");
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);

		CustomFieldWithButton tf = new CustomFieldWithButton(value);
		tf.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		HBox.setHgrow(tf, Priority.ALWAYS);
		box.getChildren().addAll(tf);
		pane.add(lbl, 2, 0);
		pane.add(box, 3, 0);
	}

	private class CustomRowFactory extends TableRow<TableBean>
	{
		private final PseudoClass customSelected = PseudoClassState.getPseudoClass("customSelectedState");
		private final PseudoClass selected       = PseudoClassState.getPseudoClass("selected");

		public CustomRowFactory()
		{
			this.getStyleClass().addAll(CssVariables.CUSTOM_TABLE_ROW);
			this.selectedProperty().addListener((observable, oldValue, newValue) -> {
				this.pseudoClassStateChanged(customSelected, newValue);
				this.pseudoClassStateChanged(selected, false); // remove selected pseudostate, cause this state change text color
			});
		}

		@Override
		protected void updateItem(TableBean item, boolean empty)
		{
			super.updateItem(item, empty);
			this.getStyleClass().removeAll(Arrays.stream(MarkerStyle.values()).map(MarkerStyle::getCssStyle).collect(Collectors.toList()));
			if (item != null && !empty && item.getStyle() != null)
			{
				this.getStyleClass().add(item.getStyle());
			}
		}
	}
	//endregion
}
