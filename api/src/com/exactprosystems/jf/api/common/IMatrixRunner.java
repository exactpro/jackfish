////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

public interface IMatrixRunner
{
	void start() throws Exception;
	void join(long time) throws Exception;
	void stop();
	void pause();
	void step();
	boolean resetAllBreakPoints();
	boolean isRunning();
    String getMatrixName();
	Object getGlobalVariable(String s);
	void setGlobalVariable(String string, Object value);
	int passed();
	int failed();
	String getReportName();
	String getImagesDirPath();
}
