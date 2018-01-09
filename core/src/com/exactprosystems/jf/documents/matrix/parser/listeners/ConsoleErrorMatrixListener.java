////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

public class ConsoleErrorMatrixListener extends MatrixListener
{
	@Override
	public void error(Matrix matrix, int lineNumber, MatrixItem item, String message)
	{
		super.error(matrix, lineNumber, item, message);

		if (item != null)
		{
			System.out.println(String.format("%s[%3d] %s %s", matrix.getNameProperty().get(), item.getNumber(), item.getPath(), message));
		}
		else
		{
			System.out.println(message);
		}
	}
}
