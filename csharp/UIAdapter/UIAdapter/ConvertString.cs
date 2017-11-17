////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary 
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
using System;
using System.Text;
using System.Text.RegularExpressions;

namespace UIAdapter
{
    class ConvertString
    {
        public static string replaceNonASCIIToUnicode(string entryString)
        {
            if (entryString == null)
            {
                return null;
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                foreach (char ch in entryString.ToCharArray())
                {
                    if (ch > 127)
                    {
                        sb.Append("\\u").Append(String.Format("{0:X}", Convert.ToInt32(ch)).PadLeft(4, '0'));
                    }
                    else
                    {
                        sb.Append(ch);
                    }
                }
                return sb.ToString();
            }
        }

        public static String replaceUnicodeSubStringToChar(String entryString)
        {
            if (entryString == null)
            {
                return null;
            }
            else
            {
                return Regex.Replace(entryString, @"\\u(?<Value>[a-zA-Z0-9]{4})", m =>
                {
                    return ((char)int.Parse(m.Groups["Value"].Value, System.Globalization.NumberStyles.HexNumber)).ToString();
                });
            }
        }
    }
}
