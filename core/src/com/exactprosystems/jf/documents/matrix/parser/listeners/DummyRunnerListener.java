package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.MatrixState;
import org.apache.log4j.Logger;

public class DummyRunnerListener implements RunnerListener
{
	private static final Logger logger = Logger.getLogger(DummyRunnerListener.class);

	public DummyRunnerListener()
	{
	}

	@Override
	public void subscribe(IMatrixRunner runner)
	{
		logger.trace(String.format("Matrix runner %s subscribe", runner));
	}

	@Override
	public void unsubscribe(IMatrixRunner runner)
	{
		logger.trace(String.format("Matrix runner %s unsubscribe", runner));
	}

	@Override
	public void stateChange(IMatrixRunner runner, MatrixState state, int done, int total)
	{
		logger.trace(String.format("Matrix runner %s change state to %s", runner, state));
	}
}
