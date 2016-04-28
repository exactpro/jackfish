////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.cliententry;

import com.exactprosystems.jf.documents.config.Configuration;
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

public class ClientEntryFxController implements Initializable, ContainingParent
{
	public TextField							tfClientDescription;
	public CustomFieldWithButton				cfClientJarName;
	public CustomFieldWithButton				cfClientDictionary;
	public NumberTextField						ntfClientLimit;

	public CustomTable<Configuration.Parameter>	tableView;
	public Button								btnRemoveEntry;
	public Button								btnPossibilities;
	public GridPane								tableGrid;
	public GridPane								mainGrid;

	private Configuration.ClientEntry			entry;
	private ConfigurationFx						model;
	private TitledPane							parent;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfClientDescription != null : "fx:id=\"tfClientDescription\" was not injected: check your FXML file 'ClientEntryFx.fxml'.";
		assert btnRemoveEntry != null : "fx:id=\"btnRemoveEntry\" was not injected: check your FXML file 'ClientEntryFx.fxml'.";
		assert tableGrid != null : "fx:id=\"tableGrid\" was not injected: check your FXML file 'ClientEntryFx.fxml'.";
		assert btnPossibilities != null : "fx:id=\"btnPossibilities\" was not injected: check your FXML file 'ClientEntryFx.fxml'.";
		this.ntfClientLimit = new NumberTextField();
		mainGrid.add(this.ntfClientLimit, 1, 3);

		this.cfClientJarName = new CustomFieldWithButton();
		this.cfClientJarName.setButtonText("...");
		this.cfClientJarName.setHandler(h -> Common.tryCatch(() ->
		{
			File file = DialogsHelper.showOpenSaveDialog("Choose client jar", "Jar files (*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.clientJar, Common.absolutePath(file));
		}, "Error on change path to client jar"));
		this.mainGrid.add(this.cfClientJarName, 1, 1);

		this.cfClientDictionary = new CustomFieldWithButton();
		this.cfClientDictionary.setButtonText("...");
		this.cfClientDictionary.setHandler(h -> Common.tryCatch(() ->
		{
			File file = DialogsHelper.showOpenSaveDialog("Choose client dictionary", "Xml files (*.xml)", "*.xml", DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.clientDictionary, Common.absolutePath(file));
		}, "Error on choose dictionary"));
		this.mainGrid.add(this.cfClientDictionary, 1, 2);
		createTableView();
		listeners();
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = (TitledPane) parent;
	}

	public void init(ConfigurationFx model, Configuration.ClientEntry entry, Accordion accordionClientEntries)
	{
		Common.tryCatch(() -> {
			this.model = model;
			this.entry = entry;
			this.parent.setText(this.entry.toString());
			accordionClientEntries.getPanes().add(accordionClientEntries.getPanes().size(), this.parent);
		}, "Error on init client controller");
	}

	public void display()
	{
		Common.tryCatch(() ->
		{
			this.tfClientDescription.setText(entry.get(Configuration.clientDescription));
			this.cfClientDictionary.setText(entry.get(Configuration.clientDictionary));
			this.cfClientJarName.setText(entry.get(Configuration.clientJar));
			this.ntfClientLimit.setText(entry.get(Configuration.clientLimit));
			displayEntryParameters(entry.getParameters());
		}, "Error on update all fields in client controller");
	}

	public void displayEntryParameters(List<Configuration.Parameter> parameters)
	{
		tableView.setItems(FXCollections.observableList(parameters));
		tableView.update();
	}

	public void remove(Accordion accordionClientEntries)
	{
		accordionClientEntries.getPanes().remove(parent);
	}

	public void displaySupported(SupportedEntry value)
	{
		ConfigurationFxController.displaySupported(parent, value, this.entry.toString());
	}

	// ============================================================
	// events methods
	// ============================================================
	public void showPossibilities(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.showPossibilities(entry), "Error on show possibilities");
	}

	public void removeEntry(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.removeClientEntry(entry), "Error on remove client entry");
	}

	public void addAllKnowParameters(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.addAllKnowClientParameters(entry, new ArrayList<>(tableView.getItems())), "Error on add all known parameters");
	}

	// ============================================================
	// private methods
	// ============================================================
	private void createTableView()
	{
		this.tableView = new CustomTable<>(true);
		this.tableView.completeFirstColumn("Key", Configuration.parametersKey, false, false);
		this.tableView.completeSecondColumn("Value", Configuration.parametersValue, true, false);
		this.tableView.onFinishEditSecondColumn((parameter, newValue) -> Common.tryCatch(() -> model.updateParameter(this.entry, parameter, newValue), "Error on update parameter"));
		GridPane.setColumnSpan(this.tableView, 4);
		tableGrid.add(this.tableView, 0, 0);
	}

	private Map<TextField, String> changeEntry()
	{
		Map<TextField, String> map = new HashMap<>();
		map.put(cfClientJarName, Configuration.clientJar);
		map.put(tfClientDescription, Configuration.clientDescription);
		map.put(cfClientDictionary, Configuration.clientDictionary);
		return map;
	}

	private void listeners()
	{
		changeEntry().forEach((tf, name) -> tf.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(()-> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, name, tf.getText());
			}
		},"Error on change application entry '"+name+"'.")));
		this.ntfClientLimit.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, Configuration.clientLimit, ntfClientLimit.getValue());
			}
		},"Error on change client limit parameter"));
	}
}