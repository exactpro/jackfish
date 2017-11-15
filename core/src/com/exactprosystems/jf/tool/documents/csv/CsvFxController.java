////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.csv;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

@ControllerInfo(resourceName = "CsvFx.fxml")
public class CsvFxController extends AbstractDocumentController<CsvFx>
{
	public SpreadsheetView         view;
	public ToolBar                 toolBar;
	public ComboBox<ReadableValue> cbDelimiter;

	private DataProvider<String> provider;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		super.initialize(url, resourceBundle);
		this.cbDelimiter.getItems().addAll(
				new ReadableValue(",", "comma"),
				new ReadableValue(";", "semicolon"),
				new ReadableValue(":", "colon"),
				new ReadableValue("-", "dash"),
				new ReadableValue("\t", "tab")
		);
		this.cbDelimiter.getSelectionModel().select(1);
	}

	public void setDelimiter(ActionEvent event)
	{
		Common.tryCatch(() -> {
			this.model.setDelimiter(cbDelimiter.getSelectionModel().getSelectedItem().getValue().charAt(0));
			if (!this.model.getNameProperty().get().startsWith(Csv.class.getAnnotation(DocumentInfo.class).newName()))
			{
				this.model.load(new FileReader(this.model.getNameProperty().get()));
				this.model.display();
			}
		}, "Error on set delimiter");
	}

	public void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);

		this.model.getProvider().setOnChangeListener((o,n) ->
		{
			this.provider = this.model.getProvider();
			this.view = new SpreadsheetView(this.provider);
			this.provider.displayFunction(this.view::display);
			((BorderPane) this.parent).setCenter(this.view);
		});
	}
}
