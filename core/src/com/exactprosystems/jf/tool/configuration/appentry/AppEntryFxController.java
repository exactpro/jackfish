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
	public TextField tfJarName;
	public Button btnChooseAppJar;
	public Button btnRemoveEntry;
	public CustomTable<Configuration.Parameter> tableView;
	public TextField tfWorkDir;
	public Button btnChooseWorkDir;
//	public TextField tfStartPort;
	public NumberTextField ntfStartPort;
	public TextField tfDictionaryPath;
	public Button btnChooseDictionaryPath;
	public GridPane gridTable;
	public GridPane mainGrid;

	private TitledPane parent;
	private ConfigurationFx model;
	private Configuration.AppEntry entry;

	private String lastStartPort = "";

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfJarName != null : "fx:id=\"tfJarName\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert btnRemoveEntry != null : "fx:id=\"btnRemoveEntry\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert tfDictionaryPath != null : "fx:id=\"tfDictionaryPath\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert tfWorkDir != null : "fx:id=\"tfWorkDir\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert tfDescription != null : "fx:id=\"tfDescription\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert btnChooseWorkDir != null : "fx:id=\"btnChooseWorkDir\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert btnChooseDictionaryPath != null : "fx:id=\"btnChooseDictionaryPath\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		assert btnChooseAppJar != null : "fx:id=\"btnChooseAppJar\" was not injected: check your FXML file 'AppEntryFx.fxml'.";
		this.ntfStartPort = new NumberTextField(0);
		this.mainGrid.add(this.ntfStartPort, 1, 4);
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
	public void chooseAppJar(ActionEvent event)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose app jar", "Jar files (*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile);
			model.changeEntryPath(entry, Configuration.appJar, Common.absolutePath(file));
		}, "Error on change application path");
	}

	public void removeEntry(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.removeAppEntry(entry), "Error on remove app entry");
	}

	public void chooseWordDir(ActionEvent event)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showDirChooseDialog("Choose path to work dir");
			this.model.changeEntryPath(entry, Configuration.appWorkDir, Common.absolutePath(file));
		}, "Error on change work dir");
	}

	public void addAllKnowParameters(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.addAllKnowAppParameters(entry, new ArrayList<>(tableView.getItems())), "Error on add all known parameters");
	}

	public void chooseDictionaryPath(ActionEvent event)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose dictionary", "Xml files (*.xml)", "*.xml", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.appDicPath, Common.absolutePath(file));
		}, "Error on change dictionary path");
	}

	public void openDictionary(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadDictionary(tfDictionaryPath.getText(), entry.toString()), "Error on open dictionary");
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
			this.tfJarName.setText(entry.get(Configuration.appJar));
			this.tfDictionaryPath.setText(entry.get(Configuration.appDicPath));
			this.tfWorkDir.setText(entry.get(Configuration.appWorkDir));
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
		gridTable.add(tableView, 0, 0);
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
		map.put(tfJarName, Configuration.appJar);
		map.put(tfWorkDir, Configuration.appWorkDir);
		map.put(tfDictionaryPath, Configuration.appDicPath);
		map.put(tfDescription, Configuration.appDescription);
		return map;
	}
}
