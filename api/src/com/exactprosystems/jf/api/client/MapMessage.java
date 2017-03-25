////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.exactprosystems.jf.api.app.Mutable;

public class MapMessage implements Map<String, Object>, Serializable, Mutable, Cloneable
{
	private static final long	serialVersionUID	= -1159773416112486653L;
	
	public static final String messageTypeName = "MessageType";

	public MapMessage(String source)
	{
		this(null, null, source);
	}
	
	public MapMessage(Map<String, Object> map, String source)
	{
		this(null, map, source);
	}

	public MapMessage(String messageType, Map<String, Object> map, String source)
	{
		this.changed = false;
		this.messageType = messageType;
		if (map != null)
		{
			this.fields = new LinkedHashMap<String, Object>();
			this.fields.putAll(map);
		}
		this.source = source;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
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

	//==============================================================================================
	// Interface Cloneable
	//==============================================================================================
	@Override
	public MapMessage clone() throws CloneNotSupportedException
	{
		MapMessage clon = new MapMessage(this.messageType, this.fields, this.source);
		return clon;
	}
	//==============================================================================================
	
	public void addError(String error)
	{
		if (this.errors == null)
		{
			this.errors = new ArrayList<String>();
		}
		
		this.errors.add(error);
	}
	
	public boolean isCorrect()
	{
		return this.errors == null || this.errors.isEmpty();
	}
	
	public String getMessageType()
	{
		return this.messageType;
	}
	
	public void setMessageType(String messageType)
	{
		this.messageType = messageType;
	}

	public String getSource()
	{
	    return this.source;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("MapMessage { ");
		if (this.messageType != null)
		{
			sb.append(messageTypeName)
				.append(" = ")
				.append(this.messageType)
				.append("\n");
		}
		
		String comma = "";
		for (Map.Entry<String, Object> entry : this.fields.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			
			sb.append(comma);
			sb.append(key); 
			sb.append(" : ");

			if (value == null)
			{
				sb.append("null");
			}
			else if (value.getClass().isArray())
			{
				sb.append(Arrays.toString((Object[])value));
			}
			else
			{
				sb.append(value.toString());
			}
			comma = ", ";
		}
		
		sb.append(" } ");
		
		if (this.source != null)
		{
			sb.append("\nSource : ");
			sb.append(this.source);
		}
		
		if (this.errors != null)
		{
			sb.append("\nErrors : ");
			sb.append(Arrays.toString(this.errors.toArray()));
		}
		
		return sb.toString();
	}
	
	public boolean extendEquals(MapMessage obj, String[] exculde)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (fields == null)
		{
			if (obj.fields != null)
			{
				return false;
			}
		} 
		
		List<String> list =  exculde == null ? new ArrayList<String>() : Arrays.asList(exculde);

		Iterator<Entry<String, Object>> iter = entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			if (list.contains(key))
			{
				continue;
			}
			
			Object value = entry.getValue();
			if (value == null)
			{
				if (!(obj.get(key) == null && obj.containsKey(key)))
				{
					return false;
				}
			} 
			else
			{
				Object otherValue = obj.get(key);
				if (otherValue == null)
				{
					return false;
				}

				if (value.getClass().isArray())
				{
					if (!otherValue.getClass().isArray())
					{
						return false;
					}

					Object[] arrValue = (Object[])value;
					Object[] arrOtherValue = (Object[])otherValue;
					if (arrValue.length != arrOtherValue.length)
					{
						return false;
					}
					
					for (int i = 0; i < arrValue.length; i++)
					{
						Object val1 = arrValue[i];
						Object val2 = arrOtherValue[i];
						
						if (val1 != null && val2 != null && val1 instanceof MapMessage && val2 instanceof MapMessage)
						{
							if (!((MapMessage)val1).extendEquals((MapMessage)val2, null))
							{
								return false;
							}
						}
					}
					
				}
				else if (!value.equals(obj.get(key)))
				{
					return false;
				}
			}
		}		
		
		
		return true;
	}
	
	@Override
	public int size()
	{
		return this.fields.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.fields.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.fields.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.fields.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		return this.fields.get(key);
	}

	@Override
	public Object put(String key, Object value)
	{
		this.changed = true;
		return this.fields.put(key, value);
	}

	@Override
	public Object remove(Object key)
	{
		this.changed = true;
		return this.fields.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.changed = true;
		this.fields.putAll(m);
	}

	@Override
	public void clear()
	{
		this.changed = true;
		this.fields.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.fields.keySet();
	}

	@Override
	public Collection<Object> values()
	{
		return this.fields.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet()
	{
		return this.fields.entrySet();
	}
	
	private boolean changed;
	private String messageType = null;
	private Map<String, Object>fields = new LinkedHashMap<String, Object>();
	private String source = null;

	private List<String> errors = null;
}
