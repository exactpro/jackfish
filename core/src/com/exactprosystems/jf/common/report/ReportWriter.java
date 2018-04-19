/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.io.InputStream;

public abstract class ReportWriter  
{
	public final ReportWriter newline() throws IOException
	{
		this.fwrite(System.lineSeparator());
		return this;
	}

	public abstract ReportWriter fwrite(String fmt, Object... args) throws IOException;

	public abstract ReportWriter fwrite(String str) throws IOException;

	public abstract void close() throws IOException;

	public abstract void include(InputStream in) throws IOException;

	public abstract String fileName();
}