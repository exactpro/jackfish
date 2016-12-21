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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					= ActionGroups.GUI, 
		suffix					= "DLGFLL",
		generalDescription 		= "Finds or opens dialog window, fills in fields and pushes buttons.", 
		additionFieldsAllowed 	= true, 
		outputDescription 		= "A map wich contains all text of requested fields.", 
		outputType 				= Map.class
	)
public class DialogFill extends AbstractAction
{
	public final static String	connectionName			= "AppConnection";
	public final static String	dialogName				= "Dialog";
	public final static String	doNotOpenName			= "DoNotOpen";
	public final static String	doNotCloseName			= "DoNotClose";
	public final static String	stopOnFailName			= "StopOnFail";
	public static final String	fieldsName				= "Fields";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection		connection			= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "A name of the dialog.")
	protected String			dialog				= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, description = "Do not open a new dialog.")
	protected Boolean			doNotOpen;
	
	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, description = "Do not close a dialog.")
	protected Boolean			doNotClose;
	
	@ActionFieldAttribute(name = stopOnFailName, mandatory = false, description = "Stop action on fail")
	protected Boolean			stopOnFail;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, description = "Map of control name : control operation.")
	protected Map<String, Object> fields;

	public DialogFill()
	{
	}
	
	@Override
	public void initDefaultValues() {
		stopOnFail	= true;
		doNotClose	= false;
		doNotOpen	= false;
		fields		= null;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case dialogName:
			case doNotOpenName:
			case doNotCloseName:
			case stopOnFailName:	
				return HelpKind.ChooseFromList;
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
			case stopOnFailName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
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
		if (connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			super.setError("Connection is not established", ErrorKind.APPLICATION_ERROR);
			return;
		}
		IGuiDictionary dictionary = connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		Set<ControlKind> supportedControls = new HashSet<>();
		supportedControls.addAll(Arrays.asList(app.getFactory().supportedControlKinds()));

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		Parameters controlMap;
		if (this.fields != null)
		{
			controlMap = new Parameters();
			Parameters finalControlMap = controlMap;
			this.fields.entrySet().forEach(entry -> finalControlMap.add(entry.getKey(), "" + entry.getValue()));
			controlMap.evaluateAll(context.getEvaluator());
		}
		else
		{
			controlMap = parameters.select(TypeMandatory.Extra);
		}
		window.checkParams(controlMap.keySet());
		service.startNewDialog();
		if (!this.doNotOpen)
		{
			SectionKind onOpen = SectionKind.OnOpen;
			logger.debug("Perform " + onOpen);
			for (IControl control : window.getSection(onOpen).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onOpen, control, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, "" + res.getValue()), ErrorKind.OPERATION_FAILED);
					return;
				}
			}
		}

		Map<String, Object> outValue = new LinkedHashMap<>();
		Map<String, MatrixError> errorsValue = new LinkedHashMap<>();

		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		String allReportErrors = "";
		for (Parameter parameter : controlMap)
		{
			String name = parameter.getName();
			Object obj = parameter.getValue();

			IControl control;
			if (name.isEmpty())
			{
				control = AbstractControl.createDummy();
			}
			else
			{
				control = sectionRun.getControlByIdAndValue(name, obj);
			}
			if (control == null)
			{
				String message = message(id, window, run, control, "is not allowed");
				errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
				super.setErrors(errorsValue);
				super.setError(message, ErrorKind.LOCATOR_NOT_FOUND);
				return;
			}

			if (checkControl(supportedControls, control))
			{
				String message = message(id, window, run, control, "is not allowed");
				errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
				super.setErrors(errorsValue);
				super.setError(message, ErrorKind.LOCATOR_NOT_FOUND);
				return;
			}

			try
			{
				OperationResult res = control.operate(service, window, obj);
				if (res.isOk())
				{
					Object value = res.getValue();
					if (value instanceof String[][])
					{
						value = new Table((String[][]) value, evaluator);

					}
					outValue.put(name, value);
				}
				else
				{
					String message = message(id, window, run, control, "" + res.getValue());
					errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
					if (this.stopOnFail)
					{
						super.setErrors(errorsValue);
						super.setError(message, ErrorKind.OPERATION_FAILED);
						return;
					}
					else
					{
						allReportErrors += message;
					}
				}
			}
			catch (ServerException e) // TODO disgusting code. we need to redo it any way.
			{
				//Todo i think, that all exception from remote side need be instance of JFRemoteException
				RemoteException t = (RemoteException) e.getCause();
				String mes = message(id, window, run, control, t.getMessage());

				if (!this.stopOnFail)
				{
					//TODO which error kind we need place here?
					errorsValue.put(name, new MatrixError(mes, ErrorKind.EXCEPTION, this.owner));
					allReportErrors += mes;
				}
				else
				{
					ErrorKind errorKind = ErrorKind.EXCEPTION;
					if (t instanceof ElementNotFoundException)
					{
						errorKind = ErrorKind.ELEMENT_NOT_FOUND;
					}
					else if (t instanceof OperationNotAllowedException)
					{
						errorKind = ErrorKind.OPERATION_NOT_ALLOWED;
					}
					else if (t instanceof NullParameterException)
					{
						errorKind = ErrorKind.EMPTY_PARAMETER;
					}
					errorsValue.put(name, new MatrixError(t.getMessage(), errorKind, owner));
					super.setErrors(errorsValue);
					super.setError(t.getMessage(), errorKind);
					return;
				}
				//can't throw exception. use setError and return
				//				throw t;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				if (this.stopOnFail)
				{

					String message = message(id, window, run, control, e.getMessage());
					errorsValue.put(name, new MatrixError(message, ErrorKind.EXCEPTION, owner));
					super.setErrors(errorsValue);
					super.setError(message, ErrorKind.EXCEPTION);
					return;
				}
				else
				{

					String message = message(id, window, run, control, e.getMessage());
					errorsValue.put(name, new MatrixError(message, ErrorKind.EXCEPTION, owner));
					allReportErrors += message;
				}
			}
		}

		if (!this.doNotClose)
		{
			SectionKind onClose = SectionKind.OnClose;
			logger.debug("Perform " + onClose);
			for (IControl control : window.getSection(onClose).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onClose, control, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, "" + res.getValue()), ErrorKind.OPERATION_FAILED);
					return;
				}
			}
		}

		super.setResult(outValue);
		if (!errorsValue.isEmpty())
		{
			super.setErrors(errorsValue);
		}
		if (!Str.IsNullOrEmpty(allReportErrors))
		{
			super.setError(allReportErrors, ErrorKind.MANY_ERRORS);
		}
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control) throws Exception
	{
		return !supportedControls.contains(control.getBindedClass());
	}

}
