////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.help.ActionsList;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 		= "Adds new action into matrix",
		shouldContain 		= { Tokens.Action},
		mayContain 			= { Tokens.Id, Tokens.Off, Tokens.Global, Tokens.IgnoreErr, Tokens.Assert, Tokens.AssertOutIs, Tokens.AssertOutIsNot },
		real				= true,
		hasValue 			= true,
		hasParameters 		= true,
		hasChildren 		= false
	)
public final class ActionItem extends MatrixItem
{
	public enum HelpKind { ChooseSaveFile, ChooseOpenFile, ChooseFolder, ChooseDateTime, ChooseFromList, BuildQuery, BuildXPath, BuildLayoutExpression }
	
	
	public ActionItem()
	{
		super();
		this.assertBool = new Parameter(Tokens.Assert.get(), null);
		this.assertOutIs = new Parameter(Tokens.AssertOutIs.get(), null);
		this.assertOutIsNot = new Parameter(Tokens.AssertOutIsNot.get(), null);
	}

	public ActionItem(String actionName) throws Exception
	{
		this();
		this.action = actionByName(actionName);
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		ActionItem clone = (ActionItem) super.clone();

		clone.assertBool = this.assertBool.clone();
		clone.assertOutIs = this.assertOutIs.clone();
		clone.assertOutIsNot = this.assertOutIsNot.clone();

		clone.action = action.clone();

		return clone;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + this.action.toString();
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 4);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id::set, this.id::get, () -> this.id.get() + ".Out");
		driver.showTitle(this, layout, 1, 1, Tokens.Action.get(), context.getFactory().getSettings());
		driver.showTitle(this, layout, 1, 2, getActionName(), context.getFactory().getSettings());
		driver.showParameters(this, layout, 1, 3, this.parameters, () -> this.id.get() + ".In.", false);
		driver.showCheckBox(this, layout, 2, 0, "Global", this.global, this.global);
		driver.showCheckBox(this, layout, 2, 1, "Ignore", this.ignoreErr, this.ignoreErr);
		long count = Arrays.asList(this.assertBool, this.assertOutIs, this.assertOutIsNot).stream()
				.map(p -> !p.isExpressionNullOrEmpty()).filter(b -> b).count();
		driver.showToggleButton(this, layout, 2, 2, "Asserts", b ->
		{
			driver.hide(this, layout, 3, b);
			return null;
		}, !(count == 0));
		driver.showLabel(this, layout, 3, 0, Tokens.Assert.get());
		driver.showExpressionField(this, layout, 3, 1, Tokens.Assert.get(), this.assertBool, this.assertBool, null, null, null, null);
		driver.showLabel(this, layout, 3, 2, Tokens.AssertOutIs.get());
		driver.showExpressionField(this, layout, 3, 3, Tokens.AssertOutIs.get(), this.assertOutIs, this.assertOutIs, null, null, null, null);
		driver.showLabel(this, layout, 3, 4, Tokens.AssertOutIsNot.get());
		driver.showExpressionField(this, layout, 3, 5, Tokens.AssertOutIsNot.get(), this.assertOutIsNot, this.assertOutIsNot, null, null, null, null);
		driver.hide(this, layout, 3, count == 0);
		return layout;
	}

	//==============================================================================================
	// Getters / setters
	//==============================================================================================
	public String getActionName()
	{
		return this.action != null ? this.action.getClass().getSimpleName() : "null";
	}

	public HelpKind howHelpWithParameter(Context context, String parameter) throws Exception
	{
		if (Str.IsNullOrEmpty(parameter))
		{
			return null;
		}
		
		return this.action.howHelpWithParameter(context, parameter, this.parameters);
	}

	public List<ReadableValue> listToFillParameter(Context context, String parameter) throws Exception
	{
		return this.action.listToFillParameter(context, parameter, this.parameters);
	}

	public Map<ReadableValue, TypeMandatory> helpToAddParameters(Context context) throws Exception
	{
		return this.action.helpToAddParameters(context, this.parameters);
	}

	public void setOwner()
	{
		if (this.action != null)
		{
			this.action.setOwner(this);
		}
	}

	public ActionGroups group()
	{
		return this.action.getClass().getAnnotation(ActionAttribute.class).group();
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (	this.assertBool.isChanged()
    		||	this.assertOutIs.isChanged()
    		||	this.assertOutIsNot.isChanged() )
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.assertBool.saved();
    	this.assertOutIs.saved();
    	this.assertOutIsNot.saved();
    }
	
	//==============================================================================================
	// Protected members should be overridden
	//==============================================================================================
	@Override
	public void correctParametersType()
	{
		this.action.correctParametersType(this.parameters);
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " (" + this.getActionName() + ")";
	}

	@Override
	public void addKnownParameters()
	{
		if (this.action != null)
		{
			this.action.addParameters(this.parameters);
		}
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		String actionName = systemParameters.get(Tokens.Action);
		this.assertBool.setExpression(systemParameters.get(Tokens.Assert));
		this.assertOutIs.setExpression(systemParameters.get(Tokens.AssertOutIs));
		this.assertOutIsNot.setExpression(systemParameters.get(Tokens.AssertOutIsNot));

		try
		{
			if (this.action == null)
			{
				this.action = actionByName(actionName);
				this.action.setOwner(this);
			}
		}
		catch (Exception e)
		{
			throw new MatrixException(super.getNumber(), this, "Can not to find the action: " + actionName);
		}
	}

	@Override
	protected String itemSuffixSelf()
	{
		if (this.action != null)
		{
			String res = this.action.actionSuffix();
			return Str.IsNullOrEmpty(res) ? null : res;
		}
		return null;
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Action.get(), getActionName());

		if (!this.assertBool.isExpressionNullOrEmpty())
		{
			super.addParameter(firstLine, secondLine, Tokens.Assert.get(), this.assertBool.getExpression());
		}
		if (!this.assertOutIs.isExpressionNullOrEmpty())
		{
			super.addParameter(firstLine, secondLine, Tokens.AssertOutIs.get(), this.assertOutIs.getExpression());
		}
		if (!this.assertOutIsNot.isExpressionNullOrEmpty())
		{
			super.addParameter(firstLine, secondLine, Tokens.AssertOutIsNot.get(), this.assertOutIsNot.getExpression());
		}

		for (Parameter parameter : getParameters())
		{
			super.addParameter(firstLine, secondLine, parameter.getName(), parameter.getExpression());
		}
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
		this.action.doDocumentation(context, report);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, ids, parameters);

		this.action.checkAction(listener, this, parameters);
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		this.action.initDefaultValues();
		Result result = this.action.doAction(context, evaluator, report, parameters, super.getId(), this.assertBool, this.assertOutIs, this.assertOutIsNot);

		return new ReturnAndResult(result, this.action.getOut(), this.action.getReason());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.action.getClass().getSimpleName(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.assertBool.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.assertOutIs.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.assertOutIsNot.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(Tokens.Action.get(), what, caseSensitive, wholeWord) ||
				getParameters().matches(what, caseSensitive, wholeWord);
	}

	//==============================================================================================
	// Private members
	//==============================================================================================
	private AbstractAction actionByName(String actionName) throws Exception
	{
		Class<?> found = null;
		for (Class<?> type : ActionsList.actions)
		{
			if (type.getSimpleName().equals(actionName))
			{
				found = type;
				break;
			}
		}
		if (found == null)
		{
			throw new Exception("Action with name " + actionName + " is unknown.");
		}

		AbstractAction ret = (AbstractAction) found.newInstance();

		return ret;
	}

	private AbstractAction action;

	private Parameter assertBool;

	private Parameter assertOutIs;

	private Parameter assertOutIsNot;
}