////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.File;
import java.util.List;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "IMGRPT",
		generalDescription 		= "Get an image from screen",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Path to image.",
		outputType 				= String.class
	)
public class ImageReport extends AbstractAction
{
	public final static String	imageName	= "Image";
	public final static String beforeTestCaseName = "BeforeTestCase";
	public final static String	titleName	= "Title";

	public final static String	toReportName		= "ToReport";
	public final static String	asLinkName		= "asLink";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = "Rerouting report")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = imageName, mandatory = true, description = "Image to report.")
	protected ImageWrapper		image		= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "The name of Testcase before witch the picture will be put.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = false, description = "Title for picture.")
	protected String			title;

	@ActionFieldAttribute(name = asLinkName, mandatory = false, description = "Save image.")
	protected Boolean			asLink;

    public ImageReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		this.beforeTestCase	= null;
		this.title			= null;
		this.toReport = null;
		this.asLink = false;
	}
    
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		if (this.image == null)
		{
		    super.setError(imageName, ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		report = this.toReport == null ? report : this.toReport;

		if (this.asLink)
		{
			String dirName = report.getReportDir();
			String filename = this.image.saveToDir(dirName).getName();
			String link = report.decorateLink(new File("Result"),  new File(dirName).getName() + File.separator + filename);
			report.outLine(this.owner, this.beforeTestCase, link, null);
			report.outLine(this.owner, this.beforeTestCase, "   ", null);

		}
		else
		{
			report.outImage(super.owner, this.beforeTestCase, this.image.getName(report.getReportDir()), Str.asString(this.title));
		}

		super.setResult(this.image.getFileName());
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
				return HelpKind.ChooseFromList;
			case asLinkName:
				return HelpKind.ChooseFromList;
		}

		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case beforeTestCaseName:
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list);
				break;
			case asLinkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			default:
		}
	}

}
