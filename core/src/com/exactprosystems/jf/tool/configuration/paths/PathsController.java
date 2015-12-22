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
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class PathsController implements Initializable, ContainingParent
{
	public Button btnOpenVars;
	public Button btnOpenUserVars;
	public CustomFieldWithButton cfPathReports;
	public CustomFieldWithButton cfPathSystemVars;
	public CustomFieldWithButton cfPathUsersVars;

	public GridPane mainGrid;

	private Parent pane;
	private ConfigurationFx model;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.cfPathReports = new CustomFieldWithButton();
		this.cfPathReports.setButtonText("...");
		this.cfPathReports.setHandler(handle -> tryCatch(() -> {
			File file = DialogsHelper.showDirChooseDialog("Reports path directory");
			this.model.changePaths(Common.absolutePath(file), cfPathSystemVars.getText(), cfPathUsersVars.getText());
		}, "Error on change output path"));
		this.mainGrid.add(this.cfPathReports, 1, 0);

		this.cfPathSystemVars = new CustomFieldWithButton();
		this.cfPathSystemVars.setButtonText("...");
		this.cfPathSystemVars.setHandler(handle -> tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose Vars.ini file", "Ini files (*.ini)", "*.ini", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changePaths(cfPathReports.getText(), Common.absolutePath(file), cfPathUsersVars.getText());
		}, "Error on change system vars path"));
		this.mainGrid.add(this.cfPathSystemVars, 1, 1);

		this.cfPathUsersVars = new CustomFieldWithButton();
		this.cfPathUsersVars.setButtonText("...");
		this.cfPathUsersVars.setHandler(handle -> tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose user vars file", "Ini files (*.ini)", "*.ini", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changePaths(cfPathReports.getText(), cfPathSystemVars.getText(), Common.absolutePath(file));
		}, "Error on changing paths"));
		this.mainGrid.add(cfPathUsersVars, 1, 2);

		Arrays.asList(cfPathReports, cfPathSystemVars, cfPathUsersVars).forEach((tf) -> tf.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
			tryCatch(() -> {
				if (!newValue && oldValue)
				{
					model.changePaths(cfPathReports.getText(), cfPathSystemVars.getText(), cfPathUsersVars.getText());
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

	public void openUserVars(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openVars(cfPathUsersVars.getText()), "Error on open user vars");
	}

	public void openSystemVars(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openVars(cfPathSystemVars.getText()), "Error on open system vars");
	}

	public void init(ConfigurationFx model, TitledPane titledPane)
	{
		this.model = model;
		titledPane.setContent(this.pane);
	}

	public void display(String output, String sysVars, String userSysVars)
	{
		cfPathReports.setText(output);
		cfPathSystemVars.setText(sysVars);
		cfPathUsersVars.setText(userSysVars);
	}
}
