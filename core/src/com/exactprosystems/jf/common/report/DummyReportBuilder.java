////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.IOException;
import java.util.Date;

public class DummyReportBuilder extends ReportBuilder
{

	public DummyReportBuilder() throws IOException
	{
		super(null, null, new Date());
	}

	@Override
	protected String postProcess(String result)
	{
		return super.postProcess(result);
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		return "";
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		return "";
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return "";
	}

	@Override
	protected String replaceMarker(String marker)
	{
		return "";
	}

	@Override
	protected String generateReportName(String outputPath, String matrixName, String suffix, Date date)
			throws IOException
	{
		return "";
	}

	@Override
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
		return null;
	}

    @Override
    protected String decorateGroupCell(String content, int level, boolean isNode)
    {
        return null;
    }

	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
	}
	
	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version)
			throws IOException
	{
	}

	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrix)
			throws IOException
	{
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line)
			throws IOException
	{
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date)
			throws IOException
	{
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName)
			throws IOException
	{
	}

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem entry,
			Integer id) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title, Boolean asLink) throws IOException
	{
		
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item,
			String beforeTestcase, String string, String labelId) throws IOException
	{
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem entry,
			Integer id, long time, ImageWrapper screenshot) throws IOException
	{
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table,
			String tableTitle, String[] columns, int[] percents) throws IOException
	{
	}

	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value)
			throws IOException
	{
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		
	}
}
