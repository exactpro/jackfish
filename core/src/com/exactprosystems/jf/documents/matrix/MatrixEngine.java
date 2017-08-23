////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Blob;
import java.util.Date;

public class MatrixEngine implements AutoCloseable
{
    public static final String  parameterName = "parameter";
    private static final Logger logger        = Logger.getLogger(MatrixEngine.class);

    private Matrix              matrix        = null;
    private Context             context       = null;
    private ReportBuilder       report        = null;
    private Thread              thread        = null;

	protected MatrixEngine(Context context, Matrix matrix)
	{
		this.context = context;
		this.matrix = matrix;
	}


	public Context getContext()
	{
	    return this.context;
	}
	
	public Blob reportAsBlob() throws Exception
	{
	    return Converter.storableToBlob(this.report);
	}
	
	public Table getTable()
	{
		return this.context.getTable();
	}
	
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
			this.context.close();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public void start(Date time, Object parameter) throws Exception
	{
        Date startTime = time == null ? new Date() : time;
	    
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
        evaluator.getGlobals().set(parameterName, parameter);

        String fileName = new File(this.matrix.getNameProperty().get()).getName();
		this.report = configuration.getReportFactory().createReportBuilder(configuration.getReports().get(), fileName, new Date());
		StringBuilder errorMsg = new StringBuilder();
		if (!this.matrix.checkMatrix(this.context, evaluator, errorMsg))
		{
			throw new MatrixException(0, null, "Matrix is incorrect. Errors : " + errorMsg.toString());
		}
		
        changeState(MatrixState.Waiting);

		this.thread = new Thread(() -> 
		{
			while(new Date().before(startTime))
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
			changeState(MatrixState.Running);
			this.matrix.start(context, evaluator, report);
			changeState(MatrixState.Finished);
		});
		this.thread.setName("Start matrix thread, thread id : " + thread.getId());
		this.context.prepareMonitor();
		this.thread.start();
	}
	
	public boolean join(long time) throws Exception
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
				close();
				return joinSuccess;
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}
	
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
		this.matrix.getRoot().bypass(item -> item.changeState(item.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None));
	}
	
	public void pause()
	{
		this.context.pause();
		changeState(MatrixState.Pausing);
	}

	public void step()
	{
		changeState(MatrixState.Running);
		this.context.step();
        changeState(MatrixState.Pausing);
	}
	
	public boolean isRunning()
	{
		return this.thread != null && this.thread.isAlive();
	}

	public String getImagesDirPath()
	{
		return this.report.getReportDir();
	}


	private void changeState(MatrixState newState)
    {
	    this.matrix.getStateProperty().set(newState);
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
}
