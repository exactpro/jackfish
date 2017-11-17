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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace mock_win
{
    public partial class Dialog : Form
    {
        public Dialog()
        {
            InitializeComponent();
        }

        private void Dialog_KeyDown(object sender, KeyEventArgs e)
        {
            ((MockWin)Application.OpenForms[1]).GlobalKeyDown(sender, e);
        }

        private void Dialog_KeyPress(object sender, KeyPressEventArgs e)
        {
            ((MockWin)Application.OpenForms[1]).GlobalKeyPress(sender, e);
        }

        private void Dialog_KeyUp(object sender, KeyEventArgs e)
        {
            ((MockWin)Application.OpenForms[1]).GlobalKeyUp(sender, e);
        }

        private void Dialog_MouseDown(object sender, MouseEventArgs e)
        {
            string text = ((MockWin)Application.OpenForms[1]).writeControlNameOnCentralLabel(sender);
            ((MockWin)Application.OpenForms[1]).writeTextOncentralLabelMouse(text, e);
        }

        private void Dialog_MouseMove(object sender, MouseEventArgs e)
        {
            ((MockWin)Application.OpenForms[1]).CommonMouseMove(sender, e);
        }
    }
}
