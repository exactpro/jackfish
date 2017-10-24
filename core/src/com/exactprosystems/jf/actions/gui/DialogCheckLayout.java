////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					   = ActionGroups.GUI,
		constantGeneralDescription = R.DIALOG_CHECK_LAYOUT_GENERAL_DESC,
		additionFieldsAllowed 	   = true,
		suffix 					   = "DLGCL",
		outputType = Table.class
	)
public class DialogCheckLayout extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";
	public final static String	doNotOpenName	= "DoNotOpen";
	public final static String	doNotCloseName	= "DoNotClose";
	public final static String	fieldsName		= "Fields";
	public final static String  tableName		= "Table";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_CHECK_LAYOUT_APP_CONNECTION)
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_CHECK_LAYOUT_DIALOG)
	protected String			dialog			= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_CHECK_LAYOUT_DO_NOT_OPEN)
	protected Boolean			doNotOpen;

	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_CHECK_LAYOUT_DO_NOT_CLOSE)
	protected Boolean			doNotClose;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_CHECK_LAYOUT_FIELDS)
	protected Map<String, Object> fields;

	@ActionFieldAttribute(name = tableName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_CHECK_LAYOUT_TABLE)
	protected Table table;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case dialogName:
			case doNotOpenName:
			case doNotCloseName:
				return HelpKind.ChooseFromList;
			case connectionName:
				return null;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			case doNotCloseName:
			case doNotOpenName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			default:
				break;
		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), false);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			throw new NullPointerException(String.format("Service with id '%s' not started yet", id));
		}
		IGuiDictionary dictionary = connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		Helper.throwExceptionIfDialogNull(window, this.dialog);
		Set<ControlKind> supportedControls = app.getFactory().supportedControlKinds();

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		Parameters controlMap;
		if (this.table != null)
		{
			controlMap = new Parameters();
			this.table.stream()
					.flatMap(row -> row.entrySet().stream())
					.filter(entry -> !(entry.getKey().equals(this.dialog)))
					.forEach(entry -> controlMap.add(entry.getKey(), Str.asString(entry.getValue())));

			controlMap.evaluateAll(context.getEvaluator());
		}
		else if (this.fields != null)
		{
			controlMap = new Parameters();
			this.fields.forEach((key, value) -> controlMap.add(key, "" + value));
			for (Parameter p : controlMap)
			{
				Object o = this.fields.get(p.getName());
				if (o instanceof Spec)
				{
					p.setValue(o);
				}
				else
				{
					p.evaluate(evaluator);
				}
			}
		}
		else
		{
			controlMap = parameters.select(TypeMandatory.Extra);
		}
		window.checkParams(controlMap.keySet());

		if (!this.doNotOpen)
		{
			SectionKind onOpen = SectionKind.OnOpen;
			logger.debug("Perform " + onOpen);
			for (IControl control : window.getSection(onOpen).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onOpen, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, res.getLocator(), "" + res.getValue()), ErrorKind.DIALOG_CHECK_LAYOUT);
					return;
				}
			}
		}
		
		boolean totalResult = true;
		Table resTable = null;
		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		for (Parameter parameter : controlMap)
		{
			String name = parameter.getName();
			Object obj = parameter.getValue();

			IControl control = sectionRun.getControlByIdAndValue(name, obj);
			if (control == null)
			{
				super.setError(message(id, window, run, null, null, "control with name '" + name + "' not found in the dictionary"), ErrorKind.LOCATOR_NOT_FOUND);
				return;
			}

			if (checkControl(supportedControls, control))
			{
				super.setError(message(id, window, run, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
				return;
			}
			
			CheckingLayoutResult res = control.checkLayout(service, window, obj);
			totalResult = totalResult && res.isOk();
			
			if (!res.isOk())
			{
				resTable = createTable(resTable, evaluator);
				for (CheckingLayoutResultBean bean : res.getNewErrors())
				{
					resTable.addValue(new Object[]{name, bean.getRelativeField(), bean.getRelation(), bean.getActual(), bean.getExpected()});
				}
			}
		}

		if (!totalResult)
		{
			super.setResult(resTable);
			super.setError(ErrorKind.DIALOG_CHECK_LAYOUT.toString(), ErrorKind.DIALOG_CHECK_LAYOUT);
			return;
		}

		
		if (!this.doNotClose)
		{
			SectionKind onClose = SectionKind.OnClose;
			logger.debug("Perform " + onClose);
			for (IControl control : window.getSection(onClose).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onClose, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, res.getLocator(), " returned 'false'. Process is stopped."), ErrorKind.DIALOG_CHECK_LAYOUT);
					return;
				}
			}
		}

		super.setResult(null);
	}

	private Table createTable(Table table, AbstractEvaluator evaluator)
	{
		if (table != null)
		{
			return table;
		}
		return new Table(new String[]{"Base field", "Relative field", "Relation", "Actual", "Expected"}, evaluator);
	}

	private ReportTable createTable(ReportTable table, ReportBuilder report)
	{
		if (table != null)
		{
			return table;
		}
		return report.addTable("Layout mismatching", null, true, true, new int[]{20, 20, 20, 20, 20}, "Base field", "Relative field", "Relation", "Actual", "Expected");
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control)
	{
		return !supportedControls.contains(control.getBindedClass());
	}


}
