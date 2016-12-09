////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Text;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Raw data.", 
		shouldContain 	= { Tokens.RawText }, 
		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff, Tokens.Global }, 
		real 			= true, 
		hasValue 		= true, 
		hasParameters 	= false, 
		hasChildren 	= true, 
		raw 			= true
	)
public class RawText extends MatrixItem
{
	public RawText()
	{
		super();
		this.text = new Text();
		this.description = new MutableValue<>();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		RawText data = ((RawText) super.clone());
		data.text = this.text.clone();
		data.description = this.description.clone();
		return data;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.RawText.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.description, this.description, () -> this.description.get());
		driver.showCheckBox(this, layout, 1, 3, "Global", this.global, this.global);
		driver.showTextArea(this, layout, 2, 0, this.text, list -> {
			this.text.clear();
			this.text.addAll(list);
		});
		driver.showToggleButton(this, layout, 1, 4, "Hide", b -> {
			driver.hide(this, layout, 2, b);
			return null;
		}, this.text.size() == 0);

		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================

	// ==============================================================================================
	// Interface Mutable
	// ==============================================================================================
	@Override
	public boolean isChanged()
	{
		if (this.text.isChanged() || this.description.isChanged())
		{
			return true;
		}
		return super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.text.saved();
		this.description.saved();
	}

	// ==============================================================================================
	// Protected members should be overridden
	// ==============================================================================================
	@Override
	public void processRawData(String[] str)
	{
		if (this.firstUsing)
		{
			this.text = new Text();
			this.firstUsing = false;
		    this.text.add(str[0]);

			return;
		}

		this.text.add(str[0]);
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.description.get();
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
        this.description.set(systemParameters.get(Tokens.RawText));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "TEXT";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, Tokens.RawText.get(), this.description.get()); 
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		try
		{
		    for (String str : this.text)
		    {
		        writer.writeRecord(new String[] { indent + str }, true);
		    }
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}

		super.addParameter(line, Tokens.EndRawText.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.text.toString(), what, caseSensitive, wholeWord)
		        || SearchHelper.matches(this.description.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
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
		table = report.addTable("", null, true, 100, new int[] { 30, 70 }, new String[] { "Chapter", "Description" });

		table.addValues("Destination", "To describe block of text");
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
	    System.err.println(this.text);
	    
		try
		{
			this.text.report(report, null, this.description.get());
			Variables vars = isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
			
			ReturnAndResult ret = new ReturnAndResult(start, Result.Passed, this.text);

			if (super.getId() != null && !super.getId().isEmpty())
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
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}

	// ==============================================================================================
	// Private members
	// ==============================================================================================
    private MutableValue<String>    description;
    private Text                    text;

	private boolean					firstUsing	= true;
}
