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

    public enum AttributeKind : int
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

    public enum LocatorFieldKind : int
    {
        UID = 0, 
        CLAZZ = 1, 
        NAME = 2, 
        TITLE = 3, 
        ACTION = 4, 
        TEXT = 5, 
        TOOLTIP = 6, 
    
        XPATH = 7, 
        EXPRESSION = 8, 
        COLUMNS = 9, 
        WEAK = 10,
        ADDITION = 11, 
        VISIBILITY = 12, 
        USE_NUMERIC_HEADER = 13, 
        USE_ABSOLUTE_XPATH = 14
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
                case "num_lock": return VirtualKeyCode.NUMLOCK;
                case "num_separator": return VirtualKeyCode.SEPARATOR;
                case "num_divide": return VirtualKeyCode.DIVIDE;
                case "num_multiply" : return VirtualKeyCode.MULTIPLY;
                case "num_minus" : return VirtualKeyCode.SUBTRACT;
                case "num_dig7" : return VirtualKeyCode.NUMPAD7;
                case "num_dig8" : return VirtualKeyCode.NUMPAD8;
                case "num_dig9": return VirtualKeyCode.NUMPAD9;
                case "num_plus": return VirtualKeyCode.ADD;
                case "num_dig4": return VirtualKeyCode.NUMPAD4;
                case "num_dig5": return VirtualKeyCode.NUMPAD5;
                case "num_dig6": return VirtualKeyCode.NUMPAD6;
                case "num_dig1": return VirtualKeyCode.NUMPAD1;
                case "num_dig2": return VirtualKeyCode.NUMPAD2;
                case "num_dig3": return VirtualKeyCode.NUMPAD3;
                case "num_dig0": return VirtualKeyCode.NUMPAD0;
                case "num_dot": return VirtualKeyCode.DECIMAL;
                case "num_enter": return VirtualKeyCode.RETURN;

                default: return VirtualKeyCode.VK_Q;
            }
        }
    }

    #endregion
}
