////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.actions.app.ApplicationResize;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Arrays;
import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
        group					   = ActionGroups.GUI,
        suffix					   = "DLGR",
        constantGeneralDescription = R.DIALOG_RESIZE_GENERAL_DESC,
        additionFieldsAllowed 	   = false,
        constantExamples    	   = R.DIALOG_RESIZE_EXAMPLE,
        seeAlsoClass 			   = {ApplicationResize.class}
)
public class DialogResize extends AbstractAction
{
	private static final String CONNECTION_NAME = "AppConnection";
	private static final String DIALOG_NAME     = "Dialog";
	private static final String RESIZE_NAME     = "Resize";
	private static final String HEIGHT_NAME     = "Height";
	private static final String WIDTH_NAME      = "Width";

	@ActionFieldAttribute(name = CONNECTION_NAME, mandatory = true, constantDescription = R.DIALOG_RESIZE_CONNECTION)
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = DIALOG_NAME, mandatory = true, constantDescription = R.DIALOG_RESIZE_DIALOG)
	protected String dialog = null;

	@ActionFieldAttribute(name = HEIGHT_NAME, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_RESIZE_HEIGHT)
	protected Integer height;

	@ActionFieldAttribute(name = WIDTH_NAME, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_RESIZE_WIDTH)
	protected Integer width;

	@ActionFieldAttribute(name = RESIZE_NAME, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_RESIZE_RESIZE)
	protected Resize resize;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case RESIZE_NAME:
			case DIALOG_NAME:
				return HelpKind.ChooseFromList;

			default:
				break;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case RESIZE_NAME:
				Arrays.stream(Resize.values()).map(r -> Resize.class.getSimpleName() + "." + r.name()).map(ReadableValue::new).forEach(list::add);
				break;

			case DIALOG_NAME:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			default:
				break;
		}
		super.listToFillParameterDerived(list, context, parameterToFill, parameters);
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.resize == null && this.width == null && this.height == null)
		{
			setError("No one resizing parameter is filled.", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if (checkInt(WIDTH_NAME, this.width, parameters) || checkInt(HEIGHT_NAME, this.height, parameters))
		{
			return;
		}
		if (this.resize != null && (this.height != null || this.width != null))
		{
			setError("Need set resize or dimension, but no both together", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if ((this.height == null && this.width != null) || (this.height != null && this.width == null))
		{
			setError("Need set both the parameters " + WIDTH_NAME + " and " + HEIGHT_NAME, ErrorKind.WRONG_PARAMETERS);
			return;
		}

		IApplication app = Helper.getApplication(this.connection);
		IGuiDictionary dictionary = app.getFactory().getDictionary();

		IWindow window = Helper.getWindow(dictionary, this.dialog);
		String id = this.connection.getId();

		logger.debug("Process dialog : " + window);

		IControl element = window.getSelfControl();

		if (element == null)
		{
			super.setError(message(id, window, IWindow.SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
			return;
		}

		app.service().resizeDialog(element.locator(), this.resize, this.height == null ? 0 : this.height, this.width == null ? 0 : this.width);

		super.setResult(null);
	}

	private boolean checkInt(String keyName, Object value, Parameters parameters)
	{
		return check(keyName, value, parameters, "Parameter " + keyName + " must be from 0 to " + Integer.MAX_VALUE);
	}

	private boolean check(String keyName, Object value, Parameters parameters, String message)
	{
		if (parameters.getByName(keyName) != null && value == null)
		{
			setError(message, ErrorKind.WRONG_PARAMETERS);
			return true;
		}
		return false;
	}
}
