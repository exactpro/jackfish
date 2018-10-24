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

namespace WindowsInput
{
#pragma warning disable 649
    /// <summary>
    /// The INPUT structure is used by SendInput to store information for synthesizing input events such as keystrokes, mouse movement, and mouse clicks. (see: http://msdn.microsoft.com/en-us/library/ms646270(VS.85).aspx)
    /// Declared in Winuser.h, include Windows.h
    /// </summary>
    /// <remarks>
    /// This structure contains information identical to that used in the parameter list of the keybd_event or mouse_event function.
    /// Windows 2000/XP: INPUT_KEYBOARD supports nonkeyboard input methods, such as handwriting recognition or voice recognition, as if it were text input by using the KEYEVENTF_UNICODE flag. For more information, see the remarks section of KEYBDINPUT.
    /// </remarks>
    struct INPUT
    {
        /// <summary>
        /// Specifies the type of the input event. This member can be one of the following values. 
        /// InputType.MOUSE - The event is a mouse event. Use the mi structure of the union.
        /// InputType.KEYBOARD - The event is a keyboard event. Use the ki structure of the union.
        /// InputType.HARDWARE - Windows 95/98/Me: The event is from input hardware other than a keyboard or mouse. Use the hi structure of the union.
        /// </summary>
        public UInt32 Type;

        /// <summary>
        /// The data structure that contains information about the simulated Mouse, Keyboard or Hardware event.
        /// </summary>
        public MOUSEKEYBDHARDWAREINPUT Data;
    }
#pragma warning restore 649
}
