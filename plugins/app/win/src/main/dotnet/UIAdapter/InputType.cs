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
    /// <summary>
    /// Specifies the type of the input event. This member can be one of the following values. 
    /// </summary>
    public enum InputType : uint // UInt32
    {
        /// <summary>
        /// INPUT_MOUSE = 0x00 (The event is a mouse event. Use the mi structure of the union.)
        /// </summary>
        MOUSE = 0,

        /// <summary>
        /// INPUT_KEYBOARD = 0x01 (The event is a keyboard event. Use the ki structure of the union.)
        /// </summary>
        KEYBOARD = 1,

        /// <summary>
        /// INPUT_HARDWARE = 0x02 (Windows 95/98/Me: The event is from input hardware other than a keyboard or mouse. Use the hi structure of the union.)
        /// </summary>
        HARDWARE = 2,
    }
}
