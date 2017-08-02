package com.exactprosystems.jf.tool.search.results;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class FailedResult extends SingleResult
{
	private String msg;

	public FailedResult(String msg)
	{
		super(null, null, 0, null);
		this.msg = msg;
	}

	@Override
	public Node toView()
	{
		Label label = new Label(this.msg);
		label.getStyleClass().addAll(CssVariables.INCORRECT_FIELD);
		return label;
	}
}
