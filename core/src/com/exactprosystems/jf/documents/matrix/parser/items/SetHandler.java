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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.Context.EntryPoint;
import com.exactprosystems.jf.documents.config.HandlerKind;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		constantGeneralDescription = R.SET_HANDLER_DESCRIPTION,
		constantExamples = R.SET_HANDLER_EXAMPLE,
		shouldContain 	= { Tokens.SetHandler },
		mayContain 		= { Tokens.Off, Tokens.RepOff, Tokens.Kind },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false,
		seeAlsoClass 	= {SubCase.class, Step.class, TestCase.class}
)
public final class SetHandler extends MatrixItem 
{
	private MutableValue<String> name;
	private MutableValue<String> kind;

	public SetHandler()
	{
		super();
		this.name = new MutableValue<>();
        this.kind = new MutableValue<>();
	}

	public SetHandler(SetHandler handler)
	{
		this.name = new MutableValue<>(handler.name);
		this.kind = new MutableValue<>(handler.kind);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new SetHandler(this);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.SetHandler.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.SetHandler.get(), this.name, this.name,
			(str) -> 
			{
				String res = DialogsHelper.selectFromList(R.COMMON_CHOOSE_SUB_CASE.get(), new ReadableValue(str), context.subcases(this)).getValue();
				return res;
			},
			(str) -> 
			{ 
			    EntryPoint entryPoint = context.referenceToSubcase(str, this);
				driver.setCurrentItem(entryPoint.getSubCase(), entryPoint.getMatrix(), false);
				return str;
			}, null, 'G' );
		
		driver.showLabel(this, layout, 1, 2, Tokens.Kind.get());
        driver.showComboBox(this, layout, 1, 3, this.kind, this.kind, () ->
        {
            return Arrays.stream(HandlerKind.values()).map(k -> k.toString()).collect(Collectors.toList());
        }, (str) -> true);

		return layout;
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
		return super.getItemName() + " " + this.name + " " + this.kind;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.SetHandler)); 
		this.kind.set(systemParameters.get(Tokens.Kind)); 
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.SetHandler.get(), this.name.get());
        addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Kind.get(), this.kind.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return 
                SearchHelper.matches(Tokens.SetHandler.get(), what, caseSensitive, wholeWord) ||
                SearchHelper.matches(Tokens.Kind.get(), what, caseSensitive, wholeWord) ||
                SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord) ||
		        SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			if (Str.IsNullOrEmpty(this.kind.get()))
			{
				return new ReturnAndResult(start, Result.Failed, "Kind is null", ErrorKind.EMPTY_PARAMETER, this);
			}
		    HandlerKind handlerKind = HandlerKind.valueOf(this.kind.get());
		    context.setHandler(handlerKind, this.name.get(), this);

			return new ReturnAndResult(start, Result.Passed, null);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}	

	//==============================================================================================
	// Private members
	//==============================================================================================
}
