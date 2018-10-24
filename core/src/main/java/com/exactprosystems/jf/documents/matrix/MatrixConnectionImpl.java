/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.Table;

import java.sql.Blob;

public class MatrixConnectionImpl implements MatrixConnection
{
	private final Matrix matrix;

	MatrixConnectionImpl(Matrix matrix)
	{
		this.matrix = matrix;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName()
				+ "[" + "name=" + this.getMatrixName()
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
