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

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;

@MatrixItemAttribute(
		constantGeneralDescription = R.ON_ERROR_DESCRIPTION,
		constantExamples = R.ON_ERROR_EXAMPLE,
		shouldContain 	= { Tokens.OnError },
		mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { For.class, ForEach.class, OnError.class, Step.class, SubCase.class, TestCase.class },
		real			= true,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {Fail.class}
)
public final class OnError extends MatrixItem 
{
	private MatrixError matrixError = null;

	public OnError()
	{
		super();
	}

	public OnError(OnError onError)
	{
		this.matrixError = new MatrixError(onError.matrixError);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new OnError(this);
	}

	public void setError(MatrixError error)
	{
		this.matrixError = error;
	}

	//region region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.OnError.get(), context.getFactory().getSettings());

		return layout;
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, TypeMandatory.System, Tokens.OnError.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.OnError.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			evaluator.getLocals().getVars().put(Parser.error, this.matrixError == null ? null : this.matrixError.Message);
			evaluator.getLocals().getVars().put(Parser.err, this.matrixError == null ? new MatrixError("Unknown", ErrorKind.OTHER, this) : this.matrixError);

			ReturnAndResult ret = super.executeItSelf(start, context, listener, evaluator, report, parameters);
			Result result = ret.getResult();

			if (result.isFail())
			{
				MatrixItem branchOnError = super.find(false, OnError.class, null);
				if (branchOnError != null && branchOnError instanceof OnError)
				{
					((OnError)branchOnError).setError(ret.getError());
					
					ret = branchOnError.execute(context, listener, evaluator, report);
				}
			}

			return new ReturnAndResult(start, ret);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
	}
	//endregion
}
