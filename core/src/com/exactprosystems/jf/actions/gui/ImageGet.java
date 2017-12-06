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
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
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
		suffix					   = "IMGGET",
		constantGeneralDescription = R.IMAGE_GET_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.IMAGE_GET_OUTPUT_DESC,
		outputType 				   = ImageWrapper.class,
		constantExamples 		   = R.IMAGE_GET_EXAMPLE
	)
public class ImageGet extends AbstractAction
{
	public static final String connectionName  = "AppConnection";
	public static final String dialogName      = "Dialog";
	public static final String nameName        = "Name";
	public static final String descriptionName = "Description";
	public static final String x_leftUp        = "X1";
	public static final String y_leftUp        = "Y1";
	public static final String x_rightDown     = "X2";
	public static final String y_rightDown     = "Y2";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.IMAGE_GET_APP_CONNECTION)
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, constantDescription = R.IMAGE_GET_DIALOG)
	protected String dialog = null;

	@ActionFieldAttribute(name = nameName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_GET_NAME)
	protected String name;

	@ActionFieldAttribute(name = descriptionName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.IMAGE_GET_DESCRIPTION)
	protected String description;

	@ActionFieldAttribute(name = x_leftUp, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_X1)
	protected Integer x1;

	@ActionFieldAttribute(name = y_leftUp, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_Y1)
	protected Integer y1;

	@ActionFieldAttribute(name = x_rightDown, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_X2)
	protected Integer x2;

	@ActionFieldAttribute(name = y_rightDown, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_Y2)
	protected Integer y2;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return dialogName.equals(fieldName) || nameName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			case nameName:
				Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), true);
				break;

			default:
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();
		String id = this.connection.getId();

		ImageWrapper imageWrapper;

		if (this.dialog == null)
		{
			imageWrapper = service.getImage(null, null);
		}
		else
		{
			IGuiDictionary dictionary = app.getFactory().getDictionary();
			IWindow window = Helper.getWindow(dictionary, this.dialog);

			if (this.name == null)
			{
				IControl selfControl = window.getSelfControl();
				if (selfControl == null)
				{
					super.setError("Self control not found", ErrorKind.ELEMENT_NOT_FOUND);
					return;
				}
				imageWrapper = service.getImage(null, selfControl.locator());
			}
			else
			{
				IControl control = window.getControlForName(SectionKind.Run, this.name);
				if (control == null)
				{
					super.setError(message(id, window, SectionKind.Run, null, null, String.format("Element with name %s not found", this.name)), ErrorKind.ELEMENT_NOT_FOUND);
					return;
				}
				IControl owner = window.getOwnerControl(control);
				
				imageWrapper = service.getImage(owner == null ? null : owner.locator(), control.locator());
			}
		}

		if (imageWrapper != null)
		{
			if(this.x1 != Integer.MIN_VALUE && this.y1 != Integer.MIN_VALUE && this.x2 != Integer.MIN_VALUE && this.y2 != Integer.MIN_VALUE)
			{
				imageWrapper = imageWrapper.cutImage(this.x1, this.y1, this.x2, this.y2);
			}
			imageWrapper.setDescription(this.description);
		}
        super.setResult(imageWrapper);
	}

}
