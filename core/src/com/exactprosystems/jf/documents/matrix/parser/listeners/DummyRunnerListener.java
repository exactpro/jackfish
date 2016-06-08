package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.documents.config.Configuration;

import org.apache.log4j.Logger;

public class DummyRunnerListener implements RunnerListener
{
	private static final Logger logger = Logger.getLogger(DummyRunnerListener.class);

	public DummyRunnerListener()
	{
	}

	@Override
	public void subscribe(MatrixRunner runner)
	{
		logger.trace(String.format("Matrix runner %s subscribe", runner));
	}

	@Override
	public void unsubscribe(MatrixRunner runner)
	{
		logger.trace(String.format("Matrix runner %s unsubscribe", runner));
	}

	@Override
	public void stateChange(MatrixRunner runner, MatrixRunner.State state, int done, int total)
	{
		logger.trace(String.format("Matrix runner %s change state to %s", runner, state));
	}
}
