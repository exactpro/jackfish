/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.exceptions;

import com.exactprosystems.jf.api.common.i18n.R;

public class ColumnIsPresentException extends RuntimeException
{
	public ColumnIsPresentException(String columnName)
	{
		super(String.format(R.COLUMN_IS_PRESENT_EXCEPTION.get(), columnName));
	}
}
