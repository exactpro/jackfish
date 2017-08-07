// (c) Copyright Microsoft Corporation.
// This source is subject to the Microsoft Permissive License.
// See http://www.microsoft.com/opensource/licenses.mspx#Ms-PL.
// All other rights reserved.

using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;
using WindowsInput;


namespace UIAdapter.Win32
{
    public struct MyRect
    {
        public int Left {get;set; }
        public int Top {get;set; }
        public int Right {get;set; }
        public int Bottom { get; set; }
    }

    internal static class UnsafeNativeMethods
    {
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        internal static extern IntPtr SendMessage(IntPtr hWnd, uint Msg, IntPtr wParam, string lParam);

        [DllImport("user32.dll")]
        internal static extern uint MapVirtualKey(uint uCode, uint uMapType);
        [DllImport("user32.dll", SetLastError = true)]
        internal static extern uint SendInput(uint numberOfInputs, INPUT[] inputs, int sizeOfInputStructure);

        [DllImport("user32.dll")]
        internal static extern int GetClassName(IntPtr hwnd, StringBuilder lpClassName, int nMaxCount);

        [DllImport("user32.dll", SetLastError = true)]
        internal static extern bool MoveWindow(IntPtr hWnd, int X, int Y, int nWidth, int nHeight, bool bRepaint);
        [DllImport("user32.dll")]
        internal static extern bool GetWindowRect(IntPtr hWnd, ref MyRect rect);

        [DllImport("gdi32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern IntPtr GetCurrentObject(IntPtr hdc, uint objectType);
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern IntPtr GetDC(IntPtr hWnd);
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern IntPtr GetWindowDC(IntPtr hWnd);
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        internal static extern int GetWindowLong(IntPtr hWnd, int nIndex);
        [DllImport("ntdll.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern void NtClose(IntPtr hToken);
        [DllImport("ntdll.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern int NtOpenProcessToken(IntPtr hProcess, uint accessMask, out IntPtr hToken);
        [DllImport("ntdll.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern int NtQueryInformationToken(IntPtr hToken, uint tokenElevationType, out IntPtr elevationInfo, uint bufferSize, out uint tokensize);
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        internal static extern bool RegisterHotKey(IntPtr hWnd, int atom, uint fsModifiers, uint vk);
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern int ReleaseDC(IntPtr hWnd, IntPtr hDC);
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern bool SetForegroundWindow(IntPtr hWnd);
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        internal static extern int SetWindowLong(IntPtr hWnd, int nIndex, int dwNewLong);
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        internal static extern bool UnregisterHotKey(IntPtr hWnd, int atom);

        [StructLayout(LayoutKind.Sequential)]
        internal struct TOKEN_ELEVATION_INFO
        {
            [MarshalAs(UnmanagedType.U4)]
            internal uint TokenIsElevated;
        }
    }

    internal static class SafeNativeMethods
    {
        // Methods
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("gdi32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern bool BitBlt(IntPtr hDC, int x, int y, int nWidth, int nHeight, IntPtr hSrcDC, int xSrc, int ySrc, int dwRop);
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("user32.dll", ExactSpelling = true)]
        internal static extern bool SetProcessDPIAware();
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern bool SetWindowPos(IntPtr hWnd, IntPtr hwndAfter, int x, int y, int width, int height, int flags);
        [return: MarshalAs(UnmanagedType.Bool)]
        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        internal static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

        [DllImport("user32.dll")]
        public static extern int SetForegroundWindow(IntPtr hWnd);
        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        internal static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint cButtons, uint dwExtraInfo);
    }

    static class NativeMethods
    {
        public const int DLGC_STATIC = 0x100;
        public const int GWL_EXSTYLE = -20;
        public static readonly IntPtr HWND_TOPMOST = new IntPtr(-1);
        public const uint MOD_ALT = 1;
        public const uint MOD_CONTROL = 2;
        public const uint MOD_SHIFT = 4;
        public const uint OBJ_BITMAP = 7;
        public const int SRCCOPY = 0xcc0020;
        public const int SW_RESTORE = 9;
        public const int SW_SHOWNA = 8;
        public const int SWP_NOACTIVATE = 0x10;
        public const int TOKEN_ELEVATION = 20;
        public const int TOKEN_ELEVATION_TYPE = 0x12;
        public const int TOKEN_ELEVATION_TYPE_DEFAULT = 1;
        public const int TOKEN_ELEVATION_TYPE_FULL = 2;
        public const int TOKEN_ELEVATION_TYPE_LIMITED = 3;
        public const int TOKEN_QUERY = 8;
        public const int VK_F1 = 0x70;
        public const uint VK_R = 0x52;
        public const int VK_SHIFT = 0x10;
        public const int WM_GETDLGCODE = 0x87;
        public const int WM_HOTKEY = 0x312;
        public const int WM_KEYDOWN = 0x100;
        public const int WM_NCLBUTTONDBLCLK = 0xa3;
        public const int WS_EX_TOOLWINDOW = 0x80;
    }

}
