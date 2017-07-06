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
using System.Windows.Automation;
using System.Windows.Forms;

namespace mock_win
{
    public partial class Main : Form
    {
        private int counter = 0;
        private int sliderValue = -99;
        private int scrollBarValue = -99;
        Timer timer;
        Point mouseDownPos;
        long mouseTimeClick;
        MenuItem menuItem;
        MenuItem menu2;
        MenuItem menuItem2;
        MenuItem menuSpace;
        MenuItem menuItem3;
        Point cursorOnMainPos;
        bool flagClickMenuItem = false;
        bool flagPopup = false;

        public Main()
        {
            InitializeComponent();
            fillTable();
            fillTable1();
            fillListView();
            fillContextMenu();
            ComboBox.SelectedIndex = 0;  
            createDialog();
            createMenu();

            this.timer = new Timer();
            this.timer.Interval = 100;
            this.timer.Tick += new EventHandler(timer_Tick);
            this.timer.Enabled = true;

            this.Spinner.MouseMove += new MouseEventHandler(CommonMouseMove);

            Tree.AfterExpand += Tree_AfterExpand;
            Tree.BeforeExpand += Tree_BeforeExpand;
            Tree.AfterCollapse += Tree_AfterCollapse;
            Tree.BeforeCollapse += Tree_BeforeCollapse;
        }

        private void Tree_BeforeCollapse(object sender, TreeViewCancelEventArgs e)
        {
            if (e.Node.Text == "colors")
            {
                selectLabel.Text = "colors_collapse";
            }
        }

        private void Tree_BeforeExpand(object sender, TreeViewCancelEventArgs e)
        {
            if (e.Node.Text == "colors")
            {
                selectLabel.Text = "colors_expand";
            }
        }

        private void Tree_AfterCollapse(object sender, TreeViewEventArgs e)
        {
            if (e.Node.Text == "colors")
            {
                selectLabel.Text = "colors_collapse";
            }
        }

        private void Tree_AfterExpand(object sender, TreeViewEventArgs e)
        {
            if (e.Node.Text == "colors")
            {
                selectLabel.Text = "colors_expand";
            }
        }

        //private void TreeViewItem_Expanded(object sender, RoutedEventArgs e)
        //{
        //    TreeViewItem tvi = e.OriginalSource as TreeViewItem;
        //    if (tvi != null)
        //    {
        //        MessageBox.Show(string.Format("TreeNode '{0}' was expanded", tvi.Header));
        //    }
        //}

        private void createMenu()
        {
            menu2 = new MenuItem("Menu2");
            menuItem = new MenuItem("MenuItem");
            menuSpace = new MenuItem("                                                                                                                                                                                                                                       ");
            menuItem2 = new MenuItem("MenuItem2");
            menuItem3 = new MenuItem("MenuItem3");

            menuItem.MenuItems.Add(menuItem2);
            menuItem2.MenuItems.Add(menuItem3);

            this.Menu = new MainMenu(new MenuItem[] { menu2, menuItem, menuSpace });

            menuSpace.Click += new EventHandler(MClick);

            menuItem.Select += new EventHandler(MSelect);
            menuItem.Popup += new EventHandler(MPopup);

            menuItem3.Select += new EventHandler(MSelect);
            menuItem2.Select += new EventHandler(MSelect);
        }

        private void MPopup(object sender, EventArgs e)
        {
            if (flagPopup)
            {
                CentralLabel.Text = ((MenuItem)sender).Text + "_double_click";
                flagPopup = false;
            }
            else
            {
                CentralLabel.Text = ((MenuItem)sender).Text + "_click";
                flagPopup = true;
            }
        }

        private void MSelect(object sender, EventArgs e)
        {
            selectLabel.Text = ((MenuItem)sender).Text+"_select";
            moveLabel.Text = ((MenuItem)sender).Text + "_move";
        }

        private void MClick(object sender, EventArgs e)
        {
            long timeDif = DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond - mouseTimeClick;
            mouseTimeClick = DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond;
            CentralLabel.Text = "Menu_click";

            int dx = Cursor.Position.X - mouseDownPos.X;
            int dy = Cursor.Position.Y - mouseDownPos.Y;
            if (Math.Abs(dx) <= SystemInformation.DoubleClickSize.Width && Math.Abs(dy) <= SystemInformation.DoubleClickSize.Height)
            {
                if (timeDif <= 500)
                {
                    CentralLabel.Text = "Menu_double_click";
                }
            }
            mouseDownPos = Cursor.Position;
            flagClickMenuItem = true;
        }

        private void timer_Tick(object sender, EventArgs e)
        {
            if (Slider.Value != sliderValue)
            {
                sliderValue = Slider.Value;
                sliderLabel.Text = "Slider_" + Slider.Value;
            }

            Point point = new Point(10, 10);
            if (listBox1.IndexFromPoint(point) != scrollBarValue)
            {
                scrollBarValue = listBox1.IndexFromPoint(point);
                sliderLabel.Text = "ScrollBar_" + listBox1.IndexFromPoint(point);
            }

            moveUponRect(new Point(7,29), new Point(51,47), "Menu");
            moveUponRect(new Point(52, 29), new Point(127, 47), "MenuItem");
            moveUponRect(new Point(128, 29), new Point(600, 47), "Menu");
        }

        private void moveUponRect(Point leftUp, Point rightDown, string text)
        {
            if (Cursor.Position.X > Location.X + leftUp.X &&
               Cursor.Position.X < Location.X + rightDown.X &&
               Cursor.Position.Y > Location.Y + leftUp.Y &&
               Cursor.Position.Y < Location.Y + rightDown.Y)
            {
                if (cursorOnMainPos != Cursor.Position)
                {
                    cursorOnMainPos = Cursor.Position;
                    moveLabel.Text = text + "_move";
                }
            }
        }

        public void createDialog()
        {
            Dialog form = new Dialog() { Left = 1000, Top = 300 };
            form.StartPosition = FormStartPosition.Manual;
            form.Show(this);
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
            String name = "";
            if (sender is Control)
            {
                Control control = (Control)sender;
                name = control.Name;
            }
            if (sender is ToolStripMenuItem)
            {
                ToolStripMenuItem menuItem = (ToolStripMenuItem)sender;
                name = menuItem.Name;
            }
            
            string text;
            switch (name)
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
                case "panel2":
                case "panel3":
                    text = "ScrollBar";
                    break;
                case "tabPage1":
                case "tabPage2":
                case "tabPage3":
                case "tabPage4":
                    text = "TabPanel";
                    break;
                default:
                    text = name;
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
            pushLabel.Text = "pushLabel";
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

        private void GlobalMouseDown(object sender, MouseEventArgs e)
        {
            if (flagClickMenuItem)
            {
                long timeDif = DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond - mouseTimeClick;

                int dx = Cursor.Position.X - mouseDownPos.X;
                int dy = Cursor.Position.Y - mouseDownPos.Y;
                if (Math.Abs(dx) <= SystemInformation.DoubleClickSize.Width && Math.Abs(dy) <= SystemInformation.DoubleClickSize.Height)
                {
                    if (timeDif <= 500)
                    {
                        CentralLabel.Text = "MenuItem_double_click";
                    }
                }
                flagClickMenuItem = false;
            }
        }

        public string writeControlNameOnCentralLabel(object sender)
        {
            String name = "";
            if (sender is Control)
            {
                Control control = (Control)sender;
                name = control.Name;
            }
            if (sender is ToolStripMenuItem)
            {
                ToolStripMenuItem menuItem = (ToolStripMenuItem)sender;
                name = menuItem.Name;
            }
            string text;
            switch (name)
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
                case "panel2":
                case "panel3":
                    text = "ScrollBar";
                    break;
                case "tabPage1":
                case "tabPage2":
                case "tabPage3":
                case "tabPage4":
                    text = "TabPanel";
                    break;
                default:
                    text = name;
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

        private void hScrollBar1_Scroll(object sender, ScrollEventArgs e)
        {
            sliderLabel.Text = ((ScrollBar)sender).Value.ToString();
        }

        private void listBox1_MouseEnter(object sender, EventArgs e)
        {
            sliderLabel.Text = "Enter";
        }

        private void panel2_MouseEnter(object sender, EventArgs e)
        {
            moveLabel.Text = "ScrollBar_move";
        }

        private void listBox1_RegionChanged(object sender, EventArgs e)
        {

        }

        private void Main_RightToLeftChanged(object sender, EventArgs e)
        {
        }

        private void Menu2_DropDownOpening(object sender, EventArgs e)
        {
            Console.WriteLine("Menu2_DropDownOpening");
        }

        private void Menu2_DropDownOpened(object sender, EventArgs e)
        {
            Console.WriteLine("Menu2_DropDownOpened");
        }

        private void Menu2_DropDownItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            Console.WriteLine("Menu2_DropDownItemClicked"); 
        }

        private void Menu2_DropDownClosed(object sender, EventArgs e)
        {
            Console.WriteLine("Menu2_DropDownClosed");
        }

        private void Menu2_DoubleClick(object sender, EventArgs e)
        {
            Console.WriteLine("Menu2_DoubleClick");
        }

        private void Spinner_ValueChanged(object sender, EventArgs e)
        {
        }
    }
}