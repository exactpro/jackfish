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

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = R.STRING_CONDITION_DESCRIPTION)
public class StringCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 2230671827371752791L;

	public StringCondition(String name, String value)
	{
		this(name, value, false);
	}

	public StringCondition(String name, String value, boolean ignoreCase)
	{
		super(name);
		this.value = value;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + separator + this.value + separator + this.ignoreCase + finish;
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		String name = getName();
		if (Str.IsNullOrEmpty(name))
		{
			return true;
		}
		Object value = map.get(name);
		String strValue = "" + value;
		if (this.ignoreCase)
		{
			return strValue.equalsIgnoreCase(this.value);
		}
		return strValue.equalsIgnoreCase(this.value);
	}

	@Override
	public String toString()
	{
		return StringCondition.class.getSimpleName() + " [name=" + getName() + ", value=" + value + ", ignoreCase=" + ignoreCase + "]";
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		if (this.ignoreCase)
		{
			return "'" + String.valueOf(this.value).toLowerCase() + "' != '" + String.valueOf(actualValue).toLowerCase() + "'";
		}
		
		return "'" + this.value + "' != '" + actualValue + "'";
	}

	public String getValue()
	{
		return value;
	}

	private boolean ignoreCase;
	
	private String value = null;

}
