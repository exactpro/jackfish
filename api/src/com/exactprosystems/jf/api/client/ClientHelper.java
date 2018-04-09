/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.*;

public class ClientHelper
{
	private ClientHelper()
	{

	}

	public static Map<String, String> difference(MapMessage actual, ICondition[] conditions) throws Exception 
	{
		Map<String, String> result = new HashMap<String, String>();
		
		for (ICondition condition : conditions)
		{
			String name = condition.getName();
			Object actualValue = valueByName(actual, name);
			
			if (!condition.isMatched(actual))
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
		if (!Str.areEqual("*", messageType) && !Str.areEqual(message.getMessageType(), messageType))
		{
			return false;
		}
		if (conditions == null || conditions.length == 0)
		{
			return true;
		}
		
		for (ICondition condition : conditions)
		{
			if (!condition.isMatched(message))
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
	
	public static Set<Possibility> possibilities(Class<?> clazz)
	{
		return new HashSet<>(Arrays.asList(clazz.getAnnotation(ClientAttribute.class).possibilities()));
	}
	
	public static void errorIfDisable(Class<?> clazz, Possibility possibility) throws PossibilityIsDisable
	{
		if (!Arrays.asList(clazz.getAnnotation(ClientAttribute.class).possibilities()).contains(possibility))
		{
			throw new PossibilityIsDisable(String.format(R.CLIENT_HELPER_POSSIBILITY_IS_NOT_ALLOWED.get(), clazz.getSimpleName(), possibility.getDescription()));
		}
	}
}
