////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;

import java.lang.reflect.Array;

public class Parameter implements Mutable, Cloneable, Setter<String>, Getter<String>
{
	public Parameter(String name, String expression)
	{
		this.name = name;
		this.expression = expression;
		this.compiled = null;
		this.value = null;
		setString(null);
		this.isValid = false;
		this.type = TypeMandatory.Extra;
		this.changed = false;
	}

	public void setAll(Parameter parameter)
	{
		this.setExpression(parameter.expression);
		this.setType(parameter.type);
		this.compiled = null;
		
		this.value = parameter.value;
		setString(this.value);
		this.isValid = parameter.isValid;
	}

	@Override
	public Parameter clone() throws CloneNotSupportedException
	{
		Parameter clone = ((Parameter) super.clone());
		clone.value = null;
		clone.valueAsString = "null";
		clone.type = type;
		clone.name = name;
		clone.expression = expression;
		clone.compiled = compiled;
		clone.changed = changed;

		return clone;
	}

	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		this.changed = false;
	}

	@Override
	public String get()
	{
		return getExpression();
	}

	@Override
	public void set(String value)
	{
		setExpression(value);
	}

	
	public boolean matches(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.name, what, caseSensitive, wholeWord) || SearchHelper.matches(this.expression, what, caseSensitive, wholeWord);
	}

	public void setExpression(String expression)
	{
		this.changed = changed || !Str.areEqual(this.expression, expression);
		this.expression = expression;
		this.compiled = null;
	}

	public void setName(String name)
	{
		this.changed = changed || !Str.areEqual(this.name, name);
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public String getExpression()
	{
		return this.expression;
	}

	public Object getValue()
	{
		return this.value;
	}

	public String getValueAsString()
	{
		return this.valueAsString;
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	public boolean isExpressionNullOrEmpty()
	{
		return this.expression == null || this.expression.isEmpty();
	}

	public TypeMandatory getType()
	{
		return this.type;
	}


	public void setType(TypeMandatory type)
	{
		this.changed = changed || !(this.type != null && this.type == type);
		this.type = type;
	}
	
	public void prepareAndCheck(AbstractEvaluator evaluator, IMatrixListener listener, MatrixItem item)
	{
		try
		{
			this.compiled = evaluator.compile(this.expression);
		}
		catch (Exception e)
		{
			listener.error(item.getMatrix(), 0, item, e.getMessage());
		}
	}

	public boolean evaluate(AbstractEvaluator evaluator)
	{
		this.value = null;
		this.isValid = false;
		try
		{
			if (this.compiled == null)
			{
				this.compiled = evaluator.compile(this.expression);
			}
			
			if (this.compiled != null)
			{
				this.value = evaluator.execute(this.compiled);
				setString(this.value);
			}
			this.isValid = true;
		}
		catch (Exception e)
		{
			this.value = e.getMessage();
			setString(this.value);
		}
		return this.isValid;
	}
	
	public void reset()
	{
		this.isValid = false;
		this.value = null;
		this.valueAsString = "null";
		this.type = TypeMandatory.Extra;

		this.changed = true;
	}
	
	@Override
	public String toString()
	{
		return this.name + " : " + this.expression;
	}

	private void setString(Object value)
	{
		valueAsString = value == null ?  "null" : value.toString();
	}

    public final void correctType(Class<?> type) throws Exception
    {
    	try
    	{
    		this.isValid = true;

    		if (this.value == null)
	        {
	            return;
	        }

	        Class<?> valueType = value.getClass();

	        if (type.isArray() && valueType.isArray())
	        {
	        	if (valueType.getComponentType() == type.getComponentType())
	        	{
	        		return;
	        	}

	        	if (valueType.getComponentType() == byte.class)
	        	{
	        		this.value = Converter.convertByteArray(type.getComponentType(), this.value);
	        		return;
	        	}
	        	
	        	this.value = Converter.convertArray(type.getComponentType(), this.value);
	            return;
	        }
	        else if (type.isArray() && !valueType.isArray())
	        {
	            if (type.getComponentType().isAssignableFrom(valueType))
	            {
	                Object[] array = (Object[])Array.newInstance(type.getComponentType(), 1);

	                array[0] = value;
	                this.value = Converter.convertArray(type.getComponentType(), array);
	                return;
	            }
	            else
	            {
	                throw new Exception("Type " + valueType.getName() + " is not the same type as " + type.getComponentType().getName());
	            }
	        }
	        else if (!type.isArray() && valueType.isArray())
	        {
	            throw new Exception("Type " + valueType.getName() + " is an array. It needs an single object " + type.getName());
	        }
	        else if (!type.isArray() && !valueType.isArray())
	        {
	            if (type.isAssignableFrom(valueType))
	            {
	                return;
	            }
	            else
	            {
	                throw new Exception("Type " + valueType.getName() + " cannot be cast to " + type.getName());
	            }
	        }
	        else
	        {
	            throw new Exception("It is impossible. Call the developers.");
	        }
    	}
        catch (Exception e)
        {
        	this.isValid = false;
        	this.value = e.getMessage(); 
        }
    }

	private String name;
	String expression;
	Object compiled;
	Object value;
	private String valueAsString;
	boolean isValid;
	TypeMandatory type;
	private boolean changed;
}