package com.exactprosystems.jf;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.log4j.Logger;
import java.awt.Component;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

public class TreeViewDialog extends JDialog
{
	private static final long serialVersionUID = 247708413367214611L;

	private final JPanel contentPanel = new JPanel();

	public TreeViewDialog(Frame frame)
	{
		super(frame);

		setTitle("TreeView");
		setBounds(100, 100, 572, 520);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();

		JPanel panel_1 = new JPanel();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_contentPanel
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 234,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 280,
								Short.MAX_VALUE).addContainerGap()));
		gl_contentPanel
				.setVerticalGroup(gl_contentPanel
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								Alignment.TRAILING,
								gl_contentPanel
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_contentPanel
														.createParallelGroup(
																Alignment.TRAILING)
														.addComponent(
																panel_1,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE,
																424,
																Short.MAX_VALUE)
														.addComponent(
																panel,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE,
																424,
																Short.MAX_VALUE))
										.addContainerGap()));
		panel_1.setLayout(new BorderLayout(0, 0));

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setName("pane1");
		panel_1.add(tabbedPane, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Tab 1", null, panel_2, null);
		panel_2.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setName("pane1_1");
		panel_2.add(tabbedPane_1, BorderLayout.CENTER);

		JPanel panel_4 = new JPanel();
		tabbedPane_1.addTab("Inside 1_1", null, panel_4, null);
		panel_4.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_4.add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		table.setModel(new DefaultTableModel(new Object[][] { { null, null,
				null, null, null }, }, new String[] { "Col1", "Col2", "Col3",
				"Col4", "Col5" }));
		scrollPane.setViewportView(table);

		JPopupMenu popupMenu_1 = new JPopupMenu();
		addPopup(table, popupMenu_1);

		JMenuItem mntmAddrow = new JMenuItem("AddRow");
		mntmAddrow.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TreeViewDialog.this.table.setValueAt("Value", 0, 0);
				tabbedPane.setTitleAt(0, "Tab 1 [1/2]");
			}
		});
		popupMenu_1.add(mntmAddrow);

		JPanel panel_5 = new JPanel();
		tabbedPane_1.addTab("Inside 1_2", null, panel_5, null);
		panel_5.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Tab 2", null, panel_3, null);
		panel_3.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_2.setName("pane1_2");
		panel_3.add(tabbedPane_2, BorderLayout.CENTER);

		JPanel panel_6 = new JPanel();
		tabbedPane_2.addTab("Tab 1_1", null, panel_6, null);
		panel_6.setLayout(new BorderLayout(0, 0));

		JPanel panel_7 = new JPanel();
		tabbedPane_2.addTab("Tab 1_2", null, panel_7, null);
		panel_7.setLayout(new BorderLayout(0, 0));
		panel.setLayout(new BorderLayout(0, 0));

		JTree tree = new JTree();
		tree.setExpandsSelectedPaths(false);
		panel.add(tree, BorderLayout.CENTER);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);

		JMenuItem mntmAdd = new JMenuItem("add");
		mntmAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(TreeViewDialog.this, "add");
			}
		});
		popupMenu.add(mntmAdd);

		JMenuItem mntmDelete = new JMenuItem("delete");
		mntmDelete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(TreeViewDialog.this, "delete");
			}
		});
		popupMenu.add(mntmDelete);

		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						TreeViewDialog.this.setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						TreeViewDialog.this.setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private static final Logger logger = Logger.getLogger(TreeViewDialog.class);
	private JTable table;

	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
