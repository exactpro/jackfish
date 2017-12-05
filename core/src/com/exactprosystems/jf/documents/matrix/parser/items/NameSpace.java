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
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
        constantGeneralDescription = R.NAME_SPACE_DESCRIPTION,
		constantExamples = R.NAME_SPACE_EXAMPLE,
        shouldContain 	= { Tokens.NameSpace },
        mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff },
        parents			= { MatrixRoot.class },
        real			= true,
        hasValue 		= true,
        hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {SubCase.class, Call.class}
)
public final class NameSpace extends MatrixItem
{
	private MutableValue<String> name;

	public NameSpace()
	{
		super();
		this.name = new MutableValue<String>();
	}

	public NameSpace(NameSpace ns)
	{
		this.name = new MutableValue<>(ns.name);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new NameSpace(this);
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
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.NameSpace.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);

		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================
	public String getName()
	{
		return this.name.get();
	}

    @Override
    public Object get(Tokens key)
    {
        switch (key)
        {
        case NameSpace:
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
        case NameSpace:
            this.name.set((String)value);
        default:
            super.set(key, value);
        }
    }
	
	// ==============================================================================================
	// Protected members should be overridden
	// ==============================================================================================
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.name == null || this.name.isNullOrEmpty() ? "" : "(" + this.name + ")");
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.NameSpace));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.NameSpace.get(), this.name.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.NameSpace.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(getId(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndNameSpace.get());
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	    super.beforeReport(report);;
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		Variables locals = evaluator.createLocals();
		try
		{
			report.itemIntermediate(this);

            evaluator.getLocals().set(parameters.makeCopy());
			ReturnAndResult res = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });

			return res;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
			evaluator.setLocals(locals);
		}
	}

	@Override
	protected void afterReport(ReportBuilder report)
	{
	    super.afterReport(report);
	}

	// ==============================================================================================
	// Private members
	// ==============================================================================================
}
