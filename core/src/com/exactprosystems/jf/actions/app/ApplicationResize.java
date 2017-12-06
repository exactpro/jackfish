////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.actions.gui.DialogResize;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.App,
		suffix					   = "APPR",
		constantGeneralDescription = R.APP_RESIZE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples    	   = R.APP_RESIZE_EXAMPLE,
		seeAlsoClass 			   = {ApplicationStart.class, ApplicationConnectTo.class, DialogResize.class}
	)

public class ApplicationResize extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String heightName     = "Height";
	public static final String widthName      = "Width";
	public static final String resizeName     = "Resize";
	public static final String minimizeName   = "Minimize";
	public static final String maximizeName   = "Maximize";
	public static final String normalName     = "Normal";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_RESIZE_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = heightName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.APPLICATION_RESIZE_HEIGHT)
	protected Integer height;

	@ActionFieldAttribute(name = widthName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.APPLICATION_RESIZE_WIDTH)
	protected Integer width;

	@ActionFieldAttribute(name = resizeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.APPLICATION_RESIZE_RESIZE)
	protected Resize resize;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case resizeName :
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
			case resizeName :
				list.addAll(CommonHelper.convertEnumsToReadableList(Resize.values()));
				break;

			default:
				break;
		}
		super.listToFillParameterDerived(list, context, parameterToFill, parameters);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.resize == null && this.width == null && this.height == null)
		{
			setError("No one resizing parameter is filled.", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		//TODO this check is unnecessary
		if (checkInt(widthName, this.width, parameters) || checkInt(heightName, this.height, parameters))
		{
			return;
		}
		//TODO this check is unnecessary
		if (parameters.getByName(resizeName) != null && this.resize == null)
		{
			setError("Parameter " + resizeName + " must be Resize.Maximize, Resize.Minimize or Resize.Normal", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if (this.resize != null && (this.height != null || this.width != null))
		{
			setError("Need set resize or dimension, but no both together", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if ((this.height == null && this.width != null) || (this.height != null && this.width == null))
		{
			setError("Need set both the parameters " + widthName + " and " + heightName, ErrorKind.WRONG_PARAMETERS);
			return;
		}
		IApplication app = connection.getApplication();
		app.service().resize(this.resize
				, this.height == null ? 0 : this.height
				, this.width  == null ? 0 : this.width);

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
