/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
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
	private final Parameter variant;

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

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.variant.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.variant.saved();
	}
	//endregion

	//region region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
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
	protected void initItSelf(Map<Tokens, String> systemParameters)
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
		return SearchHelper.matches(Tokens.Case.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.variant.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		this.variant.prepareAndCheck(evaluator, listener, this);
	}
	//endregion

	public Parameter getVariant()
	{
		return this.variant;
	}
}
