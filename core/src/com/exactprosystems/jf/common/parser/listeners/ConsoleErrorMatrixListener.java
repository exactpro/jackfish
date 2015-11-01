////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.listeners;

import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.items.MatrixItem;

public class ConsoleErrorMatrixListener extends MatrixListener
{
	@Override
	public void error(Matrix matrix, int lineNumber, MatrixItem item, String message)
	{
		super.error(matrix, lineNumber, item, message);
		
		if (item != null)
		{
            System.out.println(String.format("%s[%3d] %s %s", matrix.getName(), item.getNumber(), item.getPath(), message));
		}
		else
		{
			System.out.println(message);
		}
	}
}
