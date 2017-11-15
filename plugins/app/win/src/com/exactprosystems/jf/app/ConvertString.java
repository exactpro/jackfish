////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertString
{
    public static String replaceNonASCIISymbolsToUnicodeSubString(String entryString)
    {
        if(entryString == null)
        {
            return null;
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (char ch : entryString.toCharArray()) {
                if (ch > 127)
                {
                    sb.append("\\u").append(String.format("%04x", (int)ch));
                }
                else
                {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }

    public static String replaceUnicodeSubStringsToCharSymbols(String entryString)
    {
        if(entryString == null) {
            return null;
        }
        else {
            Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
            Matcher m = p.matcher(entryString);
            StringBuffer buf = new StringBuffer(entryString.length());
            while (m.find()) {
                String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
                m.appendReplacement(buf, Matcher.quoteReplacement(ch));
            }
            m.appendTail(buf);
            return buf.toString();
        }
    }
}