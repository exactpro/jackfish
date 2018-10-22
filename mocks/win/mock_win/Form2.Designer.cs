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
namespace mock_win
{
    partial class Dialog
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.DialogButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // DialogButton
            // 
            this.DialogButton.Location = new System.Drawing.Point(98, 12);
            this.DialogButton.Name = "DialogButton";
            this.DialogButton.Size = new System.Drawing.Size(89, 23);
            this.DialogButton.TabIndex = 0;
            this.DialogButton.Text = "DialogButton";
            this.DialogButton.UseVisualStyleBackColor = true;
            // 
            // Dialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
            this.ClientSize = new System.Drawing.Size(284, 262);
            this.Controls.Add(this.DialogButton);
            this.KeyPreview = true;
            this.Location = new System.Drawing.Point(600, 400);
            this.Name = "Dialog";
            this.Text = "Dialog";
            this.KeyDown += new System.Windows.Forms.KeyEventHandler(this.Dialog_KeyDown);
            this.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.Dialog_KeyPress);
            this.KeyUp += new System.Windows.Forms.KeyEventHandler(this.Dialog_KeyUp);
            this.MouseDown += new System.Windows.Forms.MouseEventHandler(this.Dialog_MouseDown);
            this.MouseMove += new System.Windows.Forms.MouseEventHandler(this.Dialog_MouseMove);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button DialogButton;
    }
}