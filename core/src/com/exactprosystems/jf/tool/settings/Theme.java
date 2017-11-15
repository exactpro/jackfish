////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

public enum Theme
{
	GENERAL("com/exactprosystems/jf/tool/css/general/general.css", "com/exactprosystems/jf/tool/css/general/icons", false, null, null),

	WHITE("com/exactprosystems/jf/tool/css/white/white.css", "com/exactprosystems/jf/tool/css/white/icons", true, Color.WHITE, Color.BLACK),
	DARK("com/exactprosystems/jf/tool/css/dark/dark.css", "com/exactprosystems/jf/tool/css/dark/icons", true, Color.BLACK, Color.web("#eeeeee"));

	private String path;
	private String pathToIcons;
	private boolean visible;
	private Color mainColor;
	private Color reverseColor;

	private static Theme currentTheme = WHITE;

	Theme(String path, String pathToIcons, boolean visible, Color mainColor, Color reverseColor)
	{
		this.path = path;
		this.pathToIcons = pathToIcons;
		this.visible = visible;
		this.mainColor = mainColor;
		this.reverseColor = reverseColor;
	}

	public static Theme currentTheme()
	{
		return currentTheme;
	}

	public static void setCurrentTheme(String themeName)
	{
		currentTheme = Theme.valueOf(themeName.toUpperCase());
	}

	public static List<String> currentThemesPaths()
	{
		return Arrays.asList(Theme.GENERAL.getPath(), currentTheme.getPath());
	}

	public String getPath()
	{
		return path;
	}

	public String getPathToIcons()
	{
		return pathToIcons;
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
