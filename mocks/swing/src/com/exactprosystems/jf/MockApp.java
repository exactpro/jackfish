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
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
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
	private JLabel selectLabel;
	private JLabel checkedLabel;
	private JLabel downUpLabel;
	private JLabel moveLabel;
	private JLabel sliderLabel;
	private JFrame frame3;
	private boolean firstRun;
	private int counter;
	private static final String NEWLINE = "\r\n";

	private JButton button;
	private JTextField textField;
	private JCheckBox checkBox;
	private JButton any;
	private JToggleButton toggleButton;
	private JComboBox<String> comboBox;
	private JSpinner spinner;
	private JTable table;
	private JPanel tablePanel;
	private JRadioButton buttonGreen;
	private JRadioButton buttonYellow;
	private JRadioButton buttonOrange;
	private JRadioButton buttonBlue;
	private JPanel panel;
	private JProgressBar progressBar;
	private JScrollBar scrollBar;
	private JList<String> list;
	private JTabbedPane tabPane;
	private JScrollPane treePanel;
	private JSplitPane splitPane;
	private JLabel imageLabel;
	private JSlider slider;
	private JPanel showHidePanel;
	private JTextArea protocolText;
	private JButton protocolClear;
	private JPanel disabledComponentsPanel;
	private JPanel panelRepeat;
	private JPanel panelVisibleEnabled;
	private JButton colorButton;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(MockApp::new);
	}

	private void setSizeForAllControls()
	{
		// visibility

		Dimension sizeS = new Dimension(25, 25);
		Dimension sizeM = new Dimension(100, 25);
		Dimension sizeL = new Dimension(250, 75);

		// other
		this.slider.setBounds		(0, 							300, 				this.frame.getWidth()-15, sizeM.height);

		this.pressLabel.setBounds	(0,							sizeM.height*7,	sizeL.width, sizeM.height);
		this.pushLabel.setBounds	(0,							sizeM.height*8,	sizeL.width, sizeM.height);
		this.selectLabel.setBounds	(0,							sizeM.height*9,	sizeL.width, sizeM.height);
		this.checkedLabel.setBounds	(sizeL.width,					sizeM.height*7,	sizeL.width, sizeM.height);
		this.downUpLabel.setBounds	(sizeL.width,					sizeM.height*8,	sizeL.width, sizeM.height);
		this.moveLabel.setBounds	(sizeL.width,					sizeM.height*9,	sizeL.width, sizeM.height);
		this.sliderLabel.setBounds	(sizeL.width,					sizeM.height*10,	sizeL.width, sizeM.height);

		this.showHidePanel.setBounds(0,							sizeM.height*16,	sizeL.width, sizeM.height*2);
		this.disabledComponentsPanel.setBounds(0,					sizeM.height*18,	sizeL.width, sizeM.height*2);
		this.panelRepeat.setBounds	(0,							sizeM.height*20,	sizeL.width, sizeM.height);
		this.panelVisibleEnabled.setBounds	(0,					sizeM.height*21,	sizeL.width, sizeM.height);

		this.colorButton.setBounds	(342,							715,				sizeM.width, sizeM.height);

		// first column
		this.centralLabel.setBounds	(0, 							0, 				sizeM.width*2 + sizeS.width, sizeM.height);
		this.imageLabel.setBounds	(sizeM.width*2 + sizeS.width,	0,				sizeS.width, sizeS.height);
		this.tablePanel.setBounds	(0,							sizeM.height, 		sizeL.width, sizeL.height);
		this.tabPane.setBounds		(0,							sizeM.height*4,	sizeL.width, sizeM.height*2);
		this.splitPane.setBounds	(0,							sizeM.height*6,	sizeL.width, sizeM.height);

		// second column
		this.treePanel.setBounds	(sizeL.width,					0,				sizeM.width, sizeL.height*2);

		// third column
		this.button.setBounds		(sizeL.width + sizeM.width, 	0, 				sizeM.width, sizeM.height);
		this.any.setBounds			(sizeL.width + sizeM.width,	sizeM.height, 		sizeM.width, sizeM.height);
		this.toggleButton.setBounds	(sizeL.width + sizeM.width,	sizeM.height*2, 	sizeM.width, sizeM.height);
		this.textField.setBounds	(sizeL.width + sizeM.width,	sizeM.height*3, 	sizeM.width, sizeM.height);
		this.panel.setBounds		(sizeL.width + sizeM.width,	sizeM.height*4, 	sizeM.width, sizeM.height);
		this.progressBar.setBounds	(sizeL.width + sizeM.width,	sizeM.height*5,	sizeM.width, sizeM.height);
		this.scrollBar.setBounds	(sizeL.width + sizeM.width,	sizeM.height*6,	sizeM.width, sizeM.height);
		this.spinner.setBounds		(sizeL.width + sizeM.width,	sizeM.height*7,	sizeM.width, sizeM.height);
		this.comboBox.setBounds		(sizeL.width + sizeM.width,	sizeM.height*8, 	sizeM.width, sizeM.height);

		// four column
		this.buttonGreen.setBounds	(sizeL.width + sizeM.width*2,	0, 				sizeM.width, sizeM.height);
		this.buttonYellow.setBounds	(sizeL.width + sizeM.width*2,	sizeM.height, 		sizeM.width, sizeM.height);
		this.buttonOrange.setBounds	(sizeL.width + sizeM.width*2,	sizeM.height*2, 	sizeM.width, sizeM.height);
		this.buttonBlue.setBounds	(sizeL.width + sizeM.width*2,	sizeM.height*3, 	sizeM.width, sizeM.height);
		this.checkBox.setBounds		(sizeL.width + sizeM.width*2,	sizeM.height*4, 	sizeM.width, sizeM.height);
		this.list.setBounds			(sizeL.width + sizeM.width*2,	sizeM.height*5,	sizeM.width, sizeL.height);

		// five column
		this.protocolText.setBounds	(sizeL.width + sizeM.width*3,	0,				sizeL.width, sizeL.height*3);
		this.protocolClear.setBounds(sizeL.width + sizeM.width*4 + sizeS.width,	sizeL.height*3,	sizeM.width, sizeM.height);
	}

	public MockApp()
	{
		this.frame = new JFrame("Mock swing app");
		addListeners(frame, "Frame", false);
		//LayoutManager grid = new BoxLayout(this.frame.getContentPane(), BoxLayout.PAGE_AXIS);
		JMenuBar menuBar = createMenu();
		this.frame.setJMenuBar(menuBar);
		this.frame.setLayout(null);
//		JPanel mainPanel = new JPanel();
//		mainPanel.setLayout(null);
//		this.frame.add(mainPanel);
		this.frame.setLocation(200 , 32);
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		createPanelCentralPanel();

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
		createPanelSpinner();
		createProtocol();

		this.frame.setSize(new Dimension(800, 800));
		this.frame.setVisible(true);
		setSizeForAllControls();

		// frame two
//		this.frame2 = new JFrame("Mock swing app additional frame");
//		this.frame2.setLocation(1000, 200);
//		this.frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		this.frame2.setSize(new Dimension(1100,700));
//		this.frame2.setVisible(true);
//		allEvents(createPanelFrame2());
		//createAndShowGui();
	}

	private void createProtocol() {
		this.protocolText = new JTextArea();
		this.protocolClear = new JButton("Clear");
		this.protocolText.setName("protocolText");
		this.protocolClear.setName("protocolClear");

		this.protocolClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				protocolText.setText("");
			}
		});

		this.frame.add(this.protocolText);
		this.frame.add(this.protocolClear);
	}

	private void createPanelSpinner() {
		this.spinner = new JSpinner();
		this.spinner.setName("Spinner");
		this.spinner.getModel().addChangeListener(e -> sliderLabel.setText("Slider_" + String.valueOf(Integer.valueOf((Integer) this.spinner.getValue()))));
		addListeners(this.spinner, this.spinner.getName());
		this.frame.add(this.spinner);
	}

	private void createPanelColorAndRect() {
		this.colorButton = new JButton("#red");
		this.colorButton.setPreferredSize(new Dimension(100, 25));
		this.colorButton.setBackground(Color.RED);
		this.colorButton.setForeground(Color.WHITE);
		this.colorButton.setName("colorButton");
		this.frame.add(this.colorButton);
	}

	private void createPanelVisibleEnabled() {
		this.panelVisibleEnabled = createPanel("panelVisibleEnabled");
		JButton notEnabledButton = new JButton("notEnabledButton");
		notEnabledButton.setName("notEnabledButton");
		notEnabledButton.setEnabled(false);
		JButton invisibleButton = new JButton("invisibleButton");
		invisibleButton.setName("invisibleButton");
		invisibleButton.setVisible(false);
		this.panelVisibleEnabled.add(notEnabledButton);
		this.panelVisibleEnabled.add(invisibleButton);
	}

	private void createPanelRepeat()
	{
		counter = 0;
		this.panelRepeat = createPanel("panelRepeat");
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

		this.panelRepeat.add(countButtonClear);
		this.panelRepeat.add(countButton);
		this.panelRepeat.add(countLabel);
		this.frame.add(this.panelRepeat);
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
		this.disabledComponentsPanel = new JPanel();
		// disabled
		JLabel label = new JLabel("Panel");
		JComboBox<String> comboBox = new JComboBox<>(new String[]{"Green", "Yellow", "Orange", "Blue"});
		JCheckBox checkBox = new JCheckBox();
		JTextField textField = new JTextField("some text");
		label.setEnabled(false);
		comboBox.setEnabled(false);
		checkBox.setEnabled(false);
		textField.setEnabled(false);
		this.disabledComponentsPanel.add(label);
		this.disabledComponentsPanel.add(comboBox);
		this.disabledComponentsPanel.add(checkBox);
		this.disabledComponentsPanel.add(textField);

		this.frame.add(this.disabledComponentsPanel);
	}

	private void createPanelWithHiddenArea()
	{
		this.showHidePanel = new JPanel();
		this.showHidePanel.setName("panelWithHiddenArea");
		JPanel hiddenPanel = new JPanel();

		JButton buttonShowArea = new JButton("Show area");
		buttonShowArea.setName("showButton");
		JButton buttonHideArea = new JButton("Hide area");
		buttonHideArea.setName("hideButton");

		hiddenPanel.add(buttonHideArea);
		this.showHidePanel.add(buttonShowArea);
		this.showHidePanel.add(hiddenPanel);

		hiddenPanel.setVisible(false);

		buttonShowArea.addActionListener(event -> hiddenPanel.setVisible(true));
		buttonHideArea.addActionListener(event -> hiddenPanel.setVisible(false));
		this.frame.add(this.showHidePanel);
	}

	private JMenuBar createMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		String menuName = "Menu";
		String menuItemName1 = "MenuItem";
		String menuItemName2 = "MenuItem2";
		String menuItemName3 = "MenuItem3";
		JMenu menu = new JMenu(menuName);
		JMenuItem menuItem1 = new JMenu(menuItemName1);
		JMenuItem menuItem2 = new JMenu(menuItemName2);
		JMenuItem menuItem3 = new JMenu(menuItemName3);
		menu.add(menuItem1);
		menuItem1.add(menuItem2);
		menuItem2.add(menuItem3);
		menuItem3.addPropertyChangeListener(evt -> {
            if(evt.getNewValue() != null)
            {
                selectLabel.setText(menuItemName3 + "_select");
            }
            else
            {
                selectLabel.setText(menuItemName2 + "_select");
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
				protocolText.append(menuName + "_up_" + e.getKeyCode() + NEWLINE);
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
				protocolText.append(menuName + "_down_" + e.getKeyCode() + NEWLINE);
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e)
			{
				protocolText.append(menuName + "_press_" + (int)e.getKeyChar() + NEWLINE);
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
				protocolText.append(menuName + "_up_" + e.getKeyCode() + NEWLINE);
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
				protocolText.append(menuName + "_down_" + e.getKeyCode() + NEWLINE);
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e)
			{
				protocolText.append(menuName + "_press_" + (int)e.getKeyChar() + NEWLINE);
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

	private void createPanelCentralPanel()
	{
		this.centralLabel = new JLabel();
		this.pushLabel = new JLabel();
		this.selectLabel = new JLabel();
		this.checkedLabel = new JLabel();
		this.pressLabel = new JLabel();
		this.moveLabel = new JLabel();
		this.downUpLabel = new JLabel();
		this.sliderLabel = new JLabel();
		addListeners(this.centralLabel, "Label");

		this.centralLabel.setName("CentralLabel");
		this.centralLabel.setText("CentralLabel");
		this.moveLabel.setName("moveLabel");
		this.moveLabel.setText("Movelabel");
		this.pressLabel.setName("pressLabel");
		this.pressLabel.setText("Presslabel");
		this.downUpLabel.setName("downUpLabel");
		this.downUpLabel.setText("DownUplabel");
		this.sliderLabel.setName("sliderLabel");
		this.sliderLabel.setText("sliderlabel");
		this.pushLabel.setName("pushLabel");
		this.pushLabel.setText("pushLabel");
		this.selectLabel.setName("selectLabel");
		this.selectLabel.setText("selectLabel");
		this.checkedLabel.setName("checkedLabel");
		this.checkedLabel.setText("checkedLabel");
		this.frame.add(new JLabel());
		this.frame.add(this.centralLabel);
		this.frame.add(this.moveLabel);
		this.frame.add(this.pressLabel);
		this.frame.add(this.downUpLabel);
		this.frame.add(this.sliderLabel);
		this.frame.add(this.pushLabel);
		this.frame.add(this.selectLabel);
		this.frame.add(this.checkedLabel);
	}

	private void createPanelButton()
	{
		String name = "Button";
		this.button = new JButton(name);
		this.button.setToolTipText("Button");
		this.button.addActionListener(event -> centralLabel.setText("Button_click"));
		this.button.addMouseListener(mouseListener(name));
		addListeners(this.button, name);
		this.frame.add(this.button);
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
		this.frame.add(textField);
	}

	private void createPanelCheckbox()
	{
		String name = "CheckBox";
		this.checkBox = new JCheckBox(name,true);
		this.checkBox.addActionListener(event -> centralLabel.setText(checkBox.isSelected() ? "checked" : "unchecked"));
		addListeners(this.checkBox, name);
		this.checkBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				centralLabel.setText("CheckBox_checked");
			}
			else if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				centralLabel.setText("CheckBox_unchecked");
			}
		});
		this.frame.add(this.checkBox);
	}

	private void createPanelTable()
	{
		this.table = new JTable(new DefaultTableModel(new Object[][]
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
		this.table.setName("Table");
		this.table.addMouseListener(new MouseAdapter()
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
		this.table.setCellEditor(new TableCellEditor()
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
		this.table.addMouseListener(new MouseAdapter()
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
						centralLabel.setText("Table_double_click");
					}
					else
					{
						centralLabel.setText("Table_click");
					}
				}
			}
		});
		this.table.addMouseMotionListener(new MouseMotionListener()
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
					moveLabel.setText("Table_move");
				}
			}
		});
		this.table.addKeyListener(keyListener("Table"));

		this.tablePanel = new JPanel();
		this.tablePanel.setLayout(new BorderLayout());
		this.tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
		this.tablePanel.add(table, BorderLayout.CENTER);
		this.tablePanel.add(this.table);
		this.frame.add(tablePanel);
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
		this.slider = new JSlider(JSlider.HORIZONTAL);
		String name = "Slider";
		this.slider.setValue(0);
		addListeners(this.slider, name);
		this.slider.getModel().addChangeListener(e -> sliderLabel.setText("Slider_" + String.valueOf(this.slider.getValue())));
		this.frame.add(this.slider);
	}

	private void createPanelCombobox()
	{
		this.comboBox = new JComboBox<>(new String[]{"Green", "Yellow", "Orange", "Blue"});
		this.comboBox.setEditable(true);
		String name = "ComboBox";
		this.comboBox.getModel().setSelectedItem("Green");
		addListeners(this.comboBox, name);
		this.comboBox.addActionListener(event -> centralLabel.setText("ComboBox_" + this.comboBox.getSelectedItem().toString()));
		this.frame.add(this.comboBox);

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
		this.frame.add(editableComboBox);
	}

	private void createPanelRadioGroup()
	{
		ButtonGroup group = new ButtonGroup();
		buttonGreen = new JRadioButton("Green");
		buttonYellow = new JRadioButton("Yellow");
		buttonOrange = new JRadioButton("Orange");
		buttonBlue = new JRadioButton("Blue");
		String name = "RadioButton";
		addListeners(buttonGreen, name);
		buttonYellow.addItemListener(event -> centralLabel.setText(name + "1_" + (buttonYellow.isSelected() ? "checked" : "unchecked")));
		group.add(buttonGreen);
		group.add(buttonYellow);
		group.add(buttonOrange);
		group.add(buttonBlue);
		this.frame.add(buttonGreen);
		this.frame.add(buttonYellow);
		this.frame.add(buttonOrange);
		this.frame.add(buttonBlue);
	}

	private void createPanelToggleButton()
	{
		this.toggleButton = new JToggleButton("ToggleButton", true);
		String name = "ToggleButton";
		addListeners(this.toggleButton, name);
		this.toggleButton.addItemListener(event -> centralLabel.setText(name + "_" + (this.toggleButton.isSelected() ? "checked" : "unchecked")));
		this.frame.add(this.toggleButton);
	}

	private void createPanelImage()
	{
		String pathname = new File("").getAbsolutePath() + "/../mocks/swing/ide.png";
		//for debug String pathname = new File("").getAbsolutePath() + "\\ide.png";
		try {
			BufferedImage img = ImageIO.read(new File(pathname));
			ImageIcon icon = new ImageIcon(img);
			this.imageLabel = new JLabel(icon);
		} catch (IOException e) {
			this.imageLabel = new JLabel();
			imageLabel.setOpaque(true);
			imageLabel.setBackground(Color.PINK);
		}
		addListeners(this.imageLabel, "Image");
		this.imageLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON2) {
					textField.setText(pathname);
				}
			}
		});
		this.frame.add(this.imageLabel);
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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Green");
		JTree tree = new JTree(root);
		DefaultMutableTreeNode yellow = new DefaultMutableTreeNode("Yellow");
		DefaultMutableTreeNode orange = new DefaultMutableTreeNode("Orange");
		DefaultMutableTreeNode blue = new DefaultMutableTreeNode("Blue");
		DefaultMutableTreeNode blue2 = new DefaultMutableTreeNode("Blue");
		DefaultMutableTreeNode blue3 = new DefaultMutableTreeNode("Blue");
		DefaultMutableTreeNode blue4 = new DefaultMutableTreeNode("Blue");
		DefaultMutableTreeNode colors = new DefaultMutableTreeNode("colors");
		DefaultMutableTreeNode colors2 = new DefaultMutableTreeNode("colors2");
		DefaultMutableTreeNode red = new DefaultMutableTreeNode("red");
		root.add(yellow);
		root.add(orange);
		root.add(blue);
		root.add(colors);
		colors.add(red);
		colors.add(blue2);
		//root.add(colors2);
		colors2.add(blue3);
		blue3.add(blue4);
		tree.expandRow(0);
		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent)
					throws ExpandVetoException {

				TreePath path = treeExpansionEvent.getPath();
				selectLabel.setText("Collapse_" + String.valueOf(path));

			}

			public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {

				TreePath path = treeExpansionEvent.getPath();
				selectLabel.setText("Expand_" + String.valueOf(path));

			}
		});
		this.treePanel = new JScrollPane(tree);
		addListeners(tree, "Tree");
		this.frame.add(this.treePanel);
	}

	private void createTabPanel()
	{
		this.tabPane = new JTabbedPane();
		String name = "TabPanel";
		this.tabPane.addTab("Green", new JLabel("tab1"));
		this.tabPane.addTab("Yellow", new JLabel("tab2"));
		this.tabPane.addTab("Orange", new JLabel("tab3"));
		this.tabPane.addTab("Blue", new JLabel("tab4"));
		addListeners(this.tabPane, name);
		this.tabPane.getModel().addChangeListener(e -> centralLabel.setText(name + "_" + this.tabPane.getTitleAt(this.tabPane.getSelectedIndex())));
		this.frame.add(this.tabPane);
	}

	private void createPanelListView()
	{
		DefaultListModel<String> stringDefaultListModel = new DefaultListModel<>();
		stringDefaultListModel.addElement("Green");
		stringDefaultListModel.addElement("Yellow");
		stringDefaultListModel.addElement("Orange");
		stringDefaultListModel.addElement("Blue");

		this.list = new JList<>(stringDefaultListModel);
		String name = "List";
		addListeners(this.list, name);
		this.list.getSelectionModel().addListSelectionListener(l -> centralLabel.setText(name + "_" + this.list.getSelectedValue()));
		this.frame.add(this.list);
	}

	private void createPanelProgressBar()
	{
		this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
		addListeners(this.progressBar, "ProgressBar");
		this.progressBar.setValue(10);
		this.frame.add(this.progressBar);
	}

	private void createPanelPanel()
	{
		this.panel = new JPanel();
		this.panel.add(new JLabel("Panel"));
		this.panel.setBackground(Color.PINK);
		addListeners(this.panel, "Panel");
		this.frame.add(this.panel);
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
		this.scrollBar = new JScrollBar(Adjustable.HORIZONTAL);
		String name = "ScrollBar";
		addListeners(this.scrollBar, name);
		this.scrollBar.getModel().addChangeListener(event -> sliderLabel.setText("ScrollBar_" + String.valueOf(this.scrollBar.getValue())));
		this.frame.add(this.scrollBar);
	}

	private void createPanelSplitPane()
	{
		String name = "Splitter";
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		this.splitPane.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY))
			{
				if(firstRun)
				{
					sliderLabel.setText(name + "_" + this.splitPane.getDividerLocation());
				}
				firstRun = true;
			}
		});
		this.splitPane.setLeftComponent(new JLabel("LEFT COMPONENT"));
		this.splitPane.setRightComponent(new JLabel("RIGHT COMPONENT"));
		addListeners(this.splitPane, name);
		this.frame.add(this.splitPane);
	}

	private void createPanelAny()
	{
		String name = "Any";
		this.any = new JButton(name);
		addListeners(this.any, name);
		this.frame.add(this.any);
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
				protocolText.append(name + "_down_" + e.getKeyCode() + NEWLINE);
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
				protocolText.append(name + "_up_" + e.getKeyCode() + NEWLINE);
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				protocolText.append(name + "_press_" + (int)e.getKeyChar() + NEWLINE);
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
				}
				if (e.getClickCount() == 1)
				{
					centralLabel.setText(name + "_click");
					pushLabel.setText(name + "_push");
					moveLabel.setText("");
				}
			}
		};
	}
}
