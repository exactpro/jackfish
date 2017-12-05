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
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.Context.EntryPoint;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.exceptions.ParametersException;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import org.junit.runners.model.FrameworkField;

import java.time.chrono.ThaiBuddhistChronology;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		constantGeneralDescription = R.CALL_DESCRIPTION,
		constantExamples = R.CALL_EXAMPLE,
		shouldContain 	= { Tokens.Call },

		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= true,
        hasChildren 	= false,
		seeAlsoClass 	= {SubCase.class, Return.class}
)
public final class Call extends MatrixItem 
{
	private MutableValue<String> name;
	private SubCase ref;

	public Call()
	{
		super();
		this.name = new MutableValue<String>();
	}

	/**
	 * copy constructor
	 */
	public Call(Call call)
	{
		this.name = new MutableValue<>(call.name);
		if (call.ref != null)
		{
			this.ref = new SubCase(call.ref);
		}
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Call(this);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.Call.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 2, Tokens.Call.get(), this.name, this.name,
			(str) -> 
			{
				String res = DialogsHelper.selectFromList(R.COMMON_CHOOSE_SUB_CASE.get(), new ReadableValue(str), context.subcases(this)).getValue();
				if (Str.IsNullOrEmpty(res))
				{
					return res;
				}
				updateReference(context, res);
				if (this.ref == null)
				{
					DialogsHelper.showError(String.format(R.CALL_CANT_FIND_SUBCASE.get(), res));
				}
				else
				{
					((MatrixFx) this.getMatrix()).setupCall(this, res, new Parameters(this.ref.getParameters()));
				}
				return res;
			},
			(str) -> 
			{ 
			    EntryPoint entryPoint = context.referenceToSubcase(str, this);
				driver.setCurrentItem(entryPoint.getSubCase(), entryPoint.getMatrix(), false);
				return str;
			}, null, 'G' ); 
		driver.showParameters(this, layout, 1, 3, this.parameters, null, false);
		driver.showCheckBox(this, layout, 2, 0, "Global", this.global, this.global);

		return layout;
	}
	
    @Override
    public Object get(Tokens key)
    {
        switch (key)
        {
        case Call:
            return this.name.get();
        default:
            return super.get(key);
        }
    }
	
	@Override
	public void set(Tokens key, Object value)
	{
        switch (key)
        {
        case Call:
            this.name.set((String)value);
        default:
            super.set(key, value);
        }
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
	public void addKnownParameters()
	{
		if (this.ref != null)
		{
			this.parameters.addAll(this.ref.getParameters());
		}
	}

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
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Call.get(), this.name.get());
	
		for (Parameter parameter : getParameters())
		{
			super.addParameter(firstLine, secondLine, TypeMandatory.Extra, parameter.getName(), parameter.getExpression());
		}
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return 
                SearchHelper.matches(Tokens.Call.get(), what, caseSensitive, wholeWord) ||
		        SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord) ||
				getParameters().matches(what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		checkValidId(this.id, listener);
		updateReference(context, this.name.get());
		
		if (this.ref == null)
		{
			listener.error(this.owner, this.getNumber(), this, String.format(R.CALL_CANT_FIND_SUBCASE.get(), this.name));
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
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        Variables locals = evaluator.createLocals(); 
		try
		{
			boolean parametersAreCorrect = parameters.evaluateAll(evaluator);

			if (!parametersAreCorrect)
			{
				reportParameters(report, parameters);
				throw new ParametersException(R.CALL_PARAMS_EXCEPTION.get(), parameters);
			}
			if (this.ref == null)
			{
				updateReference(context, this.name.get());
			}
			if (this.ref != null)
			{
			    evaluator.getLocals().clear();
			    
				this.ref.setRealParameters(parameters);
				boolean isSubcaseIntoMatrix = this.getMatrix().getRoot().find(true, SubCase.class, this.ref.getId()) == this.ref;
				if (isSubcaseIntoMatrix)
				{
					this.changeState(isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.ExecutingParent);
				}
				ReturnAndResult ret = this.ref.execute(context, listener, evaluator, report);
				if (isSubcaseIntoMatrix)
				{
					this.changeState(isBreakPoint() ? MatrixItemState.ExecutingWithBreakPoint : MatrixItemState.Executing);
				}
				Result result = ret.getResult();

				if (super.getId() != null && !super.getId().isEmpty())
				{
	                Variables vars = isGlobal() ? evaluator.getGlobals() : locals;
					vars.set(super.getId(), ret.getOut());
				}

				if (result.isFail())
				{
					return new ReturnAndResult(start, ret);
				}

				return new ReturnAndResult(start, Result.Passed, ret.getOut());
			}
			report.outLine(this, null, "Sub case '" + this.name + "' is not found.", null);
			throw new MatrixException(super.getNumber(), this, String.format(R.CALL_CANT_FIND_SUBCASE.get(), this.name));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
		    evaluator.setLocals(locals);
		}
	}	

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        if (!parameters.isEmpty())
        {
            ReportTable table = report.addTable("Input parameters", null, true, true,
                    new int[] {20, 40, 40}, new String[] {"Parameter", "Expression", "Value"});

            for (Parameter param : parameters)
            {
                table.addValues(param.getName(), param.getExpression(), param.getValue());
            }
        }
    }

	void updateReference(Context context, String name)
	{
	    this.ref = context.referenceToSubcase(name, this).getSubCase();
	}
	
	//==============================================================================================
	// Private members
	//==============================================================================================
}
