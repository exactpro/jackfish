////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.app.Mutable;

import java.io.Serializable;
import java.util.*;

public class MapMessage implements Map<String, Object>, Serializable, Mutable
{
	private static final long	serialVersionUID	= -1159773416112486653L;
	
	public static final String messageTypeName = "MessageType";
	private boolean changed;
	private String messageType = null;
	private Map<String, Object> fields = new LinkedHashMap<>();
	private String source = null;

	private List<String> errors = null;

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

	public MapMessage(String source, MapMessage message)
	{
		this(source);
		this.fields.putAll(message.fields);
	}

	/**
	 * copy constructor
	 */
	public MapMessage(MapMessage mapMessage)
	{
		if (mapMessage != null)
		{
			this.changed = mapMessage.changed;
			this.messageType = mapMessage.messageType;
			this.fields = new LinkedHashMap<>(mapMessage.fields);
			this.source = mapMessage.source;
			if (mapMessage.errors != null)
			{
				this.errors = new ArrayList<>(mapMessage.errors);
			}
		}
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
       return MapMessage.class.getSimpleName() + " [" + messageTypeName + ":" + getMessageType() + "]";
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
}
