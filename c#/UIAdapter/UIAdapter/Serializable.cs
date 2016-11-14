using System;
using System.Collections.Generic;
using System.Reflection;
using System.Drawing;

namespace UIAdapter
{
    #region Enums ==================================================================================================================

    public static class MouseConstants
    {
        internal const uint MOUSEEVENTF_MOVE = 0x0001;   // Movement occurred.
        internal const uint MOUSEEVENTF_LEFTDOWN = 0x0002;   // The left button is down.
        internal const uint MOUSEEVENTF_LEFTUP = 0x0004;   // The left button is up.
        internal const uint MOUSEEVENTF_RIGHTDOWN = 0x0008;   // The right button is down.
        internal const uint MOUSEEVENTF_RIGHTUP = 0x0010;   // The right button is up.
        internal const uint MOUSEEVENTF_MIDDLEDOWN = 0x0020;   // The middle button is down.
        internal const uint MOUSEEVENTF_MIDDLEUP = 0x0040;   // The middle button is up.
        internal const uint MOUSEEVENTF_XDOWN = 0x0080;   // An X button was pressed.
        internal const uint MOUSEEVENTF_XUP = 0x0100;   // An X button was released.
        internal const uint MOUSEEVENTF_WHEEL = 0x0800;   // The wheel has been moved, if the mouse has a wheel. The amount of movement is specified in dwData
        internal const uint MOUSEEVENTF_HWHEEL = 0x01000;  // The wheel button is tilted.
        internal const uint MOUSEEVENTF_ABSOLUTE = 0x8000;
    }

    enum AttributeKind : int
    {
        ID = 0,
        UID = 1,
        CLASS = 2,
        TEXT = 3,
        NAME = 4,
        TYPE_NAME = 5,
        ENABLED = 6,
        VISIBLE = 7,
        ITEMS = 8
    }

    enum MouseAction : uint
    {
        Move = 0,
        LeftClick = 1,
        RightClick = 2,
        LeftDoubleClick = 3,
        RightDoubleClick = 4
    }

    public enum KindInformation
    {
        Value = 0, Color = 1, BackColor = 2
    }

    public enum ControlKind : long
    {
        Any = 0,
        Wait = 1,
        Button = 2,
        CheckBox = 3,
        ComboBox = 4,
        Dialog = 5,
        Frame = 6,
        Image = 7,
        Label = 8,
        ListView = 9,
        Menu = 10,
        MenuItem = 11,
        Panel = 12,
        ProgressBar = 13,
        RadioGroup = 14,
        Row = 15,
        ScrollBar = 16,
        Slider = 17,
        Splitter = 18,
        Spinner = 19,
        Table = 20,
        TabPanel = 21,
        TextBox = 22,
        ToggleButton = 23,
        Tooltip = 24,
        Tree = 25,
        TreeItem = 26
    }

    public class KeyboardNew
    {
        private string key;

        private KeyboardNew(string key)
        {
            this.key = key;
        }

        public static string getKey(string fieldName)
        {
            Type type = typeof(KeyboardNew);
            FieldInfo[] fields = type.GetFields(BindingFlags.Static | BindingFlags.Public);
            foreach (FieldInfo field in fields)
            {
                if (field.Name.Equals(fieldName))
                {
                    return (field.GetValue(null) as KeyboardNew).key;
                }
            }
            return fieldName.ToLower();
        }

        public static readonly KeyboardNew ESCAPE = new KeyboardNew("{ESC}");
        public static readonly KeyboardNew F1 = new KeyboardNew("{F1}");
        public static readonly KeyboardNew F2 = new KeyboardNew("{F2}");
        public static readonly KeyboardNew F3 = new KeyboardNew("{F3}");
        public static readonly KeyboardNew F4 = new KeyboardNew("{F4}");
        public static readonly KeyboardNew F5 = new KeyboardNew("{F5}");
        public static readonly KeyboardNew F6 = new KeyboardNew("{F6}");
        public static readonly KeyboardNew F7 = new KeyboardNew("{F7}");
        public static readonly KeyboardNew F8 = new KeyboardNew("{F8}");
        public static readonly KeyboardNew F9 = new KeyboardNew("{F9}");
        public static readonly KeyboardNew F10 = new KeyboardNew("{F10}");
        public static readonly KeyboardNew F11 = new KeyboardNew("{F11}");
        public static readonly KeyboardNew F12 = new KeyboardNew("{F12}");

        public static readonly KeyboardNew DIG0 = new KeyboardNew("0");
        public static readonly KeyboardNew DIG1 = new KeyboardNew("1");
        public static readonly KeyboardNew DIG2 = new KeyboardNew("2");
        public static readonly KeyboardNew DIG3 = new KeyboardNew("3");
        public static readonly KeyboardNew DIG4 = new KeyboardNew("4");
        public static readonly KeyboardNew DIG5 = new KeyboardNew("5");
        public static readonly KeyboardNew DIG6 = new KeyboardNew("6");
        public static readonly KeyboardNew DIG7 = new KeyboardNew("7");
        public static readonly KeyboardNew DIG8 = new KeyboardNew("8");
        public static readonly KeyboardNew DIG9 = new KeyboardNew("9");

        public static readonly KeyboardNew BACK_SPACE = new KeyboardNew("{BACKSPACE}");
        public static readonly KeyboardNew INSERT = new KeyboardNew("{INSERT}");
        public static readonly KeyboardNew HOME = new KeyboardNew("{HOME}");
        public static readonly KeyboardNew PAGE_UP = new KeyboardNew("{PGUP}");
        public static readonly KeyboardNew TAB = new KeyboardNew("{TAB}");
        public static readonly KeyboardNew CAPS_LOCK = new KeyboardNew("{CAPSLOCK}");
        public static readonly KeyboardNew DELETE = new KeyboardNew("{DELETE}");
        public static readonly KeyboardNew END = new KeyboardNew("{END}");
        public static readonly KeyboardNew PAGE_DOWN = new KeyboardNew("{PGDN}");
        public static readonly KeyboardNew ENTER = new KeyboardNew("{ENTER}");

        public static readonly KeyboardNew SPACE = new KeyboardNew(" ");
        public static readonly KeyboardNew SLASH = new KeyboardNew("/");
        public static readonly KeyboardNew BACK_SLASH = new KeyboardNew("\\");
        public static readonly KeyboardNew QUOTE = new KeyboardNew("'");
        public static readonly KeyboardNew DOUBLE_QUOTE = new KeyboardNew("\"");

        public static readonly KeyboardNew UP = new KeyboardNew("{UP}");
        public static readonly KeyboardNew DOWN = new KeyboardNew("{DOWN}");
        public static readonly KeyboardNew LEFT = new KeyboardNew("{LEFT}");
        public static readonly KeyboardNew RIGHT = new KeyboardNew("{RIGHT}");

        public static readonly KeyboardNew UNDERSCORE = new KeyboardNew("_");
    }

    #endregion
}
