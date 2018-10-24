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
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Map;

@DescriptionAttribute(text = R.NOT_CONDITION_DESCRIPTION)

public class NotCondition extends Condition
{
	private static final long serialVersionUID = 1308424226945776212L;

	public NotCondition(Condition cond)
	{
		super(null);
		this.cond = cond;
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + this.cond.serialize() + finish;
	}
	
	@Override
	public String toString()
	{
		return "Not " + this.cond.toString();
	}
	
	@Override
	public String getName()
	{
		return this.cond.getName();
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return !this.cond.isMatched(map);
	}

	@Override
	public boolean isMatchedName(String otherName)
	{
		return this.cond.isMatchedName(otherName);
	}
	
    @Override
    public String explanation(String name, Object actualValue)
    {
    	return " !(" + cond.explanation(name, actualValue) + ")";
    }

	private Condition cond;
}
