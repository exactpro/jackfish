package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.documents.config.Configuration;

public interface RunnerListener
{
	void subscribe(MatrixRunner runner);
	void unsubscribe(MatrixRunner runner);
	void stateChange(MatrixRunner runner, MatrixRunner.State state, int done, int total);
	void setConfiguration(Configuration configuration);
}
