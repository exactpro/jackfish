////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.logs;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class LogsController implements Initializable, ContainingParent
{
	public Button btnLogFile;
	public TextField tfLogFile;
	public ComboBox<String> cbLevel;
	public TextField tfPattern;

	private Parent pane;
	private ConfigurationFx model;
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfLogFile != null : "fx:id=\"tfLogFile\" was not injected: check your FXML file 'Logs.fxml'.";
		assert tfPattern != null : "fx:id=\"tfPattern\" was not injected: check your FXML file 'Logs.fxml'.";
		assert btnLogFile != null : "fx:id=\"btnLogFile\" was not injected: check your FXML file 'Logs.fxml'.";
		assert cbLevel != null : "fx:id=\"cbLevel\" was not injected: check your FXML file 'Logs.fxml'.";
		cbLevel.getItems().addAll("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL");
		cbLevel.setOnAction(actionEvent -> tryCatch(() ->
			model.changeLogs(tfLogFile.getText(), cbLevel.getSelectionModel().getSelectedItem(), tfPattern.getText()),
			"Error on changing log parameters"));
		Arrays.asList(tfLogFile, tfPattern).forEach((textField -> textField.focusedProperty().addListener((observableValue, oldValue, newValue) ->
		{
			tryCatch(() -> {
				if (!newValue && oldValue)
				{
					model.changeLogs(tfLogFile.getText(), cbLevel.getSelectionModel().getSelectedItem(), tfPattern.getText());
				}
			},"Error on change log parameters");
		})));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}
	//============================================================
	// events methods
	//============================================================
	public void chooseLogFile(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose log file", "Log files (*.log)", "*.log", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeLogs(Common.absolutePath(file), cbLevel.getSelectionModel().getSelectedItem(), tfPattern.getText());
		}, "Error on change path to log file");
	}

	public void init(ConfigurationFx model, TitledPane titledPaneLogs)
	{
		this.model = model;
		titledPaneLogs.setContent(this.pane);
	}

	public void display(String logFile, String level, String pattern)
	{
		tfLogFile.setText(logFile);
		cbLevel.getSelectionModel().select(level);
		tfPattern.setText(pattern);
	}
}
