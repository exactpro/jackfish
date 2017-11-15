////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.Table;

import java.sql.Blob;

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
	public Blob reportAsBlob() throws Exception
	{
		if (this.matrix.getEngine() != null)
		{
			return this.matrix.getEngine().reportAsBlob();
		}
		return null;
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
        return this.matrix.getEngine() != null && this.matrix.getEngine().isRunning();
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
