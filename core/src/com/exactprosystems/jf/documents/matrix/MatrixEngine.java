/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.api.common.Storable;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Blob;
import java.util.Date;

public class MatrixEngine implements AutoCloseable
{
	public static final  String parameterName = "parameter";
	private static final Logger logger        = Logger.getLogger(MatrixEngine.class);

	private final Matrix  matrix;
	private final Context context;
	private ReportBuilder report = null;
	private Thread        thread = null;

	protected MatrixEngine(Context context, Matrix matrix)
	{
		this.context = context;
		this.matrix = matrix;
	}

	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Convert a report for the matrix to a Blob objec
	 *
	 * @see Converter#storableToBlob(Storable)
	 */
	public Blob reportAsBlob() throws Exception
	{
		return Converter.storableToBlob(this.report);
	}

	/**
	 * @return a result table for the matrix. If the matrix not started yet, will return null
	 */
	public Table getTable()
	{
		return this.context.getTable();
	}

	/**
	 * @return a report name for the matrix. If the matrix is not started yet, will return null
	 */
	public String getReportName()
	{
		return this.report == null ? null : report.getReportName();
	}

	/**
	 * Stop the matrix and change the matrix state to {@link MatrixState#Destroyed}
	 */
	@Override
	public void close()
	{
		try
		{
			this.stop();
			this.changeState(MatrixState.Destroyed);
			this.context.close();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Start the matrix at the passed time and with the passed parameter.
	 * If matrix is running now, will change state to {@link MatrixState#Running}
	 *
	 * @param time the time for staring the matrix. If time is null, {@code new Date()} will used
	 * @param parameter the parameter to matrix
	 *
	 * @throws Exception if matrix has errors or can't create a report ( e.g. no free space on hard driver)
	 *
	 * @see Matrix#start(Date, Object)
	 */
	public void start(Date time, Object parameter) throws Exception
	{
		Date startTime = time == null ? new Date() : time;

		if (this.isRunning())
		{
			this.changeState(MatrixState.Running);
			this.context.resume();
			return;
		}
		else
		{
			this.context.createResultTable();
		}

		Configuration configuration = this.context.getConfiguration();
		AbstractEvaluator evaluator = this.context.getEvaluator();
		evaluator.getGlobals().set(parameterName, parameter);

		String fileName = new File(this.matrix.getNameProperty().get()).getName();
		this.report = configuration.getReportFactory().createReportBuilder(configuration.getReports().get(), fileName, new Date());
		StringBuilder errorMsg = new StringBuilder();
		if (!this.matrix.checkMatrix(this.context, evaluator, errorMsg))
		{
			throw new MatrixException(0, null, String.format(R.MATRIX_ENGINE_MATRIX_INCORRECT.get(), errorMsg.toString()));
		}

		this.changeState(MatrixState.Waiting);

		this.thread = new Thread(() ->
		{
			while (new Date().before(startTime))
			{
				if (context.isStop())
				{
					return;
				}
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			this.changeState(MatrixState.Running);
			this.matrix.start(context, evaluator, report);
			this.changeState(MatrixState.Finished);
		});
		this.thread.setName("Start matrix thread, thread id : " + thread.getId());
		this.context.prepareMonitor();
		this.thread.start();
	}

	/**
	 * Join to the engine thread
	 *
	 * @param time the time to wait
	 *
	 * @return true, if after join the thread is not alive or the matrix is not started yet
	 *
	 * @see Thread#join(long)
	 */
	public boolean join(long time)
	{
		if (this.thread != null)
		{
			try
			{
				if (time > 0)
				{
					this.thread.join(time);
				}
				else
				{
					this.thread.join();
				}
				boolean joinSuccess = !this.thread.isAlive();
				this.close();
				return joinSuccess;
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}

	/**
	 * Stop the matrix. Change the matrix state to {@link MatrixState#Stopped} and change for all items state {@link MatrixItemState#None} or {@link MatrixItemState#BreakPoint}
	 *
	 * @see Matrix#stop()
	 */
	public void stop()
	{
		this.context.stop();
		this.changeState(MatrixState.Stopped);
		if (this.thread != null)
		{
			try
			{
				this.thread.join(1);
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
			finally
			{
				this.thread = null;
			}
		}
		this.matrix.getRoot().bypass(item -> item.changeState(item.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None));
	}

	/**
	 * Pause execution of the matrix. Change state to {@link MatrixState#Pausing}
	 */
	public void pause()
	{
		this.context.pause();
		this.changeState(MatrixState.Pausing);
	}

	/**
	 * Step the matrix. It's mean, that a current item will execute and the matrix will paused on a following matrix item
	 */
	public void step()
	{
		this.changeState(MatrixState.Running);
		this.context.step();
		this.changeState(MatrixState.Pausing);
	}

	/**
	 * @return true, if matrix is running
	 */
	public boolean isRunning()
	{
		return this.thread != null && this.thread.isAlive();
	}

	public String getImagesDirPath()
	{
		return this.report.getReportDir();
	}

	//region private methods
	private void changeState(MatrixState newState)
	{
		this.matrix.getStateProperty().accept(newState);
		if (newState == MatrixState.Finished)
		{
			try
			{
				this.context.reset();
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
	//endregion
}
