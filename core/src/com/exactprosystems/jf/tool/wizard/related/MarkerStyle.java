////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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