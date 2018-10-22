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

package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.paint.Color;

public enum MarkerStyle
{
    SELECT          (CssVariables.Icons.REFRESH,        CssVariables.XPATH_RECTANGLE,   null),
    INSPECT         (CssVariables.Icons.REFRESH,        CssVariables.XPATH_INSPECT_RECTNAGLE,   null),
	UPDATE          (CssVariables.Icons.REFRESH,        CssVariables.COLOR_UPDATE,      Color.web("#2687fb")),
	ADD             (CssVariables.Icons.ADD_16_ICON,    CssVariables.COLOR_ADD,         Color.web("#2687fb")),
	MARK            (CssVariables.Icons.MARK_ICON,      CssVariables.COLOR_MARK,        Color.web("#2a9635")),
	QUESTION        (CssVariables.Icons.QUESTION_ICON,  CssVariables.COLOR_QUESTION,    Color.web("#f3c738"));

	MarkerStyle(String iconPath, String cssStyle, Color color)
	{
		this.iconPath = iconPath;
		this.cssStyle = cssStyle;
		this.color = color;
	}

	public String getIconPath()
	{
		return this.iconPath;
	}
	
	public String getCssStyle()
	{
	    return this.cssStyle;
	}

    public Color color()
	{
		return this.color;
	}

	private String cssStyle;
    private String iconPath;
    private Color color;
}