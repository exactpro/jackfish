/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.helpers;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ExpressionFieldsPane extends BorderPane
{
	private Label key;
	private ExpressionField expressionField;

	public ExpressionFieldsPane(String name, String value, AbstractEvaluator evaluator, ListProvider provider)
	{
		this.key = new Label(name);
		this.expressionField = new ExpressionField(evaluator);
		this.expressionField.setText(value);
		this.expressionField.setHelperForExpressionField(name, null);
		if (provider != null)
		{
			this.expressionField.setChooserForExpressionField("", provider);
		}

		Common.sizeLabel(this.key);
		this.expressionField.sizeTextField();

		this.setTop(this.key);
		this.setCenter(this.expressionField);
	}

	public Label getKey()
	{
		return this.key;
	}

	public ExpressionField getValue()
	{
		return this.expressionField;
	}
}
