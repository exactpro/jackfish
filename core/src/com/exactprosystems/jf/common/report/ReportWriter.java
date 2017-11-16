////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.io.InputStream;

public abstract class ReportWriter  
{
    public abstract ReportWriter newline() throws IOException;
    
	public abstract ReportWriter fwrite(String fmt, Object... args) throws IOException; 
	
	public abstract ReportWriter fwrite(String str) throws IOException; 
	
	public abstract void close() throws IOException;
	
	public abstract void include(InputStream in) throws IOException;
	
	public abstract String fileName();
}