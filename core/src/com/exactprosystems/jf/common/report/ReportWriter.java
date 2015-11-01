////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.io.InputStream;

public abstract class ReportWriter  
{
	public abstract void fwrite(String fmt, Object... args) throws IOException; 
	
	public abstract void fwrite(String str) throws IOException; 
	
	public abstract void close() throws IOException;
	
	public abstract void include(InputStream in) throws IOException;
	
	public abstract String fileName();
}