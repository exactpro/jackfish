////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ShowAllParams
{
	private ShowAllParamsController controller;

	public ShowAllParams(Map<ReadableValue, TypeMandatory> map, Parameters parameters, String title) throws IOException
	{
		this.controller = Common.loadController(ShowAllParams.class.getResource("showAllParams.fxml"));
		this.controller.setContent(map, parameters, title);
	}

	public ArrayList<Pair<ReadableValue, TypeMandatory>> show()
	{
		return this.controller.show();
	}
}
