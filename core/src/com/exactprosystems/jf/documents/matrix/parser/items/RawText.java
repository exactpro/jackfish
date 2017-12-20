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
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Text;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		constantGeneralDescription = R.RAW_TEXT_DESCRIPTION,
		constantExamples = R.RAW_TEXT_EXAMPLE,
		shouldContain 	= { Tokens.RawText }, 
		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff, Tokens.Global, Tokens.Kind},
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real 			= true, 
		hasValue 		= true, 
		hasParameters 	= false, 
		hasChildren 	= false,
		raw 			= true,
		seeAlsoClass 	= {RawTable.class}
)
public class RawText extends MatrixItem
{
	private static final String RAW_PREFIX = "~";

	private MutableValue<String> highlighterMutableValue;
	private MutableValue<String> description;
	private Text                 text;
	private boolean firstUsing = true;

	public RawText()
	{
		super();
		this.text = new Text();
		this.text.setChangeListener(flag -> super.owner.getChangedProperty().accept(flag));
		this.description = new MutableValue<>();
		this.highlighterMutableValue = new MutableValue<>(Highlighter.None.name());
	}

	/**
	 * copy constructor
	 */
	public RawText(RawText rt)
	{
		this.highlighterMutableValue = new MutableValue<>(rt.highlighterMutableValue);
		this.description = new MutableValue<>(rt.description);
		this.text = new Text(rt.text);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new RawText(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.text.isChanged() || this.description.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.text.saved();
		this.description.saved();
	}
	//endregion

	//region override from MatrixItem

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, super.id, super.id, () -> super.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.RawText.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.description, this.description, () -> this.description.get());
		driver.showCheckBox(this, layout, 1, 3, "Global", super.global, super.global);
		driver.showTextArea(this, layout, 2, 0, this.text, list -> {
			List<String> oldList = new ArrayList<>(this.text.subList(0, this.text.size()));
			if (list.equals(oldList))
			{
				return;
			}
			Command undo = () ->
			{
				this.text.clear();
				this.text.addAll(oldList);
				driver.updateTextArea(this, layout, this.text);
			};

			Command redo = () ->
			{
				this.text.clear();
				this.text.addAll(list);
				driver.updateTextArea(this, layout, this.text);
			};
			super.owner.addCommand(undo, redo);

		}, Highlighter.byName(this.highlighterMutableValue.get()));
		driver.showToggleButton(this, layout, 1, 4,
				b -> driver.hide(this, layout, 2, b),
				b -> b ? "Hide" : "Show", !this.text.isEmpty());

		driver.showLabel(this, layout, 1, 5, "Highlighting:");
		driver.showComboBox(this, layout, 1, 6, newValue -> {
					if (newValue != null)
					{
						this.highlighterMutableValue.accept(newValue);
						driver.displayHighlight(layout, Highlighter.byName(newValue));
					}
				}, this.highlighterMutableValue,
				() -> Arrays.stream(Highlighter.values()).map(Highlighter::name).collect(Collectors.toList()), str -> true);

		return layout;
	}

	@Override
	public void processRawData(String[] str)
	{
		if (this.firstUsing)
		{
			this.text = new Text();
			this.text.setChangeListener(flag -> Optional.ofNullable(super.owner).ifPresent(own -> own.getChangedProperty().accept(flag)));
			this.firstUsing = false;
			this.text.add(this.extractData(str));
			return;
		}

		this.text.add(extractData(str));
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.description.get() == null ? "" : this.description.get());
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
        this.description.accept(systemParameters.get(Tokens.RawText));
		this.highlighterMutableValue.accept(systemParameters.getOrDefault(Tokens.Kind, Highlighter.None.name()));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "TEXT";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.RawText.get(), this.description.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Kind.get(), this.highlighterMutableValue.get());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		try
		{
			for (String str : this.text)
			{
				writer.writeRecord(new String[]{indent + RAW_PREFIX, "\"" + str.replace("\"", "\"\"") + "\""}, true);
			}
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}

		super.addParameter(line, TypeMandatory.System, Tokens.EndRawText.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.text.toString(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.description.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.text.report(report, null, this.description.get());
			Variables vars = super.isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
			
			ReturnAndResult ret = new ReturnAndResult(start, Result.Passed, new Text(this.text));

			if (!super.id.isNullOrEmpty())
			{
				vars.set(super.getId(), ret.getOut());
			}

			return ret;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
	}

	//endregion
	
	//region Private members
	private String extractData(String[] str)
	{
		if (str == null || str.length == 0)
		{
			return "";
		}
		if (str.length > 1 && str[0].endsWith(RAW_PREFIX))
		{
			return str[1];
		}
		return str[0];
	}
	
	//endregion
}
