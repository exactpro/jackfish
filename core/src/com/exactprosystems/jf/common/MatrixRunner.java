////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Result;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Date;
import java.util.Optional;

public class MatrixRunner implements IMatrixRunner, AutoCloseable
{
	public static final String parameterName = "parameter";
	
    public static enum State
    {
    	Error,
    	Created,
        Waiting,
        Running,
        Pausing,
        Stopped,
        Finished,
        Destroyed
    }

	private MatrixRunner(Context context, Date startTime, File matrixFile, Object parameter) throws Exception
	{
		this.startTime = startTime == null ? new Date() : startTime;
		this.context = context;
        this.runnerListener = context.getRunnerListner();
		this.matrixFile = matrixFile;
		
		setGlobalVariable(parameterName, parameter);
	}
	
	public MatrixRunner(Context context, Matrix matrix, Date startTime, Object parameter) throws Exception
	{
		this(context, startTime, new File(matrix.getName()), parameter);

		this.matrix = matrix;
		this.matrixFile = new File(this.matrix.getName());
		this.runnerListener.subscribe(this);
		if (context.getMatrixListener().isOk())
		{
			changeState(State.Created);
		}
		else 
		{
			String msg = context.getMatrixListener().getExceptionMessage();
			logger.error(msg);
			changeState(State.Error);
			throw new Exception("Errors in matrix." + msg);
		}
	}

	public MatrixRunner(Context context, Reader reader, Date startTime, Object parameter) throws Exception
	{
		this(context, startTime, new File("new"), parameter);
		
		loadFromReader(context, reader);
	}

	public MatrixRunner(Context context, File matrixFile, Date startTime, Object parameter) throws Exception
	{
		this(context, startTime, matrixFile, parameter);
		try (Reader reader = new FileReader(matrixFile))
		{
			loadFromReader(context, reader);
		}
	}


	private void loadFromReader(Context context, Reader reader) throws Exception
	{
		this.matrix = new Matrix(this.matrixFile.getName(), context.getConfiguration(), context.getMatrixListener());
		this.runnerListener.subscribe(this);
		changeState(State.Error);
		this.matrix.load(reader);

		if (context.getMatrixListener().isOk())
		{
			changeState(State.Created);
		}
		else 
		{
			String msg = context.getMatrixListener().getExceptionMessage();
			logger.error(msg);
			throw new Exception("Errors in matrix." + msg);
		}
	}

	@Override
	public String toString()
	{
		if (this.matrix != null)
		{
			return getClass().getSimpleName() + "["
					+ "name=" + this.matrix.getName()
					+ " start at=" + this.startTime
					+ " " + Result.Passed + "=" + this.matrix.countResult(Result.Passed)
					+ " " + Result.Failed + "=" + this.matrix.countResult(Result.Failed)
					+ "]";
		}
		return getClass().getSimpleName() + hashCode();
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

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime == null ? new Date() : startTime;
	}
	
	public Date startTime()
	{
		return this.startTime;
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
			changeState(State.Destroyed);
			this.runnerListener.unsubscribe(this);
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
			if (this.matrix != null)
			{
				changeState(State.Running);
				this.matrix.resume(); 
			}
			return;
		}
		
		Configuration configuration = this.context.getConfiguration();
        final AbstractEvaluator evaluator = configuration.getEvaluator();
		this.report = configuration.getReportFactory().createBuilder(configuration.get(Configuration.outputPath), this.matrixFile, new Date());
		
		if (this.matrix == null)
		{
			throw new Exception("Matrix is empty.");
		}
		
		if (!this.matrix.checkMatrix(this.context, evaluator))
		{
			throw new Exception("Matrix is incorrect.");
		}
		
        changeState(State.Waiting);

		this.thread = new Thread(() -> {
			while(new Date().before(startTime))
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			changeState(State.Running);
			MatrixRunner.this.matrix.start(context, evaluator, report);
			changeState(State.Finished);
		});
		this.matrix.prepareMonitor();
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
		if (this.matrix != null)
		{
			this.matrix.stop();
			changeState(State.Stopped);
		}
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
	}
	
	@Override
	public void pause()
	{
		if (this.matrix != null)
		{
			this.matrix.pause();
			changeState(State.Pausing);
		}
	}

	@Override
	public void step()
	{
		if (this.matrix != null)
		{
			changeState(State.Running);
			this.matrix.step();
	        changeState(State.Pausing);
		}
	}
	
	@Override
	public boolean resetAllBreakPoints()
	{
		if (this.matrix != null)
		{
			this.matrix.getRoot().bypass(v -> v.setBreakPoint(false));
		}
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
        return  this.matrix.getName();
    }

	@Override
	public Object getGlobalVariable(String s)
	{
		return this.context.getConfiguration().getEvaluator().getGlobals().getVariable(s);
	}
	
	@Override
	public void setGlobalVariable(String name, Object value)
	{
		this.context.getConfiguration().getEvaluator().getGlobals().set(name, value);
	}
	

	@Override
	public String getImagesDirPath()
	{
		return this.report.getReportDir();
	}

	private void changeState(State newState)
    {
		int total = this.matrix.count(null); 
		int done = this.matrix.currentItem();
		Optional.ofNullable(this.runnerListener).ifPresent(lis -> lis.stateChange(this, newState, done, total));
    }

	private Matrix matrix = null;
	private Context context = null;
	private ReportBuilder report = null; 
	private Date startTime = null;
	private RunnerListener runnerListener = null;
	
	private File matrixFile = null;
	private Thread thread = null;
	
	private static final Logger logger = Logger.getLogger(MainRunner.class);

}
