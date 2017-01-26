////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "IMGGET",
		generalDescription 		= "Get an image from screen",
		additionFieldsAllowed 	= false,
		outputDescription 		= "An image which is grabbed from the screen.", 
		outputType 				= ImageWrapper.class
	)
public class ImageGet extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";
	public final static String	nameName		= "Name";
	public final static String	descriptionName	= "Description";
	public final static String 	x_leftUp 	= "X_leftUp";
	public final static String	y_leftUp	= "Y_leftUp";
	public final static String	x_rightDown	= "X_rightDown";
	public final static String	y_rightDown	= "Y_rightDown";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, description = "A name of the dialog. If omitted, then full screen will be grabbed.")
	protected String			dialog			= null;

	@ActionFieldAttribute(name = nameName, mandatory = false, description = "A name of element of the dialog. If omitted, then full dialog will be grabbed.")
	protected String			name;

	@ActionFieldAttribute(name = descriptionName, mandatory = false, description = "A description of this image. In the report it become a tooltip.")
	protected String			description;

	@ActionFieldAttribute(name = x_leftUp, mandatory = false, description = "X coordinate is for left upper corner")
	protected Integer			x1					= Integer.MIN_VALUE;

	@ActionFieldAttribute(name = y_leftUp, mandatory = false, description = "Y coordinate is for left upper corner")
	protected Integer			y1					= Integer.MIN_VALUE;

	@ActionFieldAttribute(name = x_rightDown, mandatory = false, description = "X coordinate is for right bottom corner")
	protected Integer			x2					= Integer.MIN_VALUE;

	@ActionFieldAttribute(name = y_rightDown, mandatory = false, description = "Y coordinate is for right bottom corner")
	protected Integer			y2					= Integer.MIN_VALUE;

	public ImageGet()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		this.dialog = null;
		this.name	= null;
	}
	
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
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		ImageWrapper imageWrapper = null;

		if (this.dialog == null)
		{
			imageWrapper = service.getImage(null, null, x1, y1, x2, y2);
		}
		else
		{
			IGuiDictionary dictionary = connection.getDictionary();
			IWindow window = dictionary.getWindow(this.dialog);
			
			if (this.name == null)
			{
				boolean found = false;
				for(IControl self : window.getControls(SectionKind.Self))
				{
					found = true;
					imageWrapper = service.getImage(null, self.locator(), x1, y1, x2, y2);
					break;
				}
	
				if (!found)
				{
					throw new Exception("Cannot find any controls in dialog='" + window +"' in section " + SectionKind.Self);
				}
			}
			else
			{
				IControl control = window.getControlForName(SectionKind.Run, this.name);
				if (control == null)
				{
					super.setError(message(id, window, SectionKind.Self, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
					return;
				}
				IControl owner = window.getOwnerControl(control);
				
				imageWrapper = service.getImage(owner == null ? null : owner.locator(), control.locator(), x1, y1, x2, y2);
			}
		}

		if (imageWrapper != null)
		{
			imageWrapper.setDescription(this.description);
		}
        super.setResult(imageWrapper);
	}

}
