using System;
using System.Text;

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
                return System.Text.RegularExpressions.Regex.Unescape(entryString);
            }
        }
    }
}
