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

package com.exactprosystems.jf.api.app;

import java.awt.Color;
import java.io.Serializable;

public class ValueAndColor implements Serializable
{

	private static final long serialVersionUID = -6432053858779071676L;

	public ValueAndColor(String value, Color color, Color backColor)
	{
		this.value 		= value;
		if (color != null)
		{
			this.color 	= new InnerColor(color);
		}
		if (backColor != null)
		{
			this.backColor = new InnerColor(backColor);
		}
	}

	public String getValue()
	{
		return value;
	}

	public Color getColor()
	{
		return color;
	}

	public Color getBackColor()
	{
		return backColor;
	}

	@Override
	public String toString()
	{
		return "value='" + value + '\'' +
				", color=" + color +
				", backColor=" + backColor;
	}

	private String value = null;
	private InnerColor color = null;
	private InnerColor backColor = null;
}
