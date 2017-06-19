using System;
using System.Collections;
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
    public partial class Main : Form
    {
        private int counter = 0;
        private int sliderValue = -99;
        Timer timer;

        public Main()
        {
            InitializeComponent();
            fillTable();
            fillListView();
            fillContextMenu();
            ComboBox.SelectedIndex = 0;

            this.timer = new Timer();
            this.timer.Interval = 100;
            this.timer.Tick += new EventHandler(timer_Tick);
            this.timer.Enabled = true;
        }

        private void timer_Tick(object sender, EventArgs e)
        {
            if (Slider.Value != sliderValue)
            {
                sliderValue = Slider.Value;
                sliderLabel.Text = "Slider_" + Slider.Value;
            }
        }

        private void fillContextMenu()
        {
            ContextMenu cm = new ContextMenu();
            cm.MenuItems.Add("one");
            cm.MenuItems.Add("two");
            cm.MenuItems.Add("three");
            ToggleButton.ContextMenu = cm;
        }

        private void fillListView()
        {
            List<string[]> list = new List<string[]>();
            list.Add(new String[] { "tr_1_td_1", "tr_1_td_2", "tr_1_td_3" });
            list.Add(new String[] { "tr_2_td_1", "tr_2_td_2", "tr_2_td_3" });
            list.Add(new String[] { "tr_3_td_1", "tr_3_td_2", "tr_3_td_3" });
            list.Add(new String[] { "Green", "", "51" });
            list.Add(new String[] { "Stark", "North", "35" });

            foreach (String[] item in list)
            {
                ListViewItem lvi = new ListViewItem(item[0]);
                lvi.SubItems.Add(item[1]);
                lvi.SubItems.Add(item[2]);
                Table.Items.Add(lvi);
            }
        }

        private void fillTable()
        {
            Table1.Columns[3].DefaultCellStyle.Format = "yyyy.MM.dd HH:mm:ss";
            for (int i=0; i<5; i++)
            {
                Table1.Rows.Add();
                Table1.Rows[i].Cells["name"].Value = "name_"+i;
                Table1.Rows[i].Cells["pid"].Value = i;
                Table1.Rows[i].Cells["check"].Value = true;
                Table1.Rows[i].Cells["numberColumn"].Value = i*50;
                Table1.Rows[i].Cells["dateColumn"].Value = new DateTime(2010+i, i+1, i+1, i+1, i+1, i+1);
            }
        }

        private void saveToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (saveFileDialog1.ShowDialog() == DialogResult.Cancel) return;
        }

        private void CommonMouseMove(object sender, MouseEventArgs e)
        {
            Control control = (Control)sender;
            string text;
            switch (control.Name)
            {
                case "CentralLabel":
                    text = "Label";
                    break;
                case "Blue":
                case "Green":
                case "Yellow":
                case "Orange":
                    text = "RadioButton";
                    break;
                default:
                    text = control.Name;
                    break;
            }
            moveLabel.Text = text + "_move";
        }

        private void CommonMouseDown(object sender, MouseEventArgs e)
        {
            writeTextOncentralLabelMouse(writeControlNameOnCentralLabel(sender), e);
        }

        private void writeTextOncentralLabelMouse(string text, MouseEventArgs e)
        {
            pushLabel.Text = "";
            if (e.Button == MouseButtons.Left)
            {
                if (e.Clicks == 1)
                {
                    CentralLabel.Text = text + "_click";
                }
                else
                {
                    CentralLabel.Text = text + "_double_click";
                }
            }
            else
            {
                CentralLabel.Text = text + "_rightClick";
            }
        }

        private void CommonKeyDown(object sender, KeyEventArgs e)
        {
            string text = writeControlNameOnCentralLabel(sender);
            if (e.KeyValue == 17)
            {
                downUpLabel.Text = text + "_down_Control";
            }
        }

        private void CommonKeyUp(object sender, KeyEventArgs e)
        {
            string text = writeControlNameOnCentralLabel(sender);
            if (e.KeyValue == 17)
            {
                downUpLabel.Text = text + "_up_Control";
            }
        }

        private void CommonKeyPress(object sender, KeyPressEventArgs e)
        {
            string text = writeControlNameOnCentralLabel(sender);
            if (e.KeyChar == (int)Keys.Escape)
            {
                pressLabel.Text = text + "_press_Escape";
            }
        }

        private string writeControlNameOnCentralLabel(object sender)
        {
            Control control = (Control)sender;
            string text;
            switch (control.Name)
            {
                case "CentralLabel":
                    text = "Label";
                    break;
                case "Blue":
                case "Green":
                case "Yellow":
                case "Orange":
                    text = "RadioButton";
                    break;
                default:
                    text = control.Name;
                    break;
            }
            return text;
        }

        private void writeTextOncentralLabelKeyboard(string text, EventArgs e)
        {
            if (((KeyEventArgs)e).KeyValue == 17)
            {

            }
            //if (e == Keys.Escape)
            //{
            //    pressLabel.Text = text + "_press_Escape";
            //}

            if (Control.ModifierKeys == Keys.Control)
            {
                CentralLabel.Text = text + "_press_Control";
            }
        }

        private void CheckBox_CheckedChanged(object sender, EventArgs e)
        {
            CheckBox control = (CheckBox)sender;
            if (control.Checked)
            {
                CentralLabel.Text = control.Name + "_checked";
            }
            else
            {
                CentralLabel.Text = control.Name + "_unchecked";
            }
        }

        private void ComboBox_SelectedValueChanged(object sender, EventArgs e)
        {
            CentralLabel.Text = ComboBox.Name + "_" + ComboBox.SelectedItem;
        }

        private void ComboBox_TextValueChanged(object sender, EventArgs e)
        {
            CentralLabel.Text = ComboBox.Name + "_" + ComboBox.Text;
        }

        private void TextBox_TextChanged(object sender, EventArgs e)
        {
            CentralLabel.Text = TextBox.Name + "_" + TextBox.Text;
        }

        private void TabPanel_Selected(object sender, TabControlEventArgs e)
        {
            TabControl control = (TabControl)sender;
            CentralLabel.Text = control.Name + "_" + control.SelectedTab.Text;
        }

        private void ListView_SelectedIndexChanged(object sender, EventArgs e)
        {
            ListBox control = (ListBox)sender;
            control.GetSelected(0);
            CentralLabel.Text = control.Name + "_" + control.SelectedItem.ToString();
        }

        private void sixToolStripMenuItem_Click(object sender, EventArgs e)
        {
            CentralLabel.Text = "six_click";
        }

        private void toolStripMenuItem1_Click(object sender, EventArgs e)
        {
            CentralLabel.Text = "cm_one_click";
        }

        private void showButton_Click(object sender, EventArgs e)
        {
            hideButton.Visible = true;
        }

        private void hideButton_Click(object sender, EventArgs e)
        {
            hideButton.Visible = false;
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void aboutToolStripMenuItem_Click(object sender, EventArgs e)
        {

        }

        private void Table_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void ComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void countButton_Click(object sender, EventArgs e)
        {
            counter++;
            countLabel.Text = counter.ToString();
        }

        private void countButtonClear_Click(object sender, EventArgs e)
        {
            counter = 0;
            countLabel.Text = "0";
        }

        private void countLabel_Click(object sender, EventArgs e)
        {

        }

        private void Button_Click(object sender, EventArgs e)
        {
            pushLabel.Text = "Button_push";
        }

        //private void Slider_ValueChanged(object sender, EventArgs e)
        //{
        //    sliderLabel.Text = "Slider_" + ((TrackBar)sender).Value;
        //}
    }
}