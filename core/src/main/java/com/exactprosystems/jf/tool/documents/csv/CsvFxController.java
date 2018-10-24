/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.documents.csv;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
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
				new ReadableValue(",", R.CSV_FX_CONTR_COMMA.get()),
				new ReadableValue(";", R.CSV_FX_CONTR_SEMICOLON.get()),
				new ReadableValue(":", R.CSV_FX_CONTR_COLON.get()),
				new ReadableValue("-", R.CSV_FX_CONTR_DASH.get()),
				new ReadableValue("\t", R.CSV_FX_CONTR_TAB.get())
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
		}, R.CSV_FX_CONTROLLER_ERROR_DELIMITER.get());
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
