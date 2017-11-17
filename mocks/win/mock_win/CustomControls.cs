////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
