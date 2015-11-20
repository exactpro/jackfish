////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.util.List;


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
	
	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "A name of the dialog.")
	protected String			dialog			= null;

	@ActionFieldAttribute(name = nameName, mandatory = false, description = "A name of element of the dialog. If omitted, then full dialog will be grabbed.")
	protected String			name			= null;

	public ImageGet()
	{
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
				ActionGuiHelper.dialogsNames(context, this.connection, list);
				break;

			case nameName:
				ActionGuiHelper.extraParameters(list, context, this.connection, Str.asString(parameters.get(dialogName)), parameters);
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplication app = connection.getApplication();
		IRemoteApplication service = app.service();
		ImageWrapper imageWrapper = null;
		
		IGuiDictionary dictionary = connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		
		if (this.name == null)
		{
			boolean found = false;
			for(IControl self : window.getControls(SectionKind.Self))
			{
				found = true;
				imageWrapper = service.getImage(null, self.locator());
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
				throw new Exception("Cannot find control in dialog='" + window +"' section='" + SectionKind.Run + "' name='" + this.name + "'");
			}
			IControl owner = window.getOwnerControl(control);
			
			imageWrapper = service.getImage(owner == null ? null : owner.locator(), control.locator());
		}

        super.setResult(imageWrapper);
	}

}
