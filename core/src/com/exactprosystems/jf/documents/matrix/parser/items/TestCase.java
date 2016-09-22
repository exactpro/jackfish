////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
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
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Test case.", 
		shouldContain 	= { Tokens.TestCase },
		mayContain 		= { Tokens.Off },
		closes			= MatrixRoot.class,
        real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public final class TestCase extends MatrixItem 
{	
	public TestCase()
	{
		super();
		this.name = new MutableValue<String>();
	}

    public TestCase(String name)
	{
    	this();
		this.name.set(name);
	}


	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		TestCase clone = (TestCase) super.clone();
		clone.name = name.clone();
		return clone;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + getName();
	}
	
	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.name.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.name.saved();
    }

	//==============================================================================================

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.TestCase.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.name, this.name, null);

		return layout;
	}

	public String getName()
	{
		return this.name.get();
	}
	
	@Override
	public String getItemName()
	{
		return super.getItemName() + "  " + (this.name == null ? "" : "(" + this.name + ")");
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.TestCase)); 
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, Tokens.TestCase.get(), this.name.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.TestCase.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord);
	}
	
	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		ids = new HashSet<String>();
		
		super.checkItSelf(context, evaluator, listener, ids, parameters);
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
		report.reportSwitch(true);
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "TestCase is needed to make script shorter.");
        table.addValues("Examples", "<code>#Id;#TestCase<p>ID;Description</code>");
        table.addValues("See also", "");
	}
	
	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		Result res = null;
		ReturnAndResult ret = null;
		Table table = context.getTable();
		RowTable row = new RowTable();
		
		try
		{
			if (table != null)
			{
				row.put(Context.matrixColumn, 			this.owner.getName());
				row.put(Context.testCaseNumberColumn, 	this.getNumber());
				row.put(Context.testCaseColumn, 		this);
				
				table.add(row);
			}
			
			this.locals = evaluator.createLocals();

			ret = executeChildren(context, listener, evaluator, report, new Class<?>[] { OnError.class }, this.locals);
			res = ret.getResult();
			
			if (res == Result.Failed)
			{
				MatrixItem branchOnError = super.find(false, OnError.class, null);
				if (branchOnError != null && branchOnError instanceof OnError)
				{
					((OnError)branchOnError).setError(ret.getError());
					
					ret = branchOnError.execute(context, listener, evaluator, report);
					res = ret.getResult();
				}
			}
			if (table != null && table.size() > 0)
			{
				row.put(Context.resultColumn, 			res);
				
				MatrixError error = ret.getError();
				if (error != null)
				{
					row.put(Context.errorKindColumn, 		error.Kind);
					row.put(Context.errorPlaceColumn, 		error.Where);
					row.put(Context.errorPlacePathColumn, 	error.Where.getPath());
					row.put(Context.errorMessageColumn, 	error.Message);
				}
				
				table.setValue(table.size() - 1, row);
			}
		} 
		catch (Exception e)
		{
			if (table != null && table.size() > 0)
			{
				row.put(Context.resultColumn, 			res);
				
				row.put(Context.errorKindColumn, 		ErrorKind.EXCEPTION);
				row.put(Context.errorPlaceColumn, 		this);
				row.put(Context.errorPlacePathColumn, 	this.getPath());
				row.put(Context.errorMessageColumn, 	e.getMessage());
				
				table.setValue(table.size() - 1, row);
			}
		}
		return ret;
	}

	@Override
	protected void afterReport(ReportBuilder report)
	{
		report.reportSwitch(true);
	}

	
	private Variables locals = null;
	
	private MutableValue<String> name;
}
