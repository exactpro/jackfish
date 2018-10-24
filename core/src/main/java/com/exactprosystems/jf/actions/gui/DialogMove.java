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
import com.exactprosystems.jf.actions.app.ApplicationMove;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
        group					   = ActionGroups.GUI,
        suffix					   = "DLGMV",
        constantGeneralDescription = R.DIALOG_MOVE_GENERAL_DESC,
        additionFieldsAllowed 	   = false,
        constantExamples    	   = R.DIALOG_MOVE_EXAMPLE,
        seeAlsoClass 			   = {ApplicationMove.class}
)
public class DialogMove extends AbstractAction
{
	public static final String CONNECTION_NAME   = "AppConnection";
	public static final String DIALOG_NAME       = "Dialog";
	public static final String X_COORDINATE_NAME = "X";
	public static final String Y_COORDINATE_NAME = "Y";

	@ActionFieldAttribute(name = CONNECTION_NAME, mandatory = true, constantDescription = R.DIALOG_MOVE_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = DIALOG_NAME, mandatory = true, constantDescription = R.DIALOG_MOVE_DIALOG)
	protected String dialog;

	@ActionFieldAttribute(name = X_COORDINATE_NAME, mandatory = true, def = DefaultValuePool.IntMin, constantDescription = R.DIALOG_MOVE_X_COORDINATE)
	protected Integer x;

	@ActionFieldAttribute(name = Y_COORDINATE_NAME, mandatory = true, def = DefaultValuePool.IntMin, constantDescription = R.DIALOG_MOVE_Y_COORDINATE)
	protected Integer y;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (DIALOG_NAME.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (DIALOG_NAME.equals(parameterToFill))
		{
			Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();

		IGuiDictionary dictionary = app.getFactory().getDictionary();
		IWindow window = Helper.getWindow(dictionary, this.dialog);
		String id = this.connection.getId();

		logger.debug("Process dialog : " + window);

		IControl selfControl = window.getSelfControl();

		if (selfControl == null)
		{
			super.setError(message(id, window, IWindow.SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
			return;
		}

		Locator selfLocator = selfControl.locator();
		service.moveDialog(IControl.evaluateTemplate(selfLocator, evaluator), this.x, this.y);

		super.setResult(null);
	}
}
