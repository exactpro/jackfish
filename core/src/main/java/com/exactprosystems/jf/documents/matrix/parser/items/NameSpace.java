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
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

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
	private final MutableValue<String> name;

	public NameSpace()
	{
		super();
		this.name = new MutableValue<>();
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

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.name.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.name.saved();
	}
	//endregion

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, super.id, super.id, () -> super.id.get(), null);
		driver.showTitle(this, layout, 1, 1, Tokens.NameSpace.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null, null);

		return layout;
	}

	//region Getters / setters
	public String getName()
	{
		return this.name.get();
	}

	@Override
	public Object get(Tokens key)
	{
		if (key == Tokens.NameSpace)
		{
			return this.name.get();
		}
		return super.get(key);
	}

	@Override
	public void set(Tokens key, Object value)
	{
		if (key == Tokens.NameSpace)
		{
			this.name.accept((String) value);
		}
		else
		{
			super.set(key, value);
		}
	}
	//endregion
	
	//region override from MatrixItem
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.name.isNullOrEmpty() ? "" : "(" + this.name + ")");
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.accept(systemParameters.get(Tokens.NameSpace));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.NameSpace.get(), this.name.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.NameSpace.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndNameSpace.get());
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		Variables locals = evaluator.createLocals();
		try
		{
			report.itemIntermediate(this);
			evaluator.getLocals().set(parameters.makeCopy());
			return super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
		finally
		{
			evaluator.setLocals(locals);
		}
	}
	//endregion
}
