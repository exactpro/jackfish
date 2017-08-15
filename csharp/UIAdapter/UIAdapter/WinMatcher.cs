﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;
using System.Threading.Tasks;
using System.Windows.Automation;

namespace UIAdapter
{
    static class WinMatcher
    {
        public static AutomationElement[] Find(AutomationElement owner, ControlKind controlKind, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many, int maxTimeout)
        {
            Task<AutomationElement[]> task = Task<AutomationElement[]>.Factory.StartNew(() =>
            {
                AutomationElement[] ret = null;
                if (!String.IsNullOrEmpty(Xpath))
                {
                    long start = Program.getMilis();
                    ret = WinMatcher.FindByXpath(owner, controlKind, Uid, Xpath, Clazz, Name, Title, Text);
                    Program.logger.All("find elements by xpath", Program.getMilis() - start);
                }
                else
                {
                    long start = Program.getMilis();
                    // TODO not use find all with scope Descendants. use recursive invoke with scope Children, and see,
                    // that current element not table
                    Condition by = WinMatcher.Conditions(controlKind, Uid, Xpath, Clazz, Name, Title, Text);
                    if (!many)
                    {
                        AutomationElement firstElement;
                        Program.logger.All("Start find in descedants " + DateTime.Now.ToString(), -1);
                        firstElement = owner.FindFirst(TreeScope.Descendants, by);
                        Program.logger.All("End   find in descedants " + DateTime.Now.ToString(), -1);
                        if (firstElement == null)
                        {
                            if (controlKind == ControlKind.Wait)
                            {
                                return null;
                            }
                            if (isMatches(owner, Uid, Clazz, Name))
                            {
                                ret = new AutomationElement[1];
                                ret[0] = owner;
                                return ret;
                            }
                            String msg = String.Format("Element not found\ncontrolKind {0}\nuid {1}\nxpath{2}\nclazz{3}\nname{4}\ntitle{5}\ntext{6}", controlKind, Uid, Xpath, Clazz, Name, Title, Text);
                            Program.logger.All(msg, 0);
                            throw new Exception("Element not found");
                        }
                        ret = new AutomationElement[1];
                        ret[0] = firstElement;
                        return ret;
                    }
                    else
                    {
                        AutomationElementCollection col = owner.FindAll(TreeScope.Descendants, by);
                        if (col.Count == 0 && isMatches(owner, Uid, Clazz, Name))
                        {
                            return new AutomationElement[] { owner };
                        }
                        else
                        {
                            ret = new AutomationElement[col.Count];
                            int i = 0;
                            foreach (AutomationElement e in col)
                            {
                                ret[i++] = e;
                            }
                            return ret;
                        }
                    }
                }
                return ret;
            });
            try
            {
                Program.logger.All("Start found component during " + maxTimeout + " ms", -1);
                bool res = task.Wait(maxTimeout);
                Program.logger.All("Result is " + res, -1);
                if (res)
                {
                    return task.Result;
                }
                else
                {
                    Program.SetLastErrorNumber(7);
                    throw new Exception("Timeout waiting during " + maxTimeout + " ms");
                }
            }
            catch (AggregateException e)
            {
                Program.logger.All("Tut u nas exception : " + e.InnerException.Message, -1);
                throw e.InnerException;
            }
        }

        private static bool isMatches(AutomationElement owner, string uid, string clazz, string name)
        {
            AutomationElement.AutomationElementInformation info = owner.Current;
            if (uid == null && clazz == null && name == null)
            {
                return false;
            }
            if ((uid != null && uid != info.AutomationId) ||
                (clazz != null && clazz != info.ClassName) ||
                (name != null && name != info.Name))
            {
                return false;
            }
            return true;
        }

        public static void ClearCache()
        {
            cacheDoms.Clear();
        }

        public static Condition Conditions(ControlKind controlKind, string Uid, string Xpath, string Clazz, string Name, string Title, string Text)
        {
            List<Condition> list = new List<Condition>();
            AddToList(list, Program.pluginInfo.conditionByKind(controlKind));

            AddPropertyToList(list, Uid, AutomationElement.AutomationIdProperty);
            AddPropertyToList(list, Clazz, AutomationElement.ClassNameProperty);
            AddPropertyToList(list, Name, AutomationElement.NameProperty);
            AddPropertyToList(list, Title, AutomationElement.LabeledByProperty);
            AddPropertyToList(list, Text, AutomationElement.HelpTextProperty);

            Condition result = null;
            if (list.Count == 0)
            {
                result = Condition.FalseCondition;
            }
            else if (list.Count == 1)
            {
                result = list[0];
            }
            else
            {
                result = new AndCondition(list.ToArray());
            }

            return result;
        }

        private static void AddPropertyToList(List<Condition> list, string value, AutomationProperty property)
        {
            if (!string.IsNullOrEmpty(value))
            {
                AddToList(list, new PropertyCondition(property, value));
            }
        }

        private static void AddToList(List<Condition> list, Condition condition)
        {
            if (condition == null)
            {
                return;
            }
            list.Add(condition);
        }

        public static AutomationElement[] FindByXpath(AutomationElement owner, ControlKind controlKind, string Uid, string Xpath, string Clazz, string Name, string Title, string Text)
        {
            long start = Program.getMilis();
            XmlDocument document = null;
            int[] ownerRuntimeId = owner.GetRuntimeId();
            if (cacheDoms.ContainsKey(ownerRuntimeId))
            {
                document = cacheDoms[ownerRuntimeId];
            }
            else
            {
                document = new XmlDocument();
                BuildDom(document, document, owner);
                Program.logger.All("build dom", Program.getMilis() - start);
                cacheDoms.Add(ownerRuntimeId, document);
            }
            start = Program.getMilis();
            XmlElement root = document.DocumentElement;
            XmlNodeList list = root.SelectNodes(Xpath);
            Program.logger.All("find by xpath", Program.getMilis() - start);
            AutomationElement[] ret = new AutomationElement[list.Count];
            for (int i = 0; i < ret.Length; i++)
            {
                ret[i] = ((XmlElementWithObject)list[i]).UserData;
            }
            return ret;
        }

        private static string CreateXpath(ControlKind controlKind, string Uid, string Xpath, string Clazz, string Name, string Title, string Text)
        {
            if (!String.IsNullOrEmpty(Xpath))
            {
                return Xpath;
            }
            String[] ar = Program.pluginInfo.nodeByKind(controlKind);
            /*
            switch (controlKind)
            {
                case ControlKind.Button: ar = new string[] { "Button", "SplitButton" }; break;
                case ControlKind.CheckBox: ar = new string[] { "CheckBox" }; break;
                case ControlKind.ComboBox: ar = new string[] { "ComboBox" }; break;
                case ControlKind.Dialog: ar = new string[] { "Window" }; break;
                case ControlKind.Frame: ar = new string[] { "Window" }; break;
                case ControlKind.Label: ar = new string[] { "Text" }; break;
                case ControlKind.MenuItem: ar = new string[] { "MenuItem" }; break;
                case ControlKind.Panel: ar = new string[] { "Pane" }; break;
                case ControlKind.RadioGroup: ar = new string[] { "RadioButton" }; break;
                case ControlKind.Row: ar = new string[] { "Custom" }; break;
                case ControlKind.Table: ar = new string[] { "DataGrid", "Table" }; break;
                case ControlKind.TabPanel: ar = new string[] { "Tab" }; break;
                case ControlKind.TextBox: ar = new string[] { "Edit", "Document" }; break;
                case ControlKind.ToggleButton: ar = new string[] { "Button" }; break;
                case ControlKind.ListView: ar = new string[] { "List" }; break;
                case ControlKind.Tree: ar = new string[] { "Tree" }; break;
                case ControlKind.Tooltip: ar = new string[] { "ToolTip" }; break;
                case ControlKind.Image: ar = new string[] { "Image" }; break;
                case ControlKind.ProgressBar: ar = new string[] { "ProgressBar" }; break;
                case ControlKind.ScrollBar: ar = new string[] { "ScrollBar" }; break;
                case ControlKind.Slider: ar = new string[] { "Slider" }; break;
                default: ar = new string[] { "*" }; break;
            }
            */
            if (ar == null || ar.Length == 0)
            {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            String separator = "";
            foreach (var str in ar)
            {
                string filter = FilterForLocator(Uid, Clazz, Name, Title, Text);
                sb.Append(separator);

                sb.Append(".//").Append(str).Append("[").Append(filter).Append("]");
                separator = " | ";
            }
            return sb.ToString();
        }

        private static string FilterForLocator(string uid, string clazz, string name, string title, string text)
        {
            StringBuilder b = new StringBuilder();
            string separator = "";
            if (!String.IsNullOrEmpty(uid))
            {
                b.Append(separator); separator = " and ";
                b.Append("@uid='").Append(uid).Append("'");
            }
            if (!String.IsNullOrEmpty(clazz))
            {
                foreach (var part in clazz.Split(new char[] { ' ' }))
                {
                    b.Append(separator); separator = " and ";
                    if (part.StartsWith("!"))
                    {
                        b.Append("not (contains(@class,'").Append(part.Substring(1)).Append("'))");
                    }
                    else
                    {
                        b.Append("contains(@class,'").Append(part).Append("')");
                    }
                }
            }
            if (!String.IsNullOrEmpty(name))
            {
                b.Append(separator); separator = " and ";
                b.Append("@name='").Append(name).Append("'");
            }
            if (!String.IsNullOrEmpty(text))
            {
                b.Append(separator); separator = " and ";
                b.Append(".='").Append(text).Append("'");
            }
            if (!String.IsNullOrEmpty(title))
            {
                b.Append(separator); separator = " and ";
                b.Append("@title='").Append(title).Append("'");
            }
            return b.Length == 0 ? "*" : b.ToString();
        }

        public static AutomationElement[] GetElements(XmlElementWithObject owner, string xpath)
        {
            XmlNodeList list = owner.SelectNodes(xpath);
            AutomationElement[] res = new AutomationElement[list.Count];
            for (int i = 0; i < list.Count; i++)
            {
                res[i] = ((XmlElementWithObject)list[i]).UserData;
            }
            return res;
        }

        // for XPathBuilder
        public static void BuildDomForTree(XmlDocument document, XmlNode current, AutomationElement element, Dictionary<int[], AutomationElement> cacheRuntimeId)
        {
            if (element == null)
            {
                return;
            }

            int[] runtimeId = element.GetRuntimeId();
            XmlElement node = document.CreateElement("item");
            if (element.Current.FrameworkId.ToUpper().Contains("SILVER"))
            {
                if (Tag(element).ToUpper().Equals("TREEITEM"))
                {
                    AutomationElement header = null;
                    var ch = element.FindAll(TreeScope.Children, Condition.TrueCondition);
                    foreach (AutomationElement e in ch)
                    {
                        if (e.Current.AutomationId.ToUpper().Contains("HEADER"))
                        {
                            header = e;
                            break;
                        }
                    }

                    if (header != null)
                    {
                        TreeWalker walker = TreeWalker.RawViewWalker;
                        var child = walker.GetFirstChild(header);
                        while (child != null)
                        {
                            if (Tag(child).ToUpper().Equals("TEXT"))
                            {
                                node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.NAME).ToLower(), child.Current.Name);
                                break;
                            }
                            else
                            {
                                child = walker.GetNextSibling(child);
                            }
                        }
                    }
                }
            }
            else
            {
                node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.NAME).ToLower(), element.Current.Name);
            }
            node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.UID), element.Current.AutomationId);
            node.SetAttribute(RUNTIME_ID_ATTRIBUTE, string.Join(SEPARATOR, runtimeId));

            object obj;
            if (element.TryGetCurrentPattern(ExpandCollapsePattern.Pattern, out obj))
            {
                var valPattern = (ExpandCollapsePattern)obj;
                node.SetAttribute("state", valPattern.Current.ExpandCollapseState.ToString());
            }

            if (!cacheRuntimeId.ContainsKey(runtimeId))
            {
                cacheRuntimeId.Add(runtimeId, element);
            }

            if (Tag(element).ToUpper().Equals("TREEITEM") || Tag(element).ToUpper().Equals("TREE"))
            {
                current.AppendChild(node);
            }
            var col = element.FindAll(TreeScope.Children, Condition.TrueCondition);
            foreach (AutomationElement e in col)
            {
                BuildDomForTree(document, node, e, cacheRuntimeId);
            }
        }

        // for XPathBuilder
        public static void BuildDom(XmlDocument document, XmlNode current, AutomationElement element)
        {
            if (element == null)
            {
                return;
            }
            XmlElementWithObject node = null;
            string simpleName = Tag(element);
            node = XmlElementWithObject.Create(document.CreateElement(simpleName));
            node.UserData = element;
            node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.UID), element.Current.AutomationId);
            node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.CLAZZ), element.Current.ClassName);
            node.SetAttribute(Program.pluginInfo.attributeName(LocatorFieldKind.NAME).ToLower(), element.Current.Name);
            node.SetAttribute(RUNTIME_ID_ATTRIBUTE, string.Join(SEPARATOR, element.GetRuntimeId()));
            object obj;
            if (element.TryGetCurrentPattern(ValuePattern.Pattern, out obj))
            {
                try
                {
                    var valPattern = (ValuePattern)obj;
                    var value = valPattern.Current.Value;
                    if (!String.IsNullOrEmpty(value))
                    {
                        node.InnerText = value;
                    }
                }
                catch (Exception e)
                { }
            }

            if (!element.Current.FrameworkId.ToUpper().Equals("SILVERLIGHT") && simpleName.ToUpper().Equals("TABLE"))
            {
                return;
            }
            if (simpleName.ToUpper().Equals("COMBOBOX"))
            {
                current.AppendChild(node);
                return;
            }
            if (current.Name.ToUpper().Equals("DATAGRID"))
            {
                if (!simpleName.ToUpper().Equals("HEADER"))
                {
                    return;
                }
            }
            current.AppendChild(node);
            var col = element.FindAll(TreeScope.Children, Condition.TrueCondition);
            foreach (AutomationElement e in col)
            {
                BuildDom(document, node, e);
            }
        }

        private static string Tag(AutomationElement e)
        {
            string name = e.Current.ControlType.ProgrammaticName;
            string simpleName = name.Substring(name.IndexOf('.') + 1);
            return simpleName;
        }

        public class XmlElementWithObject : XmlElement
        {
            public static XmlElementWithObject Create(XmlElement element)
            {
                XmlElementWithObject newElement = new XmlElementWithObject(element.Prefix, element.LocalName, element.NamespaceURI, element.OwnerDocument);
                newElement.currentElement = element;
                return newElement;
            }

            public XmlElementWithObject(string prefix, string localName, string namespaceURI, XmlDocument doc)
                : base(prefix, localName, namespaceURI, doc)
            {

            }

            public AutomationElement UserData { get; set; }

            private XmlElement currentElement { get; set; }
        }

        private readonly static string RUNTIME_ID_ATTRIBUTE = "runtimeId";
        private readonly static string SEPARATOR = ",";

        private static Dictionary<int[], XmlDocument> cacheDoms = new Dictionary<int[], XmlDocument>(new Program.DictionaryMatcher());

    }
}