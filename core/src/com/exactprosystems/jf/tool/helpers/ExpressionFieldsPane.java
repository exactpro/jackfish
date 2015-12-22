package com.exactprosystems.jf.tool.helpers;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ExpressionFieldsPane extends BorderPane
{
	private Label key;
	private ExpressionField expressionField;

	public ExpressionFieldsPane(String name, String value, AbstractEvaluator evaluator)
	{
		this.key = new Label(name);
		this.expressionField = new ExpressionField(evaluator);
		this.expressionField.setText(value);
		this.expressionField.setHelperForExpressionField(name, null);
		
		Common.sizeLabel(this.key);
		this.expressionField.sizeTextField();

		this.setLeft(this.key);
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
