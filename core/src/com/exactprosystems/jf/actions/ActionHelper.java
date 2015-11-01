////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.app.ValueAndColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ActionHelper
{
	public static Map<String, Object> convertMaps(Map<String, ValueAndColor> tableData)
	{
		Map<String, Object> values = new LinkedHashMap<String, Object>();

		for (Entry<String, ValueAndColor> entry : tableData.entrySet())
		{
			values.put(entry.getKey(), entry.getValue().getValue());
			if (entry.getValue().getColor() != null)
			{
				values.put(entry.getKey() + ".Color", entry.getValue().getColor());
			}
			if (entry.getValue().getBackColor() != null)
			{
				values.put(entry.getKey() + ".BackColor", entry.getValue().getBackColor());
			}
		}
		return values;
	}
}
