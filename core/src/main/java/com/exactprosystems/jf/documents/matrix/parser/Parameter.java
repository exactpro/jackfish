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

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The class, which present any parameter.
 * The parameter has name and expression
 */
public class Parameter implements Mutable, Consumer<String>, Supplier<String>
{
	protected static final Logger logger = Logger.getLogger(Parameter.class);

	private MutableValue<String> description;

	private   String        name;
	private   Object        compiled;
	private   boolean       changed;
	protected String        expression;
	protected Object        value;
	protected boolean       isValid;
	protected TypeMandatory type;

	public Parameter(String name, String expression)
	{
		this.name = name;
		this.expression = expression;
		this.compiled = null;
		this.value = null;
		this.isValid = false;
		this.type = TypeMandatory.Extra;
		this.changed = false;
	}

	/**
	 * copy constructor
	 */
	public Parameter(Parameter p)
	{
		this.value = null;
		this.compiled = null;
		if (p != null)
		{
			this.type = p.type;
			this.name = p.name;
			this.expression = p.expression;
			this.changed = p.changed;
			if (p.description != null)
			{
				this.description = new MutableValue<>(p.description.get());
			}
		}
	}

	/**
	 * Set the description for this parameter
	 * @param description the new description
	 */
	public void setDescription(String description)
	{
		this.description = Optional.ofNullable(this.description).orElseGet(MutableValue::new);
		this.description.accept(description);
		this.changed = true;
	}

	/**
	 * @return the description of this parameter. If description is not present, will return {@code null}
	 */
	public String getDescription()
	{
		return Optional.ofNullable(this.description)
				.map(MutableValue::get)
				.orElse(null);
	}

	/**
	 * Set for this parameter all values from passed parameter
	 */
	public void setAll(Parameter parameter)
	{
		this.setExpression(parameter.expression);
		this.setType(parameter.type);
		this.compiled = null;
		
		this.value = parameter.value;
		this.isValid = parameter.isValid;
		this.description = new MutableValue<>(parameter.getDescription());
	}

	//region interface Mutable
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
	//endregion

	//region interface Supplier

	/**
	 * @return the expression
	 */
	@Override
	public String get()
	{
		return this.getExpression();
	}
	//endregion

	//region interface Consumer
	/**
	 * Set the new expression
	 */
	@Override
	public void accept(String value)
	{
		this.setExpression(value);
	}
	//endregion

	public boolean matches(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.name, what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.expression, what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.getDescription(), what, caseSensitive, wholeWord)
				;
	}

	/**
	 * Set the new expression for this parameter
	 */
	public void setExpression(String expression)
	{
		this.changed = this.changed || !Str.areEqual(this.expression, expression);
		this.expression = expression;
		this.compiled = null;
	}

	/**
	 * Set the new name for this parameter
	 */
	public void setName(String name)
	{
		this.changed = this.changed || !Str.areEqual(this.name, name);
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

	/**
	 * @return the evaluated value from the expression. If expression not evaluated yet, will return {@code null}
	 */
	public Object getValue()
	{
		return this.value;
	}

	/**
	 * Set the value for this parameter
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	/**
	 * @return string representation of this value. If value is null, will return string "null"
	 */
	public String getValueAsString()
	{
		return "" + this.value;
	}

	/**
	 * @return true, if evaluating the expression was successful. Otherwise false
	 */
	public boolean isValid()
	{
		return this.isValid;
	}

	public boolean isExpressionNullOrEmpty()
	{
		return Str.IsNullOrEmpty(this.expression);
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

	/**
	 * Try to compile the expression. If compiling was failed, will notify listener error
	 */
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

	/**
	 * Try to evaluate the expression via passed evaluator
	 *
	 * @return true, if evaluating was successful
	 */
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
			}
			this.isValid = true;
		}
		catch (Exception e)
		{
			this.value = e.getMessage();
		}
		return this.isValid;
	}

	public void reset()
	{
		this.isValid = false;
		this.value = null;
		this.type = TypeMandatory.Extra;

		this.changed = true;
	}
	
	@Override
	public String toString()
	{
		return this.name + " : " + this.expression;
	}

	/**
	 * Try to convert the evaluating value to passed type.
	 * If converting was failed, the value will has type String and value - exception message
	 */
	public final void correctType(Class<?> type)
	{
		try
		{
			this.isValid = true;

			if (this.value == null)
			{
				return;
			}

			Class<?> valueType = value.getClass();

			if (type == Object.class)
			{
				return;
			}
			else if (type.isArray() && valueType.isArray())
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
					Object[] array = (Object[]) Array.newInstance(type.getComponentType(), 1);

					array[0] = value;
					this.value = Converter.convertArray(type.getComponentType(), array);
					return;
				}
				else
				{
					throw new Exception(String.format(R.PARAMETER_TYPE_EXCEPTION.get(), valueType.getName(), type.getComponentType().getName()));
				}
			}
			else if (!type.isArray() && valueType.isArray())
			{
				throw new Exception(String.format(R.PARAMETER_ARRAY_EXCEPTION.get(), valueType.getName(), type.getName()));
			}
			else
			{
				this.value = Converter.convertToType(this.value, type);
			}
		}
		catch (Exception e)
		{
			this.isValid = false;
			this.value = e.getMessage();

			logger.error(e.getMessage(), e);
		}
	}
}
