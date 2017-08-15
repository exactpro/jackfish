////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.documents.matrix.MatrixRunner;

public interface RunnerListener
{
	void subscribe(MatrixRunner runner);
	void unsubscribe(MatrixRunner runner);
	void stateChange(MatrixRunner runner, MatrixState state, int done, int total);
}
