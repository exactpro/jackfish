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
		generalDescription 		= "Get an image from the desktop",
		additionFieldsAllowed 	= false,
		outputDescription 		= "An image which is grabbed from the desktop.", 
		outputType 				= ImageWrapper.class
	)
public class DesktopScreenshot extends AbstractAction
{
	public final static String	descriptionName	= "Description";
	
	@ActionFieldAttribute(name = descriptionName, mandatory = false, description = "A description of this image. In the report it become a tooltip.")
	protected String			description;

	public DesktopScreenshot()
	{
	}

	@Override
	public void initDefaultValues() 
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
        Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new java.awt.Robot().createScreenCapture(desktopRect);

        ImageWrapper imageWrapper =  new ImageWrapper(image);

		if (imageWrapper != null)
		{
			imageWrapper.setDescription(this.description);
		}
        super.setResult(imageWrapper);
	}

}
