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