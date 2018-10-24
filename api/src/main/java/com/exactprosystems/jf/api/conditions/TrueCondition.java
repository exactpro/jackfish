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

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = R.TRUE_CONDITION_DESCRIPTION)
public class TrueCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 768671847855709009L;

	public TrueCondition()
	{
		super(null);
	}

	public TrueCondition(String name)
	{
		super(name);
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + finish;
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + "]";
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "";
	}
}
