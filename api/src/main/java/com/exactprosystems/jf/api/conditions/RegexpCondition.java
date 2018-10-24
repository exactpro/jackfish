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
import java.util.regex.Pattern;

@DescriptionAttribute(text = R.REGEXP_CONDITION_DESCRIPTION)
public class RegexpCondition extends Condition implements Serializable
{
	private static final long serialVersionUID = -1292265002640952551L;

	public RegexpCondition(String name, String pattern)
	{
		super(name);
		this.pattern = pattern;
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + separator + this.pattern + finish;
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
		return Pattern.compile(this.pattern).matcher(strValue).find();
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "'" + String.valueOf(actualValue) + "' not suitable for regular expression '" + String.valueOf(this.pattern) + "'";
	}

	@Override
	public String toString()
	{
		return RegexpCondition.class.getSimpleName() + "[name="+getName()+", pattern=" +this.pattern+"]";
	}

	private String pattern;
}
