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
