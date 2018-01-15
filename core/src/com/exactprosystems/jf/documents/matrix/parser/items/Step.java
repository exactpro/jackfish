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
import java.util.stream.Collectors;

@MatrixItemAttribute(
		constantGeneralDescription = R.STEP_DESCRIPTION,
		constantExamples = R.STEP_EXAMPLE,
		shouldContain 	= { Tokens.Step },
		mayContain 		= { Tokens.IgnoreErr, Tokens.Id, Tokens.RepOff, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.Depends },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {TestCase.class}
	)
public class Step extends MatrixItem
{
	private Parameter            identify;
	private MutableValue<String> kind;
	private Parameter            plugin;
	private MutableValue<String> depends;

	public Step()
	{
		super();
		this.kind = new MutableValue<>();
		this.plugin = new Parameter(Tokens.For.get(), null);
		this.identify = new Parameter(Tokens.Step.get(), null);
		this.depends = new MutableValue<>();
	}

	public Step(Step step)
	{
		this.identify = new Parameter(step.identify);
		this.kind = new MutableValue<>(step.kind);
		this.plugin = new Parameter(step.plugin);
		this.depends = new MutableValue<>(step.depends);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Step(this);
	}

	//region Getters / Setters
	@Override
	public Object get(Tokens key)
	{
		switch (key)
		{
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
			case Kind:
				this.kind.accept((String)value);
				break;
			case Depends:
				this.depends.accept((String)value);
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
				|| this.identify.isChanged()
				|| this.kind.isChanged()
				|| this.depends.isChanged()
				|| super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.identify.saved();
		this.kind.saved();
		this.plugin.saved();
		this.depends.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	public String toString()
	{
		String s = this.identify.getExpression();
		return Str.IsNullOrEmpty(s) ? super.toString() : s;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.identify.setExpression(systemParameters.get(Tokens.Step));
		this.kind.accept(systemParameters.get(Tokens.Kind));
		this.plugin.setExpression(systemParameters.get(Tokens.For));
		this.depends.accept(systemParameters.get(Tokens.Depends));
		super.ignoreErr.accept(false);
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Step.get(), this.identify.getExpression());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Kind.get(), this.kind.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.For.get(), this.plugin.getExpression());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Depends.get(), this.depends.get());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndStep.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Step.get(), what, caseSensitive, wholeWord) 
				|| SearchHelper.matches(this.depends.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.plugin.getExpression(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.identify.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, super.id, super.id, () -> super.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.Step.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 2, Tokens.Step.get(), this.identify, this.identify, null, null, null, null);

		driver.showLabel(this, layout, 2, 0, Tokens.Depends.get() + ":");
		driver.showAutoCompleteBox(this, layout, 2, 1, () -> super.listOfTopIds(Step.class, Arrays.asList(TestCase.class, SubCase.class)), this.depends, this.depends);
		driver.showLabel(this, layout, 2, 2, " Screenshot:");
        driver.showComboBox(this, layout, 2, 3, this.kind, this.kind,
				() -> Arrays.stream(ScreenshotKind.values()).map(Enum::toString).collect(Collectors.toList()),
				str -> true);
		driver.showLabel(this, layout, 2, 4, " Plugin:");
		driver.showExpressionField(this, layout, 2, 5, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);

		driver.showToggleButton(this, layout, 1, 3,
				b -> driver.hide(this, layout, 2, b),
				b -> (b ? "Hide" : "Show") + " additional",
				!((this.kind.isNullOrEmpty() || this.kind.get().equals(ScreenshotKind.Never.name())) && this.depends.isNullOrEmpty() && this.plugin.isExpressionNullOrEmpty()));

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + Str.asString(this.identify.getValue());
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		this.identify.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected void beforeReport(ReportBuilder report, Context context)
	{
		super.beforeReport(report, context);
		report.putMark(this.id.get());
		AbstractEvaluator evaluator = context.getEvaluator();
		this.identify.evaluate(evaluator);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		super.changeExecutingState(MatrixItemExecutingState.Executing);

		Variables locals = evaluator.createLocals();
		ReturnAndResult returnAndResult = null;
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
				MatrixItem parent = super.findParent(TestCase.class);
				if (parent instanceof TestCase)
				{
					row.put(Context.testCaseIdColumn, parent.getId());
					row.put(Context.testCaseColumn, parent);
				}
				row.put(Context.stepIdColumn, this.getId());
				row.put(Context.stepColumn, this);
			}

			if (!this.identify.evaluate(evaluator))
			{
				returnAndResult = new ReturnAndResult(start, Result.StepFailed, this.identify.getValueAsString(), ErrorKind.WRONG_PARAMETERS, this);
				this.updateTable(table, position, row, returnAndResult, returnAndResult.getError());
				super.changeExecutingState(MatrixItemExecutingState.Failed);
				return returnAndResult;
			}
			Object identifyValue = this.identify.getValue();
			if (row != null)
			{
				row.put(Context.stepIdentityColumn, "" + identifyValue);
			}

			if (!Str.IsNullOrEmpty(this.depends.get()))
			{
				//check that we can found Step with id depends.get()
				List<MatrixItem> topItems = super.listOfTopItems(Arrays.asList(TestCase.class, SubCase.class));
				Optional<MatrixItem> dependsStep = topItems.stream().filter(item -> item.getClass() == Step.class && Objects.equals(item.getId(), this.depends.get())).findFirst();

				//if step with id depends.get() is not presented - return fail
				if (!dependsStep.isPresent())
				{
					returnAndResult = new ReturnAndResult(start, Result.StepFailed, String.format(R.STEP_NOT_FOUND_EXCEPTION.get(), this.depends.get()), ErrorKind.FAIL, this);
					this.updateTable(table, position, row, returnAndResult, returnAndResult.getError());
					super.changeExecutingState(MatrixItemExecutingState.Failed);
					return returnAndResult;
				}

				//else check that depends step is failed. Is the step is failed, return fail
				MatrixItem step = dependsStep.get();
				if (step.result != null && step.result.getResult().isFail())
				{
					returnAndResult = new ReturnAndResult(start, Result.StepFailed, String.format(R.STEP_FAILED_EXCEPTION.get(), this.depends.get()), ErrorKind.FAIL, this);
					this.updateTable(table, position, row, returnAndResult, returnAndResult.getError());
					super.changeExecutingState(MatrixItemExecutingState.Failed);
					return returnAndResult;
				}
			}

			this.plugin.evaluate(evaluator);
			super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			super.doShowPopup(showPopups, context, "started", Notifier.Info);

			report.outLine(this, null, String.format("Step %s", identifyValue), null);

			returnAndResult = context.runHandler(start, context, listener, this, HandlerKind.OnStepStart, report, null, null);

			if (!returnAndResult.getResult().isFail())
			{
				returnAndResult = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});
			}

			if (returnAndResult.getResult().isFail())
			{
				this.plugin.evaluate(evaluator);
				super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnError, ScreenshotKind.OnStartOrError, ScreenshotKind.OnFinishOrError);

				MatrixError error = returnAndResult.getError();

				super.doShowPopup(showPopups, context, "error: " + error, Notifier.Error);

				returnAndResult = context.runHandler(start, context, listener, this, HandlerKind.OnStepError, report, error, super.find(false, OnError.class, null));
			}
			else
			{
				returnAndResult = context.runHandler(start, context, listener, this, HandlerKind.OnStepFinish, report, null, null);
			}

			this.plugin.evaluate(evaluator);
			super.doScreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnFinish, ScreenshotKind.OnFinishOrError);
			this.updateTable(table, position, row, returnAndResult, returnAndResult.getError());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			this.updateTable(table, position, row, returnAndResult, new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
			super.changeExecutingState(MatrixItemExecutingState.Failed);
			return new ReturnAndResult(start, Result.StepFailed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
			evaluator.setLocals(locals);
		}
		super.changeExecutingState(returnAndResult.getResult().isFail() ? MatrixItemExecutingState.Failed : MatrixItemExecutingState.Passed);
		return returnAndResult.getResult() == Result.Failed ? new ReturnAndResult(start, returnAndResult, Result.StepFailed) : returnAndResult;
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
