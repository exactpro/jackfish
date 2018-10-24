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

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group						= ActionGroups.App,
		suffix						= "APPCW",
		constantGeneralDescription	= R.APP_CLOSE_WINDOW_GENERAL_DESCRIPTION,
		additionFieldsAllowed		= false,
		constantOutputDescription	= R.APP_CLOSE_WINDOW_OUTPUT_DESCRIPTION,
		outputType					= String.class,
		constantExamples			= R.APP_CLOSE_WINDOW_EXAMPLES,
        seeAlsoClass				= { ApplicationStart.class, ApplicationConnectTo.class }
)
public class ApplicationCloseWindow extends AbstractAction
{
	public static final String APP_CONNECTION_NAME = "AppConnection";

	@ActionFieldAttribute(name = APP_CONNECTION_NAME, mandatory = true, constantDescription = R.APP_CONNECTION_NAME_DESCRIPTION)
	protected AppConnection connection;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		String windowTitle = app.service().closeWindow();

		if (Str.IsNullOrEmpty(windowTitle))
		{
			super.setError("Can not close the window", ErrorKind.ELEMENT_NOT_FOUND);
		}
		else
		{
			super.setResult(windowTitle);
		}
	}
}
