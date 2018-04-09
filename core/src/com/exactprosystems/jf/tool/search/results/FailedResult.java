/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.search.results;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class FailedResult extends SingleResult
{
	private String msg;

	public FailedResult(String msg)
	{
		super(null, null, 0, 0, null, null, null);
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
