////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = R.RELATIVE_CONDITION_DESCRIPTION)
public abstract class RelativeCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 4502728417929487437L;

	public RelativeCondition(String name, String relationStr) throws Exception
	{
		super(name);
		this.relation = Relations.value(relationStr);
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

		Integer result = compare(value);
		if (result == null)
		{
			return false;
		}

		switch (this.relation)
		{
			case Less:		return result < 0;
			case LessEqual:	return result < 0 || result == 0;
			case Equal:		return result == 0;
			case Great:		return result > 0;
			case GreatEqual:return result > 0 || result == 0;
		}

		return false;
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "expected = '" + valueStr() + "' is not" + this.relation.sign + " actual = '" + actualValue + "'";
	}

	protected abstract String valueStr();

	protected abstract Integer compare(Object otherValue);
	
	protected enum Relations 
	{
		Less("<") , LessEqual("<="), Equal("=="), GreatEqual(">="), Great(">");
		
		Relations(String sign)
		{
			this.sign = sign;
		}
		
		public static Relations value(String str) throws Exception
		{
			for(Relations r : Relations.values())
			{
				if (r.sign.equals(str))
				{
					return r;
				}
			}
			
			throw new Exception("wrong value : " + str);
		}
		
		public String getSign()
		{
			return sign;
		}

		private String sign;
	}
	
	protected Relations relation = Relations.Equal;
}
