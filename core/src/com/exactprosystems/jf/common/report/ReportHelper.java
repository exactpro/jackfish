////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.documents.matrix.parser.Parser;

public class ReportHelper
{
	public static final String objToString(Object obj, boolean addQuotes)
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
					result = "" + Parser.prefferedQuotes + obj + Parser.prefferedQuotes;
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
				String separator = "";
				StringBuilder sb = new StringBuilder();
				sb.append("[ ");
				for (Entry<?, ?> entry : ((Map<?,?>)obj).entrySet())
				{
					sb.append(separator);
					sb.append(entry.getKey());
					sb.append(" : ");
					sb.append(entry.getValue());
					separator = ", ";
				}
				sb.append(" ]");
				result =  sb.toString();
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

	protected static final Logger logger = Logger.getLogger(ReportHelper.class);
}
