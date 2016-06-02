////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
	description 	= "Loop from start value to end value with step.", 
	shouldContain 	= { Tokens.ForEach, Tokens.In },
	mayContain 		= { Tokens.Off }, 
	real			= true,
	hasValue 		= true, 
	hasParameters 	= false,
	hasChildren 	= true
)
public final class ForEach extends MatrixItem
{
	public ForEach()
	{
		super();
		this.var = new MutableValue<String>();
		this.in = new Parameter(Tokens.In.get(), null);
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		ForEach clone = ((ForEach) super.clone());
		clone.var = var.clone();
		clone.in = in.clone();
		return clone;
	}

	// ==============================================================================================
	// Interface Mutable
	// ==============================================================================================
	@Override
	public boolean isChanged()
	{
		if (this.var.isChanged() || this.in.isChanged())
		{
			return true;
		}
		return super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.var.saved();
		this.in.saved();
	}

	// ==============================================================================================
	// implements Displayed
	// ==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.ForEach.get(), context.getConfiguration().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.var, this.var, () -> this.var.get());
		driver.showTitle(this, layout, 1, 2, Tokens.In.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.In.get(), this.in, this.in, null, null, null, null);

		return layout;
	}

	// ==============================================================================================

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.var + " In " + this.in.getExpression();
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		super.initItSelf(systemParameters);

		this.var.set(systemParameters.get(Tokens.ForEach));
		this.in.setExpression(systemParameters.get(Tokens.In));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.ForEach.get(), this.var.get());
		super.addParameter(firstLine, secondLine, Tokens.In.get(), this.in.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.var.get(), what, caseSensitive, wholeWord) 
				|| SearchHelper.matches(Tokens.ForEach.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(Tokens.In.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.in.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndForEach.get());
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
		ReportTable table;
		table = report.addTable("", 100, new int[] { 30, 70 }, new String[] { "Chapter", "Description" });

		table.addValues("Destination", "To organize a loop over a collection");
		table.addValues("Examples", "<code>#ForEach;#In<p>row;TAB</code>");
		table.addValues("See also", "For, While, Break, Continue");
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, ids, parameters);
		this.in.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = new ReturnAndResult(Result.Passed, null);
			Result result = ret.getResult();

			if (!this.in.evaluate(evaluator))
			{
				throw new Exception("Error in expression #In");
			}

			Object inValue = this.in.getValue();
			if (!(inValue instanceof Iterable<?>))
			{
				throw new Exception("#In is not a collection");
			}

			Iterator<?> iterator = ((Iterable<?>)inValue).iterator(); 
			int count = 0;

			while (iterator.hasNext())
			{
				Object currentValue = iterator.next();
				evaluator.getLocals().set(this.var.get(), currentValue);

				report.outLine(this, String.format("loop %s = %s", this.var, Str.asString(currentValue)), count++);

				ret = executeChildren(context, listener, evaluator, report, new Class<?>[] { OnError.class }, null);
				result = ret.getResult();

				evaluator.getLocals().set(this.var.get(), currentValue);

				if (result == Result.Failed)
				{
					MatrixItem branchOnError = super.find(false, OnError.class, null);
					if (branchOnError != null && branchOnError instanceof OnError)
					{
						((OnError) branchOnError).setError(ret.getError());

						ret = branchOnError.execute(context, listener, evaluator, report);
						result = ret.getResult();
					}
					else
					{
						return ret;
					}
				}

				
				if(result == Result.Break)
				{
					result = Result.Passed;
					break;
				}
				
				if (result == Result.Failed || result == Result.Stopped || result == Result.Return)
				{
					break;
				}
				
				if (result == Result.Continue)
				{
					continue;
				}
			}

			return new ReturnAndResult(result == Result.Continue ? Result.Passed : result, ret.getOut());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}

	private MutableValue<String>	var;
	private Parameter				in;
}
