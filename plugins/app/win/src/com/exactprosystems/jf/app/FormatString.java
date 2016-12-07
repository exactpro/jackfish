package com.exactprosystems.jf.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatString
{
    public static String replaceNonASCIIToUnicode(String entryString)
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
                    sb.append("\\u").append(Integer.toHexString(ch));
                }
                else
                {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }

    public static String replaceUnicodeToChar(String entryString)
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