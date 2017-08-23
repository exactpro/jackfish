////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.Table;

public class MatrixConnectionImpl implements MatrixConnection
{
    private Matrix matrix; 
    
    public MatrixConnectionImpl(Matrix matrix)
    {
        this.matrix = matrix;
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "["
                + "name=" + getMatrixName()
                + " " + Result.Passed + "=" + passed()
                + " " + Result.Failed + "=" + failed()
                + "]";
    }
    
    @Override
    public boolean join(long time) throws Exception
	{
		return this.matrix.getEngine() == null || this.matrix.getEngine().join(time);
	}

    @Override
    public void stop()
    {
        if (this.matrix.getEngine() != null)
        {
            this.matrix.getEngine().stop();
        }
    }

    @Override
    public int passed()
    {
        return this.matrix.countResult(Result.Passed); 
    }

    @Override
    public int failed()
    {
        return this.matrix.countResult(Result.Failed); 
    }

    @Override
    public boolean isRunning()
    {
        return this.matrix.getEngine() == null ? false : this.matrix.getEngine().isRunning();
    }

    @Override
    public String getMatrixName()
    {
        return this.matrix.getEngine() == null ? null : this.matrix.getNameProperty().get();
    }

    @Override
    public String getReportName()
    {
        return this.matrix.getEngine() == null ? null : this.matrix.getEngine().getReportName();
    }

	@Override
	public void close() throws Exception
	{
		this.matrix.close();
	}

	@Override
    public String getImagesDirPath()
    {
        return this.matrix.getEngine() == null ? null : this.matrix.getEngine().getImagesDirPath();
    }

    public Table getTable()
    {
        return this.matrix.getEngine() == null ? null : this.matrix.getEngine().getTable();
    }
}
