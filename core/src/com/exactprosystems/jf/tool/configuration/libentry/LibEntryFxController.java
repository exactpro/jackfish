////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.libentry;

import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LibEntryFxController implements Initializable, ContainingParent
{
	public Label lblName;
	public CustomFieldWithButton cfPath;
	public Button btnOpenLib;
	public Button btnRemoveEntry;
	public BorderPane mainBorderPane;

	private GridPane parent;
	private ConfigurationFx model;
	private Configuration.LibEntry entry;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert btnOpenLib != null : "fx:id=\"btnOpenLib\" was not injected: check your FXML file 'LibEntryFx.fxml'.";
		assert lblName != null : "fx:id=\"lblName\" was not injected: check your FXML file 'LibEntryFx.fxml'.";
		assert btnRemoveEntry != null : "fx:id=\"btnRemoveEntry\" was not injected: check your FXML file 'LibEntryFx.fxml'.";
		this.cfPath = new CustomFieldWithButton();
		this.cfPath.setButtonText("...");
		this.cfPath.setHandler(handler -> Common.tryCatch(() -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose path to lib", "Matrix files (" + Configuration.matrixFilter + ")", Configuration.matrixFilter, DialogsHelper.OpenSaveMode.OpenFile);
			this.model.changeEntryPath(entry, Configuration.libPath, Common.absolutePath(file));
		}, "Error on change path to library"));
		this.mainBorderPane.setCenter(this.cfPath);
		this.cfPath.focusedProperty().addListener((observable, oldValue, newValue) -> Common.tryCatch(()-> model.changeEntryPath(entry, Configuration.libPath, this.cfPath.getText()),"Error on change path to library"));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = (GridPane) parent;
	}

	//============================================================
	// events methods
	//============================================================

	public void removeEntry(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.removeLibEntry(entry), "Error on remove entry");
	}

	public void openLib(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openLib(cfPath.getText()), "Error on open library");
	}

	public void init(ConfigurationFx model, Configuration.LibEntry libEntry, ListView<GridPane> listViewLibs)
	{
		this.model = model;
		this.entry = libEntry;
		listViewLibs.getItems().add(this.parent);
	}

	public void removeEntry(ListView<GridPane> listView)
	{
		listView.getItems().remove(this.parent);
	}

	public void display()
	{
		Common.tryCatch(() -> {
			this.cfPath.setText(entry.get(Configuration.libPath));
			this.lblName.setText(entry.toString());
		}, "Error on update all fields");
	}
}
