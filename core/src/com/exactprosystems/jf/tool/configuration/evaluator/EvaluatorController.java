////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.evaluator;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class EvaluatorController implements Initializable, ContainingParent
{

	public TextArea taEvaluatorImports;

	private Parent pane;
	private ConfigurationFx model;

	private String lastEvaluatorImports = "";
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert taEvaluatorImports != null : "fx:id=\"taEvaluatorImports\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		this.taEvaluatorImports.focusedProperty().addListener((observable, oldValue, newValue) -> tryCatch(() -> {
			if (newValue && !oldValue)
			{
				lastEvaluatorImports = this.taEvaluatorImports.getText().replace('\n', ',');
			}
			if (!newValue && oldValue)
			{
				String value = taEvaluatorImports.getText().replace('\n', ',');
				if (!Str.areEqual(value, lastEvaluatorImports))
				{
					model.changeEvaluator(value);
					lastEvaluatorImports = value;
				}
			}
		},"Error on changing evaluator"));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void init(ConfigurationFx model, TitledPane titledPaneEvaluator)
	{
		this.model = model;
		titledPaneEvaluator.setContent(this.pane);
	}

	public void display(String evaluatorImport)
	{
		this.taEvaluatorImports.setText(evaluatorImport.replace(',','\n'));
	}
}
