package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	public Label 					image;
	public ListView<String>			listViewControls;
	public CustomFieldWithButton	cfFindControl;
	public HBox						hBoxCheckBoxes;
	public BorderPane				bottomPane;
	public BorderPane				parentPane;
	private NewExpressionField		expressionField;
	private ToggleGroup				mainToggleGroup;

	private Parent					parent;
	private LayoutExpressionBuilder	model;

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert image != null : "fx:id=\"image\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert listViewControls != null : "fx:id=\"listViewControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert bottomPane != null : "fx:id=\"bottomPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert hBoxCheckBoxes != null : "fx:id=\"hBoxCheckBoxes\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		this.mainToggleGroup = new ToggleGroup();
		this.listViewControls.setCellFactory(param -> new ToggleButtonCell(this.mainToggleGroup, this.listViewControls));
		this.listViewControls.getItems().addAll("first", "second", "third");
		IntStream.range(0, 20).parallel().mapToObj(String::valueOf).forEach(this.listViewControls.getItems()::add);
		this.listViewControls.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.cfFindControl.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.isEmpty())
			{
				this.listViewControls.getSelectionModel().clearSelection();
				long count = this.listViewControls.getItems().stream().filter(item -> item.contains(newValue)).peek(this.listViewControls.getSelectionModel()::select).count();
				if (count == 1)
				{
					this.listViewControls.scrollTo(this.listViewControls.getSelectionModel().getSelectedItem());
				}
			}
			else
			{
				this.listViewControls.getSelectionModel().clearSelection();
			}
		});
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(LayoutExpressionBuilder model)
	{
		this.model = model;
		this.expressionField = new NewExpressionField(null, "expression Field"); //TODO
		this.bottomPane.setBottom(this.expressionField);
	}
	
	public String show(String title, String themePath, boolean fullScreen)
	{
		Alert dialog = createAlert(title, themePath);
		dialog.getDialogPane().setContent(parent);
		if (fullScreen)
		{
			dialog.setOnShown(event -> ((Stage) dialog.getDialogPane().getScene().getWindow()).setFullScreen(true));
		}
		Optional<ButtonType> optional = dialog.showAndWait();
		if (optional.isPresent())
		{
			if (optional.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
//				return this.mainExpression.getText();
			}
		}
		return null;
	}

	private Alert createAlert(String title, String themePath)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(themePath);
		alert.setTitle(title);
		alert.setResizable(true);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.getDialogPane().setPrefHeight(600);
		alert.getDialogPane().setPrefWidth(800);
		return alert;
	}

	private class ToggleButtonCell extends ListCell<String>
	{
		private ToggleButton button;

		public ToggleButtonCell(ToggleGroup group, ListView<String> listView)
		{
			super();
			this.button = new ToggleButton();
			this.button.setToggleGroup(group);
			this.updateListView(listView);
			this.button.prefWidthProperty().bind(this.getListView().widthProperty().subtract(30));
			this.setAlignment(Pos.CENTER);
		}

		@Override
		protected void updateItem(String item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				button.setText(item);
				setGraphic(button);
			}
			else
			{
				setGraphic(null);
			}
		}
	}
}
