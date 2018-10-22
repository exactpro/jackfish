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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Automation;

namespace UIAdapter
{
    public class PluginInfo
    {
        private Dictionary<ControlKind, ControlType[]> kindMap;
        private Dictionary<LocatorFieldKind, String> locatorMap;

        public PluginInfo(Dictionary<ControlKind, ControlType[]> kindMap, Dictionary<LocatorFieldKind, String> locatorMap)
        {
            this.kindMap = kindMap;
            this.locatorMap = locatorMap;
        }

        public static PluginInfo infoFromString(string str)
        {
            /*
             * DON'T DELETE NEXT LINE. NEVER. 
             * It's not joke. 
             * Because if you will delete next row, parsing will failing.
             * I'm understand, that variable q never used.
             * Just don't delete line
             */ 
            var q = ControlType.Button.Id;
            return new PluginInfo(evalKindMap(str), evalLocatorMap(str));
        }

        private static Dictionary<ControlKind, ControlType[]> evalKindMap(string str)
        {
            Dictionary<ControlKind, ControlType[]> dic = new Dictionary<ControlKind, ControlType[]>();
            String kindPattern = @"KindMap{(.+?)}";
            Regex kindRegexp = new Regex(kindPattern, RegexOptions.IgnoreCase);
            Match kindMatch = kindRegexp.Match(str);
            String kindStr = kindMatch.Groups[1].Value;
            /*
                kindStr need be like this
                0:-1;2:50000,50031;3:50002;4:50003;5:50032;6:50032
            */
            String[] groups = kindStr.Split(';');
            foreach (String group in groups)
            {
                String[] smallGroups = group.Split(':');
                ControlKind key = (ControlKind)Int32.Parse(smallGroups[0]);
                String value = smallGroups[1];
                String[] types = value.Split(',');
                ControlType[] newTypes = new ControlType[types.Length];
                for (int i = 0; i < newTypes.Length; i++)
                {
                    newTypes[i] = ControlType.LookupById(Int32.Parse(types[i]));
                }
                dic.Add(key, newTypes);
            }
            return dic;
        }

        private static Dictionary<LocatorFieldKind, String> evalLocatorMap(String str)
        {
            Dictionary<LocatorFieldKind, String> dic = new Dictionary<LocatorFieldKind, string>();
            String locatorPattern = @"LocatorMap{(.+?)}";
            Regex locatorRegexp = new Regex(locatorPattern, RegexOptions.IgnoreCase);
            Match locatorMatch = locatorRegexp.Match(str);
            String locatorStr = locatorMatch.Groups[1].Value;

            String[] groups = locatorStr.Split(';');
            foreach (String group in groups)
            {
                String[] smallGroups = group.Split(':');
                LocatorFieldKind key = (LocatorFieldKind)Int32.Parse(smallGroups[0]);
                String value = smallGroups[1];
                dic.Add(key, value);
            }
            return dic;
        }

        public String toStr()
        {
            StringBuilder builder = new StringBuilder();
            builder.Append("KindMap{");
            String bigSep = "";
            foreach (ControlKind key in this.kindMap.Keys)
            {
                builder.Append(bigSep).Append((int)key).Append(":");
                ControlType[] a = this.kindMap[key];
                string sep = "";
                foreach (ControlType t in a)
                {
                    builder.Append(sep).Append(t == null ? -1 : t.Id);
                    sep = ",";
                }
                bigSep = ";";
            }
            builder.AppendLine("}");
            bigSep = "";
            builder.Append("LocatorMap{");
            foreach (LocatorFieldKind kind in this.locatorMap.Keys)
            {
                builder.Append(bigSep);
                String v = this.locatorMap[kind];
                builder.Append((int)kind).Append(":").Append(v);
                bigSep = ";";
            }
            builder.Append("}");
            return builder.ToString();
        }

        public Condition conditionByKind(ControlKind kind)
        {
            if (!this.kindMap.ContainsKey(kind))
            {
                return null;
            }
            if (kind == ControlKind.Wait)
            {
                return null;
            }
            if (kind == ControlKind.Any)
            {
                return Condition.TrueCondition;
            }
            ControlType[] controlTypes = this.kindMap[kind];
            if (controlTypes.Length == 0)
            {
                return null;
            }
            else if (controlTypes.Length == 1)
            {
                return new PropertyCondition(AutomationElement.ControlTypeProperty, controlTypes[0]);
            }
            else
            {
                Condition[] conds = new Condition[controlTypes.Length];
                for (int i = 0; i < conds.Length; i++)
                {
                    conds[i] = new PropertyCondition(AutomationElement.ControlTypeProperty, controlTypes[i]);
                }
                return new OrCondition(conds);
            }
        }

        public String[] nodeByKind(ControlKind kind)
        {
            if (!this.kindMap.ContainsKey(kind))
            {
                return new String[] { "*" };
            }
            ControlType[] types = this.kindMap[kind];
            String[] res = new String[types.Length];
            for (int i = 0; i < res.Length; i++)
            {
                string name = types[i].ProgrammaticName;
                string simpleName = name.Substring(name.IndexOf('.') + 1);
                res[i] = simpleName;
            }
            return res;
        }

        public String attributeName(LocatorFieldKind kind)
        {
            if (!this.locatorMap.ContainsKey(kind))
            {
                return null;
            }
            return this.locatorMap[kind];
        }
    }
}
