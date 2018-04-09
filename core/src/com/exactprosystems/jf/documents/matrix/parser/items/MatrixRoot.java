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

import java.io.File;

@MatrixItemAttribute(
		constantGeneralDescription = R.MATRIX_ROOT_DESCRIPTION,
		shouldContain 	= { },
		mayContain 		= { },
		real			= true,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= true
)
public class MatrixRoot extends MatrixItem
{
	private String matrixName = null;

	public MatrixRoot(String matrixName)
	{
		if (matrixName != null)
		{
			this.matrixName = new File(matrixName).getName();
		}
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new MatrixRoot(this.matrixName);
	}

	//region region override from MatrixItem
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
	//endregion
}
