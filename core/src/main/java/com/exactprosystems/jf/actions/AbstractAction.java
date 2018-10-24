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

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.IErrorKind;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.HelpKind;
import org.apache.log4j.Logger;

import javax.lang.model.type.NullType;
import java.lang.reflect.Field;
import java.rmi.ServerException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAction
{
	protected static final Logger logger = Logger.getLogger(AbstractAction.class);

	private   Action     action;
	protected MatrixItem owner;

	public AbstractAction()
	{
		this.action = new Action();
		clearResults();
	}

	public static boolean additionFieldsAllow(MatrixItem item)
	{
		if (!(item instanceof ActionItem))
		{
			return item.getClass().getAnnotation(MatrixItemAttribute.class).hasParameters();
		}
		Class<? extends AbstractAction> clazz = ((ActionItem) item).getActionClass();
		ActionAttribute annotation = clazz.getAnnotation(ActionAttribute.class);
		return annotation.additionFieldsAllowed();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	//region Getters / Setters

	/**
	 * @return result from the action. If Result is null, it mean, that executing was failed
	 * @see Result
	 */
	public final Result getResult()
	{
		return this.action.Result;
	}

	/**
	 * @return a object value - result of executing the action
	 */
	public final Object getOut()
	{
		return this.action.Out;
	}

	/**
	 * @return String representation, why the action was failed
	 */
	public final String getReason()
	{
		return this.action.Reason;
	}

	/**
	 * @return ErrorKind for the failed action
	 * @see ErrorKind
	 */
	public final ErrorKind getErrorKind()
	{
		return this.action.Kind;
	}

	public void setOwner(MatrixItem owner)
	{
		if (this.action != null)
		{
			this.owner = owner;
		}
	}

	//endregion

	//region public methods

	/**
	 * Add all mandatory fields to parameters. Added parameters have empty value and type Mandatory.
	 */
	public void addParameters(Parameters parameters)
	{
		this.getFieldsAttributes().entrySet()
				.stream()
				.filter(entry -> !parameters.containsKey(entry.getKey()) && entry.getValue().attribute.mandatory())
				.forEach(entry ->
				{
					parameters.add(entry.getKey(), "");
					parameters.setType(parameters.size() - 1, TypeMandatory.Mandatory);
				});
	}

	/**
	 * Check the action for extra and missing parameters.<br>
	 * If these parameters are present, then will notify error on listener
	 */
	public final void checkAction(IMatrixListener listener, ActionItem owner, Parameters parameters)
	{
		List<String> extraFields = this.checkExtraFields(parameters.keySet());
		List<String> missingFields = this.checkMandatoryFields(parameters.keySet());

		if (!extraFields.isEmpty())
		{
			listener.error(owner.getMatrix(), owner.getNumber(), owner, "Extra parameters " + Arrays.toString(extraFields.toArray()));
		}

		if (!missingFields.isEmpty())
		{
			listener.error(owner.getMatrix(), owner.getNumber(), owner, "Missing parameters " + Arrays.toString(missingFields.toArray()));
		}
	}

	/**
	 * Execute the action.
	 * <p>
	 * After executing the action will check the assert field.
	 * If the action executing was failed, but assert return true, the action will has {@link Result#Passed}
	 * <p>
	 * If {@link Action#Result} is {@code null}, it mean, that the action works incorrectly and will throw exception
	 * @return result of executing.
	 *
	 * @see AbstractAction#getResult()
	 * @see AbstractAction#doRealAction(Context, ReportBuilder, Parameters, AbstractEvaluator)
	 */
	public final Result doAction(Context context, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters, String actionId, Parameter assertBool)
	{
		try
		{
			clearResults();
			this.action.In = parameters;
			evaluator.getLocals().set(Tokens.This.get(), this.action);
			if (!Str.IsNullOrEmpty(actionId))
			{
				Variables variables = owner.isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
				variables.set(actionId, this.action);
			}

			boolean parametersAreCorrect = parameters.evaluateAll(evaluator);
			this.reportParameters(report, parameters);
			if (parametersAreCorrect)
			{
				if (this.injectParameters(parameters))
				{
					//-------------------------------------------------------------
					// Do the action!
					//-------------------------------------------------------------
					this.doRealAction(context, report, parameters, evaluator);
					//-------------------------------------------------------------

					if (this.action.Result == null)
					{
						String str = "Action " + this.getClass() + " works incorrectly.";
						throw new Exception(str);
					}
				}
			}
			else
			{
				setError("Errors in parameter expressions", ErrorKind.EXPRESSION_ERROR);
			}
		}
		catch (ServerException e)
		{
			Throwable cause = e.getCause();
			if (cause instanceof IErrorKind)
			{
				logger.error(cause.getMessage(), cause);
				setError(cause.getMessage(), ((IErrorKind) cause).getErrorKind());
			}
			else
			{
				logger.error(e.getMessage(), e);
				setError("Exception occurred: " + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), ErrorKind.EXCEPTION);
			}
		}
		catch (JFRemoteException | JFException e)
		{
			logger.error(e.getMessage(), e);
			setError(e.getMessage(), e.getErrorKind());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			setError("Exception occurred: " + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), ErrorKind.EXCEPTION);
		}

		try
		{
			checkAsserts(evaluator, assertBool);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			setError("Exception occurred: " + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), ErrorKind.EXCEPTION);
		}

		this.reportResults(report, assertBool);

		evaluator.getLocals().delete(Tokens.This.get());
		return this.action.Result;
    }

	/**
	 * @return a suffix for the action. <br>
	 * If the action has output type is {@link NullType}, will returned empty string
	 */
	public final String actionSuffix()
	{
		return Optional.ofNullable(this.getClass().getAnnotation(ActionAttribute.class))
				.filter(annotation -> annotation.outputType() != NullType.class)
				.map(ActionAttribute::suffix)
				.orElse("");
	}

	/**
	 * Return type of help, how user can fill the passed parameter
	 * @param context a matrix context
	 * @param fieldName the name of parameter, for which should return kind of help
	 * @param parameters the list of all action parameters
	 *
	 * @return type of help, or null.
	 *
	 * @throws Exception if something went wrong
	 *
	 * @see HelpKind
	 */
	public final HelpKind howHelpWithParameter(Context context, String fieldName, Parameters parameters) throws Exception
	{
		if (parameters.containsKey(fieldName))
		{
			return howHelpWithParameterDerived(context, parameters, fieldName);
		}
		return null;
	}

	/**
	 * Return list of possible values, how we can fill the passed parameter
	 * @param context a matrix context
	 * @param parameterToFill the name of parameter, for which should return a list
	 * @param parameters all parameters from the action
	 *
	 * @return a list of possible values
	 *
	 * @throws Exception if something went wrong
	 */
	public final List<ReadableValue> listToFillParameter(Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();

		if (evaluator != null)
		{
			parameters.evaluateAll(evaluator);
			List<ReadableValue> res = new ArrayList<>();
			this.listToFillParameterDerived(res, context, parameterToFill, parameters);

			return res;
		}
		return Collections.emptyList();
	}

	/**
	 * Return map of all known parameters, which can used on the action
	 * @param context a matrix context
	 * @param parameters the list of all parameters from the action
	 *
	 * @return map of all known parameters
	 *
	 * @throws Exception if something went wrong
	 */
	public final Map<ReadableValue, TypeMandatory> helpToAddParameters(Context context, Parameters parameters) throws Exception
	{
		Map<ReadableValue, TypeMandatory> res = new LinkedHashMap<>();
		Map<String, FieldAndAttributes> map = this.getFieldsAttributes().entrySet()
				.stream()
				.filter(node -> !node.getValue().attribute.deprecated())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		for (Entry<String, FieldAndAttributes> entry : map.entrySet())
		{
			res.put(new ReadableValue(entry.getKey()), entry.getValue().attribute.mandatory() ? TypeMandatory.Mandatory : TypeMandatory.NotMandatory);
		}
		AbstractEvaluator evaluator = context.getEvaluator();

		if (evaluator != null)
		{
			List<ReadableValue> list = new ArrayList<>();
			parameters.evaluateAll(evaluator);
			this.helpToAddParametersDerived(list, context, parameters);
			list.forEach(value -> res.put(value, TypeMandatory.Extra));
		}
		return res;
	}

	public final void correctParametersType(Parameters parameters)
	{
		Map<String, FieldAndAttributes> fields = this.getFieldsAttributes();
		for (Parameter parameter : parameters)
		{
			FieldAndAttributes field = fields.get(parameter.getName());
			if (field != null)
			{
				if (field.attribute.mandatory())
				{
					parameter.setType(TypeMandatory.Mandatory);
				}
				else
				{
					parameter.setType(TypeMandatory.NotMandatory);
				}
			}
		}
	}

	/**
	 * Init default values ( via reflection) for all not mandatory fields in the action
	 */
	public final void initDefaultValues()
	{
		try
		{
			Map<String, FieldAndAttributes> attributes = this.getFieldsAttributes();
			for (FieldAndAttributes attr : attributes.values())
			{
				if (!attr.attribute.mandatory())
				{
					attr.field.setAccessible(true);
					attr.field.set(this, attr.attribute.def().getValue());
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	//endregion

	//region Protected members should be overridden
	//TODO think about removing throws Exception from here. It's unnecessary
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return null;
	}

	//TODO think about removing throws Exception from here. It's unnecessary
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
	}

	//TODO think about removing throws Exception from here. It's unnecessary
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
	}

	protected abstract void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception;
	//endregion

	//region Protected members for using
	protected final void setResult(Object out)
	{
		this.action.Out = out;
		this.action.Result = Result.Passed;
	}

	protected final void setError(String reason, ErrorKind kind)
	{
		if (Str.IsNullOrEmpty(this.action.Reason))
		{
			this.action.Reason = reason;
		}
		else
		{
			this.action.Reason = String.format("Previous result: %s%nPrevious reason: %s%n %s", this.action.Result, this.action.Reason, reason);
		}
		this.action.Result = Result.Failed;
		this.action.Kind = kind;
	}

	protected final void setErrors(Map<String, MatrixError> errors)
	{
		this.action.Errors = errors;
	}

	//endregion

	//region Private members
	private void reportParameters(ReportBuilder report, Parameters parameters)
	{
		if (!parameters.isEmpty())
		{
			ReportTable table = report.addTable("Input parameters", null, false, true, new int[]{20, 40, 40}, new String[]{"Parameter", "Expression", "Value"});
			parameters.forEach(parameter -> table.addValues(parameter.getName(), parameter.getExpression(), parameter.getValue()));
		}
	}

    private void reportResults(ReportBuilder report, Parameter assertBool)
    {
		if (!assertBool.isExpressionNullOrEmpty())
		{
			ReportTable assertTable = report.addTable("Assert", null, false, true, new int[]{60, 40}, new String[]{"Expression", "Value"});
			assertTable.addValues(assertBool.getExpression(), assertBool.getValue());
		}

		ReportTable resultTable = report.addTable("Results", null, false, true, new int[]{20, 80}, new String[]{"Parameter", "Value"});
		tableRowIfNotNull(resultTable, "Result", this.action.Result);
		tableRowIfNotNull(resultTable, "Error kind", this.action.Kind);
		tableRowIfNotNull(resultTable, "Reason", this.action.Reason.isEmpty() ? null : this.action.Reason);
		tableRowIfNotNull(resultTable, "Out", this.action.Out);
	}

	private void tableRowIfNotNull(ReportTable table, Object title, Object obj)
	{
		if (obj != null)
		{
			table.addValues(title, obj);
		}
	}

	private boolean injectParameters(Parameters parameters)
	{
		boolean allCorrect = true;

		Map<String, FieldAndAttributes> descriptions = this.getFieldsAttributes();
		for (Parameter parameter : parameters)
		{
			try
			{
				String name = parameter.getName();
				FieldAndAttributes description = descriptions.get(name);

				if (description != null)
				{
					parameter.correctType(description.field.getType());
					if (parameter.isValid()) // why allCorrect isn't changed if parameter is invalid?
					{
						Object value = parameter.getValue();
						if (value == null && description.attribute.mandatory())
						{
							setError(String.format("Mandatory parameter %s is empty", name), ErrorKind.EMPTY_PARAMETER);
							allCorrect = false;
						}
						else if (value == null && description.attribute.shouldFilled())
						{
							setError(String.format("NotMandatory parameter %s must be filled non null value or not presented", name), ErrorKind.EMPTY_PARAMETER);
							allCorrect = false;
						}
						else
						{
							description.field.setAccessible(true);
							description.field.set(this, value);
						}
					}
					else
					{
						setError(String.format("Parameter %s: %s", name, parameter.getValue()), ErrorKind.WRONG_PARAMETERS);
						allCorrect = false;
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				allCorrect = false;
			}
		}
		return allCorrect;
	}

	private Map<String, FieldAndAttributes> getFieldsAttributes()
	{
		Map<String, FieldAndAttributes> fieldAttributes = new HashMap<>();
		for (Field field : this.getClass().getDeclaredFields())
		{
			ActionFieldAttribute attr = field.getAnnotation(ActionFieldAttribute.class);
			if (attr != null)
			{
				fieldAttributes.put(attr.name(), new FieldAndAttributes(field, attr));
			}
		}
		return fieldAttributes;
	}

	private void checkAsserts(AbstractEvaluator evaluator, Parameter assertBool)
	{
		if (!assertBool.isExpressionNullOrEmpty())
		{
			if (!assertBool.evaluate(evaluator))
			{
				setError(String.format("%s error in expression: %s", Tokens.Assert, assertBool.getValueAsString()), ErrorKind.EXPRESSION_ERROR);
				return;
			}

			Object value = assertBool.getValue();
			if (value != null && value instanceof Boolean)
			{
				if (!(Boolean) value)
				{
					setError(assertBool.getExpression(), ErrorKind.ASSERT);
				}
				else
				{
					if (this.action.Result != Result.Passed)
					{
						setResult(this.action.Out);
					}
				}
			}
			else
			{
				setError(String.format("%s must have type of Boolean", Tokens.Assert), ErrorKind.EXPRESSION_ERROR);
			}
		}
	}

	private List<String> checkMandatoryFields(Set<String> fields)
	{
		return this.getFieldsAttributes().entrySet()
				.stream()
				.filter(entry -> entry.getValue().attribute.mandatory() && !fields.contains(entry.getKey()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	private List<String> checkExtraFields(Set<String> fields)
	{
		if (this.getClass().getAnnotation(ActionAttribute.class).additionFieldsAllowed())
		{
			return Collections.emptyList();
		}

		Map<String, FieldAndAttributes> descriptions = this.getFieldsAttributes();

		return fields.stream()
				.filter(name -> !descriptions.containsKey(name))
				.collect(Collectors.toList());
	}

	private void clearResults()
	{
		this.action.clearResults();
	}

	private class FieldAndAttributes
	{
		public Field                field;
		public ActionFieldAttribute attribute;

		private FieldAndAttributes(Field field, ActionFieldAttribute attributes)
		{
			this.field = field;
			this.attribute = attributes;
		}
	}

	public class Action
	{
		public Object                   Out;
		public Parameters               In;
		public String                   Reason;
		public ErrorKind                Kind;
		public Result                   Result;
		public Map<String, MatrixError> Errors;

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder(getClass().getSimpleName());
			sb.append("{ ");
			if (this.Result != null)
			{
				sb.append("Result=").append(this.Result).append(" ");
			}
			if (this.Out != null)
			{
				sb.append("Out=").append(this.Out).append(" ");
			}
			if (this.Kind != null)
			{
				sb.append("Kind=").append(this.Kind).append(" ");
			}
			if (this.Reason != null)
			{
				sb.append("Reason=").append(this.Reason).append(" ");
			}
			sb.append("}");

			return sb.toString();
		}

		private void clearResults()
		{
			this.Out = null;
			this.Result = null;
			this.Reason = "";
			this.Kind = null;
			this.Errors = null;
		}
	}

	//endregion
}
