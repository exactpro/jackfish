package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	public Label 							image;
	public VBox								vBoxControls;
	public CustomFieldWithButton			cfFindControl;
	public HBox								hBoxCheckBoxes;
	public BorderPane						bottomPane;
	public BorderPane						parentPane;
	public ScrollPane						spControls;
	private NewExpressionField				expressionField;
	private ToggleGroup						mainToggleGroup;

	private Parent							parent;
	private LayoutExpressionBuilder			model;

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert image != null : "fx:id=\"image\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert bottomPane != null : "fx:id=\"bottomPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert hBoxCheckBoxes != null : "fx:id=\"hBoxCheckBoxes\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		this.mainToggleGroup = new ToggleGroup();
		this.mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.model.displayNewLocator(((Locator) newValue.getUserData()));
			}
		});
		this.cfFindControl.textProperty().addListener((observable, oldValue, newValue) -> {
			//TODO implements this logic via DialogsHelper.showFindListView()
		});
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator)
	{
		this.model = model;
		this.expressionField = new NewExpressionField(evaluator, "expression Field");
		this.bottomPane.setBottom(this.expressionField);
	}

	public String show(String title, boolean fullScreen, Map<String, Locator> map)
	{
		Alert dialog = createAlert(title);
		map.entrySet().stream().map(entry -> {
			ToggleButton button = new ToggleButton(entry.getKey());
			button.setToggleGroup(this.mainToggleGroup);
			button.prefWidthProperty().bind(this.vBoxControls.widthProperty().subtract(20));
			button.setUserData(entry.getValue());
			button.setTooltip(new Tooltip(entry.getKey()));
			return button;
		}).forEach(this.vBoxControls.getChildren()::add);
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

	private Alert createAlert(String title)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(Common.currentTheme().getPath());
		alert.setTitle(title);
		alert.setResizable(true);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.getDialogPane().setPrefHeight(600);
		alert.getDialogPane().setPrefWidth(800);
		return alert;
	}
}
