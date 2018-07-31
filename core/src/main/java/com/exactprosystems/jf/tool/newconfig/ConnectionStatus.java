/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.image.Image;

public enum ConnectionStatus
{
	NotStarted(CssVariables.Icons.SERVICE_NOT_STARTED_ICON),
	StartSuccessful(CssVariables.Icons.SERVICE_STARTED_GOOD_ICON),
	StartFailed(CssVariables.Icons.SERVICE_STARTED_FAIL_ICON);

	private String description;
	private Image image;

	ConnectionStatus(String pathToImg)
	{
		this.image = new Image(pathToImg);
	}

	public Image getImage()
	{
		return image;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
