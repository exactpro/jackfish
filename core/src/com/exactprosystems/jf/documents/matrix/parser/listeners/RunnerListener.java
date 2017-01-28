////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.MatrixState;

public interface RunnerListener
{
	void subscribe(IMatrixRunner runner);
	void unsubscribe(IMatrixRunner runner);
	void stateChange(IMatrixRunner runner, MatrixState state, int done, int total);
}
