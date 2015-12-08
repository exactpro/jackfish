 ////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser;

 import com.exactprosystems.jf.api.app.Mutable;
 import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
 import com.exactprosystems.jf.common.parser.items.MatrixItem;
 import com.exactprosystems.jf.common.parser.items.TypeMandatory;
 import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;

 import java.util.*;
 import java.util.stream.Collectors;

 public class Parameters implements Iterable<Parameter>, Map<String, Object>, Mutable, Cloneable
{
	public Parameters()
	{
		this.changed = false;
		this.parameters = new ArrayList<>();
	}

	public void setValue(Parameters value)
	{
		this.changed = true;
		this.parameters.clear();
		this.parameters.addAll(value.parameters);
	}

	public Map<String, Object> makeCopy()
	{
		Map<String, Object> result = new HashMap<>();
		result.putAll(this);
		return result;
	}
	
	public Parameters select(TypeMandatory ... types)
	{
		if (types == null || types.length == 0)
		{
			return this;
		}

		Set<TypeMandatory> set = new HashSet<>(Arrays.asList(types));
		Parameters result = new Parameters();
		result.parameters.addAll(this.parameters.stream().filter(param -> set.contains(param.type)).collect(Collectors.toList()));
		return result;
	}

	//------------------------------------------------------------------------------------------------------------------
	// implements Cloneable
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public Parameters clone() throws CloneNotSupportedException
	{
		Parameters clone = ((Parameters) super.clone());
		clone.changed = false;
		clone.parameters = new ArrayList<>(parameters.size());
		for (Parameter parameter : parameters)
		{
			clone.parameters.add(parameter.clone());
		}
		return clone;
	}

	//------------------------------------------------------------------------------------------------------------------
	// interface Mutable
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public final boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		for (Parameter parameter : this.parameters)
		{
			if (parameter.isChanged())
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public final void saved()
	{
		this.changed = false;
		this.parameters.forEach(Parameter::saved);
	}
	
	//------------------------------------------------------------------------------------------------------------------
	// interface Iterable
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public Iterator<Parameter> iterator()
	{
		return this.parameters.iterator();
	}
	

	//------------------------------------------------------------------------------------------------------------------
	// interface Map
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isEmpty()
	{
		return this.parameters.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		for (Parameter parameter : this.parameters)
		{
			if (parameter.getName() != null && parameter.getName().equals(key))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean containsValue(Object value)
	{
		for (Parameter parameter : this.parameters)
		{
			if (parameter.getValue() != null && parameter.getValue().equals(value))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Object get(Object key)
	{
		List<Object> result = this.parameters.stream()
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

	@Deprecated
	@Override
	public Object put(String key, Object value)
	{
		throw new RuntimeException("put is not allowed for " + this.getClass());
	}

	@Deprecated
	@Override
	public Object remove(Object key)
	{
		throw new RuntimeException("remove is not allowed for " + this.getClass());
	}

	@Deprecated
	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		throw new RuntimeException("putAll is not allowed for " + this.getClass());
	}

	@Override
	public void clear()
	{
		this.changed = this.changed || !this.parameters.isEmpty();
		this.parameters.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.parameters.stream().map(Parameter::getName).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Collection<Object> values()
	{
		return this.parameters.stream().map(Parameter::getValue).collect(Collectors.toList());
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		return this.parameters.stream().map(parameter -> new ParametersEntry<>(parameter.getName(), parameter.getValue())).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Parameter getByIndex(int index)
	{
		return this.parameters.get(index);
	}
	
	public int getIndex(Parameter parameter)
	{
		int count = 0;
		for (Parameter par : this.parameters)
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
		if (index < 0 || index >= this.parameters.size())
		{
			return;
		}
		
		Parameter param = this.parameters.get(index);
		if (param != null)
		{
			param.reset();
			param.setName(name);
			param.setExpression(expression);
			param.setType(type);
		}
	}
	
	public void remove (int index)
	{
		if (index < 0 || index >= this.parameters.size())
		{
			return;
		}

		Parameter removed = this.parameters.remove(index);
		this.changed = this.changed || removed != null;
	}

	public String getExpression(String parameterName)
	{
		Optional<Parameter> first = this.parameters.stream().filter(param -> param.getName() != null && param.getName().equals(parameterName)).findFirst();
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
			Parameter parameter = expectedParameters.parameters.get(i);
			if (this.containsKey(parameter.getName()))
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
		sb.append(this.parameters.size());
		sb.append("\n{");
		String comma = "\n";
		for (Parameter p : this.parameters)
		{
			sb.append(comma);
			sb.append(p.toString());
			comma = ",\n";
		}
		sb.append("\n}");
		return sb.toString();
	}
	
	//------------------------------------------------------------------------------------------------------------------

	
	public void add(String name, String expression)
	{
		this.changed = true;
		this.parameters.add(new Parameter(name, expression));
	}
	
	public void replaceIfExists(Parameter parameter)
	{
		String name = parameter.getName();
		this.parameters.stream().filter(p -> p.getName() != null && p.getName().equals(name)).findFirst().ifPresent(p -> {
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
		this.changed = true;
		Parameter element = new Parameter(name, expression);
		element.setType(type);
		this.parameters.add(index, element);
	}

	public boolean canRemove(int index)
	{
		return index >= 0 && index <= this.parameters.size() - 1 && !this.parameters.get(index).getType().equals(TypeMandatory.Mandatory);
	}

	public boolean canMove(int index)
	{
		return index > -1;
	}

	public void moveLeft(int index)
	{
		if (index == -1) return;
		this.changed = true;
		boolean flag = index == 0;
		Parameter parameter = this.parameters.get(index);
		this.parameters.remove(index);
		if (flag)
		{
			this.parameters.add(parameter);
		}
		else
		{
			this.parameters.add(index - 1, parameter);
		}
	}

	public void moveRight(int index)
	{
		if (index == -1) return;
		this.changed = true;
		boolean flag = index == this.parameters.size() -1 ;
		Parameter parameter = this.parameters.get(index);
		this.parameters.remove(index);
		if (flag)
		{
			this.parameters.add(0,parameter);
		}
		else
		{
			this.parameters.add(index + 1, parameter);
		}
	}
	
	public int size()
	{
		return this.parameters.size();
	}

	public void setType(int index, TypeMandatory type)
	{
		this.parameters.get(index).setType(type);
	}

	public void resetAll()
	{
		this.parameters.forEach(Parameter::reset);
	}
	
	public void prepareAndCheck(AbstractEvaluator evaluator, IMatrixListener listener, MatrixItem item)
	{
		for (Parameter param : this.parameters)
		{
			param.prepareAndCheck(evaluator, listener, item);
		}
	}

	public boolean evaluateAll(AbstractEvaluator evaluator)
	{
		boolean result = true;
		
		for (Parameter param : this.parameters)
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
	
	private ArrayList<Parameter> parameters;
	
	private boolean changed;
}
