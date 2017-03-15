////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

public class MockApp
{
	private JFrame frame;
	private JLabel centralLabel;
	private JLabel moveLabel;
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(MockApp::new);
	}

	public MockApp()
	{
		this.frame = new JFrame("Mock swing app");
		addListeners(frame, "Frame", false);
		LayoutManager grid = new BoxLayout(this.frame.getContentPane(), BoxLayout.PAGE_AXIS);
		JMenuBar menuBar = createMenu();
		this.frame.setJMenuBar(menuBar);
		this.frame.setLayout(grid);
		this.frame.setLocation(200, 200);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.centralLabel = new JLabel();
		addListeners(this.centralLabel, "Label");
		this.moveLabel = new JLabel();
		createPanelCentralPanel(centralLabel, moveLabel);

		createPanelButton();
		createPanelInput();
		createPanelCheckbox();
		createPanelTable();
		createPanelTable1();
		createPanelRadioGroup();
		createPanelCombobox();
		createPanelSlider();
		createPanelToggleButton();
		createPanelImage();
		createPanelTree();
		createTabPanel();
		createPanelListView();
		createPanelProgressBar();
		createPanelPanel();
		createPanelDialog();
		createPanelScrollBar();
		createPanelSplitPane();
		createPanelAny();
		createPanelWithDisableComponents();

		this.frame.setSize(new Dimension(800, 800));
		this.frame.setVisible(true);
	}

	private void createPanelWithDisableComponents() {
		JPanel panel = new JPanel();
		// disabled
		JLabel label = new JLabel("qwe");
		JComboBox<String> comboBox = new JComboBox<>(new String[]{"Green", "Yellow", "Orange", "Blue"});
		JCheckBox checkBox = new JCheckBox();
		JTextField textField = new JTextField("some text");
		label.setEnabled(false);
		comboBox.setEnabled(false);
		checkBox.setEnabled(false);
		textField.setEnabled(false);
		panel.add(label);
		panel.add(comboBox);
		panel.add(checkBox);
		panel.add(textField);

		createPanel("panelPanel").add(panel);
	}

	private JMenuBar createMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		String menuName = "Menu";
		JMenu menu = new JMenu(menuName);
		menu.addMenuListener(new MenuListener()
		{
			@Override
			public void menuSelected(MenuEvent e)
			{
				centralLabel.setText(menuName + "_click");
			}

			@Override
			public void menuDeselected(MenuEvent e)
			{

			}

			@Override
			public void menuCanceled(MenuEvent e)
			{

			}
		});
		menu.addMenuKeyListener(new MenuKeyListener()
		{
			@Override
			public void menuKeyReleased(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(menuName + "_up_F1");
				}
			}

			@Override
			public void menuKeyPressed(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(menuName + "_down_F1");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					centralLabel.setText(menuName + "_press_Escape");
				}
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e)
			{

			}
		});
		addListeners(menu, menuName);

		String menuItemName = "MenuItem";
		JMenuItem menuItem = new JMenuItem(menuItemName);
		addListeners(menuItem, menuItemName);
		menuItem.addActionListener(e -> centralLabel.setText(menuItemName + "_click"));
		menuItem.addMenuKeyListener(new MenuKeyListener()
		{
			@Override
			public void menuKeyReleased(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(menuItemName + "_up_F1");
				}
			}

			@Override
			public void menuKeyPressed(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(menuItemName + "_down_F1");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					centralLabel.setText(menuItemName + "_press_Escape");
				}
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e)
			{

			}
		});
		JMenu jMenu = new JMenu("Menu1");
		ToolTipManager.sharedInstance().setEnabled(true);
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setReshowDelay(0);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
		jMenu.setToolTipText("ToolTip");
		jMenu.add(menuItem);
		menuBar.add(menu);
		menuBar.add(jMenu);

		JMenu menu1 = new JMenu("JMenu 1");
		JMenu menuJTree = new JMenu("JTree");
		menu1.add(menuJTree);
		JMenu menuFood = new JMenu("food");
		menuJTree.add(menuFood);
		menuFood.add(new JMenu("pizza"));
		menuBar.add(menu1);
		return menuBar;
	}

	private void createPanelCentralPanel(JLabel label, JLabel moveLabel)
	{
		JPanel panel = createPanel("panelCentralLabel");
		label.setName("centralLabel");
		label.setText("Central label");
		moveLabel.setName("moveLabel");
		moveLabel.setText("Move label");
		panel.add(new JLabel());
		panel.add(label);
		panel.add(moveLabel);
	}

	private void createPanelButton()
	{
		JPanel panelButton = createPanel("panelButton");

		String name = "Button";
		JButton button = new JButton(name);
		button.addActionListener(event -> centralLabel.setText("Button_push"));
		addListeners(button, name);
		panelButton.add(button);
	}

	private void createPanelInput()
	{
		JPanel inputPanel = createPanel("panelInput");

		JTextField textField = new JTextField();
		String name = "TextBox";
		addListeners(textField, name);
		textField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				centralLabel.setText("TextBox_"+textField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
			}
		});
		inputPanel.add(textField);
		textField.setSize(100, 10);
		inputPanel.add(textField);
	}

	private void createPanelCheckbox()
	{
		JPanel panel = createPanel("panelCheckbox");

		String name = "CheckBox";
		JCheckBox checkBox = new JCheckBox(name);
		checkBox.addActionListener(event -> centralLabel.setText(checkBox.isSelected() ? "checked" : "unchecked"));
		addListeners(checkBox, name);
		checkBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				centralLabel.setText("CheckBox_checked");
			}
			else if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				centralLabel.setText("CheckBox_unchecked");
			}
		});
		panel.add(checkBox);
	}

	private void createPanelTable()
	{
		JPanel panelTable = createPanel("panelTable");
		JTable table = new JTable(new DefaultTableModel(new Object[][]
				{
						{"tr_1_td_1", "tr_1_td_2", "tr_1_td_3"},
						{"tr_2_td_1", "tr_2_td_2", "tr_2_td_3"},
						{"tr_3_td_1", "tr_3_td_2", "tr_3_td_3"}
				}, new Object[]{"Head1", "Head2", "Head3"}
		){
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return super.isCellEditable(row, column) && row == 1 && column == 1;
			}
		});
		table.setName("Table");
		JScrollPane scrollPane = new JScrollPane(table);

		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (row > -1 && column > -1)
				{
					if (e.getClickCount() == 2)
					{
						//centralLabel.setText("Table_double_click_" + row + "_" + column);
						centralLabel.setText("Table_double_click");
						table.setValueAt("tr_2_td_2",1,1);
						//table.setValueAt("123123123",1,1);
					}
				}
			}
		});


//		for (int c = 0; c < table.getColumnCount(); c++)
//		{
//			if (c != 1)
//			{
//				Class<?> col_class = table.getColumnClass(c);
//				table.setDefaultEditor(col_class, null);        // remove editor
//			}
//		}
		table.setCellEditor(new TableCellEditor()
		{
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
			{
				return null;
			}

			@Override
			public Object getCellEditorValue()
			{
				return null;
			}

			@Override
			public boolean isCellEditable(EventObject anEvent)
			{
				return false;
			}

			@Override
			public boolean shouldSelectCell(EventObject anEvent)
			{
				return false;
			}

			@Override
			public boolean stopCellEditing()
			{
				return false;
			}

			@Override
			public void cancelCellEditing()
			{

			}

			@Override
			public void addCellEditorListener(CellEditorListener l)
			{

			}

			@Override
			public void removeCellEditorListener(CellEditorListener l)
			{

			}
		});
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (row > -1 && column > -1)
				{
					if (e.getClickCount() == 2)
					{
//						centralLabel.setText("Table_double_click_" + row + "_" + column);
						centralLabel.setText("Table_double_click");
					}
					else
					{
//						centralLabel.setText("Table_click_" + row + "_" + column);
						centralLabel.setText("Table_click");
					}
				}
			}
		});
		table.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{

			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (row > -1 && column > -1)
				{
//					centralLabel.setText("Table_move_" + row + "_" + column);
					moveLabel.setText("Table_move");
				}
			}
		});
		table.addKeyListener(keyListener("Table"));
		panelTable.add(scrollPane);
	}

	private void createPanelTable1()
	{
		JPanel panelTable = createPanel("panelTable1");
		JTable table = new JTable(new DefaultTableModel(new Object[][]
				{
						{"tr-1-td-1", "tr-1-td-2", "tr-1-td-3"},
						{"tr-2-td-1", "tr-2-td-2", "tr-2-td-3"},
						{"tr-3-td-1", "tr-3-td-2", "tr-3-td-3"}
				}, new Object[]{"Head1", "Head2", "Head3"}
		));
		table.setName("Table1");
		JScrollPane scrollPane = new JScrollPane(table);
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (row > -1 && column > -1)
				{
					if (e.getClickCount() == 2)
					{
						//						centralLabel.setText("Table_double_click_" + row + "_" + column);
						centralLabel.setText("Table_double_click");
					}
					else
					{
						//						centralLabel.setText("Table_click_" + row + "_" + column);
						centralLabel.setText("Table_click");
					}
				}
			}
		});
		table.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{

			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (row > -1 && column > -1)
				{
					//					centralLabel.setText("Table_move_" + row + "_" + column);
					moveLabel.setText("Table_move");
				}
			}
		});
		table.addKeyListener(keyListener("Table1"));
		panelTable.add(scrollPane);
	}

	private void createPanelSlider()
	{
		JPanel panel = createPanel("panelSlider", 3);
		JSlider slider = new JSlider(JSlider.HORIZONTAL);
		String name = "Slider";
		slider.setValue(0);
		addListeners(slider, name);
		slider.getModel().addChangeListener(e -> centralLabel.setText("Slider_" + String.valueOf(slider.getValue())));

		panel.add(slider);
		panel.add(new JLabel());
		panel.add(new JLabel());
	}

	private void createPanelCombobox()
	{
		JPanel panel = createPanel("panelCombobox", 3);
		JComboBox<String> comboBox = new JComboBox<>(new String[]{"Green", "Yellow", "Orange", "Blue"});
		String name = "ComboBox";
		comboBox.getModel().setSelectedItem("Green");
		addListeners(comboBox, name);
		comboBox.addActionListener(event -> centralLabel.setText("ComboBox_" + comboBox.getSelectedItem().toString()));
		panel.add(comboBox);
		panel.add(new JLabel());
		panel.add(new JLabel());
	}

	private void createPanelRadioGroup()
	{
		JPanel panel = createPanel("panelRadiogroup", 2);
		ButtonGroup group = new ButtonGroup();
		JRadioButton buttonGreen = new JRadioButton("Green");
		JRadioButton buttonYellow = new JRadioButton("Yellow");
		JRadioButton buttonOrange = new JRadioButton("Orange");
		JRadioButton buttonBlue = new JRadioButton("Blue");
		String name = "RadioButton";
		addListeners(buttonGreen, name);
		buttonYellow.addItemListener(event -> centralLabel.setText(name + "1_" + (buttonYellow.isSelected() ? "checked" : "unchecked")));
		panel.add(buttonGreen);
		panel.add(buttonYellow);
		panel.add(buttonOrange);
		panel.add(buttonBlue);
		group.add(buttonGreen);
		group.add(buttonYellow);
		group.add(buttonOrange);
		group.add(buttonBlue);
	}

	private void createPanelToggleButton()
	{
		JPanel panel = createPanel("panelToggleButton");
		JToggleButton toggleButton = new JToggleButton("Press_me");
		String name = "ToggleButton";
		addListeners(toggleButton, name);
		toggleButton.addItemListener(event -> centralLabel.setText(name + "_" + (toggleButton.isSelected() ? "checked" : "unchecked")));
		panel.add(toggleButton);
	}

	private void createPanelImage()
	{

	}

	private void addListeners(Component component, String name)
	{
		addListeners(component, name, true);
	}

	private void addListeners(Component component, String name, boolean withSetName)
	{
		if (withSetName)
		{
			component.setName(name);
		}
		component.addMouseListener(mouseListener(name));
		component.addMouseMotionListener(mouseMotionListener(name));
		component.addKeyListener(keyListener(name));
	}

	private void createPanelTree()
	{
		JPanel panel = createPanel("panelTree");

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
		JTree tree = new JTree(root);
		DefaultMutableTreeNode colorsRed = new DefaultMutableTreeNode("colors, red");
		DefaultMutableTreeNode colors = new DefaultMutableTreeNode("colors");
		DefaultMutableTreeNode orange = new DefaultMutableTreeNode("Orange");
		DefaultMutableTreeNode red = new DefaultMutableTreeNode("red");
		DefaultMutableTreeNode blue = new DefaultMutableTreeNode("blue");
		DefaultMutableTreeNode green = new DefaultMutableTreeNode("green");
		root.add(colorsRed);
		root.add(colors);
		colors.add(red);
		colors.add(blue);
		colors.add(green);
		root.add(orange);
		tree.expandRow(0);

		JScrollPane scrollPane = new JScrollPane(tree);
		addListeners(tree, "Tree");
		tree.getSelectionModel().addTreeSelectionListener(listener -> System.out.println(listener.getPath()));
		panel.add(scrollPane);
	}

	private void createTabPanel()
	{
		JPanel panel = createPanel("panelTabPanel");
		JTabbedPane tabPane = new JTabbedPane();
		String name = "TabPanel";
		tabPane.addTab("Yellow", new JLabel("tab1"));
		tabPane.addTab("Orange", new JLabel("tab2"));
		addListeners(tabPane, name);
		tabPane.getModel().addChangeListener(e -> centralLabel.setText(name + "_" + tabPane.getTitleAt(tabPane.getSelectedIndex())));
		panel.add(tabPane);
	}

	private void createPanelListView()
	{
		JPanel panel = createPanel("panelListView");
		DefaultListModel<String> stringDefaultListModel = new DefaultListModel<>();
		stringDefaultListModel.addElement("Green");
		stringDefaultListModel.addElement("Yellow");
		stringDefaultListModel.addElement("Red");
		stringDefaultListModel.addElement("Blue");
		stringDefaultListModel.addElement("Orange");
		JList<String> list = new JList<>(stringDefaultListModel);
		String name = "ListView";
		addListeners(list, name);
		list.getSelectionModel().addListSelectionListener(l -> centralLabel.setText(name + "_" + list.getSelectedValue()));
		panel.add(list);
	}

	private void createPanelProgressBar()
	{
		JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);
		addListeners(bar, "ProgressBar");
		bar.setValue(10);

		createPanel("panelProgressBar").add(bar);
	}

	private void createPanelPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("qwe"));
		addListeners(panel, "Panel");
		createPanel("panelPanel").add(panel);
	}

	private void createPanelDialog()
	{
		JDialog dialog = new JDialog(this.frame, "Dialog");
		addListeners(dialog, "DialogD");
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setSize(100, 100);
		dialog.setVisible(true);
	}

	private void createPanelScrollBar()
	{
		JScrollBar bar = new JScrollBar(Adjustable.HORIZONTAL);
		String name = "ScrollBar";
		addListeners(bar, name);
		bar.getModel().addChangeListener(event -> centralLabel.setText("ScrollBar_" + String.valueOf(bar.getValue())));
		createPanel("panelScrollBar").add(bar);
	}

	private void createPanelSplitPane()
	{
		JPanel panel = createPanel("panelSplitPane");
		String name = "Splitter";
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY))
			{
				centralLabel.setText(name + "_" + splitPane.getDividerLocation());
			}
		});
		splitPane.setLeftComponent(new JLabel("LEFT COMPONENT"));
		splitPane.setRightComponent(new JLabel("RIGHT COMPONENT"));
		addListeners(splitPane, name);
		panel.add(splitPane);
	}

	private void createPanelAny()
	{
		JPanel panelButton = createPanel("panelAny");
		String name = "Any";
		JButton button = new JButton(name);
		addListeners(button, name);
		panelButton.add(button);
	}

	private JPanel createPanel(String name)
	{
		return createPanel(name, 1);
	}

	private JPanel createPanel(String name, int colCount)
	{
		JPanel panel = new JPanel(new GridLayout(1, colCount));
		panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		panel.setName(name);
		this.frame.getContentPane().add(panel);
		return panel;
	}

	private KeyListener keyListener(String name)
	{
		return new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(name + "_down_F1");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					centralLabel.setText(name + "_press_Escape");
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_F1)
				{
					centralLabel.setText(name + "_up_F1");
				}
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					centralLabel.setText(name + "_up_CONTROL");
				}
			}
		};
	}

	private MouseMotionListener mouseMotionListener(String name)
	{
		return new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				moveLabel.setText(name + "_move");
//				if (e.getX() < 10 && e.getY() < 10)
//				{
//					//					centralLabel.setText(name + "_move_" + e.getX() + "_" + e.getY());
//					centralLabel.setText(name + "_move");
//				}
//				else
//				{
//					centralLabel.setText(name + "_move");
//				}
			}
		};
	}

	private MouseListener mouseListener(String name)
	{
		return new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				if (e.getClickCount() == 2)
				{
					centralLabel.setText(name + "_double_click");
					moveLabel.setText("");
					//					if (e.getX() < 10 && e.getY() < 10)
					//					{
					//						centralLabel.setText(name + "_double_click_" + e.getX() + "_" + e.getY());
					//					}
					//					else
					//					{
					//						centralLabel.setText(name + "_double_click");
					//					}
				}
				if (e.getClickCount() == 1)
				{
					centralLabel.setText(name + "_click");
					moveLabel.setText("");
					//					if (e.getX() < 10 && e.getY() < 10)
					//					{
					//						centralLabel.setText(name + "_click" + e.getX() + "_" + e.getY());
					//					}
					//					else
					//					{
					//						centralLabel.setText(name + "_click");
					//					}
				}
			}
		};
	}
}
