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

import java.util.List;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "IMGRPT",
		generalDescription 		= "The purpose of this action Is to place the generated image in the report.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Path to image.",
		outputType 				= String.class,
		examples 				= "{{`1. Start the application.`}}"
				+ "{{`2. Capture the whole application.`}}"
				+ "{{`3. Create new report.`}}"
				+ "{{`4. Send the generated image to the new report.`}}"
				+ "\n"
				+ "{{##Id;#Action;#Browser;#URL;#AppId\n"
				+ "APPSTR1;ApplicationStart;'Chrome';'https://google.com';'WEB'\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#AppConnection\n"
				+ "IMGGET1;ImageGet;APPSTR1.Out\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#ReportName\n"
				+ "REP1;ReportStart;'New Report'\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#Image\n"
				+ "IMGRPT1;ImageReport;IMGGET1.Out#}}"
	)
public class ImageReport extends AbstractAction
{
	public final static String	imageName	= "Image";
	public final static String beforeTestCaseName = "BeforeTestCase";
	public final static String	titleName	= "Title";

	public final static String	toReportName		= "ToReport";
	public final static String	asLinkName		= "AsLink";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = "The Report object which will include the indicated image is specified."
			+ "  Report is an  an output value of the ReportStart action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = imageName, mandatory = true, description = "The image to be placed in the report.")
	protected ImageWrapper		image		= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "Enables to output the table on the highest level of the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = false, description = "The title of the image.")
	protected String			title;

	@ActionFieldAttribute(name = asLinkName, mandatory = false, description = "Instead of the image the link to it is generated in the report.")
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
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		report.outImage(super.owner, this.beforeTestCase, this.image.getName(report.getReportDir()), Str.asString(this.title), this.asLink);
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
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list, context.getEvaluator());
				break;
			case asLinkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			default:
		}
	}

}
