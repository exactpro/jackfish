////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ChartKind;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
	}
	
	@Override
	protected void reportHeader(ReportWriter writer, Matrix context, Date date)
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
	protected void reportHeaderTotal(ReportWriter writer, Matrix context, Date date)
			throws IOException
	{
	}

	@Override
	protected void reportFooter(ReportWriter writer, MatrixItem entry, Date date, String name)
			throws IOException
	{
	}

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem entry,
			Integer id) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title) throws IOException
	{
		
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item,
			String beforeTestcase, String string, String labelId) throws IOException
	{
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem entry,
			Integer id, long time) throws IOException
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
	protected void histogram(ReportWriter writer, String title, int intervalCount, int interval, List<Long> copyDate)
	{

	}

	@Override
	protected void reportChar(ReportWriter writer, ChartKind chartKind, String title, String beforeTestCase, Table table, Map<String, Object> values) throws IOException
	{
		
	}
}
