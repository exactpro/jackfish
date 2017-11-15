////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.sql.Blob;

public interface MatrixConnection
{
    boolean join(long time) throws Exception;
    void stop();

	Blob reportAsBlob() throws Exception;
	int passed();

    int failed();

    boolean isRunning();

    String getMatrixName();

    String getReportName();

	void close() throws Exception;

    String getImagesDirPath();
}
