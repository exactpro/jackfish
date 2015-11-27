////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.sqlentry;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SqlEntryFxController implements Initializable, ContainingParent
{
	public Button					btnRemove;
	public TextField				tfConnectionString;
	public CustomFieldWithButton	cfJarName;
	public GridPane					mainGridPane;

	private TitledPane				parent;
	private ConfigurationFx			model;
	private Configuration.SqlEntry	entry;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfConnectionString != null : "fx:id=\"tfConnectionString\" was not injected: check your FXML file 'SqlEntryFx.fxml'.";
		assert btnRemove != null : "fx:id=\"btnRemove\" was not injected: check your FXML file 'SqlEntryFx.fxml'.";
		this.cfJarName = new CustomFieldWithButton();
		this.cfJarName.setButtonText("...");
		this.cfJarName.setHandler(handler -> Common.tryCatch(() ->
		{
			File file = DialogsHelper.showOpenSaveDialog("Choose sql jar", "Jar files (*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.sqlJar, Common.absolutePath(file));
		}, "Error on change path to sql jar"));
		this.mainGridPane.add(this.cfJarName, 1, 0);
		listeners();
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = (TitledPane) parent;
	}

	// ============================================================
	// events methods
	// ============================================================
	public void removeEntry(ActionEvent event)
	{
		Common.tryCatch(() ->
		{
			this.model.removeSqlEntry(entry);
		}, "Error on remove sql entry");
	}

	public void testEntry(ActionEvent actionEvent)
	{
		Common.tryCatch(() ->
		{
			this.model.testSql(entry);
		}, "Error on test entry");
	}

	public void init(ConfigurationFx model, Configuration.SqlEntry entry, Accordion accordionSqlEntries)
	{
		Common.tryCatch(() -> {
			this.entry = entry;
			this.model = model;
			this.parent.setText(this.entry.toString());
			accordionSqlEntries.getPanes().add(parent);
		}, "Error on init sql controller");
	}

	public void remove(Accordion accordion)
	{
		accordion.getPanes().remove(this.parent);
	}

	public void display()
	{
		Common.tryCatch(() ->
		{
			tfConnectionString.setText(entry.get(Configuration.sqlConnection));
			cfJarName.setText(entry.get(Configuration.sqlJar));
		}, "Error on update all fields in service controller");
	}

	// ============================================================
	// private methods
	// ============================================================
	private Map<TextField, String> changeEntry()
	{
		Map<TextField, String> map = new HashMap<>();
		map.put(tfConnectionString, Configuration.sqlConnection);
		map.put(cfJarName, Configuration.sqlJar);
		return map;
	}

	private void listeners()
	{
		changeEntry().forEach((tf,name) -> tf.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, name, tf.getText());
			}
		},"Error on change entry")));
	}
}
