package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.documents.RunnerListener;
import com.exactprosystems.jf.documents.matrix.MatrixRunner;

import org.apache.log4j.Logger;

@SuppressWarnings("deprecation")
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
	public void stateChange(MatrixRunner runner, MatrixState state, int done, int total)
	{
		logger.trace(String.format("Matrix runner %s change state to %s", runner, state));
	}
}
