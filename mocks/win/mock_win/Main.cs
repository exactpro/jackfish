using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Runtime.InteropServices;
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
        Point mouseDownPos;
        long mouseTimeClick;

        public Main()
        {
            InitializeComponent();
            fillTable();
            fillTable1();
            fillListView();
            fillContextMenu();
            ComboBox.SelectedIndex = 0;
            createDialog();

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

        public static void createDialog()
        {
            Dialog form = new Dialog() { Left = 1000, Top = 300 };
            form.StartPosition = FormStartPosition.Manual;
            form.Show();
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

            foreach (String[] item in list)
            {
                ListViewItem lvi = new ListViewItem(item[0]);
                lvi.SubItems.Add(item[1]);
                lvi.SubItems.Add(item[2]);
                Table3.Items.Add(lvi);
            }
        }

        private void fillTable()
        {
            for (int i = 0; i < 3; i++)
            {
                Table.Rows.Add();
                Table.Rows[i].Cells["Head1"].Value = "tr_" + (i + 1) + "_td_1";
                Table.Rows[i].Cells["Head2"].Value = "tr_" + (i + 1) + "_td_2";
                Table.Rows[i].Cells["Head3"].Value = "tr_" + (i + 1) + "_td_3";
            }
        }

        private void fillTable1()
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

        public void CommonMouseMove(object sender, MouseEventArgs e)
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

        public void writeTextOncentralLabelMouse(string text, MouseEventArgs e)
        {
            long timeDif = DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond - mouseTimeClick;
            mouseTimeClick = DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond;
            pushLabel.Text = "";
            if (e.Button == MouseButtons.Left)
            {
                if (e.Clicks == 1)
                {
                    CentralLabel.Text = text + "_click";

                    int dx = e.X - mouseDownPos.X;
                    int dy = e.Y - mouseDownPos.Y;
                    if (Math.Abs(dx) <= SystemInformation.DoubleClickSize.Width && Math.Abs(dy) <= SystemInformation.DoubleClickSize.Height)
                    {
                        if (timeDif <= 500)
                        {
                            CentralLabel.Text = text + "_double_click";
                        }
                    }
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
            mouseDownPos = e.Location;
            Console.WriteLine(timeDif);
        }

        private void CommonKeyDown(object sender, KeyEventArgs e)
        {
            //Console.WriteLine("4");
            //string text = writeControlNameOnCentralLabel(sender);
            //if (e.KeyValue == 17)
            //{
            //    downUpLabel.Text = text + "_down_Control";
            //}
        }

        private void CommonKeyUp(object sender, KeyEventArgs e)
        {
            //Console.WriteLine("5");
            //string text = writeControlNameOnCentralLabel(sender);
            //if (e.KeyValue == 17)
            //{
            //    downUpLabel.Text = text + "_up_Control";
            //}
        }

        private void CommonKeyPress(object sender, KeyPressEventArgs e)
        {
            //Console.WriteLine("6");
            //string text = writeControlNameOnCentralLabel(sender);
            //if (e.KeyChar == (int)Keys.Escape)
            //{
            //    pressLabel.Text = text + "_press_Escape";
            //}
        }

        public void GlobalKeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyValue == 17)
            {
                downUpLabel.Text = moveLabel.Text.Split('_')[0] + "_down_Control";
            }
        }

        public void GlobalKeyUp(object sender, KeyEventArgs e)
        {
            if (e.KeyValue == 17)
            {
                downUpLabel.Text = moveLabel.Text.Split('_')[0] + "_up_Control";
            }
        }

        public void GlobalKeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (int)Keys.Escape)
            {
                pressLabel.Text = moveLabel.Text.Split('_')[0] + "_press_Escape";
            }
        }

        public string writeControlNameOnCentralLabel(object sender)
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
                checkedLabel.Text = control.Name + "_checked";
            }
            else
            {
                checkedLabel.Text = control.Name + "_unchecked";
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

        private void CheckBox_MouseUp(object sender, MouseEventArgs e)
        {

        }

        private void Slider_DragDrop(object sender, DragEventArgs e)
        {
            Console.WriteLine("drop");
        }

        private void Slider_DragOver(object sender, DragEventArgs e)
        {
            Console.WriteLine("over");
        }

        private void Table_CellContentClick(object sender, DataGridViewCellEventArgs e)
        {

        }
    }
}