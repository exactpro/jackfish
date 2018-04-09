/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;

@MatrixItemAttribute(
		constantGeneralDescription = R.END_DESCRIPTION,
		shouldContain 	= { },
		mayContain 		= { },
		closes			= For.class,
		real			= false,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= false
)
public class End extends MatrixItem
{
	public End(MatrixItem startItem)
	{
		super();
		this.parent = startItem;
		super.setNumber(-1);
	}

	/**
	 * copy constructor
	 */
	public End(End end)
	{
		this.parent = end.parent.makeCopy();
		super.setNumber(-1);
	}

	//region override from MatrixItem
	@Override
	protected MatrixItem makeCopy()
	{
		return new End(this);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, -1);
		driver.showLabel(this, layout, 0, 0, "End " + this.parent.getClass().getSimpleName());
		return layout;
	}
	//endregion
}
