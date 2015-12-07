package com.exactprosystems.jf.tool.custom.layout;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	private Parent					parent;
	private LayoutExpressionBuilder	model;

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}
	
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(LayoutExpressionBuilder model)
	{
		this.model = model;
	}
	
	public String show(String title, String themePath, boolean fullScreen)
	{
		Alert dialog = createAlert(title, themePath);
		dialog.getDialogPane().setContent(parent);
//		dialog.getDialogPane().setHeader(this.headerPane);
//		dialog.setOnShowing(event -> this.model.applyXpath(this.mainExpression.getText()));
		if (fullScreen)
		{
			dialog.setOnShown(event -> ((Stage) dialog.getDialogPane().getScene().getWindow()).setFullScreen(true));
		}
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
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

}
