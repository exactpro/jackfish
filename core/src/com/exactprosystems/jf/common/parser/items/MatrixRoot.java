////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.parser.DisplayDriver;

import java.io.File;

public class MatrixRoot extends MatrixItem
{
	public MatrixRoot(String matrixName)
	{
		if (matrixName != null)
		{
			this.matrixName = new File(matrixName).getName();
		}
	}

	public String getMatrixName()
	{
		return this.matrixName;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		return driver.createLayout(this, 0);
	}

	@Override
	public String getItemName()
	{
		return "Matrix " + "(" + this.matrixName + ")";
	}

	private String matrixName = null;

}
