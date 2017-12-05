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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		constantGeneralDescription = R.CASE_DESCRIPTION,
		constantExamples = R.CASE_EXAMPLE,
		shouldContain 	= { Tokens.Case },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
        closes			= Switch.class,
		parents			= { Switch.class },
        real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {Switch.class}
	)
public class Case extends MatrixItem
{
	private Parameter variant;

	public Case()
	{
		super();
		this.variant = new Parameter(Tokens.Case.get(),	null); 
	}

	/**
	 * copy constructor
	 */
	public Case(Case caze)
	{
		this.variant = new Parameter(caze.variant);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Case(this);
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.variant.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.variant.saved();
    }

    //==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Case.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Case.get(), this.variant, this.variant, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.variant.getExpression() == null ? "" : ": " + this.variant.getExpression());
	}
	
	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.variant.setExpression(systemParameters.get(Tokens.Case));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Case.get(), this.variant.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Case.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.variant.getExpression(), what, caseSensitive, wholeWord);
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, parameters);
        this.variant.prepareAndCheck(evaluator, listener, this);
    }
    
	public Parameter getVariant()
	{
		return this.variant;
	}
}
