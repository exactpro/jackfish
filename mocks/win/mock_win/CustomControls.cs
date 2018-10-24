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
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace mock_win
{
    class CustomListBox : ListBox
    {
        public delegate void ScrollEventHandler(Control sender);
        public event ScrollEventHandler ScrollPositionChange;

        Timer timer;
        private int topIndex_now;

        public CustomListBox()
        {
            this.timer = new Timer();
            this.timer.Interval = 10;
            this.timer.Tick += Timer_Tick;
            this.timer.Enabled = true;
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            if (topIndex_now != this.TopIndex)
            {
                topIndex_now = this.TopIndex;
                ScrollPositionChange(this);
            }
        }
    }

    class CustomTreeView : TreeView
    {
        public delegate void ScrollEventHandler(Control sender);
        public event ScrollEventHandler ScrollPositionChange;

        Timer timer;
        String previewTopNodeText = "Green";

        public CustomTreeView()
        {
            this.timer = new Timer();
            this.timer.Interval = 10;
            this.timer.Tick += Timer_Tick;
            this.timer.Enabled = true;
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            if (this.TopNode != null)
            {
                if (previewTopNodeText != this.TopNode.Text)
                {
                    previewTopNodeText = this.TopNode.Text;
                    ScrollPositionChange(this);
                }
            }
        }
    }

    class CustomComboBox : ComboBox
    {
        public delegate void ScrollEventHandler(Control sender);
        public event ScrollEventHandler ScrollPositionChange;
        public int skip = -1;
        public bool scroll = false;

        protected override void WndProc(ref Message m)
        {
            base.WndProc(ref m);
            if (m.Msg == 308)
            {
                if (skip == 0)
                {
                    if (!scroll)
                    {
                        ScrollPositionChange(this);
                    }
                    scroll = !scroll;
                }
                if (skip > 0)
                {
                    skip -= 1;
                }
            }
        }
    }
}
