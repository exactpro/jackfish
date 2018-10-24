/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.IErrorKind;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.error.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Xml;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.exactprosystems.jf.actions.gui.Helper.cutMessage;
import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 						  = ActionGroups.GUI,
		suffix						  = "DLGFLL",
		constantGeneralDescription    = R.DIALOG_FILL_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantOutputDescription 	  = R.DIALOG_FILL_OUTPUT_DESC,
		outputType 					  = Map.class,
	constantExamples				  =	R.DIALOG_FILL_EXAMPLE
	)
public class DialogFill extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String dialogName     = "Dialog";
	public static final String doNotOpenName  = "DoNotOpen";
	public static final String doNotCloseName = "DoNotClose";
	public static final String stopOnFailName = "StopOnFail";
	public static final String fieldsName     = "Fields";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_FILL_APP_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_FILL_DIALOG)
	protected String dialog;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_FILL_DO_NOT_OPEN)
	protected Boolean doNotOpen;

	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_FILL_DO_NOT_CLOSE)
	protected Boolean doNotClose;

	@ActionFieldAttribute(name = stopOnFailName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.DIALOG_FILL_STOP_ON_FAIL)
	protected Boolean stopOnFail;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_FILL_FIELDS)
	protected Map<String, Object> fields;

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
			default:
				return null;
		}
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
		IApplication app = Helper.getApplication(this.connection);
		String id = this.connection.getId();
		IRemoteApplication service = app.service();
		IGuiDictionary dictionary = app.getFactory().getDictionary();

		IWindow window = Helper.getWindow(dictionary, this.dialog);

        Set<ControlKind> supportedControls = app.getFactory().supportedControlKinds();

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		Parameters controlMap;
		if (this.fields != null)
		{
			controlMap = new Parameters();
			Parameters finalControlMap = controlMap;
			this.fields.forEach((key, value) -> finalControlMap.add(key, "" + value));
			for (Parameter parameter : controlMap)
			{
				Object o = this.fields.get(parameter.getName());
				if (o instanceof Operation)
				{
					parameter.setValue(o);
				}
				else
				{
					parameter.evaluate(evaluator);
				}
			}
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
					super.setError(message(id, window, onOpen, control, null, "is not supported"), ErrorKind.CONTROL_NOT_SUPPORTED);
					return;
				}

				OperationResult res = control.operate(service, window, null, evaluator);
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, res.getLocator(), "" + res.getValue()), ErrorKind.OPERATION_FAILED);
					return;
				}
			}
		}

		Map<String, Object> outValue = new LinkedHashMap<>();
		Map<String, MatrixError> errorsValue = new LinkedHashMap<>();

		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		StringBuilder allReportErrors = new StringBuilder();
		for (Parameter parameter : controlMap)
		{
			String name = parameter.getName();
			Object obj = parameter.getValue();

			IControl control;
			//TODO think about it
			if (name.isEmpty() || name.contains("dummy_") || name.contains("Dummy_"))
			{
				control = AbstractControl.createDummy();
			}
			else
			{
				logger.error(String.format("try to find control by id %s and value %s", name, obj));
				control = sectionRun.getControlByIdAndValue(name, obj);
				logger.error("Found control : " + control);
			}
			if (control == null)
			{
				String message = message(id, window, run, null, null, "control with name '" + name + "' not found in the dictionary");
				errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
				super.setErrors(errorsValue);
				super.setError(message, ErrorKind.LOCATOR_NOT_FOUND);

				logger.error("Error on found control");
				this.printExternalInformation(message, dictionary, sectionRun);

				return;
			}

			if (checkControl(supportedControls, control))
			{
				String message = message(id, window, run, control, null, "is not supported");
				errorsValue.put(name, new MatrixError(message, ErrorKind.CONTROL_NOT_SUPPORTED, this.owner));
				super.setErrors(errorsValue);
				super.setError(message, ErrorKind.CONTROL_NOT_SUPPORTED);
				return;
			}

			try
			{
				OperationResult res = control.operate(service, window, obj, evaluator);
				if (res.isOk())
				{
					Object value = res.getValue();
					if (value instanceof org.w3c.dom.Document)
					{
						value = new Xml((org.w3c.dom.Document) value);
					}
					if (value instanceof String[][])
					{
						value = new Table((String[][]) value, evaluator);
					}
					outValue.put(name, value);
				}
				else
				{
					String message = message(id, window, run, control, res.getLocator(), "" + res.getValue());
					logger.error("Error on operate");
					printExternalInformation(message, dictionary, sectionRun);
					errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
					if (this.stopOnFail)
					{
						super.setErrors(errorsValue);
						super.setError(message, ErrorKind.OPERATION_FAILED);
						return;
					}
					else
					{
						allReportErrors.append(message);
					}
				}
			}
			catch (JFRemoteException | JFException e)
			{
				logger.error(e.getMessage(), e);
				ErrorKind errorKind = e.getErrorKind();
				String msg = message(id, window, run, control, null, e.getMessage());
				errorsValue.put(name, new MatrixError(msg, errorKind, this.owner));
				if (!stopOnFail)
				{
					allReportErrors.append(msg);
				}
				else
				{
					super.setErrors(errorsValue);
					super.setError(cutMessage(msg), errorKind);
					return;
				}
			}
			catch (ServerException e)
			{
				logger.error(e.getMessage(), e);
				ErrorKind errorKind = ErrorKind.EXCEPTION;
				String msg = message(id, window, run, control, null, e.getMessage());
				if (e.getCause() != null)
				{
					if (e.getCause() instanceof IErrorKind)
					{
						errorKind = ((IErrorKind) e.getCause()).getErrorKind();
					}
					msg = message(id, window, run, control, null, e.getCause().getMessage());
				}

				errorsValue.put(name, new MatrixError(msg, errorKind, this.owner));
				if (!stopOnFail)
				{
					allReportErrors.append(msg);
				}
				else
				{
					super.setErrors(errorsValue);
					super.setError(cutMessage(msg), errorKind);
					return;
				}
			}
			catch (Exception e)
			{
				String msg = e.getMessage();
				logger.error(msg, e);
				String message = message(id, window, run, control, null, msg);
				errorsValue.put(name, new MatrixError(message, ErrorKind.EXCEPTION, owner));
				if (this.stopOnFail)
				{
					super.setErrors(errorsValue);
					super.setError(msg.length() > 35 ? message.split(" ")[0] + msg.substring(0, 35) + " ... See log for more details" : message, ErrorKind.EXCEPTION);
					return;
				}
				else
				{
					allReportErrors.append(message);
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
					super.setError(message(id, window, onClose, control, null, "is not supported"), ErrorKind.CONTROL_NOT_SUPPORTED);
					return;
				}

				OperationResult res = control.operate(service, window, null, evaluator);
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, res.getLocator(), "" + res.getValue()), ErrorKind.OPERATION_FAILED);
					return;
				}
			}
		}

		super.setResult(outValue);
		if (!errorsValue.isEmpty())
		{
			super.setErrors(errorsValue);
		}
		if (!Str.IsNullOrEmpty(allReportErrors.toString()))
		{
			super.setError(allReportErrors.toString(), ErrorKind.MANY_ERRORS);
		}
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control)
	{
		return !supportedControls.contains(control.getBindedClass());
	}

	private void printExternalInformation(String message, IGuiDictionary dictionary, ISection section) {
		logger.error("Print external information");
		logger.error("Message : " + message);
		logger.error("Dictionary : " + dictionary);
		logger.error("Section : " + section);
		for (String controlName : section.getControlsNames()) {
			logger.error("control from section : " + controlName);
		}
	}

}
