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
