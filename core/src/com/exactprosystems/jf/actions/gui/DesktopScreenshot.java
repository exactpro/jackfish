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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "DSK",
		generalDescription 		= "The purpose of the action is to capture the full screenshot along with the desktop.\n"
				+ "This gives the user the advantage to see system notifications of third-party applications.  The disadvantage"
				+ " is that the screenshot is captured on the 'upper layer', i.e the events/windows overlapped by other windows will not be seen. ",
		additionFieldsAllowed 	= false,
		outputDescription 		= "An image which is grabbed from the desktop.", 
		outputType 				= ImageWrapper.class,
		examples 				= "{{#\n" +
				"#Id;#Action;#Description\n"
				+ "DSK1;DesktopScreenshot;'My desktop'#}}"
	)
public class DesktopScreenshot extends AbstractAction
{
	public final static String	descriptionName	= "Description";
	
	@ActionFieldAttribute(name = descriptionName, mandatory = false, def = DefaultValuePool.EmptyString, description = "The description of the image which will be displayed in tooltip.")
	protected String			description;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
        Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new java.awt.Robot().createScreenCapture(desktopRect);

        ImageWrapper imageWrapper =  new ImageWrapper(image);

		if (!Str.IsNullOrEmpty(this.description))
		{
			imageWrapper.setDescription(this.description);
		}
        super.setResult(imageWrapper);
	}

}
