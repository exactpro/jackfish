 /*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

 public class Parameters extends MutableArrayList<Parameter>
{
	public Parameters()
	{
	}

	/**
	 * copy constructor
	 */
	public Parameters(Parameters params)
	{
		params.stream()
				.map(Parameter::new)
				.forEach(this::add);
	}

	public Map<String, Object> makeCopy()
	{
		Map<String, Object> result = new LinkedHashMap<>();
		super.forEach(e -> result.put(e.getName(), e.getValue()));
		return result;
	}

	public void setValue(Parameters value)
	{
		super.clear();
		super.addAll(value);
	}

	/**
	 * @return new parameters, which has all parameter with passed types from this
	 */
	public Parameters select(TypeMandatory ... types)
	{
		if (types == null || types.length == 0)
		{
			return this;
		}

		Set<TypeMandatory> set = new HashSet<>(Arrays.asList(types));
		Parameters result = new Parameters();
		result.addAll(super.stream().filter(param -> set.contains(param.type)).collect(Collectors.toList()));
		return result;
	}

	/**
	 * @return true, if any parameter has name like the passed key
	 */
	public boolean containsKey(Object key)
	{
		return super.stream()
				.anyMatch(parameter -> parameter.getName() != null && parameter.getName().equals(key));
	}

	/**
	 * @return
	 * <ul>
	 *   <li> {@code } null, if a parameter by passed name not found</li>
	 *   <li> if a parameter found alone, will return value of the parameter </li>
	 *   <li> if found many parameters, will return list of values of these parameters </li>
	 * </ul>
	 */
	public Object get(String key)
	{
		List<Object> result = super.stream()
				.filter(parameter -> parameter.getName() != null && parameter.getName().equals(key))
				.map(Parameter::getValue)
				.collect(Collectors.toList());

		switch (result.size())
		{
			case 0: return null;
			case 1: return result.get(0);
			default: return result.toArray();
		}
	}

	/**
	 * @return set of names of all parameters
	 */
	public Set<String> keySet()
	{
		return super.stream()
				.map(Parameter::getName)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * @return collection of values of all parameters
	 */
	public Collection<Object> values()
	{
		return super.stream()
				.map(Parameter::getValue)
				.collect(Collectors.toList());
	}

	/**
	 * @return entry set of all parameters.
	 *
	 * @see ParametersEntry
	 */
	public Set<Entry<String, Object>> entrySet()
	{
		return super.stream()
				.map(parameter -> new ParametersEntry<>(parameter.getName(), parameter.getValue()))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * @return Parameter by passed index
	 */
	public Parameter getByIndex(int index)
	{
		return super.get(index);
	}

	/**
	 * @return a parameter by passed name. If the parameter not found, will return null
	 */
	public Parameter getByName(String name)
	{
		return super.stream()
				.filter(param -> param.getName() != null && param.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * @return index of the passed parameter. If the passed parameter not found in this collection, will return -1
	 */
	public int getIndex(Parameter parameter)
	{
		int count = 0;
		for (Parameter par : this)
		{
			if (par == parameter)
			{
				return count;
			}
			count++;
		}
		return -1;
	}

	/**
	 * Set passed values to a parameter by passed index. If out of range, nothing happens
	 */
	public void set(int index, String name, String expression, TypeMandatory type)
	{
		if (index < 0 || index >= super.size())
		{
			return;
		}
		
		Parameter param = super.get(index);
		if (param != null)
		{
			param.reset();
			param.setName(name);
			param.setExpression(expression);
			param.setType(type);
		}
	}

	/**
	 * @return a parameter expression or null, if the parameter by passed name not found
	 */
	public String getExpression(String parameterName)
	{
		return super.stream()
				.filter(param -> param.getName() != null && param.getName().equals(parameterName))
				.findFirst()
				.map(Parameter::getExpression)
				.orElse(null);
	}

	public void retain(Parameters expectedParameters)
	{
		Parameters newParameters = new Parameters();
		for (int i = 0; i < expectedParameters.size(); i++)
		{
			Parameter parameter = expectedParameters.get(i);
			if (containsKey(parameter.getName()))
			{
				parameter.setExpression(this.getExpression(parameter.getName()));
			}
			newParameters.add(parameter.getName(), parameter.getExpression());
		}
		setValue(newParameters);
	}

	final class ParametersEntry<K, V> implements Map.Entry<K, V>
	{
		private final K key;
		private       V value;

		public ParametersEntry(K key, V value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey()
		{
			return key;
		}

		@Override
		public V getValue()
		{
			return value;
		}

		@Override
		public V setValue(V value)
		{
			V old = this.value;
			this.value = value;
			return old;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(":");
		sb.append(super.size());
		sb.append("\n{");
		String comma = "\n";
		for (Parameter p : this)
		{
			sb.append(comma);
			sb.append(p.toString());
			comma = ",\n";
		}
		sb.append("\n}");
		return sb.toString();
	}

	/**
	 * Add a new parameter, which has passed values, to the end of this parameters.
	 */
	public void add(String name, String expression, TypeMandatory mandatory)
	{
		Parameter par = new Parameter(name, expression);
		par.setType(mandatory);
		super.add(par);
	}

	/**
	 * Add a new parameter, which has passed values, to the end of this parameters.
	 */
	public void add(String name, String expression)
	{
		this.add(name, expression, null);
	}

	/**
	 * Replace values of a parameter, which will found by name of the passed parameter, to values from the passed parameter.
	 * If a parameter not found, nothing happens
	 */
	public void replaceIfExists(Parameter parameter)
	{
		String name = parameter.getName();
		super.stream()
				.filter(p -> p.getName() != null && p.getName().equals(name))
				.findFirst()
				.ifPresent(p ->
				{
					p.setAll(parameter);
					p.expression = parameter.expression;
					p.value = parameter.value;
					p.isValid = parameter.isValid;
					p.type = parameter.type;
				});
	}

	/**
	 * Insert a new parameter, which has passed value, by passed index
	 */
	public void insert(int index, String name, String expression, TypeMandatory type)
	{
		if (index == -1)
		{
			return;
		}
		Parameter element = new Parameter(name, expression);
		element.setType(type);
		super.add(Math.min(index, super.size()), element);
	}

	/**
	 * @return true, if parameter by passed index can be removed. Only parameters, which has type {@link TypeMandatory#NotMandatory} and {@link TypeMandatory#Extra} can be removed.
	 */
	public boolean canRemove(int index)
	{
		return index >= 0
				&& index <= super.size() - 1
				&& !super.get(index).getType().equals(TypeMandatory.Mandatory);
	}

	/**
	 * @return true, if parameter can move ( if index greater than -1)
	 */
	public boolean canMove(int index)
	{
		return index > -1;
	}

	/**
	 * Move a parameter by passed index to left. If the parameter is first of this collection, this parameter will move to end of this collection
	 * @param index index of parameter, which should moved
	 */
	public void moveLeft(int index)
	{
		if (index == -1)
		{
			return;
		}
		boolean flag = index == 0;
		Parameter parameter = super.get(index);
		super.remove(index);
		if (flag)
		{
			super.add(parameter);
		}
		else
		{
			super.add(index - 1, parameter);
		}
	}

	/**
	 * Move a parameter by passed index to right. If the parameter is last of this collection, this parameter will move to start of this collection
	 * @param index index of parameter, which should moved
	 */
	public void moveRight(int index)
	{
		if (index == -1)
		{
			return;
		}
		boolean flag = index == size() - 1;
		Parameter parameter = super.get(index);
		super.remove(index);
		if (flag)
		{
			super.add(0, parameter);
		}
		else
		{
			super.add(index + 1, parameter);
		}
	}

	/**
	 * Set passed type to a parameter by passed index
	 */
	public void setType(int index, TypeMandatory type)
	{
		super.get(index).setType(type);
	}

	public void resetAll()
	{
		super.forEach(Parameter::reset);
	}

	/**
	 * Prepare and check all parameters from this collection
	 *
	 * @see Parameter#prepareAndCheck(AbstractEvaluator, IMatrixListener, MatrixItem)
	 */
	public void prepareAndCheck(AbstractEvaluator evaluator, IMatrixListener listener, MatrixItem item)
	{
		this.forEach(param -> param.prepareAndCheck(evaluator, listener, item));
	}

	/**
	 * Try to evaluate all parameters from this collection.
	 * @return true, if all parameters evaluated successful
	 *
	 * @see Parameter#evaluate(AbstractEvaluator)
	 */
	public boolean evaluateAll(AbstractEvaluator evaluator)
	{
		boolean result = true;

		for (Parameter param : this)
		{
			boolean resOne = param.evaluate(evaluator);
			result = result && resOne;
		}

		return result;
	}

	public final boolean matches(String what, boolean caseSensitive, boolean wholeWord)
	{
		return super.stream()
				.anyMatch(parameter -> parameter.matches(what, caseSensitive, wholeWord));
	}

}
