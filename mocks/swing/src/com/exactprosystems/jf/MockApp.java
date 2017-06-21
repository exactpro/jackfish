////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;

public class MockApp
{
	private JFrame frame;
	private JFrame frame2;
	private JLabel centralLabel;
	private JLabel pressLabel;
	private JLabel pushLabel;
	private JLabel checkedLabel;
	private JLabel downUpLabel;
	private JLabel moveLabel;
	private JLabel sliderLabel;
	private JTextField textField;
	private JFrame frame3;
	private boolean firstRun;
	private int counter;

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
		this.frame.setLocation(200 , 33);
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.centralLabel = new JLabel();
		this.pushLabel = new JLabel();
		this.checkedLabel = new JLabel();
		this.pressLabel = new JLabel();
		addListeners(this.centralLabel, "Label");
		this.moveLabel = new JLabel();
		this.downUpLabel = new JLabel();
		this.sliderLabel = new JLabel();
		createPanelCentralPanel(centralLabel, moveLabel, pressLabel, downUpLabel, sliderLabel, pushLabel, checkedLabel);

		createPanelButton();
		createPanelInput();
		createPanelCheckbox();
		createPanelTable();
		createPanelTable1();
		createPanelRadioGroup();
		createPanelCombobox();
		createPanelSlider();
		createPanelToggleButton();
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
		createPanelWithHiddenArea();
		createPanelImage();
		createPanelRepeat();
		createPanelVisibleEnabled();
		createPanelColorAndRect();

		this.frame.setSize(new Dimension(800, 800));
		this.frame.setVisible(true);

		// frame two
//		this.frame2 = new JFrame("Mock swing app additional frame");
//		this.frame2.setLocation(1000, 200);
//		this.frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		this.frame2.setSize(new Dimension(1100,700));
//		this.frame2.setVisible(true);
//		allEvents(createPanelFrame2());
		//createAndShowGui();
	}

	private void createPanelColorAndRect() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setName("panelVisibleEnabled");
		JButton colorButton = new JButton("#red");
		colorButton.setPreferredSize(new Dimension(100, 25));
		colorButton.setBackground(Color.RED);
		colorButton.setForeground(Color.WHITE);
		colorButton.setName("colorButton");
		panel.add(colorButton);
		this.frame.getContentPane().add(panel);
	}

	private void createPanelVisibleEnabled() {
		JPanel panel = createPanel("panelVisibleEnabled");
		JButton notEnabledButton = new JButton("notEnabledButton");
		notEnabledButton.setName("notEnabledButton");
		notEnabledButton.setEnabled(false);
		JButton invisibleButton = new JButton("invisibleButton");
		invisibleButton.setName("invisibleButton");
		invisibleButton.setVisible(false);
		panel.add(notEnabledButton);
		panel.add(invisibleButton);
	}

	private void createPanelRepeat()
	{
		counter = 0;
		JPanel panel = createPanel("panelRepeat");
		JButton countButtonClear = new JButton("Clear");
		countButtonClear.setName("countButtonClear");
		JButton countButton = new JButton("+1");
		countButton.setName("countButton");
		JLabel countLabel = new JLabel("Count");
		countLabel.setName("countLabel");

		countButtonClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				counter = 0;
				countLabel.setText(String.valueOf(counter));
			}
		});
		countButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				counter++;
				countLabel.setText(String.valueOf(counter));
			}
		});

		panel.add(countButtonClear);
		panel.add(countButton);
		panel.add(countLabel);
	}

	private void createAndShowGui() {
			//Create and set up the window.
			frame3 = new JFrame("TopLevelDemo");
			frame3.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			//Create the menu bar.  Make it have a green background.
			JMenuBar greenMenuBar = new JMenuBar();
			greenMenuBar.setOpaque(true);
			greenMenuBar.setBackground(new Color(154, 165, 127));
			greenMenuBar.setPreferredSize(new Dimension(200, 20));

			//Create a yellow label to put in the content pane.
			JLabel yellowLabel = new JLabel();
			yellowLabel.setOpaque(true);
			yellowLabel.setBackground(new Color(248, 213, 131));
			yellowLabel.setPreferredSize(new Dimension(200, 180));

			//Set the menu bar and add the label to the content pane.
			frame3.setJMenuBar(greenMenuBar);
			frame3.getContentPane().add(yellowLabel, BorderLayout.CENTER);

			//Display the window.
			frame3.pack();
			frame3.setVisible(true);
	}

	private void allEvents(JTextArea textArea) {
		Toolkit.getDefaultToolkit().addAWTEventListener(
				event -> {
//					if(event instanceof TimedWindowEvent)
//					{
//						int i = 0;
//						i++;
//					}
					if(event instanceof FocusEvent)
					{
						int i = 0;
						i++;
					}
//					textArea.append("\n"+event.toString());
//					textArea.moveCaretPosition(0);
				},
				AWTEvent.COMPONENT_EVENT_MASK |
				AWTEvent.CONTAINER_EVENT_MASK |
				AWTEvent.FOCUS_EVENT_MASK |
						AWTEvent.KEY_EVENT_MASK |
						//AWTEvent.MOUSE_EVENT_MASK |
						//AWTEvent.MOUSE_MOTION_EVENT_MASK |
						AWTEvent.WINDOW_EVENT_MASK |
						AWTEvent.ACTION_EVENT_MASK |
						AWTEvent.ADJUSTMENT_EVENT_MASK |
						AWTEvent.ITEM_EVENT_MASK |
						AWTEvent.TEXT_EVENT_MASK |
						AWTEvent.INPUT_METHOD_EVENT_MASK |
						AWTEvent.PAINT_EVENT_MASK |
						AWTEvent.INVOCATION_EVENT_MASK |
						AWTEvent.HIERARCHY_EVENT_MASK |
						AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK |
						AWTEvent.MOUSE_WHEEL_EVENT_MASK |
						AWTEvent.WINDOW_STATE_EVENT_MASK |
						AWTEvent.WINDOW_FOCUS_EVENT_MASK
		);
	}

	private void createPanelWithDisableComponents() {
		JPanel panel = new JPanel();
		// disabled
		JLabel label = new JLabel("Panel");
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

	private void createPanelWithHiddenArea()
	{
		JPanel panel = new JPanel();
		JPanel hiddenPanel = new JPanel();

		JButton buttonShowArea = new JButton("Show area");
		buttonShowArea.setName("showButton");
		JButton buttonHideArea = new JButton("Hide area");
		buttonHideArea.setName("hideButton");

		hiddenPanel.add(buttonHideArea);
		panel.add(buttonShowArea);
		panel.add(hiddenPanel);

		hiddenPanel.setVisible(false);

		buttonShowArea.addActionListener(event -> hiddenPanel.setVisible(true));
		buttonHideArea.addActionListener(event -> hiddenPanel.setVisible(false));
		createPanel("panelWithHiddenArea").add(panel);
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
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(menuName + "_up_Control");
				}
			}

			@Override
			public void menuKeyPressed(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(menuName + "_down_Control");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					pressLabel.setText(menuName + "_press_Escape");
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
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(menuItemName + "_up_Control");
				}
			}

			@Override
			public void menuKeyPressed(MenuKeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(menuItemName + "_down_Control");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					pressLabel.setText(menuItemName + "_press_Escape");
				}
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e)
			{

			}
		});
		JMenu jMenu = new JMenu("Menu2");
		jMenu.setName("menu2");
		ToolTipManager.sharedInstance().setEnabled(true);
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setReshowDelay(0);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
		jMenu.setToolTipText("Tooltip");
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

	private void createPanelCentralPanel(JLabel centralLabel, JLabel moveLabel, JLabel pressLabel, JLabel downUpLabel,
                                         JLabel sliderLabel, JLabel pushLabel, JLabel checkedLabel)
	{
		JPanel panel = createPanel("panelCentralLabel");
		centralLabel.setName("CentralLabel");
		centralLabel.setText("CentralLabel");
		moveLabel.setName("moveLabel");
		moveLabel.setText("Movelabel");
		pressLabel.setName("pressLabel");
		pressLabel.setText("Presslabel");
		downUpLabel.setName("downUpLabel");
		downUpLabel.setText("DownUplabel");
		sliderLabel.setName("sliderLabel");
		sliderLabel.setText("sliderlabel");
		pushLabel.setName("pushLabel");
		pushLabel.setText("pushLabel");
		checkedLabel.setName("checkedLabel");
		checkedLabel.setText("checkedLabel");
		panel.add(new JLabel());
		panel.add(centralLabel);
		panel.add(moveLabel);
		panel.add(pressLabel);
		panel.add(downUpLabel);
		panel.add(sliderLabel);
		panel.add(pushLabel);
		panel.add(checkedLabel);
	}

	private void createPanelButton()
	{
		JPanel panelButton = createPanel("panelButton");

		String name = "Button";
		JButton button = new JButton(name);
		button.addActionListener(event -> centralLabel.setText("Button_click"));
		button.addMouseListener(mouseListener(name));
		addListeners(button, name);
		panelButton.add(button);
	}

	private JTextArea createPanelFrame2()
	{
		JPanel panel = new JPanel(new FlowLayout());
		this.frame2.getContentPane().add(panel);
		JButton button = new JButton("Open modal dialog");
		JButton buttonClear = new JButton("Clear list");
		button.setName("button");
		JButton buttonDispatch = new JButton("dispatch");
		buttonDispatch.setName("dispatch");
		buttonDispatch.addActionListener(e -> {

		});

		JTextArea textArea = new JTextArea();
		JTextField textField = new JTextField();
		textField.setName("text");
		textField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 98)
				{
					//frame2.dispatchEvent(new FocusEvent(buttonDispatch, FocusEvent.FOCUS_LOST));
					//frame2.dispatchEvent(new sun.awt.TimedWindowEvent(frame2, WINDOW_LOST_FOCUS, null, 0));
					//frame2.dispatchEvent(new WindowEvent(frame2, WindowEvent.WINDOW_DEACTIVATED, 0, 0));
					frame3.dispatchEvent(new WindowEvent(frame3, WindowEvent.WINDOW_ACTIVATED, 0, 0));
//					frame3.dispatchEvent(new sun.awt.TimedWindowEvent(frame3, WINDOW_GAINED_FOCUS, null, 0));
					//frame.dispatchEvent(new FocusEvent(frame, FocusEvent.FOCUS_GAINED));
					//frame.dispatchEvent(new PaintEvent(frame, PaintEvent.PAINT, frame.getBounds()));
					//frame2.dispatchEvent(new PaintEvent(frame2, PaintEvent.PAINT, frame2.getBounds()));

					//frame.getFocusCycleRootAncestor();
					JLayeredPane pane = frame3.getLayeredPane();
					//Point point = org.fest.swing.awt.AWT.visibleCenterOf(pane);
//					frame3.dispatchEvent(new FocusEvent(pane, FocusEvent.FOCUS_GAINED));
//					frame3.dispatchEvent(new MouseEvent(pane, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, point.x, point.y, 0, false, MouseEvent.NOBUTTON));
//					frame3.dispatchEvent(new MouseEvent(pane, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, point.x, point.y, 0, false, MouseEvent.NOBUTTON));
//					frame3.dispatchEvent(new MouseEvent(pane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
//					frame3.dispatchEvent(new MouseEvent(pane, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
//					frame3.dispatchEvent(new MouseEvent(pane, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
				}
				if(e.getKeyCode() == 97)
				{
					textArea.setText(null);
				}
			}

			public void keyReleased(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {

			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(1000,500));
		buttonClear.addActionListener(e -> textArea.setText(null));
		textField.setPreferredSize( new Dimension( 200, 24 ));
		button.addActionListener(event -> {
			JDialog dialog = new JDialog(frame2, "ModalDialog");
			JButton button1 = new JButton("Close Dialog");
			button1.addActionListener(e -> dialog.dispose());
			dialog.setModal(true);
			dialog.setLayout(new FlowLayout());
			dialog.add(button1);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setSize(150, 100);
			dialog.setLocation(1350, 200);
			dialog.setVisible(true);
		});
		panel.add(button);
		panel.add(buttonClear);
		panel.add(textField);
		panel.add(scrollPane);
		panel.add(buttonDispatch);
		return textArea;
	}

	private void createPanelInput()
	{
		JPanel inputPanel = createPanel("panelInput");

		textField = new JTextField();
		textField.setOpaque(true);
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
		JCheckBox checkBox = new JCheckBox(name,true);
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
		JPanel panel = createPanel("panelSlider");
		JSlider slider = new JSlider(JSlider.HORIZONTAL);
		String name = "Slider";
		slider.setValue(0);
		addListeners(slider, name);
		slider.getModel().addChangeListener(e -> sliderLabel.setText("Slider_" + String.valueOf(slider.getValue())));

		panel.add(slider);
	}

	private void createPanelCombobox()
	{
		JPanel panel = createPanel("panelCombobox", 3);
		JComboBox<String> comboBox = new JComboBox<>(new String[]{"Green", "Yellow", "Orange", "Blue"});
		comboBox.setEditable(true);
		String name = "ComboBox";
		comboBox.getModel().setSelectedItem("Green");
		addListeners(comboBox, name);
		comboBox.addActionListener(event -> centralLabel.setText("ComboBox_" + comboBox.getSelectedItem().toString()));
		panel.add(comboBox);
		panel.add(new JLabel());

		JComboBox<String> editableComboBox = new JComboBox<>(new String[]{"", "One", "Two", "Three"});
		editableComboBox.setEditable(true);
		JTextComponent editorComponent = (JTextComponent) editableComboBox.getEditor().getEditorComponent();
		String editableName = "EditableComboBox";
		editorComponent.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				centralLabel.setText(editableName + "_" + editorComponent.getText());
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

		editableComboBox.setName(editableName);
		panel.add(editableComboBox);
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
		JToggleButton toggleButton = new JToggleButton("ToggleButton", true);
		String name = "ToggleButton";
		addListeners(toggleButton, name);
		toggleButton.addItemListener(event -> centralLabel.setText(name + "_" + (toggleButton.isSelected() ? "checked" : "unchecked")));
		panel.add(toggleButton);
	}

	private void createPanelImage()
	{
		String pathname = new File("").getAbsolutePath() + "/../mocks/swing/ide.png";
		JPanel panel = createPanel("panelImage");
		panel.add(new JLabel(pathname));
		try {
			BufferedImage img = ImageIO.read(new File(pathname));
			ImageIcon icon = new ImageIcon(img);
			JLabel label = new JLabel(icon);
			addListeners(label, "Image");
			panel.add(label);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		root.add(orange);
		root.add(colors);
		colors.add(red);
		colors.add(blue);
		colors.add(green);
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
		stringDefaultListModel.addElement("Orange");
		stringDefaultListModel.addElement("Blue");

		JList<String> list = new JList<>(stringDefaultListModel);
		String name = "List";
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
		panel.add(new JLabel("Panel"));
		addListeners(panel, "Panel");
		createPanel("panelPanel").add(panel);
	}

	private void createPanelDialog()
	{
		JDialog dialog = new JDialog(frame, "Dialog");
		dialog.setLocation(1000,600);
		JButton button = new JButton("ButtonD");
		addListeners(dialog, "Dialog");
		addListeners(button, "ButtonD");
		dialog.setLayout(new FlowLayout());
		dialog.add(button);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setSize(100, 200);
		dialog.setVisible(true);
	}

	private void createPanelScrollBar()
	{
		JScrollBar bar = new JScrollBar(Adjustable.HORIZONTAL);
		String name = "ScrollBar";
		addListeners(bar, name);
		bar.getModel().addChangeListener(event -> sliderLabel.setText("ScrollBar_" + String.valueOf(bar.getValue())));
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
				if(firstRun)
				{
					sliderLabel.setText(name + "_" + splitPane.getDividerLocation());
				}
				firstRun = true;
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
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(name + "_down_Control");
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					pressLabel.setText(name + "_press_Escape");
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					downUpLabel.setText(name + "_up_Control");
				}
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				{
					centralLabel.setText(name + "_up_Control");
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
			private void createPopupMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem item1 = new JMenuItem(new AbstractAction("one") {
					@Override
					public void actionPerformed(ActionEvent e) {
						centralLabel.setText("cm_one_click");
					}
				});
				JMenuItem item2 = new JMenuItem("two");
				JMenuItem item3 = new JMenuItem("three");
				menu.add(item1);
				menu.add(item2);
				menu.add(item3);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger())
				{
					createPopupMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger())
				{
					createPopupMenu(e);
				}
			}

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
					pushLabel.setText(name + "_push");
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
