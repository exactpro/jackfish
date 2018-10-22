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
package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.PerformKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "DLGALRT",
		constantGeneralDescription = R.DIALOG_ALERT_GENERAL_DESC,
		additionFieldsAllowed	   = false,
		constantOutputDescription  = R.DIALOG_ALERT_OUTPUT_DESC,
		outputType				   = String.class,
		constantExamples 		   = R.DIALOG_ALERT_EXAMPLE
	)
public class DialogAlert extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String performName    = "Perform";
	public static final String textName       = "Text";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_ALERT_APP_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = performName, mandatory = true, constantDescription = R.DIALOG_ALERT_PERFORM_KIND)
	protected PerformKind perform;

	@ActionFieldAttribute(name = textName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_ALERT_TEXT)
	protected String text;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case performName:
				return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		list.add(new ReadableValue(PerformKind.class.getSimpleName() + "." + PerformKind.Accept, "Press ok"));
		list.add(new ReadableValue(PerformKind.class.getSimpleName() + "." + PerformKind.Dismiss, "Press cancel"));
		list.add(new ReadableValue(PerformKind.class.getSimpleName() + "." + PerformKind.Nothing, "Do nothing"));
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();

		String alertText = service.getAlertText();

		service.setAlertText(this.text, this.perform);
		this.setResult(alertText);
	}
}
