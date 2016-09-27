////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.Str;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientHelper
{
	public static Map<String, String> difference(MapMessage actual, ICondition[] conditions) throws Exception 
	{
		Map<String, String> result = new HashMap<String, String>();
		
		for (ICondition condition : conditions)
		{
			String name = condition.getName();
			Object actualValue = valueByName(actual, name);
			
			if (!condition.isMatched(name, actualValue))
			{
				result.put(name, condition.explanation(name, actualValue)); 
			}
		}
		
		if (result.size() > 0)
		{
			return result;
		}
		return null;
	}
	
	public static boolean isMatched(MapMessage message, String messageType, ICondition[] conditions) throws Exception
	{
		if (!Str.areEqual(message.getMessageType(), messageType))
		{
			return false;
		}
		if (conditions == null || conditions.length == 0)
		{
			return true;
		}
		
		for (ICondition condition : conditions)
		{
			String name = condition.getName();
			
			Object value = valueByName(message, name);
			
			if (!condition.isMatched(name, value))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static Object valueByName(MapMessage msg, String name) throws Exception
	{
		return msg.get(name);
	}
	
	public static Set<Possibility> possebilities(Class<?> clazz)
	{
		Set<Possibility> res = new HashSet<Possibility>();
		ClientAttribute attr = clazz.getAnnotation(ClientAttribute.class);
		for (Possibility possibility : attr.possibilities())
		{
			res.add(possibility);
		}
		return res;
	}
	
	public static void errorIfDisable(Class<?> clazz, Possibility possibility) throws PossibilityIsDisable
	{
		ClientAttribute attr = clazz.getAnnotation(ClientAttribute.class);
		for (Possibility poss : attr.possibilities())
		{
			if (poss == possibility)
			{
				return;
			}
		}
		
		throw new PossibilityIsDisable("For client " + clazz.getSimpleName() + " possibility " + possibility.getDescription() 
				+ " is not allowed.");
	}
	

}
