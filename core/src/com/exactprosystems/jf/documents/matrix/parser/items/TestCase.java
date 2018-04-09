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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.HandlerKind;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.util.*;

@MatrixItemAttribute(
		constantGeneralDescription = R.TEST_CASE_DESCRIPTION,
		constantExamples = R.TEST_CASE_EXAMPLE,
		shouldContain 	= { Tokens.TestCase },
		mayContain 		= { Tokens.Id, Tokens.RepOff, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.Depends },
		parents			= { MatrixRoot.class },
		closes			= MatrixRoot.class,
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
		hasChildren 	= true,
		seeAlsoClass 	= {Step.class}
	)
public final class TestCase extends MatrixItem
{
	private final Parameter            plugin;
	private final MutableValue<String> name;
	private final MutableValue<String> kind;
	private final MutableValue<String> depends;

	public TestCase()
	{
		super();
		this.plugin = new Parameter(Tokens.For.get(), null);
		this.name = new MutableValue<>();
		this.kind = new MutableValue<>();
		this.depends = new MutableValue<>();
	}

	public TestCase(String name)
	{
		this();
		this.name.accept(name);
		this.kind.accept(ScreenshotKind.Never.name());
	}

	public TestCase(TestCase testCase)
	{
		this.plugin = new Parameter(testCase.plugin);
		this.name = new MutableValue<>(testCase.name);
		this.kind = new MutableValue<>(testCase.kind);
		this.depends = new MutableValue<>(testCase.depends);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new TestCase(this);
	}

	//region Getter / Setter
	@Override
	public Object get(Tokens key)
	{
		switch (key)
		{
			case TestCase:
				return this.name.get();
			case Kind:
				return this.kind.get();
			case Depends:
				return this.depends.get();
			default:
				return super.get(key);
		}
	}

	@Override
	public void set(Tokens key, Object value)
	{
		switch (key)
		{
			case TestCase:
				this.name.accept((String) value);
				break;
			case Kind:
				this.kind.accept((String) value);
				break;
			case Depends:
				this.depends.accept((String) value);
				break;
			default:
				super.set(key, value);
		}
	}
	//endregion

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.plugin.isChanged()
				|| this.name.isChanged()
				|| this.kind.isChanged()
				|| this.depends.isChanged()
				|| super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.plugin.saved();
		this.name.saved();
		this.kind.saved();
		this.depends.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, super.id, super.id, () -> super.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.TestCase.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);
		driver.showLabel(this, layout, 2, 0, Tokens.Depends.get() + ":");
		driver.showAutoCompleteBox(this, layout, 2, 1, () -> super.listOfTopIds(TestCase.class, Collections.singletonList(MatrixRoot.class)), this.depends, this.depends);
		driver.showLabel(this, layout, 2, 2, "Screenshot:");
		driver.showComboBox(this, layout, 2, 3, this.kind, this.kind, () ->
		{
			List<String> list = ScreenshotKind.names();
			list.add(0, "");
			return list;
		}, str -> true);
		driver.showLabel(this, layout, 2, 4, "Plugin:");
		driver.showExpressionField(this, layout, 2, 5, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);
		driver.showToggleButton(this, layout, 1, 3, b -> driver.hide(this, layout, 2, b),
				b -> (b ? "Hide" : "Show") + " additional",
				!((this.kind.isNullOrEmpty() || this.kind.get().equals(ScreenshotKind.Never.name())) && this.depends.isNullOrEmpty() && this.plugin.isExpressionNullOrEmpty()));

		return layout;
	}

	public String getName()
	{
		return this.name.get();
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + "  " +
				(this.name.get() == null ? "" : "(" + this.name + ")") +
				(this.kind.get() == null ? "" : (" Screenshot: " + this.kind + ""));
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.plugin.setExpression(systemParameters.get(Tokens.For));
		this.name.accept(systemParameters.get(Tokens.TestCase));
		this.kind.accept(systemParameters.get(Tokens.Kind));
		this.depends.accept(systemParameters.get(Tokens.Depends));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.TestCase.get(), this.name.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Kind.get(), this.kind.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Depends.get(), this.depends.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.For.get(), this.plugin.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.TestCase.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.plugin.getExpression(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.depends.get(), what,caseSensitive, wholeWord);
	}

	@Override
	protected void beforeReport(ReportBuilder report, Context context)
	{
		super.beforeReport(report, context);
		report.putMark(this.id.get());
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		super.changeExecutingState(MatrixItemExecutingState.Executing);
		Variables locals = evaluator.createLocals();
		ReturnAndResult ret = null;
		Table table = context.getTable();
		int position = -1;
		RowTable row = null;
		if (!super.isRepOff())
		{
			position = table.size();
			row = table.addNew();
		}

		super.screenshot = null;
		try
		{
			Settings settings = context.getFactory().getSettings();
			String kindStr = this.kind.get();
			if (Str.IsNullOrEmpty(kindStr))
			{
				kindStr = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_DEFAULT_SCREENSHOT).getValue();
			}
			ScreenshotKind screenshotKind = ScreenshotKind.valueByName(kindStr);
			String str = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_POPUPS).getValue();
			boolean showPopups = Boolean.parseBoolean(str);

			if (row != null)
			{
				row.put(Context.matrixColumn, this.owner.getNameProperty().get());
				row.put(Context.testCaseIdColumn, super.getId());
				row.put(Context.testCaseColumn, this);
			}

			if (!Str.IsNullOrEmpty(this.depends.get()))
			{
				//check that we can found TestCase with id depends.get()
				List<MatrixItem> topItems = super.listOfTopItems(Collections.singletonList(MatrixRoot.class));
				Optional<MatrixItem> dependsTestCase = topItems.stream().filter(item -> item.getClass() == TestCase.class && Objects.equals(item.getId(), this.depends.get())).findFirst();

				//if TestCase with id depends.get() is not presented - return fail
				if (!dependsTestCase.isPresent())
				{
					ret = new ReturnAndResult(start, Result.Failed, String.format(R.TEST_CASE_NOT_FOUND_EXCEPTION.get(), this.depends.get()), ErrorKind.FAIL, this);
					this.updateTable(table, position, row, ret, ret.getError());
					super.changeExecutingState(MatrixItemExecutingState.Failed);
					return ret;
				}

				//else check that depends testCase is failed. Is the testCase is failed, return fail
				MatrixItem testCase = dependsTestCase.get();
				if (testCase.result != null && testCase.result.getResult().isFail())
				{
					ret = new ReturnAndResult(start, Result.Failed, String.format(R.TEST_CASE_FAILED_EXCEPTION.get(), this.depends.get()), ErrorKind.FAIL, this);
					this.updateTable(table, position, row, ret, ret.getError());
					super.changeExecutingState(MatrixItemExecutingState.Failed);
					return ret;
				}
			}

			this.plugin.evaluate(evaluator);
			super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			super.doShowPopup(showPopups, context, "started", Notifier.Info);

			ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseStart, report, null, null);

			if (!ret.getResult().isFail())
			{
				ret = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});
			}

			if (ret.getResult().isFail())
			{
				this.plugin.evaluate(evaluator);
				super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnError, ScreenshotKind.OnStartOrError, ScreenshotKind.OnFinishOrError);
				MatrixError error = ret.getError();

				super.doShowPopup(showPopups, context, "error: " + error, Notifier.Error);

				ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseError, report, error, super.find(false, OnError.class, null));
			}
			else
			{
				ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseFinish, report, null, null);
			}

			this.plugin.evaluate(evaluator);
			super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnFinish, ScreenshotKind.OnFinishOrError);
			this.updateTable(table, position, row, ret, ret.getError());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			this.updateTable(table, position, row, ret, new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
			super.changeExecutingState(MatrixItemExecutingState.Failed);
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
			evaluator.setLocals(locals);
		}
		super.changeExecutingState(ret.getResult().isFail() ? MatrixItemExecutingState.Failed : MatrixItemExecutingState.Passed);
		return ret;
	}

	@Override
	public String toString()
	{
		String s = this.getName();
		return Str.IsNullOrEmpty(s) ? super.toString() : s;
	}

	//endregion

	//region private methods
	private void updateTable(Table table, int position, RowTable row, ReturnAndResult ret, MatrixError error)
	{
		if (table != null && position >= 0 && row != null)
		{
			row.put(Context.timeColumn, ret.getTime());
			row.put(Context.resultColumn, ret.getResult().isFail() ? Result.Failed : ret.getResult());
			row.put(Context.errorColumn, error);
			table.updateValue(position, row);
		}
	}
	//endregion
}
