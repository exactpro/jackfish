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
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
        group 				  		  = ActionGroups.GUI,
        suffix 						  = "DLGGP",
        constantGeneralDescription    = R.DIALOG_GET_PROPERTIES_GENERAL_DESC,
        additionFieldsAllowed 		  = false,
        outputType              	  = Map.class,
        constantOutputDescription 	  = R.DIALOG_GET_PROPERTIES_OUTPUT_DESC,
        constantExamples 			  = R.DIALOG_GET_PROPERTIES_EXAMPLE
)
public class DialogGetProperties extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String dialogName     = "Dialog";
	public static final String sizeName       = "Size";
	public static final String positionName   = "Position";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_GET_PROPERTIES_APP_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_GET_PROPERTIES_DIALOG)
	protected String dialog;

	@ActionFieldAttribute(name = sizeName, shouldFilled = false, mandatory = false, constantDescription = R.DIALOG_GET_PROPERTIES_SIZE)
	protected String size;

	@ActionFieldAttribute(name = positionName, shouldFilled = false, mandatory = false, constantDescription = R.DIALOG_GET_PROPERTIES_POSITION)
	protected String position;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		if (dialogName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (dialogName.equals(parameterToFill))
		{
			Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		IGuiDictionary dictionary = app.getFactory().getDictionary();
		IRemoteApplication service = app.service();
		String id = connection.getId();

		IWindow window = Helper.getWindow(dictionary, this.dialog);

		IControl selfControl = window.getSelfControl();

		if (selfControl == null)
		{
			super.setError(message(id, window, IWindow.SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
			return;
		}

		Map<String, Object> result = new HashMap<>();

		for (Parameter parameter : parameters.select(TypeMandatory.NotMandatory))
		{
			if (parameter.getName().equals(sizeName))
			{
				Dimension dialogSize = service.getDialogSize(IControl.evaluateTemplate(selfControl.locator(), evaluator));
				result.put(parameter.getName(), dialogSize);
			}
			if (parameter.getName().equals(positionName))
			{
				Point dialogPosition = service.getDialogPosition(IControl.evaluateTemplate(selfControl.locator(), evaluator));
				result.put(parameter.getName(), dialogPosition);
			}
		}

		setResult(result);
	}
}
