using System;
using System.Collections.Generic;
using System.Reflection;
using System.Drawing;
using WindowsInput;

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
        public static readonly KeyboardNew DOT = new KeyboardNew(".");

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

    public class KeyboardVirtual
    {
        private string key;

        private KeyboardVirtual(string key)
        {
            this.key = key;
        }

        public static VirtualKeyCode getVirtualKeyCode(string key)
        {
            switch (key.ToLower())
            {
                case "escape": return VirtualKeyCode.ESCAPE;
                case "f1": return VirtualKeyCode.F1;
                case "f2": return VirtualKeyCode.F2;
                case "f3": return VirtualKeyCode.F3;
                case "f4": return VirtualKeyCode.F4;
                case "f5": return VirtualKeyCode.F5;
                case "f6": return VirtualKeyCode.F6;
                case "f7": return VirtualKeyCode.F7;
                case "f8": return VirtualKeyCode.F8;
                case "f9": return VirtualKeyCode.F9;
                case "f10": return VirtualKeyCode.F10;
                case "f11": return VirtualKeyCode.F11;
                case "f12": return VirtualKeyCode.F12;
                case "dig1": return VirtualKeyCode.VK_1;
                case "dig2": return VirtualKeyCode.VK_2;
                case "dig3": return VirtualKeyCode.VK_3;
                case "dig4": return VirtualKeyCode.VK_4;
                case "dig5": return VirtualKeyCode.VK_5;
                case "dig6": return VirtualKeyCode.VK_6;
                case "dig7": return VirtualKeyCode.VK_7;
                case "dig8": return VirtualKeyCode.VK_8;
                case "dig9": return VirtualKeyCode.VK_9;
                case "dig0": return VirtualKeyCode.VK_0;
                case "back_space": return VirtualKeyCode.BACK;
                case "insert": return VirtualKeyCode.INSERT;
                case "home": return VirtualKeyCode.HOME;
                case "page_up": return VirtualKeyCode.PRIOR;
                case "tab": return VirtualKeyCode.TAB;
                case "q": return VirtualKeyCode.VK_Q;
                case "w": return VirtualKeyCode.VK_W;
                case "e": return VirtualKeyCode.VK_E;
                case "r": return VirtualKeyCode.VK_R;
                case "t": return VirtualKeyCode.VK_T;
                case "y": return VirtualKeyCode.VK_Y;
                case "u": return VirtualKeyCode.VK_U;
                case "i": return VirtualKeyCode.VK_I;
                case "o": return VirtualKeyCode.VK_O;
                case "p": return VirtualKeyCode.VK_P;
                case "slash": return VirtualKeyCode.OEM_2;
                case "back_slash": return VirtualKeyCode.OEM_5;
                case "delete": return VirtualKeyCode.DELETE;
                case "end": return VirtualKeyCode.END;
                case "page_down": return VirtualKeyCode.NEXT;
                case "caps_lock": return VirtualKeyCode.CAPITAL;
                case "a": return VirtualKeyCode.VK_A;
                case "s": return VirtualKeyCode.VK_S;
                case "d": return VirtualKeyCode.VK_D;
                case "f": return VirtualKeyCode.VK_F;
                case "g": return VirtualKeyCode.VK_G;
                case "h": return VirtualKeyCode.VK_H;
                case "j": return VirtualKeyCode.VK_J;
                case "k": return VirtualKeyCode.VK_K;
                case "l": return VirtualKeyCode.VK_L;
                case "semicolon": return VirtualKeyCode.OEM_1;
                case "quote": return VirtualKeyCode.OEM_7;
                case "double_quote": return VirtualKeyCode.OEM_7; //todo
                case "enter": return VirtualKeyCode.RETURN;
                case "shift": return VirtualKeyCode.SHIFT;
                case "z": return VirtualKeyCode.VK_Z;
                case "x": return VirtualKeyCode.VK_X;
                case "c": return VirtualKeyCode.VK_C;
                case "v": return VirtualKeyCode.VK_V;
                case "b": return VirtualKeyCode.VK_B;
                case "n": return VirtualKeyCode.VK_N;
                case "m": return VirtualKeyCode.VK_M;
                case "dot": return VirtualKeyCode.OEM_PERIOD;
                case "up": return VirtualKeyCode.UP;
                case "control": return VirtualKeyCode.CONTROL;
                case "alt": return VirtualKeyCode.MENU;
                case "space": return VirtualKeyCode.SPACE;
                case "left": return VirtualKeyCode.LEFT;
                case "down": return VirtualKeyCode.DOWN;
                case "right": return VirtualKeyCode.RIGHT;
                case "plus": return VirtualKeyCode.OEM_PLUS;
                case "minus": return VirtualKeyCode.OEM_MINUS;
                case "underscore": return VirtualKeyCode.OEM_MINUS; //todo
                default: return VirtualKeyCode.VK_Q;
            }
        }
    }

    #endregion
}
