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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
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
import java.util.concurrent.atomic.AtomicReference;

@MatrixItemAttribute(
	description 	= "Operator ForEach is used to organize a cycle according the collection elements: massive, list, table, Map etc. \n" +
						"Fields:\n" +
						"ForEach – a variable name is given which is set with the next element value by each iteration.  \n" +
						"In – a collection is given which is needed to organize a cycle.",
	examples 		= "{{##ForEach;#In\n" +
						"a;['First', 'Second']\n" +
						"#Action;#a\n" +
						"Print;a\n" +
						"#EndForEach#}}",
	seeAlso 		= "For, While",
	shouldContain 	= { Tokens.ForEach, Tokens.In },
	mayContain 		= { Tokens.Off, Tokens.RepOff }, 
	parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
						OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
	real			= true,
	hasValue 		= true, 
	hasParameters 	= false,
	hasChildren 	= true,
	seeAlsoClass 	= {For.class, While.class}
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
		driver.showTitle(this, layout, 1, 0, Tokens.ForEach.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.var, this.var, () -> this.var.get());
		driver.showTitle(this, layout, 1, 2, Tokens.In.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.In.get(), this.in, this.in, null, null, null, null);

		return layout;
	}

	// ==============================================================================================

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.var + (this.in.getExpression() == null ? "" : " In " + this.in.getExpression());
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
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, ids, parameters);
		this.in.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = null;
			Result result = null;

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
            AtomicReference<Object> current = new AtomicReference<>(null);

			while (checkCondition(iterator, current, evaluator))
			{
				report.outLine(this, null, String.format("loop %s = %s", this.var, Str.asString(current.get())), count++);

				ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });
				result = ret.getResult();

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
                    break;
                }
                if (result == Result.Failed)
                {
                    return new ReturnAndResult(start, ret.getError(), Result.Failed);
                }
                if (result == Result.Stopped || result == Result.Return)
                {
                    return new ReturnAndResult(start, result, ret.getOut());
                }
                if (result == Result.Continue)
                {
                    continue;
                }
            }

            return new ReturnAndResult(start, Result.Passed, null);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}
	
	private boolean checkCondition(Iterator<?> iterator, AtomicReference<Object> current, AbstractEvaluator evaluator)
	{
	    boolean ret = iterator.hasNext();
	    
	    if (ret)
	    {
            Object currentValue = iterator.next();
            current.set(currentValue);
            evaluator.getLocals().set(this.var.get(), currentValue);
	    }
	    
	    return ret;
	}

	private MutableValue<String>	var;
	private Parameter				in;
}
