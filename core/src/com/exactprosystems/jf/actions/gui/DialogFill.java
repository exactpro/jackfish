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

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					= ActionGroups.GUI, 
		suffix					= "DLGFLL",
		generalDescription 		= "The purpose of this action is to work with control elements on the screen of the application under test."
				+ "  With its help the user can fill in the text boxes of the forms, manage the mouse and the keyboard and also get some parameters "
				+ "from the elements of the forms (names, attributes etc.) The action uses the dictionary containing the descriptions of forms, its elements"
				+ " and the rules for finding those elements on the screen.  In matrix editor this action can help to choose the elements which need to be "
				+ "dealt with from the dictionary.  For this, the user should choose 'Default app' on the Matrix editor control panel. The chosen application "
				+ "'is aware' of its dictionary and allows to work with it interactively by choosing a dialog from the list or indicating the elements to work with "
				+ "through 'All parameters' context menu for the specified dialog.  The elements can be of different types - buttons, text boxes, menus, table etc.  "
				+ "Each type of element can process only the operations pertaining to it.  Buttons can be clicked, but not filled with text, for example.  "
				+ "Besides, for each element the action is set by default. While being launched, the action draws the information about the dictionary from the "
				+ "connection with the application (AppConnection). The action's work starts with processing the 'OnOpen' section of the chosen dialog.  "
				+ "If there are some elements in this section then the default action will be performed for all of them one after another.  Usually the Wait "
				+ "element is found here which waits for the main container of the form to appear on the screen.  This allows to make sure the elements of the "
				+ "form are available for work.  In some cases - when the user is sure that the form is on the screen - the processing of this section can be canceled"
				+ " by setting DoNotOpen parameter in true.  By default this parameter has false value.  The same mechanism works when closing the form - the OnClose "
				+ "section is processed.  Usually it contains the Wait element which waits for the form's container to become invisible.  Processing of this section "
				+ "can be forbidden with DoNotClose parameter. Most work is performed with the Run section of the dialog in the dictionary.  All named elements "
				+ "(with ID attribute set) from this section can be used when working with DialogFill. The name of the element is set as parameter name.  "
				+ "The form's elements are processed in the order used in this action.  If the element is not virtual (like Wait), then it is first searched "
				+ "for on the screen and then the sequence of operations is performed, defined by the value of the parameter. The same element can be met "
				+ "several times.  For instance, you can click the button, input some value in the text field and click the same button again. In general, "
				+ "the expression is a sequence of operations.  There is a class for describing these sequences - Do.  For example Do.click() However, more complex "
				+ "sequences can be built for each element.  For a certain text field one can come up with such sequence: Do.text('test').delay(100).check('test')."
				+ "Here the operations are described in more detail:  [Do] if the expression doesn't represent the sequence of operations then for the specified "
				+ "element its default action is performed.  Therefore text fields can be just given string values which will be modified into a chain "
				+ "Do.text('your text') automatically.  If the value is not necessary for the operation, for example the default action for a button is just"
				+ " clicking, then this value is ignored.",
		additionFieldsAllowed 	= true, 
		outputDescription 		= "Associative array which displays the names of the elements processed by the action "
				+ "along with values received from the form, for example, as a result of the operation Do.get().  "
				+ "If some element in the action was used several times, only the last value will be returned."
				+ "Expressions are operations with elements.",
		outputType 				= Map.class,
		additionalDescription 	= "Parameter names assign names to the elements from Run section of the specified dialog.\n"
				+ "Expressions are operations with elements."
	)
public class DialogFill extends AbstractAction
{
	public final static String	connectionName			= "AppConnection";
	public final static String	dialogName				= "Dialog";
	public final static String	doNotOpenName			= "DoNotOpen";
	public final static String	doNotCloseName			= "DoNotClose";
	public final static String	stopOnFailName			= "StopOnFail";
	public static final String	fieldsName				= "Fields";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection		connection			= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "The name of the dialog being processed.")
	protected String			dialog				= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, def = DefaultValuePool.False, description = "Do not process the 'OnOpen' section of the dialog from the dictionary.  False by default.")
	protected Boolean			doNotOpen;
	
	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, def = DefaultValuePool.False, description = "Do not process the 'OnClose' section of the dialog from the dictionary.  False by default.")
	protected Boolean			doNotClose;
	
	@ActionFieldAttribute(name = stopOnFailName, mandatory = false, def = DefaultValuePool.True, description = "Stop in case of an error occurring in the processing of another element of this action. "
			+ " Otherwise the element's error code is logged into the output collection of matrix values, but the work of action is not interrupted. True by default.")
	protected Boolean			stopOnFail;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, def = DefaultValuePool.Null, description = "Is an associative array which can be used instead of parameters.  "
			+ "Array keys define the elements in the dictionary; the values are operations.  This parameter is designated more for writing "
			+ "frameworks working with dictionaries other than daily work.\n")
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
        if (window == null)
        {
            super.setError("Window " + this.dialog + " not found in the dictionary",ErrorKind.DIALOG_NOT_FOUND);
            return;
        }

        Set<ControlKind> supportedControls = app.getFactory().supportedControlKinds();

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		Parameters controlMap;
		if (this.fields != null)
		{
			controlMap = new Parameters();
			Parameters finalControlMap = controlMap;
			this.fields.forEach((key, value) -> finalControlMap.add(key, "" + value));
			controlMap.forEach(p -> p.setValue(this.fields.get(p.getName())));
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

				OperationResult res = control.operate(service, window, null);
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
		String allReportErrors = "";
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
				control = sectionRun.getControlByIdAndValue(name, obj);
			}
			if (control == null)
			{
				String message = message(id, window, run, null, null, "control with name '" + name + "' not found in the dictionary");
				errorsValue.put(name, new MatrixError(message, ErrorKind.LOCATOR_NOT_FOUND, this.owner));
				super.setErrors(errorsValue);
				super.setError(message, ErrorKind.LOCATOR_NOT_FOUND);
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
				OperationResult res = control.operate(service, window, obj);
				if (res.isOk())
				{
					Object value = res.getValue();
					if (value instanceof org.w3c.dom.Document)
					{
						value = new Xml((org.w3c.dom.Document)value);
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
				String mes = message(id, window, run, control, null, t.getMessage());

				ErrorKind errorKind = ErrorKind.EXCEPTION;
				if (t instanceof ElementNotFoundException)
				{
					errorKind = ErrorKind.ELEMENT_NOT_FOUND;
				}
				else if (t instanceof OperationNotAllowedException)
				{
					errorKind = ErrorKind.OPERATION_NOT_ALLOWED;
				}
				else if (t instanceof ControlNotSupportedException)
				{
					errorKind = ErrorKind.CONTROL_NOT_SUPPORTED;
				}
				else if (t instanceof FeatureNotSupportedException)
				{
					errorKind = ErrorKind.FEATURE_NOT_SUPPORTED;
				}
				else if (t instanceof NullParameterException)
				{
					errorKind = ErrorKind.EMPTY_PARAMETER;
				}

				if (!this.stopOnFail)
				{
					errorsValue.put(name, new MatrixError(mes, errorKind, this.owner));
					allReportErrors += mes;
				}
				else
				{
					errorsValue.put(name, new MatrixError(mes, errorKind, owner));
					super.setErrors(errorsValue);
					super.setError(mes, errorKind);
					return;
				}
				//can't throw exception. use setError and return
				//				throw t;
			}
			catch (Exception e)
			{
				String msg = e.getMessage();
				logger.error(msg, e);
				if (this.stopOnFail)
				{

					String message = message(id, window, run, control, null, msg);
					errorsValue.put(name, new MatrixError(message, ErrorKind.EXCEPTION, owner));
					super.setErrors(errorsValue);
					super.setError(msg.length() > 35 ? message.split(" ")[0] + msg.substring(0, 35) + " ... See log for more details" : message, ErrorKind.EXCEPTION);
					return;
				}
				else
				{

					String message = message(id, window, run, control, null, e.getMessage());
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
					super.setError(message(id, window, onClose, control, null, "is not supported"), ErrorKind.CONTROL_NOT_SUPPORTED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
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
