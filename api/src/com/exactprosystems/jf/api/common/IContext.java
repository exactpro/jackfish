////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.PrintStream;
import java.io.Reader;

public interface IContext
{
	IContext setOut(PrintStream console);

	IContext createCopy();

    MatrixConnection startMatrix(String string, Reader reader, Object parameter)  throws Exception;
}
