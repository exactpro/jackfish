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
        
        //bool scroll = false;
        //protected override void WndProc(ref Message m)
        //{
        //    base.WndProc(ref m);
        //    if (m.Msg == 0x115)
        //    {
        //        if (scroll)
        //        {
        //            ScrollPositionChange(this);
        //        }
        //        scroll = !scroll;
        //    }
        //}

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

        //bool scroll = false;
        //protected override void WndProc(ref Message m)
        //{
        //    base.WndProc(ref m);
        //    if (m.Msg == 0x115)
        //    {
        //        if (scroll)
        //        {
        //            ScrollPositionChange(this);
        //        }
        //        scroll = !scroll;
        //    }
        //}

        Timer timer;
        private string topIndexName = "";

        public CustomTreeView()
        {
            this.timer = new Timer();
            this.timer.Interval = 10;
            this.timer.Tick += Timer_Tick;
            this.timer.Enabled = true;
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            //if (topIndexName != this.TopNode.Text)
            //{
            //    topIndexName = this.TopNode.Text;
            //    ScrollPositionChange(this);
            //}
        }
    }

    class CustomComboBox : ComboBox
    {
        public delegate void ScrollEventHandler(Control sender);
        public event ScrollEventHandler ScrollPositionChange;

        bool scroll = false;
        protected override void WndProc(ref Message m)
        {
            base.WndProc(ref m);
            if (m.Msg == 0x134) //https://wiki.winehq.org/List_Of_Windows_Messages
            {
                if (scroll)
                {
                    ScrollPositionChange(this);
                }
                scroll = !scroll;
            }
        }

        //public delegate void TopIndexHandler(Control sender, string topIndex);
        //public event TopIndexHandler TopIndexChanged;

        //Point point;
        //Timer timer;
        //private int topIndex_now = -99;
        //private string topIndexName;

        //public CustomComboBox()
        //{
        //    this.timer = new Timer();
        //    this.timer.Interval = 10;
        //    this.timer.Tick += Timer_Tick;
        //    this.timer.Enabled = true;
        //    this.point = new Point(10, 10);
        //}

        //private void Timer_Tick(object sender, EventArgs e)
        //{
        //    if (this.DroppedDown)
        //    {
        //        var v = this.DisplayMember;
        //        this.GetChildAtPoint(new Point(10, 10));
        //    }
        //    //if (topIndexName != this.GetChildAtPoint(point).Text)
        //    //{
        //    //    topIndexName = this.GetChildAtPoint(point).Text;
        //    //    TopIndexChanged(this, topIndexName);
        //    //}
        //}
    }
}
