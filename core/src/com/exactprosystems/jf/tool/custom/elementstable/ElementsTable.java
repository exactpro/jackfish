package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.css.PseudoClassState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.function.Consumer;

public class ElementsTable extends TableView<TableBean>
{
	private Consumer<TableBean> removeConsumer;
	private Consumer<TableBean> updateConsumer;

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
				Platform.runLater(textField::requestFocus);
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
		{
			//TODO
		});
		columnId.setMinWidth(100.0);

		TableColumn<TableBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellValueFactory(new PropertyValueFactory<>("controlKind"));
		columnKind.setOnEditCommit(e ->
		{
			//TODO
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
					//TODO
					//					btnEdit.setOnAction(e -> Common.tryCatch(() -> model.changeElement(item), "Error on change element"));

					Button btnRemove = new Button();
					btnRemove.setId("btnRemove");
					btnRemove.setTooltip(new Tooltip("Remove element"));
					btnRemove.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRemove.setOnAction(e -> Optional.ofNullable(removeConsumer).ifPresent(c -> c.accept(item)));

					Button btnRelation = new Button();
					btnRelation.setId("btnRelation");
					btnRelation.setTooltip(new Tooltip("Set relation"));
					btnRelation.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnRelation.setOnAction(e -> Optional.ofNullable(updateConsumer).ifPresent(c -> c.accept(item)));
					box.getChildren().addAll(btnRelation, btnRemove);
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

	public void remove(Consumer<TableBean> consumer)
	{
		this.removeConsumer = consumer;
	}

	public void update(Consumer<TableBean> consumer)
	{
		this.updateConsumer = consumer;
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
			this.getStyleClass().removeAll(
					CssVariables.COLOR_MARK,
					CssVariables.COLOR_QUESTION,
					CssVariables.COLOR_NOT_FOUND,
					CssVariables.COLOR_NOT_FINDING,
					CssVariables.COLOR_ADD,
					CssVariables.COLOR_UPDATE
			);
			//TODO add
//			if (item != null && !empty && item.getStyleClass() != null)
//			{
//				this.getStyleClass().add(item.getStyleClass());
//			}
		}
	}
}
