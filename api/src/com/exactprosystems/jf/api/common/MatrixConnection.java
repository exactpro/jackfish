////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

public interface MatrixConnection
{
    void join(long time) throws Exception;
    
    void stop();

    int passed();

    int failed();

    boolean isRunning();

    String getMatrixName();

    String getReportName();

	void close() throws Exception;

    String getImagesDirPath();
}
