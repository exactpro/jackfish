////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
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
		description 	= "This operator is used to describe one variant to compare with for an operator Switch.\n" +
							"In each operator  Case is given one of the variants that equals the expression from the operator Switch",
		examples 		= "(Example #1)\n" +
				"Or the operator Case is performed, which value is true (Example #2)\n" +
				"After one of the blocks  case has been performed an operator  switch is being broken.\n" +
				"Example #1:\n" +
				"Variable a is transmitted into field  switch. The  Case will be performed which value matches a variable value.\n" +
				"{{#\n" +
				"#Switch\n" +
				"a\n" +
				"#Case\n" +
				"1\n" +
				"#Action;#Greeting\n" +
				"Print;'Hello!'\n" +
				"#Case\n" +
				"2\n" +
				"#Action;#Greeting\n" +
				"Print;'Bye!'\n" +
				"#EndSwitch#}} \n" +
				"Logical data type which equals true is transferred into field  switch \n" +
				"The Case will be performed, which field's expression will returns true." +
				"{{#\n" +
				"#Id;#Let\n" +
				"expression;'Annoucements(425)'\n" +
				"#Switch\n" +
				"true\n" +
				"#Case\n" +
				"expression.matches('[\\\\w|\\\\s]+[(]425{1}[)]')\n" +
				"#Action;#result\n" +
				"Print;true\n" +
				"#Case\n" +
				"expression.matches('[\\\\w|\\\\s]+[(]455{1}[)]')\n" +
				"#Action;#result\n" +
				"Print;false\n" +
				"#EndSwitch#}}",
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
	public Case()
	{
		super();
		this.variant = new Parameter(Tokens.Case.get(),	null); 
	}
	
	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Case clone = (Case) super.clone();
		clone.variant = variant.clone();
		return clone;
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

	private Parameter variant;
}
