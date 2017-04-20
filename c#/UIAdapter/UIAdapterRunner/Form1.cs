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

namespace UIAdapterRunner
{
    public partial class UIAdapterRunner : Form
    {
        public UIAdapterRunner()
        {
            InitializeComponent();
            int[] arr = new int[100];
            string dirJF = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);
            UIAdapter.Program.Run(dirJF.Substring(0, dirJF.IndexOf("JackFish")+"Jackfish".Length) + "/mocks/win/mock_win/run/mock_win.exe", "", "",false);
            UIAdapter.Program.SetPluginInfo("KindMap{0:-1;2:50000,50031,50005;3:50002;4:50003;5:50032;6:50032;7:50006;8:50020;9:50008,50028;11:50011;12:50033;13:50012;14:50013;15:50025;16:50014;17:50015;19:50016;20:50036,50028;21:50018;22:50004,50030;23:50000;24:50022;25:50023;26:50024} LocatorMap{0:uid;1:class;2:name;5:text}");
            UIAdapter.Program.FindAllForLocator(arr, arr.Length, null, 0, null, "/Window", null, null, null, null, false);
            UIAdapter.Program.FindAllForLocator(arr, arr.Length, getStringId(ref arr), (int) UIAdapter.ControlKind.Tree, "Tree", null, null, null, null, null, true);
            UIAdapter.Program.FindAll(arr, arr.Length, getStringId(ref arr), (int) TreeScope.Descendants, AutomationElementIdentifiers.NameProperty.Id, "sports");

            UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "SetValue", "30.0", 2);
            UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "Select", "30.0", 2);
            UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "Expand", "30.0", 2);
            UIAdapter.Program.DoPatternCall(getStringId(ref arr), 10003, "Collapse", "30.0", 2);
        }

        private string getStringId(ref int[] arr)
        {
            string res = arr[2] + "," + arr[3];
            Array.Clear(arr, 0, arr.Length);
            Console.WriteLine(res);
            return res;
        }
    }
}
