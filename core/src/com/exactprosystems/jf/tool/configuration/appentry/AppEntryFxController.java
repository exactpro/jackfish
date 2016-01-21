////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.appentry;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.configuration.ConfigurationFxController;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.collections.FXCollections;
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
import java.util.*;

public class AppEntryFxController implements Initializable, ContainingParent
{
	public TextField tfDescription;
	public CustomFieldWithButton cfJarName;
	public CustomFieldWithButton cfDictionaryPath;
	public CustomFieldWithButton cfWorkDir;
	public NumberTextField ntfStartPort;
	public Button btnRemoveEntry;
	public CustomTable<Configuration.Parameter> tableView;
	public GridPane mainGrid;

	private TitledPane parent;
	private ConfigurationFx model;
	private Configuration.AppEntry entry;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert btnRemoveEntry != null : "fx:id=\"btnRemoveEntry\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert tfDescription != null : "fx:id=\"tfDescription\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		this.ntfStartPort = new NumberTextField(0);
		this.mainGrid.add(this.ntfStartPort, 1, 4);
		this.cfJarName = new CustomFieldWithButton();
		this.cfJarName.setButtonText("...");
		this.cfJarName.setHandler(h -> Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose app jar", "Jar files (*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile);
			model.changeEntryPath(entry, Configuration.appJar, Common.absolutePath(file));
		}, "Error on change application path"));
		this.mainGrid.add(this.cfJarName, 1, 1);

		this.cfDictionaryPath = new CustomFieldWithButton();
		this.cfDictionaryPath.setButtonText("...");
		this.cfDictionaryPath.setHandler(h -> Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose dictionary", "Xml files (*.xml)", "*.xml", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.appDicPath, Common.absolutePath(file));
		}, "Error on change dictionary path"));
		this.mainGrid.add(this.cfDictionaryPath, 1, 2);

		this.cfWorkDir = new CustomFieldWithButton();
		this.cfWorkDir.setButtonText("...");
		this.cfWorkDir.setHandler(h -> Common.tryCatch(() -> Common.tryCatch(() -> {
			File file = DialogsHelper.showDirChooseDialog("Choose path to work dir");
			this.model.changeEntryPath(entry, Configuration.appWorkDir, Common.absolutePath(file));
		}, "Error on change work dir"), "Error on change dictionary path"));
		this.mainGrid.add(this.cfWorkDir, 1, 3);
		
		createTableView();
		listeners();
		this.ntfStartPort.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, Configuration.appStartPort, this.ntfStartPort.getText());
			}
		}, "Error on set new start port"));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = (TitledPane) parent;
	}

	//===========================================================================================
	// events methods
	//===========================================================================================
	public void removeEntry(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.removeAppEntry(entry), "Error on remove app entry");
	}

	public void addAllKnowParameters(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.addAllKnowAppParameters(entry, new ArrayList<>(tableView.getItems())), "Error on add all known parameters");
	}

	public void openDictionary(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadDictionary(cfDictionaryPath.getText(), entry.toString()), "Error on open dictionary");
	}
	
	//===========================================================================================
	public void init(ConfigurationFx model, Configuration.AppEntry entry, Accordion accordionAppEntries)
	{
		Common.tryCatch(() -> {
			this.model = model;
			this.entry = entry;
			this.parent.setText(this.entry.toString());
			accordionAppEntries.getPanes().add(accordionAppEntries.getPanes().size(), this.parent);
		}, "Error on init app entry controller");
	}

	public void display()
	{
		Common.tryCatch(() -> {
			this.tfDescription.setText(entry.get(Configuration.appDescription));
			this.cfJarName.setText(entry.get(Configuration.appJar));
			this.cfDictionaryPath.setText(entry.get(Configuration.appDicPath));
			this.cfWorkDir.setText(entry.get(Configuration.appWorkDir));
			this.ntfStartPort.setText(entry.get(Configuration.appStartPort));
			displayEntryParameters(entry.getParameters());
		}, "Error on update all fields in app controller");
	}

	public void displayEntryParameters(List<Configuration.Parameter> parameters)
	{
		tableView.setItems(FXCollections.observableArrayList(parameters));
		tableView.update();
	}

	public void remove(Accordion accordionAppEntries)
	{
		accordionAppEntries.getPanes().remove(parent);
	}

	public void displaySupported(SupportedEntry value)
	{
		ConfigurationFxController.displaySupported(parent, value, this.entry.toString());
	}

	public void showHelp(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.showHelp(this.entry), "Error on show help");
	}

	//===========================================================================================
	// private methods
	//===========================================================================================
	private void createTableView()
	{
		this.tableView = new CustomTable<>(true);
		tableView.setListener(items -> model.removeParameters(entry, items));
		this.tableView.completeFirstColumn("Key", Configuration.parametersKey, false, false);
		this.tableView.completeSecondColumn("Value", Configuration.parametersValue, true, false);
		this.tableView.onFinishEditSecondColumn((parameter, newValue) -> Common.tryCatch(() -> model.updateParameter(this.entry, parameter, newValue),"Error on update parameter"));
		GridPane.setColumnSpan(tableView, 4);
		this.mainGrid.add(tableView, 0, 5, 3, 1);
	}

	private void listeners()
	{
		changeEntry().forEach(((tf, name) -> tf.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, name, tf.getText());
			}
		},"Error on change application entry '"+name+"'."))));
	}

	private Map<TextField, String> changeEntry()
	{
		Map<TextField, String> map = new HashMap<>();
		map.put(cfJarName, Configuration.appJar);
		map.put(cfWorkDir, Configuration.appWorkDir);
		map.put(cfDictionaryPath, Configuration.appDicPath);
		map.put(tfDescription, Configuration.appDescription);
		return map;
	}
}
