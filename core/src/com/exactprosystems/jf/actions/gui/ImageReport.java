////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import java.io.File;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "IMGRPT",
		generalDescription 		= "Get an image from screen",
		additionFieldsAllowed 	= false
	)
public class ImageReport extends AbstractAction
{
	public final static String	imageName	= "Image";
	public final static String	titleName	= "Title";
	
	@ActionFieldAttribute(name = imageName, mandatory = true, description = "Image to report.")
	protected ImageWrapper		image		= null;

	@ActionFieldAttribute(name = titleName, mandatory = false, description = "Title for picture.")
	protected String			title;

    public ImageReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		title		= null;
	}
    
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		if (this.image.getFileName() == null)
		{
			File file = this.image.saveToDir(report.getReportDir());
			report.outImage(super.owner, file.getName(), this.title); 
		}
		else
		{
			report.outImage(super.owner, this.image.getFileName(), this.title); 
		}
		
		super.setResult(null);
	}

}
