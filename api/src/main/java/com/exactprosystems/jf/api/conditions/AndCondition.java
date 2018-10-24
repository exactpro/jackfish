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

import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@DescriptionAttribute(text = R.AND_CONDITION_DESCRIPTION)
public class AndCondition extends Condition
{
	private static final long serialVersionUID = -7043230215754395460L;

	public AndCondition(Condition ... cond) throws Exception
	{
		super(null);
		
		this.cond = cond == null ? Collections.emptyList() : Arrays.asList(cond);
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + this.cond.stream().map(ICondition::serialize).reduce((s1, s2) -> s1 + separator + s2).orElse("") + finish;
	}
	
	@Override
	public String toString()
	{
		return this.cond.stream().map(s -> s.toString()).reduce((s1,s2) -> s1 + " AND " + s2).orElse("");
	}
	
	@Override
	public String getName()
	{
		return this.cond.stream().map(s -> s.getName()).findFirst().orElse("");
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return this.cond.stream().map(c -> c.isMatched(map)).reduce((s1, s2) -> s1 && s2).orElse(true);
	}

    @Override
    public String explanation(String name, Object actualValue)
    {
		return this.cond.stream().map(s -> "(" + s.explanation(name, actualValue) + ")").reduce((s1,s2) -> s1 + " & " + s2).orElse("");
    }
    
	private List<Condition> cond;
}
