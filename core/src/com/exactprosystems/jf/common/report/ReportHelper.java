/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportHelper
{
	protected static final Logger logger = Logger.getLogger(ReportHelper.class);

	private ReportHelper()
	{}

	public static String objToString(Object obj, boolean addQuotes)
	{
		String result = "";
		
		try
		{
			if (obj == null)
			{
				result = "null";
			}
			else if (obj instanceof Number)
			{
				if (obj instanceof Integer || obj instanceof BigInteger || obj instanceof Long
						|| obj instanceof Short || obj instanceof Byte)
				{
					result = String.format("%d", obj);
				}
				else
				{
					result = new DecimalFormat("#####################.#####################").format(obj);  
				}
			}
			else if (obj instanceof String)
			{
				if (addQuotes)
				{
					result = "" + Parser.preferredQuotes + obj + Parser.preferredQuotes;
				}
				else
				{
					result = "" + obj;
				}
			}
			else if (obj.getClass().isArray())
			{
				if (obj.getClass().getComponentType() == byte.class)
				{
					result =  Arrays.toString((byte[]) obj);
				}
				else
				{
					result =  Arrays.toString((Object[]) obj);
				}
			}
			else if (obj instanceof Date)
			{
				result = DateTime.strDateTime((Date)obj);
			}
			else if (obj instanceof MapMessage)
			{
				result = obj.toString();
			}
			else if (obj instanceof Map<?, ?>)
			{
				result = ((Map<?,?> ) obj).entrySet()
						.stream()
						.map(entry -> entry.getKey() + " : " + entry.getValue())
						.collect(Collectors.joining(", ", "[ ", " ]"));
			}
			else 
			{
				result = obj.toString();
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			result = e.getMessage();
		}
			
		return result;
	}
}
