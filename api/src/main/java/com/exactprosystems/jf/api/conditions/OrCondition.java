/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@DescriptionAttribute(text = R.OR_CONDITION_DESCRIPTION)
public class OrCondition extends Condition
{
	private static final long serialVersionUID = 7676146584103803972L;

	public OrCondition(Condition ... cond) throws Exception
	{
		super(null);
		
		this.cond = cond == null ? Collections.emptyList() : Arrays.asList(cond);
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + this.cond.stream().map(s -> s.serialize()).reduce((s1,s2) -> s1 + separator + s2).orElse("") + finish;
	}
	
	@Override
	public String toString()
	{
		return this.cond.stream().map(s -> s.toString()).reduce((s1,s2) -> s1 + " OR " + s2).orElse("");
	}
	
	@Override
	public String getName()
	{
		return this.cond.stream().map(s -> s.getName()).findFirst().orElse("");
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return this.cond.stream().map(s -> s.isMatched(map)).reduce((s1, s2) -> s1 || s2).orElse(true);
	}

    @Override
    public String explanation(String name, Object actualValue)
    {
		return this.cond.stream().map(s -> "(" + s.explanation(name, actualValue) + ")").reduce((s1,s2) -> s1 + " | " + s2).orElse("");
    }
    
	private List<Condition> cond;
}
