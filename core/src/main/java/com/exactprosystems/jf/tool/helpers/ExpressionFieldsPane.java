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
