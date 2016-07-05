////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Table;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
description 	= "Raw data.", 
shouldContain 	= { Tokens.RawTable }, 
mayContain 		= { Tokens.Id, Tokens.Off, Tokens.Global }, 
real 			= true, 
hasValue 		= true, 
hasParameters 	= false, 
hasChildren 	= true,
raw 			= true
)
public class RawTable extends MatrixItem
{
	public RawTable()
	{
		super();
		this.typeName = new MutableValue<>();
		this.table = new Table(new String[][]{new String[]{"newH"}, new String[]{"newR"}}, null);
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		RawTable data = ((RawTable) super.clone());
		data.typeName = this.typeName.clone();
		return data;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.RawTable.get(), context.getFactory().getSettings());
		driver.showLabel(this, layout, 1, 2, this.typeName.get());
		driver.showCheckBox(this, layout, 1, 3, "Global", this.global, this.global);
		driver.showGrid(this, layout, 2, 0, this.table);
		driver.showToggleButton(this, layout, 1, 4, "Hide", b -> {
			driver.hide(this, layout, 2, b);
			return null;
		}, this.table.size() != 0);
		driver.hide(this, layout, 2, this.table.size() == 0);
		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================
	public String getType()
	{
		return this.typeName.get();
	}

	// ==============================================================================================
	// Interface Mutable
	// ==============================================================================================
	@Override
	public boolean isChanged()
	{
		if (this.typeName.isChanged() || this.table.isChanged())
		{
			return true;
		}
		return super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.typeName.saved();
		this.table.saved();
	}

	// ==============================================================================================
	// Protected members should be overridden
	// ==============================================================================================
	@Override
	public void processRawData(String[] str)
	{
		if (this.firstUsing)
		{
			this.table = new Table(str, null);
			this.firstUsing = false;
			return;
		}

		this.table.addValue(str);
	}
	
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.typeName;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.typeName.set(systemParameters.get(Tokens.RawTable));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "DATA";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, Tokens.RawTable.get(), this.typeName.get());
	}
	
	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		try
		{
			this.table.save(writer, indent, false, true);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}

		super.addParameter(line, Tokens.EndRawTable.get());
	}
	

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive,
			boolean wholeWord)
	{
		return SearchHelper.matches(this.typeName.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator,
			IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		if (this.id != null && !this.id.isNullOrEmpty() && ids.contains(this.id))
		{
			listener.error(this.owner, getNumber(), this, "id '" + this.id + "' has already defined.");
		}
		ids.add(this.id.get());
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
		ReportTable table;
		table = report.addTable("", 100, new int[] { 30, 70 }, new String[] {
				"Chapter", "Description" });

		table.addValues("Destination", "To describe block of data");
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator,
			ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.table.setEvaluator(evaluator);
			this.table.report(report, Tokens.RawTable.get(), false, false);

			Variables vars = isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();

			ReturnAndResult ret = new ReturnAndResult(Result.Passed, new Table(this.table, evaluator));

			if (!super.id.isNullOrEmpty())
			{
				// set variable into local name space
				vars.set(super.getId(), ret.getOut());
			}

			return ret;
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}


	// ==============================================================================================
	// Private members
	// ==============================================================================================
	private MutableValue<String> typeName;
	private Table table;
	
	private boolean firstUsing = true;
}