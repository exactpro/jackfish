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
