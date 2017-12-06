////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
import java.util.stream.Collectors;

public abstract class AbstractAction implements Cloneable
{
	private Action action;
	protected MatrixItem owner;

	protected static final Logger logger = Logger.getLogger(AbstractAction.class);

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
    
    //==============================================================================================
    // Getters / Setters
    //==============================================================================================
    public final Result getResult()
    {
        return this.action.Result;
    }

    public final Object getOut()
    {
        return this.action.Out;
    }

    public final String getReason()
    {
        return this.action.Reason;
    }
    
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

    //==========================================================================================================================
    // Public members
    //==========================================================================================================================

    public void addParameters(Parameters parameters)
    {
    	Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();
    	for (Entry<String, FieldAndAttributes> entry : descriptions.entrySet())
    	{
    		String name = entry.getKey();
    		FieldAndAttributes attr = entry.getValue();

    		if (!parameters.containsKey(name) && attr.attribute.mandatory())
    		{
    			parameters.add(name, "");
    			parameters.setType(parameters.size() - 1, TypeMandatory.Mandatory);
    		}
    	}
    }
    
    public final void checkAction(IMatrixListener listener, ActionItem owner, Parameters parameters)
    {
        List<String> extraFields   	= checkExtraFields(parameters.keySet());
        List<String> omittedFields 	= checkMandatoryFields(parameters.keySet());

        if (!extraFields.isEmpty())
        {
            listener.error(owner.getMatrix(), owner.getNumber(), owner, "Extra parameters " + Arrays.toString(extraFields.toArray()));
        }

        if (!omittedFields.isEmpty())
        {
            listener.error(owner.getMatrix(), owner.getNumber(), owner, "Missing parameters " + Arrays.toString(omittedFields.toArray()));
        }
    }

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
			reportParameters(report, parameters);
			if (parametersAreCorrect)
			{
				if (injectParameters(parameters))
				{
					//-------------------------------------------------------------
					// Do the action!
					//-------------------------------------------------------------
					doRealAction(context, report, parameters, evaluator);
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

        reportResults(report, assertBool);

        evaluator.getLocals().delete(Tokens.This.get());
        return this.action.Result;
    }

    public final String actionSuffix()
    {
		ActionAttribute annotation = this.getClass().getAnnotation(ActionAttribute.class);
		if (annotation.outputType() == NullType.class)
		{
			return "";
		}
		return annotation.suffix();
    }
    
    public final boolean isAdditionFieldAllowed()
    {
        return this.getClass().getAnnotation(ActionAttribute.class).additionFieldsAllowed();
    }

    public final HelpKind howHelpWithParameter(Context context, String fieldName, Parameters parameters) throws Exception
    {
    	if (parameters.containsKey(fieldName))
    	{
    		return howHelpWithParameterDerived(context, parameters, fieldName);
    	}
    	
        return null; 
    }
    
    public final List<ReadableValue> listToFillParameter(Context context, String parameterToFill, Parameters parameters) throws Exception
    {
    	AbstractEvaluator evaluator = context.getEvaluator();
    	
    	if (evaluator != null)
    	{
    		parameters.evaluateAll(evaluator);
    		List<ReadableValue> res = new ArrayList<>();
    		listToFillParameterDerived(res, context, parameterToFill, parameters);
	    	 
	    	return res;
    	}
		return Collections.emptyList();
	}
    
	public final Map<ReadableValue, TypeMandatory> helpToAddParameters(Context context, Parameters parameters)  throws Exception
	{
        Map<ReadableValue, TypeMandatory> res = new LinkedHashMap<>();
        Map<String, FieldAndAttributes> map = getFieldsAttributes();
        for (Entry<String, FieldAndAttributes> entry : map.entrySet())
        {
            res.put(new ReadableValue(entry.getKey()), entry.getValue().attribute.mandatory() ? TypeMandatory.Mandatory : TypeMandatory.NotMandatory);
        }
    	AbstractEvaluator evaluator = context.getEvaluator();
    	
    	if (evaluator != null)
    	{
    		List<ReadableValue> list = new ArrayList<>();
    		parameters.evaluateAll(evaluator);
	    	helpToAddParametersDerived(list, context, parameters);
	    	for (ReadableValue element : list)
	    	{
	    		res.put(element, TypeMandatory.Extra);
	    	}
    	}
    	return res;
	}

	public final void correctParametersType(Parameters parameters)
	{
        Map<String, FieldAndAttributes> fields = getFieldsAttributes();
        for(Parameter parameter : parameters)
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

	public final void initDefaultValues()
    {
        try
        {
            Map<String, FieldAndAttributes> attributes = getFieldsAttributes();
            for(FieldAndAttributes attr : attributes.values())
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
	    

    //==========================================================================================================================
    // Protected members should be overridden
    //==========================================================================================================================
	//TODO think about removing throws Exception from here. It's unnecessary
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
    	return null;
    }

	//TODO think about removing throws Exception from here. It's unnecessary
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters)  throws Exception
    {
    }

	//TODO think about removing throws Exception from here. It's unnecessary
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters)  throws Exception
	{
	}

    protected abstract void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception;


    //==========================================================================================================================
    // Protected members for using
    //==========================================================================================================================
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
    		this.action.Reason = "Previous result: " + this.action.Result + "\n"
    				+ "Previous reason: " + this.action.Reason + "\n"
    				+ reason;
    	}
        this.action.Result = Result.Failed;
        this.action.Kind = kind;
    }

    protected final void setErrors(Map<String, MatrixError> errors)
	{
		this.action.Errors = errors;
	}

    //==============================================================================================
    // Private members
    //==============================================================================================

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        if (!parameters.isEmpty())
        {
            ReportTable table = report.addTable("Input parameters", null, false, true,
                    new int[] {20, 40, 40}, new String[] {"Parameter", "Expression", "Value"});

            for (Parameter param : parameters)
            {
                table.addValues(param.getName(), param.getExpression(), param.getValue());
            }
        }
    }

    private void reportResults(ReportBuilder report, Parameter assertBool)
    {
        if (!assertBool.isExpressionNullOrEmpty() )
        {
	        ReportTable assertTable = report.addTable("Assert", null, false, true,
	                new int[] {60, 40}, new String[] {"Expression", "Value"});
	
	        assertTable.addValues(assertBool.getExpression(), assertBool.getValue());
        }

        ReportTable resultTable = report.addTable("Results", null, false, true,
                new int[] {20, 80}, new String[] {"Parameter", "Value"});

        tableRowIfNotNull(resultTable, "Result", 		this.action.Result);
        tableRowIfNotNull(resultTable, "Error kind", 	this.action.Kind);
        tableRowIfNotNull(resultTable, "Reason", 		this.action.Reason.isEmpty() ? null : this.action.Reason);
        tableRowIfNotNull(resultTable, "Out", 			this.action.Out);
    }

    private void tableRowIfNotNull(ReportTable table, Object title, Object obj)
    {
        if (obj != null)
        {
            table.addValues(title, obj);
        }
    }

    private boolean injectParameters(Parameters parameters) throws Exception
    {
        boolean allCorrect = true;

        Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();
        for (Parameter parameter : parameters)
        {
            try
            {
                String name 		= parameter.getName();
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
							setError(String.format("NotMandatory parameter %s must be filled or not presented", name), ErrorKind.EMPTY_PARAMETER);
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
                	    setError("Parameter " + name + ": " + parameter.getValue(), ErrorKind.WRONG_PARAMETERS);
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
        Map<String, FieldAndAttributes> fieldAttributes = new HashMap<String, FieldAndAttributes>();
        for (Field field : this.getClass().getDeclaredFields())
        {
            ActionFieldAttribute attr = field.getAnnotation(ActionFieldAttribute.class);
            if (field.getAnnotation(ActionFieldAttribute.class) != null)
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
                setError(Tokens.Assert + " error in expression: " + assertBool.getValueAsString(), ErrorKind.EXPRESSION_ERROR);
                return;
        	}
        	
            Object value = assertBool.getValue();
            if (value != null && value instanceof Boolean)
            {
                if (!(Boolean)value)
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
                setError(Tokens.Assert + " must have type of Boolean", ErrorKind.EXPRESSION_ERROR);
			}
        }
    }


    private List<String> checkMandatoryFields(Set<String> fields)
    {
		return getFieldsAttributes().entrySet()
				.stream()
				.filter(entry -> entry.getValue().attribute.mandatory() && !fields.contains(entry.getKey()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
    }


    private List<String> checkExtraFields(Set<String> fields)
    {
        if (getClass().getAnnotation(ActionAttribute.class).additionFieldsAllowed())
        {
            return Collections.emptyList();
        }

        Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();

        return fields.stream()
				.filter(name -> !descriptions.containsKey(name))
				.collect(Collectors.toList());
    }


    private void clearResults()
    {
        this.action.clearResults();
    }

    public static class FieldAndAttributes
    {
        public FieldAndAttributes(Field field, ActionFieldAttribute attributes)
        {
            this.field = field;
            this.attribute = attributes;
        }

        public Field field;
        public ActionFieldAttribute attribute;
    }

    public static class Action
    {
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
    	
		public void clearResults()
        {
            this.Out = null;
            this.Result = null;
            this.Reason = "";
            this.Kind = null;
			this.Errors = null;
        }
    	
    	public Object Out;
    	public Parameters In;
    	public String Reason;
    	public ErrorKind Kind;
    	public Result Result;
		public Map<String, MatrixError> Errors;
    }
}
