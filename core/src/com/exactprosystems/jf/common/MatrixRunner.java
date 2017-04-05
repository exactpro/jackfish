////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Reader;
import java.sql.Blob;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

public class MatrixRunner implements IMatrixRunner, AutoCloseable
{
	public static final String parameterName = "parameter";

	private MatrixRunner(Context context, String name, Date startTime, Object parameter) throws Exception
	{
		this.startTime = startTime == null ? new Date() : startTime;
		this.context = context;
		this.matrixFile = new File(name);
		
		setGlobalVariable(parameterName, parameter);
	}

	public MatrixRunner(Context context, String name, Reader reader, Date startTime, Object parameter) throws Exception
	{
		this(context, name, startTime, parameter);
		if (reader != null)
		{
			loadFromReader(context, reader);
		}
	}

	public void setMatrix(Matrix matrix) 
	{
		this.matrix = matrix;
	}

	public Context getContext()
	{
	    return this.context;
	}
	
	public void setOnFinished(Consumer<MatrixRunner> consumer)
	{
		//TODO Valery, think about it
		this.consumer = consumer;
	}

	public Boolean process (FourParameterFunction<Matrix, Context, ReportBuilder, Date, Boolean> fn) throws Exception
	{
		Boolean res = fn.apply(this.matrix, this.context, this.report, this.startTime);
		return res;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "["
				+ "name=" + this.matrixFile.getName()
				+ " start at=" + this.startTime
				+ " " + Result.Passed + "=" + this.matrix.countResult(Result.Passed)
				+ " " + Result.Failed + "=" + this.matrix.countResult(Result.Failed)
				+ "]";
	}

	public String matrix()
	{
		return this.matrix.getName();
	}

	@Override
	public int passed()
	{
		return this.matrix.countResult(Result.Passed); 
	}

	@Override
	public int failed()
	{
		return 	this.matrix.countResult(Result.Failed); 
	}

	
	public Blob reportAsBlob() throws Exception
	{
	    return Converter.storableToBlob(this.report);
	}
	
	public void setStartTime(Date startTime)
	{
		this.startTime = startTime == null ? new Date() : startTime;
	}
	
	public Date getStartTime()
	{
		return this.startTime;
	}

	public Table getTable()
	{
		return this.context.getTable();
	}
	
	@Override
	public String getReportName()
	{
		return this.report == null ? null : report.getReportName();
	}

	@Override
	public void close() throws Exception
	{
		try
		{
			stop();
			changeState(MatrixState.Destroyed);
			this.context.getConfiguration().getRunnerListener().unsubscribe(this);
			this.context.close();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void start() throws Exception
	{
		if (isRunning())
		{
			changeState(MatrixState.Running);
			this.context.resume();
			return;
		}
		else
		{
			this.context.createResultTable();
		}
		
		Configuration configuration = this.context.getConfiguration();
        final AbstractEvaluator evaluator = this.context.getEvaluator();
		this.report = configuration.getReportFactory().createReportBuilder(configuration.getReports().get(), this.matrixFile.getName(), new Date());
		StringBuilder errorMsg = new StringBuilder();
		if (!this.matrix.checkMatrix(this.context, evaluator, errorMsg))
		{
			throw new Exception("Matrix is incorrect. Errors : " + errorMsg.toString());
		}
		
        changeState(MatrixState.Waiting);

		this.thread = new Thread(() -> {
			while(new Date().before(startTime))
			{
				if (context.isStop())
				{
					Optional.ofNullable(this.consumer).ifPresent(c -> c.accept(this));
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
			changeState(MatrixState.Running);
			MatrixRunner.this.matrix.start(context, evaluator, report);
			changeState(MatrixState.Finished);
			Optional.ofNullable(this.consumer).ifPresent(c -> c.accept(this));
		});
		this.thread.setName("Start matrix thread, thread id : " + thread.getId());
		this.context.prepareMonitor();
		this.thread.start();
	}
	
	@Override
	public void join(long time) throws Exception
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
				close();
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void stop()
	{
		this.context.stop();
		changeState(MatrixState.Stopped);
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
		if (this.matrix != null)
		{
			this.matrix.getRoot().bypass(item -> item.changeState(item.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None));
		}
	}
	
	@Override
	public void pause()
	{
		this.context.pause();
		changeState(MatrixState.Pausing);
	}

	@Override
	public void step()
	{
		changeState(MatrixState.Running);
		this.context.step();
        changeState(MatrixState.Pausing);
	}
	
	@Override
	public boolean resetAllBreakPoints()
	{
		this.matrix.getRoot().bypass(v -> v.setBreakPoint(false));
		return true;
	}
	
	@Override
	public boolean isRunning()
	{
		return this.thread != null && this.thread.isAlive();
	}

	@Override
    public String getMatrixName()
    {
        return this.matrixFile.getPath();
    }

	@Override
	public Object getGlobalVariable(String s)
	{
		return this.context.getEvaluator().getGlobals().getVariable(s);
	}
	
	@Override
	public void setGlobalVariable(String name, Object value)
	{
		this.context.getEvaluator().getGlobals().set(name, value);
	}
	

	@Override
	public String getImagesDirPath()
	{
		return this.report.getReportDir();
	}

	@Override
	public void setMatrixFile(String name)
	{
		this.matrixFile = new File(name);
	}

	private void loadFromReader(Context context, Reader reader) throws Exception
	{
		this.matrix = context.getFactory().createMatrix(this.matrixFile.getName(), this);
		this.context.getConfiguration().getRunnerListener().subscribe(this);
		changeState(MatrixState.Error);
	    this.matrix.load(reader);

		if (context.getMatrixListener().isOk())
		{
			changeState(MatrixState.Created);
		}
		else 
		{
			String msg = context.getMatrixListener().getExceptionMessage();
			logger.error(msg);
			throw new Exception("Errors in matrix." + msg);
		}
	}

	private void changeState(MatrixState newState)
    {
		if (this.matrix == null)
		{
			return;
		}
		int total = this.matrix.count(null); 
		int done = this.matrix.currentItem();
		this.context.getConfiguration().getRunnerListener().stateChange(this, newState, done, total);
		
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

	private Matrix matrix = null;
	private Context context = null;
	private ReportBuilder report = null; 
	private Date startTime = null;
	
	private File matrixFile = null;
	private Thread thread = null;

	private Consumer<MatrixRunner> consumer;
	
	private static final Logger logger = Logger.getLogger(MainRunner.class);
}
