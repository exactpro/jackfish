////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.skin;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.control.IndexedCell;

//workaround from https://stackoverflow.com/a/34924750/3452146
class CustomVirtualFlow<T extends IndexedCell> extends VirtualFlow<T>
{
	@Override
	public double getPosition()
	{
		double position = super.getPosition();
		if (position == 1.0d)
		{
			return 0.99999999999;
		}
		return super.getPosition();
	}

	@Override
	public void setPosition(double newPosition)
	{
		if (newPosition == 1.0d)
		{
			newPosition = 0.99999999999;
		}
		super.setPosition(newPosition);
	}

}
