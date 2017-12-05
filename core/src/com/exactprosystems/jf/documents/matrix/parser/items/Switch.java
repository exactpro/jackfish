////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
        constantGeneralDescription = R.SWITCH_DESCRIPTION,
		constantExamples = R.SWITCH_EXAMPLE,
        shouldContain 	= { Tokens.Switch },
        mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
        real			= true,
        hasValue 		= true,
        hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {Case.class, Default.class}
)
public class Switch extends MatrixItem
{
	private Parameter switcher;

	public Switch()
	{
		super();
		this.switcher	= new Parameter(Tokens.Switch.get(),	 null); 
	}

	public Switch(Switch sw)
	{
		this.switcher = new Parameter(sw.switcher);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Switch(this);
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.switcher.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.switcher.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Switch.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Switch.get(), this.switcher, this.switcher, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.switcher.getExpression() == null ? "" :  ": " + this.switcher.getExpression());
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.switcher.setExpression(systemParameters.get(Tokens.Switch));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Switch.get(), this.switcher.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Switch.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.switcher.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndSwitch.get());
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, parameters);
        this.switcher.prepareAndCheck(evaluator, listener, this);
        
        for (MatrixItem child : this.children)
        {
        	if (!(child instanceof Case || child instanceof Default))
        	{
        		listener.error(getMatrix(), getNumber(), this, String.format(R.SWITCH_CHECK_EXCEPTION.get(), child.getItemName()));
        	}
			//we already check all child of switch
			//        	else
			//        	{
			//        		child.checkItSelf(context, evaluator, listener, ids, parameters);
			//        	}
		}
    }
    

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.switcher.evaluate(evaluator);
			if (!this.switcher.isValid())
			{
				ReportTable table = report.addTable("Switch", null, true, true, 
						new int[] {50, 50}, new String[] {"Expression", "Error"});
			
				String msg = String.format(R.COMMON_ERROR_IN_EXPRESSION.get(), this.getClass().getSimpleName());
	        	table.addValues(this.switcher.getExpression(), msg);

	        	throw new Exception(msg);
			}
			
			Object eval = this.switcher.getValue();

			for (MatrixItem item : this.children)
			{
				if (item instanceof Case)
				{
					Case caze = (Case)item;
					Parameter variant = caze.getVariant();
					if (variant.evaluate(evaluator))
					{
						Object value = variant.getValue();
						if (value == null)
						{
							if (eval == null)
							{
								return new ReturnAndResult(start, item.execute(context, listener, evaluator, report));
							}
						}
						else
						{
							if (value.equals(eval))
							{
								return new ReturnAndResult(start, item.execute(context, listener, evaluator, report));
							}
						}
					}
					else
					{
						throw new Exception(String.format(R.COMMON_ERROR_IN_EXPRESSION.get(), item.getItemName()));
					}
				}
				else if (item instanceof Default)
				{
					return new ReturnAndResult(start, item.execute(context, listener, evaluator, report));
				}
			}
			
			return new ReturnAndResult(start, Result.Passed);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}

}
