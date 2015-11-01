////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

public enum Theme
{
	PRELOADER				("com/exactprosystems/jf/tool/css/theme/preloader.css",false),
	DEFAULT					("com/exactprosystems/jf/tool/css/theme/default.css",false),
	WHITE					("com/exactprosystems/jf/tool/css/theme/white.css",true),
	DARK					("com/exactprosystems/jf/tool/css/theme/dark.css",true),
	NUMBER_SPINNER			("com/exactprosystems/jf/tool/css/theme/number_spinner.css", false);

	private String path;
	private boolean visible;
	Theme(String path, boolean visible)
	{
		this.path = path;
		this.visible = visible;
	}

	public String getPath()
	{
		return path;
	}

	public boolean isVisible()
	{
		return visible;
	}
}
