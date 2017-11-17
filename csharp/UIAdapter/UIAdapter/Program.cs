////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary 
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
using RGiesecke.DllExport;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.Globalization;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Forms;
using System.Xml;
using UIAdapter;
using UIAdapter.Win32;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Automation.Text;
using System.IO;
using UIAdapter.Logger;
using UIAdapter.Tables;
using WindowsInput;

namespace UIAdapter
{
    public class Program
    {
        #region util methods

        #region work with exceptions
        [DllExport("lastError", CallingConvention.Cdecl)]
        public static string LastError()
        {
            string error = lastError;
            lastError = null;
            return error;
        }

        [DllExport("lastErrorNumber", CallingConvention.Cdecl)]
        public static int LastErrorNumber()
        {
            int errorNum = lastErrorNumber;
            lastErrorNumber = 4; // Default value;
            return errorNum;
        }

        public static void SetLastErrorNumber(int erroNum)
        {
            lastErrorNumber = erroNum;
        }

        #endregion

        [DllExport("getFrameworkId", CallingConvention.Cdecl)]
        public static string GetFrameworId()
        {
            return frameWorkId;
        }

        [DllExport("maxTimeout", CallingConvention.Cdecl)]
        public static void MaxTimeout(int maxTimeout)
        {
            Program.maxTimeout = maxTimeout;
            logger.All("Set max timeout to : " + Program.maxTimeout);
        }

        [DllExport("createLogger", CallingConvention.Cdecl)]
        public static void CreateLogger(String logLevel)
        {
            try
            {
                logger.Dispose();
                logger = new Logger.Logger(Logger.Logger.logLevel(logLevel));
                logger.All("Create logger successful");
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        [DllExport("setPluginInfo", CallingConvention.Cdecl)]
        public static void SetPluginInfo(String str)
        {
            try
            {
                pluginInfo = PluginInfo.infoFromString(str);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }
        #endregion

        #region application methods
        [DllExport("connect", CallingConvention.Cdecl)]
        public static int Connect(string title, int height, int width, int pid, int controlKind, int timeout, bool alwaysToFront)
        {
            Task<int> task = Task<int>.Factory.StartNew(() =>
            {
                handler = null;
                process = null;
                title = ConvertString.replaceUnicodeSubStringToChar(title);

                int runningTime = 0;
                int TIMEWAIT = 100;
                while (true)
                {
                    if (runningTime > timeout)
                    {
                        throw new Exception("Could not find window still " + timeout + " ms");
                    }
                    Thread.Sleep(TIMEWAIT);
                    runningTime += TIMEWAIT;

                    try
                    {
                        long startMethod = getMilis();

                        ControlType findType = ControlType.Window;
                        if (controlKind != Int32.MinValue)
                        {
                            ControlKind kind = (ControlKind)controlKind;
                            findType = dic.FirstOrDefault(x => x.Value.Equals(kind)).Key;
                        }
                        AutomationElementCollection collection = AutomationElement.RootElement.FindAll(TreeScope.Children, new PropertyCondition(AutomationElement.ControlTypeProperty, findType));
                        logger.All("find all by findType : " + findType.ProgrammaticName);

                        foreach (AutomationElement e in collection)
                        {
                            logger.All("Found child with title : " + e.Current.Name + " and PID : " + e.Current.ProcessId);
                        }

                        bool sizeSet = height != Int32.MinValue && width != Int32.MinValue;
                        bool titleSet = !String.IsNullOrEmpty(title);
                        bool pidSet = pid != Int32.MinValue;

                        logger.All("Size set : " + sizeSet);
                        if (sizeSet) logger.All("H : " + height + " w : " + width);
                        logger.All("Title set : " + titleSet);
                        if (titleSet) logger.All("Title : " + title);
                        logger.All("Pid set : " + pidSet);
                        if (pidSet) logger.All("Pid : " + pid);

                        Predicate<AutomationElement> predicate = (e) =>
                        {
                            bool result = true;
                            if (sizeSet)
                            {
                                var rect = e.Current.BoundingRectangle;
                                logger.All("window " + e.Current.Name + " has rect : " + rect.ToString());
                                result &= (rect.Width == width && rect.Height == height);
                            }
                            if (titleSet)
                            {
                                result &= e.Current.Name.Contains(title);
                            }
                            if (pidSet)
                            {
                                result &= e.Current.ProcessId == pid;
                            }
                            return result;
                        };

                        List<AutomationElement> list = collection.Cast<AutomationElement>().Where(el => predicate(el)).ToList();
                        if (list.Count == 0)
                        {
                            continue;
                        }
                        if (list.Count > 1)
                        {
                            throw new Exception("Found " + list.Count + " windows instead 1 with parameters title=" + title + " , h = " + height + " , w = " + width);
                        }
                        handler = list.Single();
                        frameWorkId = handler.Current.FrameworkId;
                        logger.All("method Connect", getMilis() - startMethod);
                        return handler.Current.ProcessId;
                    }
                    catch (Exception e)
                    {
                        MakeError(e);
                        throw e;
                    }
                }
            }
            );
            try
            {
                bool res = task.Wait(timeout);
                logger.All("Connected successful ? " + res);
                if (res)
                {
                    Program.alwaysToFront = alwaysToFront;
                    toFront();
                    return task.Result;
                }
                else
                {
                    Program.SetLastErrorNumber(7);
                    MakeError(new Exception("Timeout waiting for connect to window during " + timeout + " ms"));
                    return -1;
                }
            }
            catch (AggregateException e)
            {
                MakeError(e.InnerException);
            }
            return -1;
        }

        [DllExport("run", CallingConvention.Cdecl)]
        public static int Run(string exec, string workDir, string param, bool alwaysToFront)
        {
            try
            {
                long startMethod = getMilis();
                workDir = workDir.Replace("/", "\\");

                ProcessStartInfo startInfo = new ProcessStartInfo()
                {
                    FileName = exec,
                    WorkingDirectory = workDir,
                    Arguments = param,
                };

                process = Process.Start(startInfo);
                process.Refresh();
                try
                {
                    process.WaitForInputIdle();
                }
                catch (Exception e)
                {
                    logger.All(e.Message);
                }
                
                Thread.Sleep(1000);
                UpdateHandler();
                frameWorkId = handler.Current.FrameworkId;
                Program.alwaysToFront = alwaysToFront;
                toFront();
                logger.All("method Run", getMilis() - startMethod);
                return handler.Current.ProcessId;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return -1;
        }

        [DllExport("stop", CallingConvention.Cdecl)]
        public static void Stop(bool needStop)
        {
            try
            {
                long startMethod = getMilis();
                if (needStop && process == null)
                {
                    process = Process.GetProcessById(handler.Current.ProcessId);
                }

                if (process != null)
                {
                    process.WaitForExit(2000);
                    process.Kill();
                    process = null;
                }
                logger.All("method Stop", getMilis() - startMethod);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        [DllExport("refresh", CallingConvention.Cdecl)]
        public static void Refresh()
        {
            try
            {
                long startMethod = getMilis();
                process.Refresh();
                logger.All("method Refresh", getMilis() - startMethod);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        [DllExport("title", CallingConvention.Cdecl)]
        public static string Title()
        {
            try
            {
                long startMethod = getMilis();
                UpdateHandler();
                var res = handler.Current.Name;
                logger.All("method Title", getMilis() - startMethod);
                return ConvertString.replaceNonASCIIToUnicode(res);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        #endregion

        #region find methods
        [DllExport("getList", CallingConvention.Cdecl)]
        public static string getList(string elementId, Boolean onlyVisible)
        {
            try
            {
                long startMethod = getMilis();

                AutomationElement owner = findOwner(elementId);
                logger.All("Fount element runtimeId : " + string.Join(",",owner.GetRuntimeId()));
                List<AutomationElement> listItems = getListItems(owner, onlyVisible);
                List<string> namesList = getNamesOfListItems(listItems);
                //TODO check if we have comboBox with checkboxes
                
                if (namesList.Count == 0)
                {
                    string result = string.Join(SEPARATOR_COMMA, namesList);
                    logger.All("method GetList", getMilis() - startMethod);
                    return ConvertString.replaceNonASCIIToUnicode(result);
                }
                bool isCheckboxes = TableFactory.GetFrameworkId(owner).Equals("Silverlight");
                if (namesList.Count == 1)
                {
                    isCheckboxes = false;
                }
                else
                {
                    string firstElement = namesList[0];
                    for (int i = 1; i < namesList.Count; i++)
                    {
                        if (!namesList[i].Equals(firstElement))
                        {
                            isCheckboxes = false;
                            break;
                        }
                    }
                }

                //TODO this code is unbelievable
                if (isCheckboxes)
                {
                    System.Windows.Point point;
                    AutomationElement node = null;
                    System.Windows.Rect rect = owner.Current.BoundingRectangle;
                    double x = rect.X + rect.Width/2;
                    double y = rect.Y + rect.Height + 1;
                    for (int i = 0; i < 100; i++)
                    {
                        point = new System.Windows.Point(x, y + i);
                        node = AutomationElement.FromPoint(point);
                        logger.All("Node " + i + ": " + node.Current.LocalizedControlType);
                        if (node.Current.ControlType.Equals(ControlType.CheckBox) || node.Current.ControlType.Equals(ControlType.List))
                        {
                            break;
                        }
                    }
                    logger.All("Found checkBox : " + node.Current.Name);
                    AutomationElementCollection checkboxes;
                    TogglePattern togglePattern;
                    TreeWalker treeWalker = TreeWalker.RawViewWalker;
                    do
                    {
                        if (node == null || node.Current.ControlType.Equals(ControlType.List))
                        {
                            break;
                        }
                        node = treeWalker.GetParent(node);
                        logger.All("ControlType node : " + node.Current.LocalizedControlType);

                    } while (true);

                    checkboxes = node.FindAll(TreeScope.Descendants, new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.CheckBox));
                    String result = "";
                    foreach (AutomationElement item in checkboxes)
                    {
                        bool needAdd = true;
                        if (onlyVisible)
                        {
                            needAdd = !item.Current.IsOffscreen;
                        }
                        if (needAdd)
                        {
                            string toggleString = "";
                            togglePattern = (TogglePattern)item.GetCurrentPattern(TogglePattern.Pattern);
                            toggleString = togglePattern.Current.ToggleState == ToggleState.On ? "+" : "-";
                            result += toggleString + item.Current.Name + SEPARATOR_COMMA;
                        }
                    }
                    logger.All("method GetList", getMilis() - startMethod);
                    return ConvertString.replaceNonASCIIToUnicode(result);
                }
                else
                {
                    string result = string.Join(SEPARATOR_COMMA, namesList);
                    logger.All("method GetList", getMilis() - startMethod);
                    return ConvertString.replaceNonASCIIToUnicode(result);
                }
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        [DllExport("listAll", CallingConvention.Cdecl)]
        public static string ListAll(string OwnerId, int controlkindId, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many, Boolean addInvisible)
        {
            try
            {
                long startMethod = getMilis();
                
                Xpath = ConvertString.replaceUnicodeSubStringToChar(Xpath);
                Name = ConvertString.replaceUnicodeSubStringToChar(Name);
                Title = ConvertString.replaceUnicodeSubStringToChar(Title);
                Text = ConvertString.replaceUnicodeSubStringToChar(Text);

                AutomationElement window = findOwner(OwnerId);
                ControlKind controlKind = (ControlKind)controlkindId;
                logger.All("Start get all components");
                AutomationElement[] elements = GetAllComponents(controlKind, window, Uid, Xpath, Clazz, Name, Title, Text, many, addInvisible);
                logger.All("End get all components. Found : " + elements.Length);
                StringBuilder result = new StringBuilder();
                foreach (AutomationElement element in elements)
                {
                    if (element.Current.IsOffscreen)
                    {
                        continue;
                    }
                    result.AppendLine("====================================================================================================");
                    result.AppendLine("Found:        " + element);
                    result.AppendLine("AutomationId: " + element.Current.AutomationId);
                    result.AppendLine("Class name:   " + element.Current.ClassName);
                    result.AppendLine("Control type: " + element.Current.ControlType.ProgrammaticName);
                    result.AppendLine("Name:         " + element.Current.Name);
                    result.AppendLine("Hwnd:         " + element.Current.NativeWindowHandle);

                    result.AppendLine("Patterns:");
                    foreach (var pattern in element.GetSupportedPatterns())
                    {
                        result.AppendLine(pattern.ProgrammaticName + "[" + pattern.Id + "]");
                    }

                    result.AppendLine("Properties:");
                    foreach (var property in element.GetSupportedProperties())
                    {
                        //we need this try for password field
                        try
                        {
                            var obj = element.GetCurrentPropertyValue(property);
                            string propertyValue = obj.ToString();
                            //TOOD mb check that obj is Enumeration
                            if (obj.GetType().IsArray)
                            {
                                object[] newVar = (object[])obj;
                                propertyValue = string.Join(",", newVar);
                            }
                            result.AppendLine(property.ProgrammaticName + "[" + property.Id + "] = " + propertyValue);
                        }
                        catch
                        { }

                    }
                    result.Append("#####");
                }
                var res = result.ToString();
                logger.All("method ListAll", getMilis() - startMethod);
                return res;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        [DllExport("findAllForLocator", CallingConvention.Cdecl)]
        public static int FindAllForLocator([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String oId, int controlkindId, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many, Boolean addInvisible)
        {
            try
            {
                long startMethod = getMilis();
                
                Xpath = ConvertString.replaceUnicodeSubStringToChar(Xpath);
                Name = ConvertString.replaceUnicodeSubStringToChar(Name);
                Title = ConvertString.replaceUnicodeSubStringToChar(Title);
                Text = ConvertString.replaceUnicodeSubStringToChar(Text);
                
                AutomationElement owner = findOwner(oId);
                ControlKind controlKind = (ControlKind)controlkindId;
                AutomationElement[] found = null;
                try
                {
                    found = GetAllComponents(controlKind, owner, Uid, Xpath, Clazz, Name, Title, Text, many, addInvisible);
                }
                catch (ElementNotAvailableException ex)
                {
                    if (owner == handler)
                    {
                        return 0;
                    }
                    throw ex;
                }

                if (found == null)
                {
                    return 0;
                }
                logger.All("Before size : " + found.Length);
                //remove all not visible elements
                int[][] res = found.Cast<AutomationElement>().Where(el => el.Current.IsOffscreen ? addInvisible : true).Select(e => e.GetRuntimeId()).ToArray();
                logger.All("After size : " + res.Length);
                int countOfElement = res.Length + 1;
                foreach (int[] r in res)
                {
                    countOfElement = countOfElement + r.Length + 1;
                }

                arr[0] = res.Length;
                int currentPosition = 1;
                foreach (int[] currentArray in res)
                {
                    arr[currentPosition++] = currentArray.Length;
                    foreach (int i in currentArray)
                    {
                        if (countOfElement > arr.Length)
                        {
                            return countOfElement;
                        }
                        arr[currentPosition++] = i;
                    }
                }
                logger.All("find all for locator, size : " + res.Length, getMilis() - startMethod);
                return countOfElement;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }

        [DllExport("findAll", CallingConvention.Cdecl)]
        public static int FindAll([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String ownerid, int scopeId, int propertyId, string value)
        {
            try
            {
                long startMethod = getMilis();
                value = ConvertString.replaceUnicodeSubStringToChar(value);
                AutomationElement owner = findOwner(ownerid);
                TreeScope treeScope = (TreeScope)scopeId;
                Condition condition;
                if (propertyId == -1)
                {
                    condition = Condition.TrueCondition;
                }
                else
                {
                    AutomationProperty property = AutomationProperty.LookupById((int)propertyId);
                    object objValue = value;
                    
                    if (propertyId == AutomationElement.ControlTypeProperty.Id)
                    {
                        objValue = ControlType.LookupById(Int32.Parse(value));
                    }
                    condition = new PropertyCondition(property, objValue);
                }

                long startUI = getMilis();
                AutomationElementCollection found = owner.FindAll(treeScope, condition);
                logger.All("findAll by scope and condition. Size : " + found.Count, getMilis() - startUI);
                if (found == null)
                {
                    return 0;
                }
                foreach (AutomationElement e in found)
                {
                    var runId = e.GetRuntimeId();
                    if (!cacheRuntimeId.ContainsKey(runId))
                    {
                        cacheRuntimeId.Add(runId, e);
                    }
                }
                int[][] res = found.Cast<AutomationElement>().Select(e => e.GetRuntimeId()).ToArray();
                int countOfElement = res.Length + 1;
                foreach (int[] r in res)
                {
                    countOfElement = countOfElement + r.Length + 1;
                }
                arr[0] = res.Length;
                int currentPosition = 1;
                foreach (int[] currentArray in res)
                {
                    arr[currentPosition++] = currentArray.Length;
                    foreach (int i in currentArray)
                    {
                        if (countOfElement > arr.Length)
                        {
                            return countOfElement;
                        }
                        arr[currentPosition++] = i;
                    }
                }
                logger.All("find all by scope", getMilis() - startMethod);
                return countOfElement;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }
        #endregion

        #region Other methods
        [DllExport("elementAttribute", CallingConvention.Cdecl)]
        public static string ElementAttribute(string inid, int partId)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                AttributeKind part = (AttributeKind)partId;

                AutomationElement element = null;
                string str = "";
                logger.All("Coordinates : " + string.Join(",", id));
                if (part == AttributeKind.ITEMS)
                {
                    // get value from combobox with checkboxes for Prime
                    System.Windows.Point point;
                    AutomationElement node = null;
                    for (int i = 0; i < 100; i++)
                    {
                        point = new System.Windows.Point(id[0], id[1] + i);
                        node = AutomationElement.FromPoint(point);
                        logger.All("Node "+i +": " + node.Current.LocalizedControlType);
                        if (node.Current.ControlType.Equals(ControlType.CheckBox) || node.Current.ControlType.Equals(ControlType.List))
                        {
                            break;
                        }
                    }
                    logger.All("Found checkBox : " + node.Current.Name);
                    AutomationElementCollection checkboxes;
                    TogglePattern togglePattern;
                    TreeWalker treeWalker = TreeWalker.RawViewWalker;
                    do
                    {
                        if (node == null || node.Current.ControlType.Equals(ControlType.List))
                        {
                            break;
                        }
                        node = treeWalker.GetParent(node);
                        logger.All("ControlType node : " + node.Current.LocalizedControlType);
                    } while (true);

                    string toggleString = "";
                    string nameString = "";

                    checkboxes = node.FindAll(TreeScope.Descendants, new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.CheckBox));
                    foreach (AutomationElement item in checkboxes)
                    {
                        togglePattern = (TogglePattern)item.GetCurrentPattern(TogglePattern.Pattern);
                        if (togglePattern.Current.ToggleState == ToggleState.On)
                        {
                            toggleString += "+";
                        }
                        else
                        {
                            toggleString += "-";
                        }
                        nameString += item.Current.Name + ";";
                    }
                    str = toggleString + ";" + nameString;
                }
                else
                {
                    element = FindByRuntimeId(id);
                    if (element != null)
                    {
                        switch (part)
                        {
                            case AttributeKind.ID:
                                str = IdFromStr(str, element.Current.Name);
                                str = IdFromStr(str, element.Current.AutomationId);
                                str = IdFromStr(str, element.Current.ClassName);
                                str = IdFromStr(str, element.Current.HelpText);
                                break;

                            case AttributeKind.UID:
                                str = element.Current.AutomationId;
                                break;

                            case AttributeKind.CLASS:
                                str = element.Current.ClassName;
                                break;

                            case AttributeKind.TEXT:
                                str = element.Current.HelpText;
                                break;

                            case AttributeKind.NAME:
                                str = element.Current.Name;
                                break;

                            case AttributeKind.TYPE_NAME:
                                string name = element.Current.ControlType.ProgrammaticName;
                                str = name.Substring(name.IndexOf('.') + 1);
                                break;

                            case AttributeKind.ENABLED:
                                str = "" + element.Current.IsEnabled;
                                break;

                            case AttributeKind.VISIBLE:
                                str = "" + !element.Current.IsOffscreen;
                                break;

                            default:
                                break;
                        }
                    }
                }
                logger.All("method Element attribute", getMilis() - startMethod);
                return ConvertString.replaceNonASCIIToUnicode(str);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        [DllExport("sendKey", CallingConvention.Cdecl)]
        public static void SendKey(String inid, String key)
        {
            try
            {
                long startMethod = getMilis();
                // TODO see this reference for understanding sendWait 
                // https://msdn.microsoft.com/ru-ru/library/system.windows.forms.sendkeys.send(v=vs.110).aspx

                int[] id = stringToIntArray(inid);
                if (id == null || id.Length == 0)
                {
                    return;
                }
                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                if (element.Current.IsKeyboardFocusable)
                {
                    element.SetFocus();
                }
                else
                {
                    AutomationElement parent = getFocusableParent(element);
                    if (parent != null)
                    {
                        parent.SetFocus();
                    }
                }
                VirtualKeyCode keyCode = KeyboardVirtual.getVirtualKeyCode(key);
                SimulateKeyPress(keyCode);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        private static AutomationElement getFocusableParent(AutomationElement element)
        {
            AutomationElement elementParent;
            elementParent = TreeWalker.ControlViewWalker.GetParent(element);

            if (elementParent == null)
            {
                return null;
            }

            if (elementParent.Current.IsKeyboardFocusable)
            {
                return elementParent;
            }
            else
            {
                return getFocusableParent(elementParent);
            }
        }

        private static void KeyDown(VirtualKeyCode keyCode)
        {
            var down = new INPUT();
            down.Type = (UInt32)InputType.KEYBOARD;
            down.Data.Keyboard = new KEYBDINPUT();
            down.Data.Keyboard.Vk = (UInt16)keyCode;
            // Scan Code here, was 0
            down.Data.Keyboard.Scan = (ushort)Win32.UnsafeNativeMethods.MapVirtualKey((UInt16)keyCode, 0);
            down.Data.Keyboard.Flags = 0;
            down.Data.Keyboard.Time = 0;
            down.Data.Keyboard.ExtraInfo = IntPtr.Zero;
            INPUT[] inputList = new INPUT[1];
            inputList[0] = down;

            var numberOfSuccessfulSimulatedInputs = Win32.UnsafeNativeMethods.SendInput(1,inputList, Marshal.SizeOf(typeof(INPUT)));
            if (numberOfSuccessfulSimulatedInputs == 0)
                throw new Exception(string.Format("The key press simulation for {0} was not successful.",keyCode));
        }

        private static void KeyUp(VirtualKeyCode keyCode)
        {
            var up = new INPUT();
            up.Type = (UInt32)InputType.KEYBOARD;
            up.Data.Keyboard = new KEYBDINPUT();
            up.Data.Keyboard.Vk = (UInt16)keyCode;
            // Scan Code here, was 0
            up.Data.Keyboard.Scan = (ushort)Win32.UnsafeNativeMethods.MapVirtualKey((UInt16)keyCode, 0);
            up.Data.Keyboard.Flags = (UInt32)KeyboardFlag.KEYUP;
            up.Data.Keyboard.Time = 0;
            up.Data.Keyboard.ExtraInfo = IntPtr.Zero;

            INPUT[] inputList = new INPUT[1];
            inputList[0] = up;

            var numberOfSuccessfulSimulatedInputs = Win32.UnsafeNativeMethods.SendInput(1,inputList, Marshal.SizeOf(typeof(INPUT)));
            if (numberOfSuccessfulSimulatedInputs == 0)
                throw new Exception(string.Format("The key press simulation for {0} was not successful.",keyCode));
        }

        private static void SimulateKeyPress(VirtualKeyCode keyCode)
        {
            KeyDown(keyCode);
            KeyUp(keyCode);
        }

        [DllExport("upAndDown", CallingConvention.Cdecl)]
        public static void UpAndDown(String inid, String key, bool isDown)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                if (id == null || id.Length == 0)
                {
                    return;
                }
                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                if (element.Current.IsKeyboardFocusable)
                {
                    element.SetFocus();
                }
                else
                {
                    AutomationElement parent = getFocusableParent(element);
                    if (parent != null)
                    {
                        parent.SetFocus();
                    }
                }

                VirtualKeyCode keyCode = 0x00;

                if (key.ToUpper().Equals("SHIFT"))
                {
                    keyCode = VirtualKeyCode.SHIFT;
                }
                else if (key.ToUpper().Equals("CONTROL"))
                {
                    keyCode = VirtualKeyCode.CONTROL;
                }
                else if (key.ToUpper().Equals("ALT"))
                {
                    keyCode = VirtualKeyCode.MENU;
                }
                if (keyCode != 0x00)
                {
                    if (isDown)
                    {
                        KeyDown(keyCode);
                    }
                    else
                    {
                        KeyUp(keyCode);
                    }
                }
                logger.All("method upAndDown", getMilis() - startMethod);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        private static string addModifiers(string key)
        {
            string newKey = "";
            if (isShiftDown)
            {
                newKey += "+";
            }
            if (isAltDown)
            {
                newKey += "%";
            }
            if (isControlDown)
            {
                newKey += "^";
            }
            return newKey + key;
        }

        [DllExport("mouse", CallingConvention.Cdecl)]
        public static void Mouse(String inid, int actionId, int x, int y)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                if (id == null || id.Length == 0)
                {
                    return;
                }
                UpdateHandler();
                toFront();
                AutomationElement element = FindByRuntimeId(id);
                MouseToElement(element, actionId, x, y);
                logger.All("method mouse", getMilis() - startMethod);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        [DllExport("dragNdrop", CallingConvention.Cdecl)]
        public static void DragNDrop(int x1, int y1, int x2, int y2)
        {
            try
            {
                Cursor.Position = new System.Drawing.Point(x1, y1);
                Win32.SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_LEFTDOWN, 0,0,0,0);
                Thread.Sleep(100);
                Cursor.Position = new System.Drawing.Point(x2, y2);
                Win32.SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        /**
	    * if @param c == -1 -> arg is null;
	    * if @param c == 0 -> arg is array of string with separator %
	    * if @param c == 1 -> arg is array of int with separator %
	    * if @param c == 2 -> arg is array of double with separator %
	    * if @param c == 3 -> arg is array of WindowVisualState with separator %
	    */
        [DllExport("doPatternCall", CallingConvention.Cdecl)]
        public static string DoPatternCall(String inid, int patternId, string method, string arg, int c)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                object[] args = null;

                if (c == -1)
                {
                    args = null;
                }
                else if (c == 0)
                {
                    args = arg.Split(new char[] { '%' });
                }
                else if (c == 1)
                {
                    string[] a = arg.Split(new char[] { '%' });
                    args = new object[a.Length];
                    for (int i = 0; i < a.Length; i++)
                    {
                        args[i] = Int32.Parse(a[i]);
                    }
                }
                else if (c == 2)
                {
                    string[] a = arg.Split(new char[] { '%' });
                    args = new object[a.Length];
                    for (int i = 0; i < a.Length; i++)
                    {
                        args[i] = Double.Parse(a[i]);
                    }
                }
                else if (c == 3)
                {
                    string[] a = arg.Split(new char[] { '%' });
                    args = new object[a.Length];
                    for (int i = 0; i < a.Length; i++)
                    {
                        args[i] = stringToWindowState(a[i]);
                    }
                }

                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                AutomationPattern pattern = AutomationPattern.LookupById((int)patternId);

                object elementPattern;
                string resStr = null;
                if (element.TryGetCurrentPattern(pattern, out elementPattern))
                {
                    if (elementPattern is ValuePattern)
                    {
                        var valuePattern = elementPattern as ValuePattern;
                        var isReadOnly = valuePattern.Current.IsReadOnly;
                        if (isReadOnly)
                        {
                            throw new Exception("Cant set value via ValuePattern, because element is read only");
                        }
                    }

                    else if (elementPattern is TransformPattern)
                    {
                        //If window is minimize or maximize, we need to set normal state, cause we can change size only on NormalState
                        object windowPattern;
                        if (element.TryGetCurrentPattern(WindowPattern.Pattern, out windowPattern))
                        {
                            var windowP = windowPattern as WindowPattern;
                            if (windowP.Current.WindowVisualState != WindowVisualState.Normal)
                            {
                                throw new Exception("Current state is " + windowP.Current.WindowVisualState + ", but need normal");
                            }
                        }
                        //TODO set position via win api
                        IntPtr hWnd = new IntPtr(element.Current.NativeWindowHandle);
                        logger.All("Window handle : " + hWnd);
                        MyRect rect = new MyRect();
                        bool f = Win32.UnsafeNativeMethods.GetWindowRect(hWnd, ref rect);
                        logger.All("GetWindowRect : " + f);
                        logger.All(String.Format("Window has left {0}, top {1}, right {2}, bottom {3}", rect.Left, rect.Top, rect.Right, rect.Bottom));
                        bool f1;
                        if (method.ToUpper().Equals("MOVE"))
                        {
                            int w = Math.Abs(rect.Left - rect.Right);
                            int h = Math.Abs(rect.Top - rect.Bottom);
                            f1 = Win32.UnsafeNativeMethods.MoveWindow(hWnd, (int)args[0], (int)args[1], w, h, true);
                        }
                        else
                        {
                            f1 = Win32.UnsafeNativeMethods.MoveWindow(hWnd, rect.Left, rect.Top, (int)args[0], (int)args[1], true);
                        }
                        logger.All("Move window : " + f1);
                        bool f2 = Win32.UnsafeNativeMethods.GetWindowRect(hWnd, ref rect);
                        logger.All("GetWindowRect : " + f2);
                        logger.All(String.Format("Window has left {0}, top {1}, right {2}, bottom {3}", rect.Left, rect.Top, rect.Right, rect.Bottom)); 
                        return null;
                    }
                    else if (elementPattern is WindowPattern)
                    {
                        //args not null if we try to change visual state of window.
                        //args is null if we try to close window. We use MethodInvoke for call this method
                        if (args != null)
                        {
                            WindowPattern windowPattern = elementPattern as WindowPattern;
                            WindowVisualState windowState = (WindowVisualState)args[0];
                            //if we try to min/max window, that was min/max - just reutrn
                            if (windowPattern.Current.WindowVisualState == windowState)
                            {
                                return null;
                            }
                        }
                    }
                    else if (elementPattern is ExpandCollapsePattern)
                    {
                        ExpandCollapsePattern expandCollapsePattern = elementPattern as ExpandCollapsePattern;
                        switch (expandCollapsePattern.Current.ExpandCollapseState)
                        {
                            case ExpandCollapseState.Collapsed:
                                if (method.ToUpper().Equals("EXPAND"))
                                {
                                    expandCollapsePattern.Expand();
                                }
                                break;
                            case ExpandCollapseState.Expanded:
                                
                                if (method.ToUpper().Equals("COLLAPSE"))
                                {
                                    expandCollapsePattern.Collapse();
                                }
                                break;
                        }
                        return null;
                    }

                    MethodInfo info = elementPattern.GetType().GetMethod(method);
                    object res = info.Invoke(elementPattern, args);

                    resStr = res == null ? null : res.ToString();
                }
                else
                {
                    throw new Exception("Pattern " + pattern.ProgrammaticName + " is not found!");
                }
                logger.All("method do pattern call", getMilis() - startMethod);
                return resStr;
            }
            catch (Exception e)
            {
                MakeError(e, MethodBase.GetCurrentMethod().ToString());
            }
            return null;
        }

        [DllExport("getXMLFromTree", CallingConvention.Cdecl)]
        public static string getXMLFromTree(String inid)
        {
            int[] id = stringToIntArray(inid);
            AutomationElement element = FindByRuntimeId(id);

            XmlDocument doc = new XmlDocument();
            WinMatcher.BuildDomForTree(doc, doc, element, cacheRuntimeId);

            return ConvertString.replaceNonASCIIToUnicode(doc.InnerXml);
        }

        [DllExport("setText", CallingConvention.Cdecl)]
        public static void SetText(String inid, String text)
        {
            try
            {
                text = ConvertString.replaceUnicodeSubStringToChar(text);
                //see this reference https://msdn.microsoft.com/ru-ru/library/ms750582(v=vs.110).aspx
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                AutomationElement element = FindByRuntimeId(id);
                SetTextToElement(element, text);
                logger.All("method setText", getMilis() - startMethod);
            }
            catch (Exception e)
            {
                MakeError(e, MethodBase.GetCurrentMethod().ToString());
            }
        }

        [DllExport("getProperty", CallingConvention.Cdecl)]
        public static string GetProperty(String inid, int propertyId)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                AutomationProperty property = AutomationProperty.LookupById((int)propertyId);
                if (property == AutomationElement.IsTextPatternAvailableProperty)
                {
                    TextPattern textPattern = element.GetCurrentPattern(TextPattern.Pattern) as TextPattern;
                    TextPatternRange textVizRange = textPattern.GetVisibleRanges().FirstOrDefault();
                    return ConvertString.replaceNonASCIIToUnicode(textVizRange.GetText(Int32.MaxValue));
                }
                if (property == AutomationElement.IsRangeValuePatternAvailableProperty)
                {
                    RangeValuePattern rangePattern = element.GetCurrentPattern(RangeValuePattern.Pattern) as RangeValuePattern;
                    var currentValue = rangePattern.Current.Value;
                    return ConvertString.replaceNonASCIIToUnicode("" + currentValue);
                }

                object ret = element.GetCurrentPropertyValue(property);
                if (ret == null)
                {
                    return null;
                }
                Type type = ret.GetType();
                if (type.IsArray)
                {
                    object[] a = (object[])ret;
                    if (a.Length == 0)
                    {
                        return null;
                    }
                    object o1 = a[0];
                    if (o1.GetType().IsAssignableFrom(typeof(AutomationElement)))
                    {
                        string[] rr = new string[a.Length];
                        for (int i = 0; i < a.Length; i++)
                        {
                            var q = (AutomationElement)a[i];
                            TreeWalker rawViewWalker = TreeWalker.RawViewWalker;
                            AutomationElement textBlock = rawViewWalker.GetFirstChild(q);
                            if (textBlock == null)
                            {
                                rr[i] = q.Current.Name;
                            }
                            else
                            {
                                string s = "";
                                while (textBlock != null)
                                {
                                    s = s + textBlock.Current.Name;
                                    textBlock = rawViewWalker.GetNextSibling(textBlock);
                                }
                                rr[i] = s;
                            }
                        }
                        logger.All("method getProperty", getMilis() - startMethod);
                        return ConvertString.replaceNonASCIIToUnicode(string.Join(",", rr));
                    }
                    logger.All("method getProperty", getMilis() - startMethod);
                    return ConvertString.replaceNonASCIIToUnicode(string.Join(",", a));
                }
                logger.All("method getProperty", getMilis() - startMethod);
                return ConvertString.replaceNonASCIIToUnicode(ret.ToString());
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        [DllExport("getPatterns", CallingConvention.Cdecl)]
        public static int GetPatterns([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String inid)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                int[] res = element.GetSupportedPatterns().Select(e => e.Id).ToArray();
                CopyArray(res, arr, len);
                logger.All("method getPatterns", getMilis() - startMethod);
                return res.Length;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }

        [DllExport("getProperties", CallingConvention.Cdecl)]
        public static int GetProperties([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String inid)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                UpdateHandler();
                AutomationElement element = FindByRuntimeId(id);
                int[] res = element.GetSupportedProperties().Select(e => e.Id).ToArray();
                CopyArray(res, arr, len);
                logger.All("method getProperties", getMilis() - startMethod);
                return res.Length;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }

        [DllExport("getImage", CallingConvention.Cdecl)]
        public static int GetImage([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String inid)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                toFront();                                
                AutomationElement component = FindByRuntimeId(id);
                var rect = component.Current.BoundingRectangle;
                Bitmap bmp = new Bitmap((int)rect.Width, (int)rect.Height, PixelFormat.Format24bppRgb);
                using (Graphics g = Graphics.FromImage(bmp))
                {
                    g.CopyFromScreen(new System.Drawing.Point((int)rect.X, (int)rect.Y), System.Drawing.Point.Empty, bmp.Size);
                }

                int Width = bmp.Width;
                int Height = bmp.Height;
                int[] Pixels = new int[2 + Height * Width];
                int count = 0;
                Pixels[count++] = Width;
                Pixels[count++] = Height;
                for (int y = 0; y < Height; y++)
                {
                    for (int x = 0; x < Width; x++)
                    {
                        Color pixel = bmp.GetPixel(x, y);
                        Pixels[count++] = pixel.ToArgb();
                    }
                }

                CopyArray(Pixels, arr, len);
                logger.All("method getImage", getMilis() - startMethod);
                return Pixels.Length;
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }

        [DllExport("clearCache", CallingConvention.Cdecl)]
        public static void ClearCache()
        {
            try
            {
                cacheRuntimeId.Clear();
                cacheLocators.Clear();
                WinMatcher.ClearCache();
            }
            catch (Exception e)
            {
                MakeError(e);
            }
        }

        #endregion`

        #region table methods

        #region with cells
        [DllExport("getValueTableCell", CallingConvention.Cdecl)]
        public static string GetValueTableCell(String tableId, int column, int row)
        {
            try
            {
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                AbstractTable abstractTable = TableFactory.createTable(table);
                return ConvertString.replaceNonASCIIToUnicode(abstractTable.GetValueRowCell(abstractTable.FindCell(column, row)));
            }
            catch (Exception e)
            {
                MakeError(e, MethodBase.GetCurrentMethod().ToString());
            }
            return "";
        }

        [DllExport("mouseTableCell", CallingConvention.Cdecl)]
        public static void MouseTableCell(String tableId, int column, int row, int mouseAction)
        {
            try
            {
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                var cell = TableFactory.createTable(table).FindCell(column, row);
                MouseToElement(cell, mouseAction, int.MinValue, int.MinValue);
            }
            catch (Exception e)
            {
                MakeError(e, MethodBase.GetCurrentMethod().ToString());
            }
        }

        [DllExport("textTableCell", CallingConvention.Cdecl)]
        public static void TextTableCell(String tableId, int column, int row, String text)
        {
            try
            {
                text = ConvertString.replaceUnicodeSubStringToChar(text);

                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                var cell = TableFactory.createTable(table).FindCell(column, row);
                SetTextToElement(cell, text);
            }
            catch (Exception e)
            {
                MakeError(e, MethodBase.GetCurrentMethod().ToString());
            }
        }

        #endregion

        #region with rows
        [DllExport("getRowByIndex", CallingConvention.Cdecl)]
        public static String GetRowByIndex(String tableId, Boolean useNumericHeader, int index)
        {
            try
            {
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return ConvertString.replaceNonASCIIToUnicode(TableFactory.createTable(table).GetRowByIndex(index));
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return "";
        }

        [DllExport("getTableSize", CallingConvention.Cdecl)]
        public static int GetTableSize(String tableId)
        {
            try
            {
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return TableFactory.createTable(table).GetTableSize();
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return 0;
        }

        [DllExport("getRowByCondition", CallingConvention.Cdecl)]
        public static String GetRow(String tableId, Boolean useNumericHeader, String condition, String columns)
        {
            try
            {
                columns = ConvertString.replaceUnicodeSubStringToChar(columns);
                long start = getMilis();
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return ConvertString.replaceNonASCIIToUnicode(TableFactory.createTable(table).GetRow(condition, columns));
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        [DllExport("getRowIndexes", CallingConvention.Cdecl)]
        public static String GetRowIndexes(String tableId, Boolean useNumericHeader, String condition, String columns)
        {
            try
            {
                columns = ConvertString.replaceUnicodeSubStringToChar(columns);
                long start = getMilis();
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return ConvertString.replaceNonASCIIToUnicode(TableFactory.createTable(table).GetRowIndexes(condition, columns));
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }
        #endregion

        [DllExport("getTable", CallingConvention.Cdecl)]
        public static String GetTable(String tableId, Boolean useNumericHeader)
        {
            try
            {
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return ConvertString.replaceNonASCIIToUnicode(TableFactory.createTable(table).GetTable());
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

       #endregion

        #region private methods

        private static List<AutomationElement> getListItems(AutomationElement element, bool onlyVisible)
        {
            TreeWalker walkerContent = TreeWalker.ContentViewWalker;
            AutomationElement child = walkerContent.GetFirstChild(element);
            List<AutomationElement> listItems = new List<AutomationElement>();
            while (child != null)
            {
                var ct = child.Current.ControlType;
                if (ct == ControlType.ListItem || ct == ControlType.DataItem || ct == ControlType.TabItem)
                {
                    bool needAdd = true;
                    if (onlyVisible)
                    {
                        needAdd = !child.Current.IsOffscreen;
                    }
                    if (needAdd)
                    {
                        listItems.Add(child);
                    }
                }
                child = walkerContent.GetNextSibling(child);
            }
            return listItems;
        }

        private static List<string> getNamesOfListItems(List<AutomationElement> listItems)
        {
            List<string> resultList = new List<string>();
            TreeWalker walkerRaw = TreeWalker.RawViewWalker;
            foreach (AutomationElement listItem in listItems)
            {
                AutomationElement childOfListItem = walkerRaw.GetFirstChild(listItem);
                if (childOfListItem == null)
                {
                    resultList.Add(listItem.Current.Name);
                }
                else
                {
                    resultList.Add(childOfListItem.Current.Name);
                }
            }
            return resultList;
        }

        private static void SetTextToElement(AutomationElement element, string text)
        {
            int windowHandle = (int)element.GetCurrentPropertyValue(AutomationElement.NativeWindowHandleProperty);
            logger.All("Get element handle : " + windowHandle);
            if (windowHandle != 0x0)
            {
                logger.All("start send message via winAPI");
                IntPtr ptr = new IntPtr(windowHandle);              // WM_SETTEXT = 0x000C
                IntPtr ret = UIAdapter.Win32.UnsafeNativeMethods.SendMessage(ptr, 0x000C, IntPtr.Zero, text);
                logger.All("end send message via winAPI. Return value : " + ret);

                //if returned value is -1 ( i think this mean, that text not setted and i don't see the text on gui) try to use ValuePattern for setting text;
                if (ret.Equals(new IntPtr(-1)))
                {
                    object obj = null;
                    if (element.TryGetCurrentPattern(ValuePattern.Pattern, out obj))
                    {
                        var vp = (ValuePattern)obj;
                        //TODO need add check that !vp.Current.IsReadOnly ???
                        vp.SetValue(text);
                    }
                }
            }
            else
            {
                object obj = null;
                if (element.TryGetCurrentPattern(ValuePattern.Pattern, out obj))
                {
                    var vp = (ValuePattern)obj;
                    //TODO need add check that !vp.Current.IsReadOnly ???
                    vp.SetValue(text);
                }
            }
        }

        private static void MouseToElement(AutomationElement element, int actionId, int x, int y)
        {
            MouseAction mouseAction = (MouseAction)actionId;

            var rect = element.Current.BoundingRectangle;
            var point = rect.TopLeft;
            int X, Y;
            if (x == Int32.MinValue || y == Int32.MinValue)
            {
                X = (int)(point.X + rect.Width / 2);
                Y = (int)(point.Y + rect.Height / 2);
            }
            else
            {
                X = (int)(x + point.X);
                Y = (int)(y + point.Y);
            }

            logger.All("element name : " + element.Current.Name + " , x = " + x + " y = " + y);

            Cursor.Position = new System.Drawing.Point(X, Y);
            switch (mouseAction)
            {
                case MouseAction.Move:
                    break;

                case MouseAction.LeftClick:
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_LEFTDOWN | MouseConstants.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
                    break;

                case MouseAction.RightClick:
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_RIGHTDOWN | MouseConstants.MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0);
                    break;

                case MouseAction.LeftDoubleClick:
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_LEFTDOWN | MouseConstants.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
                    Thread.Sleep(150);
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_LEFTDOWN | MouseConstants.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
                    break;

                case MouseAction.RightDoubleClick:
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_RIGHTDOWN | MouseConstants.MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0);
                    Thread.Sleep(150);
                    SafeNativeMethods.mouse_event(MouseConstants.MOUSEEVENTF_RIGHTDOWN | MouseConstants.MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0);
                    break;
            }
        }

        private static string IdFromStr(string id, string str)
        {
            if (!String.IsNullOrWhiteSpace(id))
            {
                return id;
            }

            TextInfo textInfo = new CultureInfo("en-US", false).TextInfo;
            string ret = textInfo.ToTitleCase(str.Replace(" ", ""));
            if (ret.Length > 0 && ret[0] >= '0' && ret[0] <= '9')
            {
                ret = "_" + ret;
            }
            return ret;
        }

        private static AutomationElement[] GetAllComponents(ControlKind controlKind, AutomationElement window, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many, Boolean addIvisible)
        {
            long startUI = getMilis();
            SimpleLocator locator = new SimpleLocator(controlKind, Uid, Xpath, Clazz, Name, Title, Text, many);
            AutomationElement[] ret = null;
            //try to find locator on cache
            if (cacheLocators.ContainsKey(locator))
            {
                ret = cacheLocators[locator];
            }
            else
            {
                logger.All("Start found components");
                ret = WinMatcher.Find(window, controlKind, Uid, Xpath, Clazz, Name, Title, Text, many, maxTimeout);
                logger.All("Found : " + ret.Length);
                if (ret != null) //TODO this rigth only for ControlKind.Wait
                {
                    cacheLocators.Add(locator, ret);
                }
                else
                {
                    return null;
                }
            }

            foreach (var e in ret)
            {
                var runId = e.GetRuntimeId();
                if (!cacheRuntimeId.ContainsKey(runId))
                {
                    cacheRuntimeId.Add(runId, e);
                }
            }
            logger.All(" method getAllComponents", getMilis() - startUI);
            return ret;
        }

        private static int[] stringToIntArray(String s)
        {
            if (s == null)
            {
                return null;
            }
            String[] temp = s.Split(',');
            int[] id = new int[temp.Length];
            for (int i = 0; i < temp.Length; ++i)
            {
                id[i] = Convert.ToInt32(temp[i]);
            }
            return id;
        }

        private static String intArrayToString(int[] a)
        {
            return string.Join(",", a);
        }

        private static AutomationElement FindByRuntimeId(int[] id)
        {
            if (id == null)
            {
                throw new Exception("Runtime id can't be null");
            }
            AutomationElement element = null;
            cacheRuntimeId.TryGetValue(id, out element);
            if (element == null)
            {
                //TODO mb find this 
                throw new Exception("cant find element by runtime id " + intArrayToString(id));
            }
            return element;
        }

        private static ControlKind ByClassName(ControlType controlType)
        {
            if (dic.ContainsKey(controlType))
            {
                return dic[controlType];
            }
            return ControlKind.Any;
        }

        private static void UpdateHandler()
        {
            if (handler == null && process != null)
            {
                int runningTime = 0;
                int MAXTIME = 60000; // wait 60 second before throw exception
                int TIMEWAIT = 100;
                IntPtr mainWindowHandle = process.MainWindowHandle;
                logger.All("Current process name : " + process.ProcessName);

                while (mainWindowHandle.Equals(IntPtr.Zero))
                {
                    if (runningTime > MAXTIME)
                    {
                        throw new Exception("Could not find window still 60 seconds");
                    }
                    List<Process> children = GetChildProcesses(process);
                    if (children.Count == 0)
                    {
                        children.Add(process);
                    }

                    logger.All("Child count : " + children.Count);
                    bool isExit = false;
                    foreach (Process p in children)
                    {
                        logger.All("Child process name : " + p.ProcessName + " and windowHandle : " + p.MainWindowHandle);
                        if (!p.MainWindowHandle.Equals(IntPtr.Zero))
                        {
                            isExit = true;
                            mainWindowHandle = p.MainWindowHandle;
                            break;
                        }
                    }
                    if (isExit)
                    {
                        break;
                    }
                    Thread.Sleep(TIMEWAIT);
                    runningTime += TIMEWAIT;
                    bool idle = process.WaitForInputIdle();
                    logger.All("idle : " + idle + " and refresh...");
                    process.Refresh();
                }
                handler = AutomationElement.FromHandle(mainWindowHandle);
            }
        }

        public static List<Process> GetChildProcesses(Process process)
        {
            List<Process> children = new List<Process>();
            System.Management.ManagementObjectSearcher mos = new System.Management.ManagementObjectSearcher(String.Format("Select * From Win32_Process Where ParentProcessID={0}", process.Id));

            foreach (System.Management.ManagementObject mo in mos.Get())
            {
                children.Add(Process.GetProcessById(Convert.ToInt32(mo["ProcessID"])));
            }
            return children;
        }

        private static void CopyArray(int[] from, int[] to, int lenTo)
        {
            for (int i = 0; i < Math.Min(from.Length, lenTo); i++)
            {
                to[i] = from[i];
            }
        }

        private static void MakeError(Exception e, string info)
        {
            lastError = info + "Exception : " + e.Message;
            logger.Error(info, e);
        }
        private static void MakeError(Exception e)
        {
            MakeError(e, "");
        }

        public static long getMilis()
        {
            TimeSpan ts = (DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
            return (long)ts.TotalMilliseconds;
        }

        private static void toFront()
        {
            try
            {
                object propValue = handler.GetCurrentPropertyValue(AutomationElement.IsKeyboardFocusableProperty);
                Boolean isKeyboardFocusable = (Boolean)propValue;
                logger.All("Is keyboard focusable : " + isKeyboardFocusable);
                if (isKeyboardFocusable)
                {
                    handler.SetFocus();
                }
                else if (!alwaysToFront)
                {
                    return;
                }
                SafeNativeMethods.SetForegroundWindow(new IntPtr(handler.Current.NativeWindowHandle));
            }
            catch (Exception e)
            {
                logger.Error("Error on toFront() : " + e.Message, e);
                Exception innerException = e.InnerException;
                logger.Error("Inner exception is null ? " + (innerException == null));
                while (innerException != null)
                {
                    logger.Error("Innder exception : " + innerException.Message, innerException);
                    innerException = innerException.InnerException;
                }
            }
        }

        private static AutomationElement findOwner(string ownerId)
        {
            int[] owner = stringToIntArray(ownerId);
            AutomationElement window = handler;
            if (ownerId != null)
            {
                window = FindByRuntimeId(owner);
            }
            return window;
        }

        private static WindowVisualState stringToWindowState(string state)
        {
            switch (state)
            {
                case "Maximized": return WindowVisualState.Maximized;
                case "Minimized": return WindowVisualState.Minimized;
                default: return WindowVisualState.Normal;
            }
        }
        #endregion

        #region variables

        public static Logger.Logger logger = new Logger.Logger(LogLevel.None);
        public static bool alwaysToFront = false;
        public static PluginInfo pluginInfo = null;

        private static int maxTimeout = 10000;

        private static string frameWorkId;

        public static readonly AutomationProperty VALUE_PROPERTY = ValuePattern.ValueProperty;
        private static Process process = null;

        private static AutomationElement handler = null;

        private static string lastError = null;
        private static int lastErrorNumber = 4;

        private static string methodTime = null;

        private static string uiAutomationTime = null;

        private static bool isShiftDown = false;
        private static bool isControlDown = false;
        private static bool isAltDown = false;

        public static readonly string SEPARATOR_CELL = "###";
        public static readonly string SEPARATOR_ROWS = ";;;";
        public static readonly string SEPARATOR_COMMA = ",";
        public static readonly string EMPTY_CELL = "EMPTY_CELL_EMPTY";

        private static Dictionary<int[], AutomationElement> cacheRuntimeId = new Dictionary<int[], AutomationElement>(new DictionaryMatcher());

        private static Dictionary<SimpleLocator, AutomationElement[]> cacheLocators = new Dictionary<SimpleLocator, AutomationElement[]>(new LocatorMatcher());

        private static Dictionary<ControlType, ControlKind> dic = new Dictionary<ControlType, ControlKind>()
            {
                { ControlType.Button,       ControlKind.Button },
                { ControlType.CheckBox,     ControlKind.CheckBox },
                { ControlType.ComboBox,     ControlKind.ComboBox },
                { ControlType.DataGrid,     ControlKind.Table },
                { ControlType.Edit,         ControlKind.TextBox },
                { ControlType.Document,     ControlKind.TextBox},
                { ControlType.List,         ControlKind.ListView },
                { ControlType.ListItem,     ControlKind.ListView },
                { ControlType.Menu,         ControlKind.Menu },
                { ControlType.MenuBar,      ControlKind.Menu },
                { ControlType.MenuItem,     ControlKind.MenuItem },
                { ControlType.Pane,         ControlKind.Panel },
                { ControlType.RadioButton,  ControlKind.RadioGroup },
                { ControlType.Tab,          ControlKind.TabPanel },
                { ControlType.TabItem,      ControlKind.TabPanel },
                { ControlType.Table,        ControlKind.Table },
                { ControlType.Text,         ControlKind.Label },
                { ControlType.ToolTip,      ControlKind.Tooltip },
                { ControlType.Tree,         ControlKind.Tree },
                { ControlType.TreeItem,     ControlKind.TreeItem },
                { ControlType.Window,       ControlKind.Dialog },
                { ControlType.Slider,       ControlKind.Slider},
                { ControlType.SplitButton,  ControlKind.Button},
                { ControlType.Image,        ControlKind.Image },
                { ControlType.ProgressBar,  ControlKind.ProgressBar },
                { ControlType.ScrollBar,    ControlKind.ScrollBar },
            };
        #endregion

        #region Classes
        public class DictionaryMatcher : IEqualityComparer<int[]>
        {
            public bool Equals(int[] o1, int[] o2)
            {
                if (o1.Length != o2.Length)
                {
                    return false;
                }
                for (int i = 0; i < o1.Length; i++)
                {
                    if (o1[i] != o2[i])
                    {
                        return false;
                    }
                }
                return true;
            }

            public int GetHashCode(int[] o)
            {
                //TODO need review this hashCode function
                int x = o[0];
                for (int i = 1; i < o.Length; i++)
                {
                    x ^= o[i];
                }
                return x;
            }
        }

        public class SimpleLocator
        {
            public ControlKind Kind { get; set; }
            public string Uid { get; set; }
            public string Xpath { get; set; }
            public string Clazz { get; set; }
            public string Name { get; set; }
            public string Title { get; set; }
            public string Text { get; set; }
            public Boolean Many { get; set; }

            public SimpleLocator(ControlKind controlKind, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many)
            {
                this.Uid = Uid;
                this.Xpath = Xpath;
                this.Clazz = Clazz;
                this.Name = Name;
                this.Title = Title;
                this.Text = Text;
                this.Kind = controlKind;
                this.Many = many;
            }

            public override string ToString()
            {
                return "uid " + Uid + " class " + Clazz + "xpath : " + Xpath;
            }
        }

        public class LocatorMatcher : IEqualityComparer<SimpleLocator>
        {
            public bool Equals(SimpleLocator l1, SimpleLocator l2)
            {
                if (l1 == l2)
                {
                    return true;
                }
                if (l1.Kind != l2.Kind)
                {
                    return false;
                }
                if (!String.Equals(l1.Uid, l2.Uid)) return false;
                if (!String.Equals(l1.Xpath, l2.Xpath)) return false;
                if (!String.Equals(l1.Clazz, l2.Clazz)) return false;
                if (!String.Equals(l1.Name, l2.Name)) return false;
                if (!String.Equals(l1.Title, l2.Title)) return false;
                if (!String.Equals(l1.Text, l2.Text)) return false;
                if (l1.Many != l2.Many) return false;
                return true;
            }

            public int GetHashCode(SimpleLocator l)
            {
                int hashCode = l.Many ? 0 : 1;
                hashCode = 31 * hashCode + convert(l.Uid);
                hashCode = 31 * hashCode + convert(l.Xpath);
                hashCode = 31 * hashCode + convert(l.Clazz);
                hashCode = 31 * hashCode + convert(l.Name);
                hashCode = 31 * hashCode + convert(l.Title);
                hashCode = 31 * hashCode + convert(l.Text);
                hashCode = 31 * hashCode + l.Kind.GetHashCode();
                return 0;
            }

            private int convert(string s)
            {
                return s == null ? 0 : s.GetHashCode();
            }
        }
        #endregion
    }
}