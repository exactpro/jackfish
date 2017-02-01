////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import java.util.List;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

@Deprecated
@MatrixItemAttribute(
		description 	= "Switches on output of the report.",
		examples 		= "#ReportOn",
		seeAlso 		= "ReportOff",
		shouldContain 	= { Tokens.ReportOn },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		real			= true,
		hasValue 		= false, 
		hasParameters 	= false, 
        hasChildren 	= false
	)
public class ReportOn extends MatrixItem
{
	public ReportOn()
	{
		super();
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.ReportOn.get(), context.getFactory().getSettings());

		return layout;
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	    super.beforeReport(report);
	    
		report.reportSwitch(true);
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, Tokens.ReportOn.get());
	}


	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        context.getOut().println("ReportOff is deprecated. Use #RepOff attribute instead.");
		return new ReturnAndResult(start, Result.Passed); 
	}
}
