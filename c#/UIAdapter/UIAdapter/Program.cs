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

        [Obsolete]
        [DllExport("methodTime", CallingConvention.Cdecl)]
        public static string MethodTime()
        {
            string res = methodTime;
            methodTime = null;
            return res;
        }

        [Obsolete]
        [DllExport("uiAutomationTime", CallingConvention.Cdecl)]
        public static string UIAutomationTime()
        {
            var res = uiAutomationTime;
            uiAutomationTime = null;
            return res;
        }

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
        public static int Connect(string title, int height, int width, int pid, int controlKind, int timeout)
        {
            //logger.All("title=" + title + " height=" + height + " width=" + width + " pid=" + pid + " controlKind=" + controlKind + " timeout=" + timeout);
            Task<int> task = Task<int>.Factory.StartNew(() =>
            {
                handler = null;
                process = null;
                title = ConvertString.replaceUnicodeSubStringToChar(title);

                int runningTime = 0;
                int TIMEWAIT = 100;
                bool flag = true;
                while (flag)
                {
                    if (runningTime > timeout)
                    {
                        flag = false;
                        throw new Exception("Could not find window still " + timeout + " ms");
                    }
                    Thread.Sleep(TIMEWAIT);
                    runningTime += TIMEWAIT;

                    try
                    {
                        long startMethod = getMilis();
                        long startUIAutomation = getMilis();
                        ControlType findType = ControlType.Window;
                        if (controlKind != Int32.MinValue)
                        {
                            ControlKind kind = (ControlKind)controlKind;
                            findType = dic.FirstOrDefault(x => x.Value.Equals(kind)).Key;
                        }
                        AutomationElementCollection collection = AutomationElement.RootElement.FindAll(TreeScope.Children, new PropertyCondition(AutomationElement.ControlTypeProperty, findType));
                        logger.All("AutomationElement.RootElement.FindAll(TreeScope.Children, new PropertyCondition(AutomationElement.ControlTypeProperty, " + findType.ProgrammaticName + "))", (getMilis() - startUIAutomation));

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
                        if (list.Count() == 0)
                        {
                            continue;
                        }
                        if (list.Count() > 1)
                        {
                            throw new Exception("Found " + list.Count() + " windows instead 1 with parameters title=" + title + " , h = " + height + " , w = " + width);
                        }
                        handler = list.Single();
                        frameWorkId = handler.Current.FrameworkId;
                        logger.All("method Connect", getMilis() - startMethod);
                        flag = false;
                        return handler.Current.ProcessId;
                    }
                    catch (Exception e)
                    {
                        MakeError(e);
                        throw e;
                    }
                }
                return -1;
            }
            );
            try
            {
                bool res = task.Wait(timeout);
                logger.All("Connected successful ? " + res);
                if (res)
                {
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
        public static int Run(string exec, string workDir, string param)
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
                process.WaitForInputIdle();
                Thread.Sleep(1000);
                UpdateHandler();
                frameWorkId = handler.Current.FrameworkId;
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
        public static string getList(string elementId)
        {
            try
            {
                long startMethod = getMilis();

                AutomationElement owner = findOwner(elementId);
                logger.All("Fount element runtimeId : " + string.Join(",",owner.GetRuntimeId()));
                List<AutomationElement> listItems = getListItems(owner);
                List<string> namesList = getNamesOfListItems(listItems);
                //TODO check if we have comboBox with checkboxes
                
                if (namesList.Count == 0)
                {
                    string result = string.Join(SEPARATOR_COMMA, namesList);
                    logger.All("method GetList", getMilis() - startMethod);
                    return ConvertString.replaceNonASCIIToUnicode(result);
                }
                bool isCheckboxes = TableFactory.GetFrameworkId(owner).Equals("Silverlight");
                string firstElement = namesList[0];
                for (int i = 1; i < namesList.Count; i++)
                {
                    if (!namesList[i].Equals(firstElement))
                    {
                        isCheckboxes = false;
                        break;
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
                        string toggleString = "";
                        togglePattern = (TogglePattern)item.GetCurrentPattern(TogglePattern.Pattern);
                        toggleString = togglePattern.Current.ToggleState == ToggleState.On ? "+" : "-";
                        result += toggleString + item.Current.Name + SEPARATOR_COMMA;
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
        public static string ListAll(string OwnerId, int controlkindId, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many)
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
                AutomationElement[] elements = GetAllComponents(controlKind, window, Uid, Xpath, Clazz, Name, Title, Text, many);
                logger.All("End get all components");
                StringBuilder result = new StringBuilder();
                foreach (AutomationElement element in elements)
                {
                    result.AppendLine("====================================================================================================");
                    result.AppendLine("Found:        " + element);
                    result.AppendLine("AutomationId: " + element.Current.AutomationId);
                    result.AppendLine("Class name:   " + element.Current.ClassName);
                    result.AppendLine("Control type: " + element.Current.ControlType.LocalizedControlType);
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
                        //TODO we need this try for password field
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
        public static int FindAllForLocator([In, Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] arr, int len, String oId, int controlkindId, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many)
        {
            //logger.All("len="+len+" oId="+" controlkindId="+" Uid="+Uid+" Xpath="+Xpath+" Clazz="+Clazz+" Name="+Name+" Title="+Title+" Text="+Text+" many="+many);
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
                    found = GetAllComponents(controlKind, owner, Uid, Xpath, Clazz, Name, Title, Text, many);
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
                logger.All("find all for locator", getMilis() - startMethod);
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
                    condition = new PropertyCondition(property, value);
                }

                long startUI = getMilis();
                AutomationElementCollection found = owner.FindAll(treeScope, condition);
                logger.All("findAll by scope and condition", getMilis() - startUI);
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
        public static void SendKey(String key)
        {
            try
            {
                long startMethod = getMilis();
                // TODO see this reference for understanding sendWait 
                // https://msdn.microsoft.com/ru-ru/library/system.windows.forms.sendkeys.send(v=vs.110).aspx
                UpdateHandler();
                //toFront();
                //old implementtation. remote it if all works
                /*
                if (1 > 2) 
                {
                    string keyPress = addModifiers(KeyboardNew.getKey(key.ToUpper()));
                    logger.All("Send keys : " + keyPress);
                    try
                    {
                        SendKeys.Send(keyPress);
                    }
                    catch (InvalidOperationException e)
                    {
                        SendKeys.SendWait(keyPress);
                    }
                    
                    logger.All("method sendKeys", getMilis() - startMethod);
                }
                 * */
                //new implementation via WindowsInputSimulation
                {
                    VirtualKeyCode keyCode = KeyboardVirtual.getVirtualKeyCode(key);

                    SimulateKeyPress(keyCode);
                }
            }
            catch (Exception e)
            {
                MakeError(e);
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
            /*
            
            var down = new INPUT();
            down.Type = (UInt32)InputType.KEYBOARD;
            down.Data.Keyboard = new KEYBDINPUT();
            down.Data.Keyboard.Vk = (UInt16)keyCode;
            // Scan Code here, was 0
            down.Data.Keyboard.Scan = (ushort)Win32.UnsafeNativeMethods.MapVirtualKey((UInt16)keyCode, 0);
            down.Data.Keyboard.Flags = 0;
            down.Data.Keyboard.Time = 0;
            down.Data.Keyboard.ExtraInfo = IntPtr.Zero;

            var up = new INPUT();
            up.Type = (UInt32)InputType.KEYBOARD;
            up.Data.Keyboard = new KEYBDINPUT();
            up.Data.Keyboard.Vk = (UInt16)keyCode;
            // Scan Code here, was 0
            up.Data.Keyboard.Scan = (ushort)Win32.UnsafeNativeMethods.MapVirtualKey((UInt16)keyCode, 0);
            up.Data.Keyboard.Flags = (UInt32)KeyboardFlag.KEYUP;
            up.Data.Keyboard.Time = 0;
            up.Data.Keyboard.ExtraInfo = IntPtr.Zero;

            INPUT[] inputList = new INPUT[2];
            inputList[0] = down;
            inputList[1] = up;

            var numberOfSuccessfulSimulatedInputs = Win32.UnsafeNativeMethods.SendInput(2,
                 inputList, Marshal.SizeOf(typeof(INPUT)));
            if (numberOfSuccessfulSimulatedInputs == 0)
                throw new Exception(
                string.Format("The key press simulation for {0} was not successful.",
                keyCode));
             
             */
        }

        [DllExport("upAndDown", CallingConvention.Cdecl)]
        public static void UpAndDown(String key, bool isDown)
        {
            try
            {
                long startMethod = getMilis();
                UpdateHandler();
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

        [DllExport("doPatternCall", CallingConvention.Cdecl)]
        public static string DoPatternCall(String inid, int patternId, string method, string arg, int c)
        {
            try
            {
                long startMethod = getMilis();
                int[] id = stringToIntArray(inid);
                object[] args = null;
                //if c == -1 - is string arg is null;
                if (c == -1)
                {
                    args = null;
                }
                //simple strings
                if (c == 0)
                {
                    args = arg.Split(new char[] { '%' });
                }
                //only int32    
                else if (c == 1)
                {
                    string[] a = arg.Split(new char[] { '%' });
                    args = new object[a.Length];
                    for (int i = 0; i < a.Length; i++)
                    {
                        args[i] = Int32.Parse(a[i]);
                    }
                }
                //double
                else if (c == 2)
                {
                    string[] a = arg.Split(new char[] { '%' });
                    args = new object[a.Length];
                    for (int i = 0; i < a.Length; i++)
                    {
                        args[i] = Double.Parse(a[i]);
                    }
                }
                //WindowVisualState
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
                            windowP.SetWindowVisualState(WindowVisualState.Normal);
                        }
                        //TODO set position via win api
                        IntPtr hWnd = new IntPtr(handler.Current.NativeWindowHandle);
                        logger.All("Window handle : " + hWnd);
                        MyRect rect = new MyRect();
                        bool f = Win32.UnsafeNativeMethods.GetWindowRect(hWnd, ref rect);
                        logger.All("GetWindowRect : " + f);
                        logger.All(String.Format("Window has left {0}, top {1}, right {2}, bottom {3}", rect.Left, rect.Top, rect.Right, rect.Bottom)); 
                        bool f1 = Win32.UnsafeNativeMethods.MoveWindow(hWnd, rect.Left, rect.Top, (int) args[0], (int) args[1], false);
                        logger.All("Move window : " + f1);
                        bool f2 = Win32.UnsafeNativeMethods.GetWindowRect(hWnd, ref rect);
                        logger.All("GetWindowRect : " + f2);
                        logger.All(String.Format("Window has left {0}, top {1}, right {2}, bottom {3}", rect.Left, rect.Top, rect.Right, rect.Bottom)); 
                        return null;

                    }
                    else if (elementPattern is WindowPattern)
                    {
                        WindowPattern windowPattern = elementPattern as WindowPattern;
                        WindowVisualState windowState = (WindowVisualState)args[0];
                        //if we try to min/max window, that was min/max - just reutrn
                        if (windowPattern.Current.WindowVisualState == windowState)
                        {
                            return null;
                        }

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
                MakeError(e, System.Reflection.MethodBase.GetCurrentMethod().ToString());
            }
            return null;
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
                MakeError(e, System.Reflection.MethodBase.GetCurrentMethod().ToString());
            }
        }

        [DllExport("elementIsEnabled", CallingConvention.Cdecl)]
        public static string ElementIsEnabled(String inid)
        {
            long startMethod = getMilis();
            int[] id = stringToIntArray(inid);
            UpdateHandler();
            AutomationElement element = FindByRuntimeId(id);
            logger.All("method elementIsEnabled", getMilis() - startMethod);
            return element.GetCurrentPropertyValue(AutomationElement.IsEnabledProperty).ToString();
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

        #endregion

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
                //var cell = findCell(table, column, row);
                //bool isWin32 = cell.Current.FrameworkId.ToUpper().Equals("WIN32");
                //return isSilverlightApp(table) || isWin32 ? cell.Current.Name : "" + cell.GetCurrentPropertyValue(VALUE_PROPERTY);
            }
            catch (Exception e)
            {
                MakeError(e, System.Reflection.MethodBase.GetCurrentMethod().ToString());
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
                //var cell = findCell(table, column, row);
                var cell = TableFactory.createTable(table).FindCell(column, row);
                MouseToElement(cell, mouseAction, int.MinValue, int.MinValue);
            }
            catch (Exception e)
            {
                MakeError(e, System.Reflection.MethodBase.GetCurrentMethod().ToString());
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
                //var cell = findCell(table, column, row);
                var cell = TableFactory.createTable(table).FindCell(column, row);
                SetTextToElement(cell, text);
            }
            catch (Exception e)
            {
                MakeError(e, System.Reflection.MethodBase.GetCurrentMethod().ToString());
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
                //TreeWalker walker = TreeWalker.RawViewWalker;
                //AutomationElement findRow = walker.GetFirstChild(table);
                //int rows = -1;
                //bool isSilverLight = isSilverlightApp(findRow);
                //if (isSilverLight)
                //{
                //    AutomationElement rowPresented = walker.GetFirstChild(table);
                //    while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                //    {
                //        rowPresented = walker.GetNextSibling(rowPresented);
                //    }
                //    findRow = walker.GetFirstChild(rowPresented);
                //    rows = 0;
                //}
                //if (findRow == null)
                //{
                //    return "";
                //}
                //Predicate<AutomationElement> isGoodRow = (r) =>
                //    {
                //        if (isSilverLight)
                //        {
                //            return true;
                //        }
                //        return !r.Current.Name.ToUpper().Contains("SCROLL");
                //    };
                //while (rows != index)
                //{
                //    logger.All("FindRow.current.name : " + findRow.Current.Name + "\n and index : " + rows + "\n is GoodRow : " + isGoodRow);
                //    bool isGood = isGoodRow(findRow);
                //    if (isGood)
                //    {
                //        rows += 1;
                //    }
                //    findRow = walker.GetNextSibling(findRow);
                //}
                //logger.All("finded row : " + rowToString(findRow), -1);
                //AutomationElement header = findHeader(table);
                //StringBuilder builder = new StringBuilder();
                //builder.Append(headerToString(header, useNumericHeader, null)).Append(SEPARATOR_ROWS).Append(rowToString(findRow));
                //return builder.ToString();
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
                //TreeWalker walker = TreeWalker.RawViewWalker;
                //AutomationElement findRow = walker.GetFirstChild(table);
                //bool isSilver = isSilverlightApp(findRow);
                //if (isSilver)
                //{
                //    AutomationElement rowPresented = walker.GetFirstChild(table);
                //    while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                //    {
                //        rowPresented = walker.GetNextSibling(rowPresented);
                //    }
                //    findRow = walker.GetFirstChild(rowPresented);
                //}
                //Predicate<AutomationElement> isGoodRow = (row) =>
                //{
                //    if (isSilver)
                //    {
                //        return true;
                //    }
                //    return !row.Current.Name.ToUpper().Contains("SCROLL");
                //};

                //int i = 0;
                //while (findRow != null)
                //{
                //    if (isGoodRow(findRow))
                //    {
                //        i++;
                //    }
                //    findRow = walker.GetNextSibling(findRow);
                //}
                //return i;
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
                //logger.All("tableId=" + tableId + " useNumericHeader=" + useNumericHeader + " condition=" + condition + " columns=" + columns);
                columns = ConvertString.replaceUnicodeSubStringToChar(columns);
                long start = getMilis();
                int[] tableRuntimeId = stringToIntArray(tableId);
                AutomationElement table = FindByRuntimeId(tableRuntimeId);
                return ConvertString.replaceNonASCIIToUnicode(TableFactory.createTable(table).GetRow(condition, columns));

                //long t1 = getMilis();
                //AutomationElement header = findHeader(table);
                //logger.All("Find header time ", getMilis() - t1);
                //String headerToStr = headerToString(header, useNumericHeader, columns);
                //StringBuilder builder = new StringBuilder().Append(headerToStr);

                //TreeWalker walker = TreeWalker.RawViewWalker;
                //AutomationElement findRow = walker.GetFirstChild(table);

                //logger.All("Get row, Condition : " + condition, -1);
                //t1 = getMilis();
                //bool isSilverLight = isSilverlightApp(findRow);
                //if (!isSilverLight)
                //{
                //    while (findRow.Current.Name.ToUpper().Contains("SCROLL"))
                //    {
                //        findRow = walker.GetNextSibling(findRow);
                //    }
                //}
                //else
                //{
                //    AutomationElement rowPresented = walker.GetFirstChild(table);
                //    while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                //    {
                //        rowPresented = walker.GetNextSibling(rowPresented);
                //    }
                //    findRow = walker.GetFirstChild(rowPresented);
                //}
                //logger.All("Find first row time ", getMilis() - t1);
                //Cond.Condition cond = null;
                //Dictionary<string, int> indexes = new Dictionary<string,int>();
                //if (!string.IsNullOrEmpty(condition))
                //{
                //    cond = Cond.Condition.Deserialize(condition);
                //    HashSet<string> names = cond.GetNames();

                //    string[] headerCells = headerToStr.Split(new char[] { SEPARATOR_CELL[0] }, StringSplitOptions.RemoveEmptyEntries);
                //    for (int i = 0; i < headerCells.Length; i++)
                //    {
                //        if (names.Contains(headerCells[i]))
                //        {
                //            indexes.Add(headerCells[i], i);
                //        }
                //    }
                //}
                ////indexes found right
                //Predicate<AutomationElement> predicate = (fr) =>
                //{
                //    if (isSilverLight)
                //    {
                //        return true;
                //    }
                //    string currentName = fr.Current.Name.ToUpper();
                //    return !currentName.Contains("TOP ROW") && !currentName.Contains("SCROLL");
                //};
                //while (findRow != null)
                //{
                //    if (predicate(findRow))
                //    {
                //        if (rowMatchesNew(findRow, indexes, cond))
                //        {
                //            builder.Append(SEPARATOR_ROWS).Append(rowToString(findRow));
                //            logger.All(String.Format("getRow(id={0}, use = {1}, cond = {2}", tableId, useNumericHeader, condition), getMilis() - start);
                //            return builder.ToString();
                //        }
                //    }
                //    t1 = getMilis();
                //    findRow = walker.GetNextSibling(findRow);
                //    logger.All("Get next sibling for row time : ", getMilis() - t1);
                //}
                //logger.All(String.Format("getRow(id={0}, use = {1}, cond = {2}, row = {3}", tableId, useNumericHeader, condition, builder.ToString()), getMilis() - start);
                //return builder.ToString();
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

                //AutomationElement header = findHeader(table);
                //String headerToStr = headerToString(header, useNumericHeader, columns);
                //StringBuilder builder = new StringBuilder();//.Append(headerToStr);

                //TreeWalker walker = TreeWalker.RawViewWalker;
                //AutomationElement findRow = walker.GetFirstChild(table);
                //bool isSilver = isSilverlightApp(findRow);

                //if (!isSilver)
                //{
                //    while (findRow.Current.Name.ToUpper().Contains("SCROLL"))
                //    {
                //        findRow = walker.GetNextSibling(findRow);
                //    }
                //}
                //else
                //{
                //    AutomationElement rowPresented = walker.GetFirstChild(table);
                //    while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                //    {
                //        rowPresented = walker.GetNextSibling(rowPresented);
                //    }
                //    findRow = walker.GetFirstChild(rowPresented);
                //}

                //Cond.Condition cond = null;
                //Dictionary<string, int> indexes = new Dictionary<string, int>();
                //if (!string.IsNullOrEmpty(condition))
                //{
                //    cond = Cond.Condition.Deserialize(condition);
                //    HashSet<string> names = cond.GetNames();

                //    string[] headerCells = headerToStr.Split(new char[] { SEPARATOR_CELL[0] }, StringSplitOptions.RemoveEmptyEntries);
                //    for (int i = 0; i < headerCells.Length; i++)
                //    {
                //        if (names.Contains(headerCells[i]))
                //        {
                //            indexes.Add(headerCells[i], i);
                //        }
                //    }
                //}

                //int findedIndex = 0;
                //string sep = "";
                //Predicate<AutomationElement> predicate = (fr) =>
                //{
                //    if (isSilver)
                //    {
                //        return true;
                //    }
                //    string currentName = fr.Current.Name.ToUpper();
                //    return !currentName.Contains("TOP ROW") && !currentName.Contains("SCROLL") && !currentName.Equals("HEADER");
                //};
                //while (findRow != null)
                //{
                //    if (predicate(findRow))
                //    {
                //        if (rowMatchesNew(findRow, indexes, cond))
                //        {
                //            builder.Append(sep).Append(findedIndex);
                //            sep = SEPARATOR_CELL;
                //        }
                //        findedIndex++;
                //    }
                //    findRow = walker.GetNextSibling(findRow);
                //}
                //logger.All(String.Format("getRow(id={0}, use = {1}, cond = {2}", tableId, useNumericHeader, condition), getMilis() - start);
                //return builder.ToString();
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

                //AutomationElement header = findHeader(table);
                //StringBuilder builder = new StringBuilder();
                //builder.Append(headerToString(header, useNumericHeader, null));

                //bool isSilver = isSilverlightApp(table);


                //TreeWalker walker = TreeWalker.RawViewWalker;
                //AutomationElement findRow = walker.GetFirstChild(table);
                //if (isSilver)
                //{
                //    AutomationElement rowPresented = walker.GetFirstChild(table);
                //    while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                //    {
                //        rowPresented = walker.GetNextSibling(rowPresented);
                //    }
                //    findRow = walker.GetFirstChild(rowPresented);
                //}

                //Predicate<AutomationElement> isGoodRow = (fr) =>
                //{
                //    if (isSilver)
                //    {
                //        return true;
                //    }
                //    return !fr.Current.Name.ToUpper().Contains("TOP ROW") && !findRow.Current.Name.ToUpper().Contains("SCROLL");
                //};
                //while (findRow != null)
                //{
                //    if (isGoodRow(findRow))
                //    {
                //        builder.Append(SEPARATOR_ROWS).Append(rowToString(findRow));
                //    }
                //    findRow = walker.GetNextSibling(findRow);
                //}
                //return builder.ToString();
            }
            catch (Exception e)
            {
                MakeError(e);
            }
            return null;
        }

        #region private methods
        private static AutomationElement findCell(AutomationElement table, int column, int row)
        {
            long start = getMilis();

            if (row < 0)
            {
                return findHeaderCell(findHeader(table), column);
            }

            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement findRow = walker.GetFirstChild(table);
            int rows = -1;
            bool isSilverLight = isSilverlightApp(findRow);
            if (isSilverLight)
            {
                AutomationElement rowPresented = walker.GetFirstChild(table);
                while (!rowPresented.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"))
                {
                    rowPresented = walker.GetNextSibling(rowPresented);
                }
                findRow = walker.GetFirstChild(rowPresented);
                rows = 0;
            }

            Predicate<AutomationElement> isGoodRow = (r) =>
            {
                if (isSilverLight)
                {
                    return true;
                }
                return !r.Current.Name.ToUpper().Contains("SCROLL");
            };
            while (rows != row)
            {
                if (findRow == null)
                {
                    throw new Exception(String.Format("Invalid row index {0}", row));
                }

                findRow = walker.GetNextSibling(findRow);
                if (isGoodRow(findRow))
                {
                    rows += 1;
                }
            }

            if (findRow == null)
            {
                throw new Exception(String.Format("Row with number {0} not found", row));
            }
            int cells = 0;
            AutomationElement findCell = walker.GetFirstChild(findRow);
            Predicate<AutomationElement> cellPredicate = (c) =>
            {
                if (isSilverLight)
                {
                    return true;
                }
                return !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
            };
            //TODO incorrect behaviour
            while (cells != column)
            {
                if (findCell == null)
                {
                    throw new Exception(String.Format("Invalid column index {0}", column));
                }
                if (cellPredicate(findCell))
                {
                    cells += 1;
                }
                findCell = walker.GetNextSibling(findCell);
            }

            logger.All("find all cell in table", (getMilis() - start));
            return findCell;
        }

        private static AutomationElement findHeader(AutomationElement table)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement header = walker.GetFirstChild(table);
            bool isWin32 = handler.Current.FrameworkId.ToUpper().Equals("WIN32");
            bool isSilverLight = isSilverlightApp(header);
            Predicate<AutomationElement> isHeader = (hh) =>
            {
                if (isSilverLight)
                {
                    return hh.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
                }
                else if (isWin32)
                {
                    return hh.Current.ControlType.Equals(ControlType.Header);
                }
                else
                {
                    return hh.Current.Name.ToUpper().Contains("TOP ROW");
                }
            };
            while (header != null && !isHeader(header))
            {
                header = walker.GetNextSibling(header);
            }
            logger.All(String.Format("find header : class {0}\nlocalControlType {1}", header.Current.ClassName, header.Current.LocalizedControlType), -1);
            return header;
        }

        private static AutomationElement findHeaderCell(AutomationElement header, int column)
        {
            TreeWalker cellWalker = TreeWalker.RawViewWalker;
            AutomationElement cell = cellWalker.GetFirstChild(header);
            bool isSilverLight = isSilverlightApp(header);
            Predicate<AutomationElement> findFirstCell = (c) =>
            {
                if (isSilverLight)
                {
                    return false;
                }
                return c.Current.Name.ToUpper().Contains("TOP LEFT HEADER CELL");
            };
            while (findFirstCell(cell))
            {
                cell = cellWalker.GetNextSibling(cell);
            }
            for (int i = 0; i < column; i++)
            {
                if (cell != null)
                {
                    cell = cellWalker.GetNextSibling(cell);
                }
                else
                {
                    throw new Exception(String.Format("Invalid column index {0}", column));
                }
            }
            return cell;
        }

        private static string headerToString(AutomationElement header, Boolean useNumericHeader, String columns)
        {
            string[] columnsArray = null;
            if (!String.IsNullOrEmpty(columns))
            {
                columnsArray = columns.Split(new char[] { SEPARATOR_CELL[0] }, StringSplitOptions.RemoveEmptyEntries);
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement firstCell = walker.GetFirstChild(header);
            StringBuilder b = new StringBuilder();
            string sep = "";
            bool isSilverLight = isSilverlightApp(firstCell);
            logger.All("Is silverlight ? : " + isSilverLight);
            Predicate<AutomationElement> findFirstCell = (fc) =>
            {
                if (isSilverLight)
                {
                    return false;
                }
                return fc.Current.Name.ToUpper().Contains("TOP LEFT HEADER");
            };
            while (findFirstCell(firstCell))
            {
                firstCell = walker.GetNextSibling(firstCell);
            }
            int i = 0;
            TreeWalker rawWalker = TreeWalker.RawViewWalker;
            while (firstCell != null)
            {
                String value;
                if (isSilverLight)
                {
                    AutomationElement cell = rawWalker.GetFirstChild(firstCell);
                    value = firstCell.Current.Name;

                    if (cell != null)
                    {
                        value = cell.Current.Name;
                        TreeWalker tw = TreeWalker.RawViewWalker;
                        AutomationElement sib = tw.GetNextSibling(cell);
                        while (sib != null)
                        {
                            value += sib.Current.Name;
                            sib = tw.GetNextSibling(sib);
                        }
                    }
                }
                else if (firstCell.Current.ControlType.Equals(ControlType.HeaderItem))
                {
                    value = firstCell.Current.Name;
                }
                else
                {
                    value = "" + firstCell.GetCurrentPropertyValue(VALUE_PROPERTY);
                }
                logger.All("firstCell value = " + value, -1);

                b.Append(sep).Append(getValueFromArray(columnsArray, i, value));
                sep = SEPARATOR_CELL;
                firstCell = walker.GetNextSibling(firstCell);
                i++;
            }
            return b.ToString();
        }

        private static string getValueFromArray(string[] array, int index, string defaultValue)
        {
            if (array == null)
            {
                return defaultValue;
            }
            if (index >= array.Length)
            {
                return "" + index;
            }
            return array[index];

        }

        private static string rowToString(AutomationElement row)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement firstCell = walker.GetFirstChild(row);

            StringBuilder b = new StringBuilder();
            string sep = "";
            bool isSilverLight = isSilverlightApp(row);
            Predicate<AutomationElement> cellIsGood = (c) =>
            {
                if (isSilverLight)
                {
                    return c.Current.IsKeyboardFocusable && !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
                }
                return !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
            };
            bool isWin32 = handler.Current.FrameworkId.ToUpper().Equals("WIN32");
            while (firstCell != null)
            {
                if (cellIsGood(firstCell))
                {
                    var cellValue = "";
                    if (isSilverLight || isWin32)
                    {
                        var name = firstCell.Current.Name;
                        cellValue = name;
                        if (String.IsNullOrEmpty(name))
                        {
                            try
                            {
                                cellValue = TreeWalker.RawViewWalker.GetFirstChild(firstCell).Current.Name;
                            }
                            catch (Exception e)
                            { }
                        }
                        if (String.IsNullOrEmpty(cellValue))
                        {
                            cellValue = EMPTY_CELL;
                        }
                    }
                    else
                    {
                        cellValue = "" + firstCell.GetCurrentPropertyValue(VALUE_PROPERTY);
                        //cellValue = firstCell.Current.Name;
                    }
                    logger.All("finded cell : " + cellValue, -1);
                    b.Append(sep).Append(cellValue);
                    sep = SEPARATOR_CELL;
                }
                firstCell = walker.GetNextSibling(firstCell);
            }
            string r = b.ToString();
            logger.All("row to string result\n" + r, -1);
            return r;
        }

        private static Boolean rowMatchesNew(AutomationElement element, Dictionary<string, int> indexes, Cond.Condition condition)
        {
            long t1 = getMilis();
            logger.All("Header : " + String.Join(" ", indexes), -1);
            Dictionary<string, object> row = rowToDictionary(element, indexes);
            logger.All("Row to matched : " + String.Join(" ", row), -1);
            bool res = condition.IsMatched(row);
            logger.All("Row matches res : " + res + " new time : ", getMilis() - t1);
            return res;
        }

        //TODO need review
        private static Dictionary<string, object> rowToDictionary(AutomationElement row, Dictionary<string, int> indexes)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement firstCell = walker.GetFirstChild(row);
            Dictionary<string, object> res = new Dictionary<string, object>();
            var iterator = indexes.GetEnumerator();
            bool isSilverLight = isSilverlightApp(row);
            Predicate<AutomationElement> cellIsGood = (c) =>
            {
                if (isSilverLight)
                {
                    return c.Current.IsKeyboardFocusable && !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
                }
                if (c.Current.ControlType == ControlType.Header)
                {
                    return false;
                }
                return !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
            };
            bool isWin32 = handler.Current.FrameworkId.ToUpper().Equals("WIN32");
            bool next = iterator.MoveNext();
            for (int count = 0; firstCell != null && next; firstCell = walker.GetNextSibling(firstCell))
            {
                bool flag = cellIsGood(firstCell);
                if (flag)
                {
                    var currentKey = iterator.Current.Key;
                    if (iterator.Current.Value == count++)
                    {
                        next = iterator.MoveNext();
                    }
                    else
                    {
                        continue;
                    }
                    object cellValue = "";
                    if (isSilverLight || isWin32)
                    {
                        var name = firstCell.Current.Name;
                        cellValue = name;
                        if (String.IsNullOrEmpty(name))
                        {
                            try
                            {
                                cellValue = TreeWalker.RawViewWalker.GetFirstChild(firstCell).Current.Name;
                            }
                            catch (Exception e)
                            { }
                        }
                    }
                    else
                    {
                        //cellValue = "" + firstCell.GetCurrentPropertyValue(VALUE_PROPERTY);
                        //cellValue = "" + firstCell.Current.Name;
                        object pattern;
                        if (firstCell.TryGetCurrentPattern(ValuePattern.Pattern, out pattern))
                        {
                            cellValue = "" + ((ValuePattern)pattern).Current.Value;
                        }
                    }
                    res.Add(currentKey, cellValue);

                    logger.All("finded cell : " + cellValue, -1);
                }
            }
            logger.All("row to dictionary result\n", -1);

            return res;
        }

        private static String cellName(AutomationElement row, int index)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement findCell = walker.GetFirstChild(row);
            bool isWin32 = findCell.Current.FrameworkId.ToUpper().Equals("WIN32");
            bool isSilverLight = isSilverlightApp(row);
            Predicate<AutomationElement> cellPredicate = (c) =>
            {
                if (isSilverLight || isWin32)
                {
                    return true;
                }
                return !c.Current.ControlType.LocalizedControlType.ToUpper().Contains("HEADER");
            };
            int cells = 0;
            while (cells != index)
            {
                if (findCell == null)
                {
                    throw new Exception(String.Format("Invalid column index {0}", index));
                }
                if (cellPredicate(findCell))
                {
                    cells += 1;
                }
                findCell = walker.GetNextSibling(findCell);
            }
            return isSilverLight || isWin32 ? findCell.Current.Name : "" + findCell.GetCurrentPropertyValue(VALUE_PROPERTY);
        }

        #endregion

        #endregion

        #region private methods

        private static List<AutomationElement> getListItems(AutomationElement element)
        {
            TreeWalker walkerContent = TreeWalker.ContentViewWalker;
            AutomationElement child = walkerContent.GetFirstChild(element);
            List<AutomationElement> listItems = new List<AutomationElement>();
            while (child != null)
            {
                var ct = child.Current.ControlType;
                if (ct == ControlType.ListItem || ct == ControlType.DataItem)
                {
                    listItems.Add(child);
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
            if (windowHandle != 0x0)
            {
                IntPtr ptr = new IntPtr(windowHandle);              // WM_SETTEXT = 0x000C
                UIAdapter.Win32.UnsafeNativeMethods.SendMessage(ptr, 0x000C, IntPtr.Zero, text);
            }
            else
            {
                object obj = null;
                if (element.TryGetCurrentPattern(ValuePattern.Pattern, out obj))
                {
                    var vp = (ValuePattern)obj;
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

        private static AutomationElement[] GetAllComponents(ControlKind controlKind, AutomationElement window, string Uid, string Xpath, string Clazz, string Name, string Title, string Text, Boolean many)
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
                ret = WinMatcher.Find(window, controlKind, Uid, Xpath, Clazz, Name, Title, Text, many, maxTimeout);
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
                while (mainWindowHandle.Equals(IntPtr.Zero))
                {
                    if (runningTime > MAXTIME)
                    {
                        throw new Exception("Could not find window still 60 seconds");
                    }
                    List<Process> children = GetChildProcesses(process);
                    bool isExit = false;
                    foreach (Process p in children)
                    {
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

        [Obsolete]
        private static void MakeMethodTime(string methodName, long milis)
        {
            methodTime = methodName + " : " + milis + " ms";
        }

        [Obsolete]
        public static void MakeUIAutomationTime(string methodName)
        {
            MakeUIAutomationTime(methodName, -1);
        }

        [Obsolete]
        public static void MakeUIAutomationTime(string methodName, long milis)
        {
            uiAutomationTime += methodName + " : " + milis + " ms\n";
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
                logger.All("Start method toFront()");
                logger.All("Start invoke win32 api method SafeNativeMethods.SetForegroundWindow(new IntPtr(handler.Current.NativeWindowHandle))");
                SafeNativeMethods.SetForegroundWindow(new IntPtr(handler.Current.NativeWindowHandle));
                logger.All("End invoke win32 api method SafeNativeMethods.SetForegroundWindow(new IntPtr(handler.Current.NativeWindowHandle))");
                logger.All("Start set focus to handler");
                object propValue = handler.GetCurrentPropertyValue(AutomationElement.IsKeyboardFocusableProperty);
                logger.All("Get property handler.GetCurrentPropertyValue(AutomationElement.IsKeyboardFocusableProperty) : " + propValue);
                Boolean isKeyboardFocusable = (Boolean)propValue;
                logger.All("Is keyboard focusable : " + isKeyboardFocusable);
                if (isKeyboardFocusable)
                {
                    handler.SetFocus();
                }
                logger.All("End set focus to handler");
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

        [Obsolete]
        private static bool isSilverlightApp(AutomationElement e)
        {
            return e.Current.FrameworkId.ToUpper().Contains("SILVER");
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