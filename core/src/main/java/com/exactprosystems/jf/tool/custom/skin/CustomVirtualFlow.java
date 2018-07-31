/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.skin;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.control.IndexedCell;

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
