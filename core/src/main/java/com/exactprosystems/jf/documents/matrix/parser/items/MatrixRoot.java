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

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;

import java.io.File;

@MatrixItemAttribute(
		constantGeneralDescription = R.MATRIX_ROOT_DESCRIPTION,
		shouldContain 	= { },
		mayContain 		= { },
		real			= true,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= true
)
public class MatrixRoot extends MatrixItem
{
	private String matrixName = null;

	public MatrixRoot(String matrixName)
	{
		if (matrixName != null)
		{
			this.matrixName = new File(matrixName).getName();
		}
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new MatrixRoot(this.matrixName);
	}

	//region region override from MatrixItem
	public String getMatrixName()
	{
		return this.matrixName;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		return driver.createLayout(this, 0);
	}

	@Override
	public String getItemName()
	{
		return "Matrix " + "(" + this.matrixName + ")";
	}
	//endregion
}
