////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.serviceentry;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.configuration.ConfigurationFxController;
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
import javafx.scene.paint.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServiceEntryFxController implements Initializable, ContainingParent
{
	public TextField tfServiceDescription;
	public TextField tfServiceJar;
	public CustomTable<Configuration.Parameter> tableView;
	public Button btnStartService;
	public Button btnStopService;
	public GridPane tableGrid;

	private TitledPane parent;
	private ConfigurationFx model;
	private Configuration.ServiceEntry entry;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfServiceJar != null : "fx:id=\"tfServiceJar\" was not injected: check your FXML file 'ServiceEntryFx.fxml'.";
		assert tfServiceDescription != null : "fx:id=\"tfServiceDescription\" was not injected: check your FXML file 'ServiceEntryFx.fxml'.";
		createTable();
		this.tfServiceDescription.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, Configuration.serviceDescription, this.tfServiceDescription.getText());
			}
		}, "Error on change service description"));
		this.tfServiceJar.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(() -> {
			if (!newValue && oldValue)
			{
				model.changeEntry(entry, Configuration.serviceJar, this.tfServiceJar.getText());
			}
		}, "Error on change path to service jar"));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = (TitledPane) parent;
	}

	//============================================================
	// events methods
	//============================================================

	public void chooseServiceJar(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose service jar", "Jar files (*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile);
			model.changeEntry(entry, Configuration.serviceJar, Common.absolutePath(file));
		}, "Error on change path to service jar");
	}

	public void addAll(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.addAllKnowServiceParameters(entry, new ArrayList<>(tableView.getItems())), "Error on add all known service parameters");
	}

	public void removeEntry(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.removeService(entry), "Error on remove service entry");
	}

	public void startService(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.startService(entry), "Error on start service");
	}

	public void stopService(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.stopService(entry), "Error on stop service");
	}

	public void init(ConfigurationFx model, Configuration.ServiceEntry entry, Accordion accordionServiceEntries)
	{
		Common.tryCatch(() -> {
			this.model = model;
			this.entry = entry;
			this.parent.setText(this.entry.toString());
			accordionServiceEntries.getPanes().add(this.parent);
			displayEntryParameters(entry.getParameters());
		}, "Error on init service entry controller");
	}

	public void displayEntryParameters(List<Configuration.Parameter> parameters)
	{
		tableView.setItems(FXCollections.observableArrayList(parameters));
	}

	public void display()
	{
		Common.tryCatch(() -> {
			this.tfServiceJar.setText(entry.get(Configuration.serviceJar));
			this.tfServiceDescription.setText(entry.get(Configuration.serviceDescription));
		}, "Error on update all fields in service controller");
	}

	public void remove(Accordion accordion)
	{
		accordion.getPanes().remove(this.parent);
	}

	public void displayAfterStartService(boolean isGood)
	{
		if (isGood)
		{
			parent.setTextFill(Color.GREEN);
		}
		else
		{
			parent.setTextFill(Color.BLACK);
		}
	}

	public void displaySupported(SupportedEntry value)
	{
		ConfigurationFxController.displaySupported(parent, value, this.entry.toString());
	}

	//============================================================
	// private methods
	//============================================================
	private void createTable()
	{
		this.tableView = new CustomTable<>(items -> {
			model.removeParameters(entry, items);
		});
		this.tableView.completeFirstColumn("Key", Configuration.parametersKey, false, false);
		this.tableView.completeSecondColumn("Value", Configuration.parametersValue, true, false);
		this.tableView.onFinishEditSecondColumn((parameter, newValue) -> Common.tryCatch(() -> model.updateParameter(this.entry, parameter, newValue), "Error on update parameter"));
		GridPane.setColumnSpan(this.tableView, 4);
		this.tableGrid.add(this.tableView, 0, 0);
	}
}
