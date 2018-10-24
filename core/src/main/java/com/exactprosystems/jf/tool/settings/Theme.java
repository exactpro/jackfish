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
