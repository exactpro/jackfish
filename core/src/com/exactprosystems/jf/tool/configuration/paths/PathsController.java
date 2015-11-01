////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.paths;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class PathsController implements Initializable, ContainingParent
{
	private static final Logger logger = Logger.getLogger(PathsController.class);
	
	public Button btnOpenVars;
	public Button btnPathsInput;
	public Button btnPathsOutput;
	public Button btnPathsSystemVars;
	public Button btnPathUsersVars;
	public Button btnOpenUserVars;
	public TextField tfPathsOutput;
	public TextField tfPathsSystemVars;
	public TextField tfPathsUsersVars;

	private Parent pane;
	private ConfigurationFx model;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfPathsUsersVars != null : "fx:id=\"tfPathsUsersVars\" was not injected: check your FXML file 'Paths.fxml'.";
		assert tfPathsSystemVars != null : "fx:id=\"tfPathsSystemVars\" was not injected: check your FXML file 'Paths.fxml'.";
		assert tfPathsOutput != null : "fx:id=\"tfPathsOutput\" was not injected: check your FXML file 'Paths.fxml'.";

		Arrays.asList(tfPathsOutput, tfPathsSystemVars, tfPathsUsersVars).forEach((tf) -> tf.focusedProperty().addListener((observableValue, oldValue, newValue) ->
		{
			tryCatch(() -> {
				if (!newValue && oldValue)
				{
					model.changePaths(tfPathsOutput.getText(), tfPathsSystemVars.getText(), tfPathsUsersVars.getText());
				}
			}, "Error on changing paths");
		}));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	//============================================================
	// events methods
	//============================================================
	public void chooseOutputDirectory(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showDirChooseDialog("Output path directory");
			this.model.changePaths(Common.absolutePath(file), tfPathsSystemVars.getText(), tfPathsUsersVars.getText());
		}, "Error on change output path");

	}

	public void chooseSystemVars(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose Vars.ini file", "Ini files (*.ini)", "*.ini", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changePaths(tfPathsOutput.getText(), Common.absolutePath(file), tfPathsUsersVars.getText());
		}, "Error on change system vars path");
	}

	public void chooseUserVars(ActionEvent actionEvent)
	{
		File file = DialogsHelper.showOpenSaveDialog("Choose user vars file", "Ini files (*.ini)", "*.ini", DialogsHelper.OpenSaveMode.OpenFile);
		tryCatch(() ->
			this.model.changePaths(tfPathsOutput.getText(), tfPathsSystemVars.getText(), Common.absolutePath(file)),
			"Error on changing paths");
	}

	public void openUserVars(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openVars(tfPathsUsersVars.getText()), "Error on open user vars");
	}

	public void openSystemVars(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openVars(tfPathsSystemVars.getText()), "Error on open system vars");
	}

	public void init(ConfigurationFx model, TitledPane titledPane)
	{
		this.model = model;
		titledPane.setContent(this.pane);
	}

	public void display(String output, String sysVars, String userSysVars)
	{
		tfPathsOutput.setText(output);
		tfPathsSystemVars.setText(sysVars);
		tfPathsUsersVars.setText(userSysVars);
	}
}
