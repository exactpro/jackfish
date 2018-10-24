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

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Date;

@ActionAttribute(
		group					   = ActionGroups.System,
		constantGeneralDescription = R.WAIT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.WAIT_EXAMPLE
	)
public class Wait extends AbstractAction 
{
	public static final String timeName   = "Time";
	public static final String byTimeName = "ByTime";

	@ActionFieldAttribute(name = timeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.WAIT_TIMEOUT)
	protected Integer timeout;

	@ActionFieldAttribute(name = byTimeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.WAIT_BY_TIME)
	protected Date byTime;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return byTimeName.equals(fieldName) ? HelpKind.ChooseDateTime : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.timeout != null)
		{
			if (this.timeout <= 0)
			{
				super.setResult(null);
				return;
			}
			sleepUpTo(context, System.currentTimeMillis() + this.timeout);
			super.setResult(null);
		}
		else if (this.byTime != null)
		{
			sleepUpTo(context, this.byTime.getTime());
			super.setResult(null);
		}
		else
		{
			super.setError(String.format("It is needed to set up either '%s' or '%s'", timeName, byTimeName), ErrorKind.WRONG_PARAMETERS);
		}
	}

	private void sleepUpTo(Context context, long finish)
	{
		long current;
		long diff = finish - System.currentTimeMillis();
		Matrix matrix = super.owner.getMatrix();
		while ((current = System.currentTimeMillis()) < finish)
		{
			if (context.isStop())
			{
				break;
			}
			try
			{
				context.getFactory().showWaits(diff, matrix);
				long min = Math.min(100, finish - current);
				diff -= min;
				Thread.sleep(min);
			}
			catch (InterruptedException ignored)
			{
				// nothing to do
			}
		}
	}
}
