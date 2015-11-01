package com.exactprosystems.jf.common.parser.listeners;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.MatrixRunner;

public interface RunnerListener
{
	void subscribe(MatrixRunner runner);
	void unsubscribe(MatrixRunner runner);
	void stateChange(MatrixRunner runner, MatrixRunner.State state, int done, int total);
	void setConfiguration(Configuration configuration);
}
