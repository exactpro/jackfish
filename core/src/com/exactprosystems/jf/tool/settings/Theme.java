////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

import javafx.scene.paint.Color;

public enum Theme
{
	GENERAL					("com/exactprosystems/jf/tool/css/general/general.css", false,null,null),

	WHITE					("com/exactprosystems/jf/tool/css/white/white.css"				, true	, Color.WHITE, Color.BLACK),
	DARK					("com/exactprosystems/jf/tool/css/dark/dark.css"				, true	, Color.BLACK, Color.web("#eeeeee"));

	private String path;
	private boolean visible;
	private Color mainColor;
	private Color reverseColor;

	Theme(String path, boolean visible, Color mainColor, Color reverseColor)
	{
		this.path = path;
		this.visible = visible;
		this.mainColor = mainColor;
		this.reverseColor = reverseColor;
	}

	public String getPath()
	{
		return path;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public Color getMainColor()
	{
		return mainColor;
	}

	public Color getReverseColor()
	{
		return reverseColor;
	}
}
