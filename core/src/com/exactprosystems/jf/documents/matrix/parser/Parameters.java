 ////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

 public class Parameters extends MutableArrayList<Parameter> implements Cloneable//, Map<String, Object>
{
    private static final long serialVersionUID = -4631358668566907225L;
    private static final Logger logger = Logger.getLogger(Parameters.class);
    
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
		return this.stream()
				.collect(Collectors.toMap(
						Parameter::getName,
						Parameter::getValue,
						(u,v) -> {throw new IllegalStateException(String.format("Duplicate key %s", u));} ,
						LinkedHashMap::new
				));
	}

	public void setValue(Parameters value)
	{
		clear();
		addAll(value);
	}

	public Parameters select(TypeMandatory ... types)
	{
		if (types == null || types.length == 0)
		{
			return this;
		}

		Set<TypeMandatory> set = new HashSet<>(Arrays.asList(types));
		Parameters result = new Parameters();
		result.addAll(stream().filter(param -> set.contains(param.type)).collect(Collectors.toList()));
		return result;
	}

	public boolean containsKey(Object key)
	{
		for (Parameter parameter : this)
		{
			if (parameter.getName() != null && parameter.getName().equals(key))
			{
				return true;
			}
		}
		
		return false;
	}

	public Object get(String key)
	{
		List<Object> result = stream()
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

	public Set<String> keySet()
	{
		return stream().map(Parameter::getName).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Collection<Object> values()
	{
		return stream().map(Parameter::getValue).collect(Collectors.toList());
	}

	public Set<Entry<String, Object>> entrySet()
	{
		return stream().map(parameter -> new ParametersEntry<>(parameter.getName(), parameter.getValue()))
		        .collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Parameter getByIndex(int index)
	{
		return get(index);
	}
	
	public Parameter getByName(String name)
	{
		Optional<Parameter> first = stream().filter(param -> param.getName() != null && param.getName().equals(name)).findFirst();
		if (first.isPresent())
		{
			return first.get();
		}
		return null;
	}

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
	
	public void set (int index, String name, String expression, TypeMandatory type)
	{
		if (index < 0 || index >= size())
		{
			return;
		}
		
		Parameter param = get(index);
		if (param != null)
		{
			param.reset();
			param.setName(name);
			param.setExpression(expression);
			param.setType(type);
		}
	}
	
	public String getExpression(String parameterName)
	{
		Optional<Parameter> first = stream().filter(param -> param.getName() != null && param.getName().equals(parameterName)).findFirst();
		if (first.isPresent())
		{
			return first.get().getExpression();
		}
		return null;
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
		private final K	key;
		private V		value;

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
	
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(":");
		sb.append(size());
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
	
	//------------------------------------------------------------------------------------------------------------------

	
	public void add(String name, String expression, TypeMandatory mandatory)
	{
		Parameter par = new Parameter(name, expression);
		par.setType(mandatory);
		add(par);
	}
	
    public void add(String name, String expression)
    {
        add(name, expression, null);
    }
    
	public void replaceIfExists(Parameter parameter)
	{
		String name = parameter.getName();
		stream().filter(p -> p.getName() != null && p.getName().equals(name))
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
	
	public void insert(int index, String name, String expression, TypeMandatory type)
	{
		if (index == -1) return;
		Parameter element = new Parameter(name, expression);
		element.setType(type);
		add(Math.min(index, size()), element);
	}

	public boolean canRemove(int index)
	{
		return index >= 0 && index <= size() - 1 && !get(index).getType().equals(TypeMandatory.Mandatory);
	}

	public boolean canMove(int index)
	{
		return index > -1;
	}

	public void moveLeft(int index)
	{
		if (index == -1) return;
		boolean flag = index == 0;
		Parameter parameter = get(index);
		remove(index);
		if (flag)
		{
			add(parameter);
		}
		else
		{
			add(index - 1, parameter);
		}
	}

	public void moveRight(int index)
	{
		if (index == -1) return;
		boolean flag = index == size() -1 ;
		Parameter parameter = get(index);
		remove(index);
		if (flag)
		{
			add(0,parameter);
		}
		else
		{
			add(index + 1, parameter);
		}
	}
	
	public void setType(int index, TypeMandatory type)
	{
		get(index).setType(type);
	}

	public void resetAll()
	{
		forEach(Parameter::reset);
	}
	
	public void prepareAndCheck(AbstractEvaluator evaluator, IMatrixListener listener, MatrixItem item)
	{
		for (Parameter param : this)
		{
			param.prepareAndCheck(evaluator, listener, item);
		}
	}

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
		for (Parameter parameter : this)
		{
			if (parameter.matches(what, caseSensitive, wholeWord))
			{
				return true;
			}
		}
		return false;
	}

}
