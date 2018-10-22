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
using System.Threading.Tasks;
using System.Windows.Forms;

namespace mock_win
{
    class GlobalMouseHandler : IMessageFilter
    {
        private const int WM_LBUTTONDOWN = 0x201;
        private const int WM_LBUTTONUP = 0x202;
        private const int WM_MOUSEMOVE = 0x200;
        private const int WM_KEYDOWN = 0x100;
        private const int WM_KEYUP = 0x101;
        private MockWin main;

        public GlobalMouseHandler(MockWin m)
        {
            this.main = m;
        }

        public bool PreFilterMessage(ref Message m)
        {
            if (m.Msg == WM_LBUTTONDOWN)
            {
                var c = Control.FromHandle(m.HWnd);
                IEnumerable<Control> controls = GetAllControls(main);
            }
            return false;
        }

        public static IEnumerable<Control> GetAllControls(Control root)
        {
            var stack = new Stack<Control>();
            stack.Push(root);

            while (stack.Any())
            {
                var next = stack.Pop();
                foreach (Control child in next.Controls)
                    stack.Push(child);

                yield return next;
            }
        }
    }
}
