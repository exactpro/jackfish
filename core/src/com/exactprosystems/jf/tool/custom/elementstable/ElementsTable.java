////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Visibility;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.sun.javafx.css.PseudoClassState;
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
	private BiConsumer<AbstractControl, Node> removeConsumer;
	private BiConsumer<AbstractControl, Node> updateConsumer;
	private BiConsumer<AbstractControl, Node> editConsumer;

	public ElementsTable()
	{
		super.setEditable(true);
		super.setRowFactory(row -> new CustomRowFactory());

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
				if (this.textField == null)
				{
					this.createTextField();
				}
				this.textField.setText(getString());
				super.setGraphic(this.textField);
				super.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Common.runLater(this.textField::requestFocus);
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				super.setText(Str.asString(getItem()));
				super.setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			@Override
			protected void updateItem(String s, boolean b)
			{
				super.updateItem(s, b);
				if (b || s == null)
				{
					super.setText(null);
					super.setGraphic(null);
				}
				else
				{
					super.setText(this.getString());
					super.setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}

			private String getString()
			{
				return Str.asString(super.getItem());
			}

			private void createTextField()
			{
				this.textField = new TextField(getString());
				this.textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
				this.textField.setMinWidth(super.getWidth() - super.getGraphicTextGap() * 2);
				this.textField.setOnKeyPressed(t ->
				{
					if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
					{
						super.commitEdit(textField.getText());
					}
					else if (t.getCode() == KeyCode.ESCAPE)
					{
						this.cancelEdit();
					}
				});
				this.textField.focusedProperty().addListener((observable, oldValue, newValue) ->
				{
					if (!newValue && textField != null)
					{
						super.commitEdit(textField.getText());
					}
				});
			}
		});
		columnId.setOnEditCommit(event -> Common.tryCatch(() -> event.getRowValue().getAbstractControl().set("id", event.getNewValue()), ""));
		columnId.setMinWidth(100.0);

		TableColumn<TableBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellValueFactory(new PropertyValueFactory<>("controlKind"));
		columnKind.setOnEditCommit(event ->
		{
			AbstractControl newControl = Common.tryCatch(() -> AbstractControl.createCopy(event.getRowValue().getAbstractControl(), event.getNewValue()), "", null);
			event.getRowValue().setAbstractControl(newControl);
		});
		columnKind.setCellFactory(e -> new TableCell<TableBean, ControlKind>()
		{
			private ChoiceBox<ControlKind> choiceBox;

			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					super.setText(getString());
					super.setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					super.setGraphic(null);
					super.setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				this.createChoiceBox();
				super.setGraphic(this.choiceBox);
				super.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				this.choiceBox.show();
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				super.setText(this.getString());
				super.setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private void createChoiceBox()
			{
				if (this.choiceBox == null)
				{
					this.choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(ControlKind.values()));
					this.choiceBox.getSelectionModel().select(getItem());
					this.choiceBox.setOnAction(e -> commitEdit(this.choiceBox.getSelectionModel().getSelectedItem()));
					this.choiceBox.showingProperty().addListener((observable, oldValue, newValue) ->
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
				return String.valueOf(super.getItem() == null ? "" : super.getItem().name());
			}
		});
		columnKind.setPrefWidth(135);
		columnKind.setMaxWidth(135);
		columnKind.setMinWidth(135);

		int value = 50;

		TableColumn<TableBean, Boolean> columnIsXpath = new TableColumn<>("Xpath");
		columnIsXpath.setCellValueFactory(new PropertyValueFactory<>("xpath"));
		columnIsXpath.setCellFactory(e -> new IconTableCell(CssVariables.Icons.MARK_ICON));
		columnIsXpath.setPrefWidth(value);
		columnIsXpath.setMaxWidth(value);
		columnIsXpath.setMinWidth(value);

		TableColumn<TableBean, Boolean> columnIsNew = new TableColumn<>("New");
		columnIsNew.setCellValueFactory(new PropertyValueFactory<>("isNew"));
		columnIsNew.setCellFactory(e -> new IconTableCell(CssVariables.Icons.MARK_ICON));
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
				super.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					super.setText(item.toString());
				}
				else
				{
					super.setText(null);
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
				super.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER);

					Button btnEdit = new Button();
					btnEdit.setId("btnEdit");
					btnEdit.setTooltip(new Tooltip(R.EDIT_ELEMENT.get()));
					btnEdit.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnEdit.setOnAction(e -> Common.tryCatch(() -> ElementsTable.this.editElement(item), R.ERROR_ON_EDIT.get()));

					Button btnRemove = new Button();
					btnRemove.setId("btnRemove");
					btnRemove.setTooltip(new Tooltip(R.REMOVE_ELEMENT.get()));
					btnRemove.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRemove.setOnAction(e ->
					{
						if (DialogsHelper.showQuestionDialog(R.REMOVE_ELEMENT.get(), R.REMOVE_ELEMENT_QUESTION.get()))
						{
							Optional.ofNullable(ElementsTable.this.removeConsumer).ifPresent(c -> c.accept(item.getAbstractControl(), item.getNode()));
							ElementsTable.super.getItems().remove(item);
						}
					});

					Button btnRelation = new Button();
					btnRelation.setId("btnRelation");
					btnRelation.setTooltip(new Tooltip(R.SET_RELATION.get()));
					btnRelation.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRelation.setOnAction(e -> Optional.ofNullable(ElementsTable.this.updateConsumer).ifPresent(c -> c.accept(item.getAbstractControl(), item.getNode())));
					box.getChildren().addAll(btnEdit, btnRelation, btnRemove);
					super.setGraphic(box);
				}
				else
				{
					super.setGraphic(null);
				}
			}
		});

		columnId.prefWidthProperty().bind(super.widthProperty().subtract(135 + value * 3 + 100 + 2 + 16));
		super.getColumns().addAll(columnId, columnKind, columnIsXpath, columnIsNew, columnCount, columnOption);
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
		super.getItems().stream()
				.filter(tb -> tb.getAbstractControl() == control)
				.findFirst()
				.ifPresent(tableBean ->
				{
					tableBean.setCount(count);
					tableBean.setIsNew(isNew);
					tableBean.setNode(node);
					tableBean.setStyle(style);
					super.refresh();
				});
	}

	public void updateControl(Node node, AbstractControl control)
	{
		this.findElement(node, tableBean -> tableBean.setAbstractControl(control));
	}

	public void updateStyle(Node node, String style)
	{
		this.findElement(node, tableBean -> tableBean.setStyle(style));
	}

	public void clearRelation(Node node)
	{
		this.findElement(node, tableBean -> tableBean.setNode(null));
	}

	public AbstractControl controlByNode(Node node)
	{
		return super.getItems()
				.stream()
				.filter(tb -> tb.getNode() == node)
				.findFirst()
				.map(TableBean::getAbstractControl)
				.orElse(null);
	}

	public List<AbstractControl> getControls()
	{
		return super.getItems()
				.stream()
				.map(TableBean::getAbstractControl)
				.collect(Collectors.toList());
	}

	//region private methods
	private void findElement(Node node, Consumer<TableBean> beanConsumer)
	{
		super.getItems().stream()
				.filter(tb -> tb.getNode() == node)
				.findFirst()
				.ifPresent(tableBean ->
				{
					beanConsumer.accept(tableBean);
					super.refresh();
				});
	}

	private void editElement(TableBean bean) throws Exception
	{
		AbstractControl newControl = this.editElement(AbstractControl.createCopy(bean.getAbstractControl()));
		if (newControl != null)
		{
			bean.setAbstractControl(newControl);
			Optional.ofNullable(this.editConsumer).ifPresent(c -> c.accept(newControl, bean.getNode()));
			super.refresh();
		}

	}

	private AbstractControl editElement(AbstractControl abstractControl)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(alert);
		Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
		alert.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		alert.getDialogPane().setHeader(new Label());
		alert.setTitle(R.CHANGE_ELEMENT.get());
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

		this.addComboToLeftPane(gridPane, "Owner : ", abstractControl.getOwnerID(), newOwner -> Common.tryCatch(()->abstractControl.set(AbstractControl.ownerIdName, newOwner), R.ERROR_ON_SET_PARAMETER.get()), index++, new ArrayList<>());
		this.addComboToLeftPane(gridPane, "Additional : ", abstractControl.getAddition(), newAdd -> Common.tryCatch(()->abstractControl.set(AbstractControl.additionName, newAdd), R.ERROR_ON_SET_PARAMETER.get()), index++, Arrays.asList(Addition.values()));
		this.addComboToLeftPane(gridPane, "Ref : ", abstractControl.getRefID(), refId -> Common.tryCatch(()->abstractControl.set(AbstractControl.refIdName, refId), R.ERROR_ON_SET_PARAMETER.get()), index++, new ArrayList<>());
		this.addToLeftPane(gridPane, "Timeout : ", String.valueOf(abstractControl.getTimeout()), newTimeout -> Common.tryCatch(()->abstractControl.set(AbstractControl.timeoutName, newTimeout), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addComboToLeftPane(gridPane, "Visibility : ", abstractControl.getVisibility(), newVis -> Common.tryCatch(()->abstractControl.set(AbstractControl.visibilityName, newVis), R.ERROR_ON_SET_PARAMETER.get()),index++, Arrays.asList(Visibility.values()));
		this.addToLeftPane(gridPane, "Columns : ", abstractControl.getColumns(), newColumns -> Common.tryCatch(() -> abstractControl.set(AbstractControl.columnsName, newColumns), R.ERROR_ON_SET_NEW_COLUMN.get()),index++ );
		this.addCheckBoxToLeftPane(gridPane, "Weak", abstractControl.isWeak(), newWeak -> Common.tryCatch(() -> abstractControl.set(AbstractControl.weakName, newWeak), R.ERROR_ON_SET_NEW_COLUMN.get()),index++ );
		index = 1;

		this.addXpathToPane(gridPane, abstractControl.getXpath(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.xpathName, newId), R.ERROR_ON_SET_PARAMETER.get()));
		this.addToRightPane(gridPane, "UID : ", abstractControl.getUID(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.uidName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Class : ", abstractControl.getClazz(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.clazzName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Name : ", abstractControl.getName(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.nameName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Title : ", abstractControl.getTitle(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.titleName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Action : ", abstractControl.getAction(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.actionName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Text : ", abstractControl.getText(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.textName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);
		this.addToRightPane(gridPane, "Tooltip : ", abstractControl.getTooltip(), newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.tooltipName, newId), R.ERROR_ON_SET_PARAMETER.get()), index++);

		return alert.showAndWait()
				.filter(type -> type == ButtonType.OK)
				.map(type -> abstractControl)
				.orElse(null);
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

		private CustomRowFactory()
		{
			super.getStyleClass().addAll(CssVariables.CUSTOM_TABLE_ROW);
			super.selectedProperty().addListener((observable, oldValue, newValue) ->
			{
				super.pseudoClassStateChanged(this.customSelected, newValue);
				super.pseudoClassStateChanged(this.selected, false); // remove selected pseudostate, cause this state change text color
			});
		}

		@Override
		protected void updateItem(TableBean item, boolean empty)
		{
			super.updateItem(item, empty);
			super.getStyleClass().removeAll(Arrays.stream(MarkerStyle.values()).map(MarkerStyle::getCssStyle).collect(Collectors.toList()));
			if (item != null && !empty && item.getStyle() != null)
			{
				super.getStyleClass().add(item.getStyle());
			}
		}
	}

	private class IconTableCell extends TableCell<TableBean, Boolean>
	{
		private final String icon;
		public IconTableCell(String icon)
		{
			super();
			this.icon = icon;
		}

		@Override
		protected void updateItem(Boolean item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				this.setAlignment(Pos.CENTER);
				super.setGraphic(item ? new ImageView(new javafx.scene.image.Image(this.icon)) : null);
			}
			else
			{
				super.setGraphic(null);
			}
		}
	}
	//endregion
}
