////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.exceptions.ParametersException;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Subroutine Test case.", 
		shouldContain 	= { Tokens.Call },
		mayContain 		= { Tokens.Id, Tokens.Off },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= true,
        hasChildren 	= false
	)
public final class Call extends MatrixItem 
{	
	public Call()
	{
		super();
		this.name = new MutableValue<String>();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Call call = ((Call) super.clone());
		call.name = this.name.clone();
		return call;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.Call.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 2, Tokens.Call.get(), this.name, this.name, 
			(str) -> 
			{
				String res = DialogsHelper.selectFromList("Choose sub case from list", new ReadableValue(str), context.subcases(this)).getValue();
				updateReference(context, res);
				if (this.ref == null)
				{
					DialogsHelper.showError("Can't find sub case with id : [" + res + "]");
				}
				else
				{
					try
					{
						driver.setupCall(this, res, this.ref.getParameters().clone());
					}
					catch (CloneNotSupportedException e) {}
				}
				return res;
			},
			(str) -> 
			{ 
				driver.setCurrentItem(context.referenceToSubcase(str, this));
				return str;
			}, null, 'G' ); 
		driver.showParameters(this, layout, 1, 3, this.parameters, null, false);
		driver.showCheckBox(this, layout, 2, 0, "Global", this.global, this.global);

		return layout;
	}

    //==============================================================================================
	// Getters / setters
	//==============================================================================================
	public String getName()
	{
		return this.name.get();
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
	// Protected members should be overridden
	//==============================================================================================
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.name;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.Call)); 
		this.id.set(systemParameters.get(Tokens.Id)); 
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "CALL_";
	}

	
	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, Tokens.Call.get(), this.name.get());
	
		for (Parameter parameter : getParameters())
		{
			super.addParameter(firstLine, secondLine, parameter.getName(), parameter.getExpression());
		}
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord) ||
				getParameters().matches(what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		updateReference(context, this.name.get());
		
		if (this.id != null && !this.id.isNullOrEmpty() && ids.contains(this.id))
		{
			listener.error(this.owner, getNumber(), this, "id '" + this.id + "' has already defined.");
		}
		ids.add(this.id.get());

		if (this.ref == null)
		{
			listener.error(this.owner, this.getNumber(), this, "Subcase with id '" + this.name + "' is not found.");
		}
		else
		{
			Set<String> extra = new HashSet<String>();
			extra.addAll(parameters.keySet());
			extra.removeAll(this.ref.getParameters().keySet());
			
			for (String e : extra)
			{
				listener.error(this.owner, this.getNumber(), this, "Extra parameter : " + e);
			}
			
			Set<String> missed = new HashSet<String>();
			missed.addAll(this.ref.getParameters().keySet());
			missed.removeAll(parameters.keySet());
			
			for (String m : missed)
			{
				listener.error(this.owner, this.getNumber(), this, "Missed parameter : " + m);
			}
		}
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "To call subroutine");
        table.addValues("Examples", "<code>#Call</code>");
        table.addValues("See also", "SubCase, Return");
	}
	

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			boolean parametersAreCorrect = parameters.evaluateAll(evaluator);

			if (!parametersAreCorrect)
			{
				reportParameters(report, parameters);
				throw new ParametersException("Errors in parameters expressions #Call", parameters);
			}
			if (this.ref == null)
			{
				updateReference(context, this.name.get());
			}
			if (this.ref != null)
			{
				Variables vars = isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();

				this.ref.setRealParameters(parameters);
				ReturnAndResult ret = this.ref.execute(context, listener, evaluator, report);
				Result result = ret.getResult();

				if (super.getId() != null && !super.getId().isEmpty())
				{
					// set variable into local name space
					vars.set(super.getId(), ret.getOut());
				}

				if (result == Result.Failed)
				{
					return ret;
				}

				return new ReturnAndResult(Result.Passed, ret.getOut());
			}
			report.outLine(this, "Sub case '" + this.name + "' is not found.", null);
			throw new MatrixException(super.getNumber(), this, "Sub case '" + this.name + "' is not found.");
		}
		catch (ParametersException e)
		{
			listener.error(getMatrix(), getNumber(), this, e.getMessage());
			for (String error : e.getParameterErrors()) 
			{
				listener.error(getMatrix(), getNumber(), this, error);
			}
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}	

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        if (!parameters.isEmpty())
        {
            ReportTable table = report.addTable("Input parameters", 2, new int[] {20, 40, 40},
                    new String[] {"Parameter", "Expression", "Value"});

            for (Parameter param : parameters)
            {
                table.addValues(param.getName(), param.getExpression(), param.getValue());
            }
        }
    }

	private void updateReference(Context context, String name)
	{
		this.ref = context.referenceToSubcase(name, this);
	}
	

	//==============================================================================================
	// Private members
	//==============================================================================================
	private MutableValue<String> name;
	
	private SubCase ref;
}
