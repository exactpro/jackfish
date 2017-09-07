using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Automation;
using System.Windows.Forms;
using UIAdapter;
using WindowsInput;

namespace UIAdapterRunner
{
    public partial class UIAdapterRunner : Form
    {
        public UIAdapterRunner()
        {
            InitializeComponent();
            int[] arr = new int[100];
            string dirJF = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);
            // start, connect
            //UIAdapter.Program.Run(dirJF.Substring(0, dirJF.IndexOf("JackFish")+"Jackfish".Length) + "/mocks/win/mock_win/run/mock_win.exe", "", "",false);
            UIAdapter.Program.Connect("SilverLightMock - Internet Explorer", Int32.MinValue, Int32.MinValue, Int32.MinValue, Int32.MinValue, 5000, false);
            //UIAdapter.Program.Connect("MockWin", Int32.MinValue, Int32.MinValue, Int32.MinValue, Int32.MinValue, 5000, false);
            //UIAdapter.Program.SetPluginInfo("KindMap{0:-1;2:50000,50031,50005;3:50002;4:50003;5:50032;6:50032;7:50006;8:50020;9:50008,50028;11:50011;12:50033;13:50012;14:50013;15:50025;16:50014;17:50015;19:50016;20:50036,50028;21:50018;22:50004,50030;23:50000;24:50022;25:50023;26:50024} LocatorMap{0:uid;1:class;2:name;5:text}");
            UIAdapter.Program.SetPluginInfo("KindMap{0:-1;1:-1;2:50000,50031,50005;3:50002;4:50003;5:50032;8:50020;9:50008,50028;10:50009;11:50011;12:50033;13:50012,50033;14:50013;15:50025;16:50014;17:50015;20:50036,50028;21:50018;22:50030,50004;23:50000;26:50024} LocatorMap{0:uid;1:class;2:name;5:text}");
            // win example
            //int i = UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, 0, null, "/Window", null, null, null, null, false);
            //int i = UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.Any, "Main", null, null, "MockWin", null, null, false);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int)ControlKind.Menu, null, null, null, "Menu", null, null, true);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.Tree, null, null, null, "Tree", null, null, true);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.Tree, "treeView1", null, null, null, null, null, true, true);
            //UIAdapter.Program.getXMLFromTree(getStringId(ref arr));
            //int m = UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.Menu, null, null, null, "Menu", null, null, true);
            //string s = UIAdapter.Program.ListAll(null, (int)ControlKind.TextBox, "protocolText", null, null, null, null, null, false, false);
            //Console.WriteLine(s);
            //string s = UIAdapter.Program.ListAll(getStringId(ref arr), (int)ControlKind.Menu, null, null, null, "Menu", null, null, true);
            // silverlight example
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, 0, null, "//Pane[@class=\"Internet Explorer_Server\" and @name=\"SilverLightMock\"]", null, null, null, null, false);

            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int) UIAdapter.ControlKind.Tree, "Tree", null, null, null, null, null, true);
            //UIAdapter.Program.ListAll(getStringId(ref arr), (int)ControlKind.Button, null, null, null, "invisibleButton", null, null, false);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.ComboBox, "comboboxSimple", null, null, null, null, null, false, false);
            UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, (int)ControlKind.ComboBox, "comboboxCheckboxes", null, null, null, null, null, false, false);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int)UIAdapter.ControlKind.Button, "Button", null, null, null, null, null, false);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int)UIAdapter.ControlKind.Button, "colorButton", null, null, null, null, null, false);
            //UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int)ControlKind.MenuItem, null, ".//MenuItem[@name=\"Menu2\"]", null, null, null, null, false);
            //UIAdapter.Program.GetProperty(getStringId(ref arr), 30001);
            //UIAdapter.Program.getRectangle(getStringId(ref arr));
            //UIAdapter.Program.UpAndDown(getStringId(ref arr), "CONTROL", true);
            //int c = UIAdapter.Program.FindAll(arr, arr.Length, getStringId(ref arr), (int)TreeScope.Descendants, 30004, "list item");

            UIAdapter.Program.getList(getStringId(ref arr),false);

            //UIAdapter.Program.FindAll(arr, arr.Length, getStringId(ref arr), (int) TreeScope.Descendants, AutomationElementIdentifiers.NameProperty.Id, "sports");

            //UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "SetValue", "30.0", 2);
            //UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "Select", "30.0", 2);
            //UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10000, "Expand", "30.0", 0);
            //UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "Collapse", "30.0", 2);
        }

        private string getStringId(ref int[] arr)
        {
            string res = "";
            for (int i = 2; i < arr.Length; i++)
            {
                if (arr[i] != 0)
                {
                    res += arr[i];
                }
                else
                {
                    break;
                }
                if (arr[i + 1] != 0)
                {
                    res += ",";
                }
            }
            Array.Clear(arr, 0, arr.Length);
            Console.WriteLine(res);
            return res;
        }
    }
}
