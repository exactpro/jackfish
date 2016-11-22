package com.exactprosystems.jf;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/* MenuSelectionManagerDemo.java requires images/middle.gif. */
 
/*
 * This class is just like MenuDemo, except every second (thanks
 * to a Timer) the selected path of the menu is printed in the text area.
 */
public class Application implements ItemListener, PropertyChangeListener
{
	private JFrame frame;
	private NewOrderDialog customDialog;
	private TreeViewDialog treeViewDialog;
	private QuotaDialog quotaDialog;
    private ProgressMonitor progressMonitor;
    private Task task;
    private JTable table;
    		
	class Task extends SwingWorker<Void, Void>
	{
		private int time = 0;
		
		public Task(int time)
		{
			this.time = time;
		}
		
		@Override
		public Void doInBackground()
		{
			setProgress(0);
			
			long current = System.currentTimeMillis();
			long begin = current;
			long end = begin + this.time * ONE_SECOND;
			
			while (current < end && !isCancelled())
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
				}
				
				setProgress(Math.min((int)(((double)(current - begin)/(double)(end - begin)) * 100.0), 100));
				current = System.currentTimeMillis();
			}

			return null;
		}

		@Override
		public void done()
		{
			Toolkit.getDefaultToolkit().beep();
			progressMonitor.setProgress(0);
		}
	}

    String newline = "\n";
    public final static int ONE_SECOND = 1000;

	private Component componentAtPosition(Component component, int x, int y)
	{
		if (component == null)
		{
			return null;
		}
		
		if (component instanceof Container)
		{
			Container container = (Container)component;
			for (Component comp : container.getComponents())
			{
				if (!comp.isVisible())
				{
					continue;
				}
				if (comp.getBounds().contains(x, y))
				{
					return componentAtPosition(comp, x - comp.getX(), y - comp.getY());
				}
			}
		}
		
		return component;
	}

    
    public Application(int timeToLoad) throws InterruptedException 
    {
    	try
    	{
	        //Create and set up the window.
	        this.frame = new JFrame("MenuSelectionManagerDemo");
	        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
	        //Create and set up the content pane.
	        this.frame.setJMenuBar(this.createMenuBar());
	        
	        
	        
	        this.frame.setContentPane(this.createContentPane());
	
	        this.customDialog = new NewOrderDialog(this.frame);
	        this.customDialog.pack();
	        
	        this.treeViewDialog = new TreeViewDialog(this.frame);
	        this.treeViewDialog.pack();
	        
	        this.quotaDialog = new QuotaDialog(this.frame);
	        this.quotaDialog.pack();

	        this.progressMonitor = new ProgressMonitor(this.frame, "Running a Long Task", "", 0, 100);
			this.progressMonitor.setProgress(0);
			this.task = new Task(timeToLoad);
			this.task.addPropertyChangeListener(this);
			this.task.execute();
	        
	        
			this.table = new JTable(new DefaultTableModel(
					new Object[][] {
					{"1",	"Ord1",	"I238",	"Buy",	"1,000.0",	"10", "20130410-11:42:03.378"},
					{"2",	"Ord2",	"I238",	"Buy",	"110.0",	"15", "20130411-22:14:35.000"},
					{"3",	"Ord3",	"I238",	"Buy",	"120.0",	"20", "20130417-10:22:11.768"},
					{"4",	"Ord4",	"I239",	"Buy",	"130.0",	"20", "20130417-10:22:12.032"},
					{"5",	"Ord5",	"I239",	"Buy",	"1400.0",	"40", "20130418-15:04:22.000"},
					
					}, new Object[] {"#", "Client Order ID", "ISIN", "Side", "Price", "Qty", "Time" }));
			
			JScrollPane tablePane = new JScrollPane(table);
			frame.add(tablePane);
			
			table.setName(String.valueOf(123456));
			table.setCellEditor(new DefaultCellEditor(new JTextField()));
			
			
			JComboBox comboBox = new JComboBox();
			comboBox.addItem("One");
			comboBox.addItem("Two");
			comboBox.setEditable(false);
			
			TableColumn aColumn = new TableColumn(5, 100, new DefaultTableCellRenderer(), new DefaultCellEditor(comboBox));
			aColumn.setHeaderValue("Combo");
			aColumn.setIdentifier("Combo");
			table.addColumn(aColumn);

			final JPopupMenu menu = new JPopupMenu("Edit row");
						
			JMenuItem addRow = new JMenuItem("Add a random row");
			addRow.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.addRow(new Object[] {
							""+(model.getRowCount()+1), 
							"Ord"+(model.getRowCount()+1), 
							"I239", 
							"Sell", 
							""+(100.0+10.0*model.getRowCount()),
							"50"});
				}
			});
			menu.add(addRow);
			
			final JMenuItem removeRow = new JMenuItem("Remove this row");
			removeRow.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.removeRow(table.getSelectedRow());
				}
			});
			menu.add(removeRow);
			
			final JMenu subMenu = new JMenu("Submenu");
			
			JMenuItem subAddRow = new JMenuItem("sub Add a random row");
			subAddRow.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.addRow(new Object[] {
							""+(model.getRowCount()+1), 
							"Ord"+(model.getRowCount()+1), 
							"I239", 
							"Sell", 
							""+(100.0+10.0*model.getRowCount()),
							"50"});
				}
			});

			subMenu.add(subAddRow);
			
			final JMenuItem subRemoveRow = new JMenuItem("sub Remove this row");
			subRemoveRow.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.removeRow(table.getSelectedRow());
				}
			});

			subMenu.add(subRemoveRow);
			
			menu.add(subMenu);
			
		    //Add listener to components that can bring up popup menus.
		    MouseListener popupListener = new MouseAdapter() {
			    public void mousePressed(MouseEvent e) {
			        maybeShowPopup(e);
			    }
	
			    public void mouseReleased(MouseEvent e) {
			        maybeShowPopup(e);
			    }
	
			    private void maybeShowPopup(MouseEvent e) {
			        if (e.isPopupTrigger()) {
				    	removeRow.setEnabled(table.getSelectedRow() >= 0);
			            menu.show(e.getComponent(),
			                       e.getX()+2, e.getY()+2);
			        }
			    }
			};			
			
		    tablePane.addMouseListener(popupListener);
			table.addMouseListener(popupListener);
		    table.getTableHeader().addMouseListener(popupListener);
			table.setVisible(true);
			table.setShowGrid(true);
			table.setGridColor(Color.black);
			table.setForeground(Color.black);
			
	        
	        //Display the window.
	        frame.setSize(450, 260);
	        frame.setVisible(true);
	        
	        if (timeToLoad > 0)
	        {
	        }
    	}
    	catch (Exception e)
    	{
			e.printStackTrace();
    	}
    }
    
    
    public JMenuBar createMenuBar() 
    {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;
        JMenuItem quoteItem;
        JMenuItem treeItem;
        JMenuItem quitItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        
        // Create the toggle button.
        final JToggleButton toggle = new JToggleButton(">>");
        toggle.setSize(50, 50);
        final JPopupMenu popupMenu = new JPopupMenu("Popup menu");

        JMenuItem popupMenuItem = new JMenuItem("New order popup...", KeyEvent.VK_N);
        popupMenuItem.setName("NewOrder popup");
        popupMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        popupMenuItem.getAccessibleContext().setAccessibleDescription("Showes the NewOrder dialog");
        popupMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        Application.this.customDialog.setVisible(true);
			}
		});
        popupMenu.add(popupMenuItem);

        JMenuItem popupMenuItem2 = new JMenuItem("New quote popup...", KeyEvent.VK_N);
        popupMenuItem2.setName("NewQuote popup");
        popupMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        popupMenuItem2.getAccessibleContext().setAccessibleDescription("Showes the NewOrder dialog");
        popupMenuItem2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        Application.this.quotaDialog.setVisible(true);
			}
		});
        popupMenu.add(popupMenuItem2);
        
        
        final JPopupMenu popupMenu2 = new JPopupMenu("Popup menu2");
        	
		JMenuItem subAddRow = new JMenuItem("subbar Add a random row");
		subAddRow.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.addRow(new Object[] {
						""+(model.getRowCount()+1), 
						"Ord"+(model.getRowCount()+1), 
						"I239", 
						"Sell", 
						""+(100.0+10.0*model.getRowCount()),
						"50"});
			}
		});

		popupMenu2.add(subAddRow);
		
		final JMenuItem subRemoveRow = new JMenuItem("subbar Remove this row");
		subRemoveRow.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.removeRow(table.getSelectedRow());
			}
		});

		popupMenu2.add(subRemoveRow);
		
		
		final JMenuItem subMenu = new JMenuItem("Submenubar");
		subMenu.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				popupMenu2.show(toggle, toggle.getWidth(), toggle.getHeight());
			}
		});
		
		//subMenu.add(popupMenu2);
			
		popupMenu.add(subMenu);
        
        toggle.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (toggle.isSelected())
				{
					popupMenu.show(toggle, 0, toggle.getHeight());
					toggle.setSelected(false);
				}
			}
		});
		
//        toggle.add(popupMenu);
        
        
        //Build the first menu.
        menu = new JMenu("Orders");
        menu.setName("Orders");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Orders menu");
        menuBar.add(menu);
        
        //a group of JMenuItems
        menuItem = new JMenuItem("New order...", KeyEvent.VK_N);
        menuItem.setName("NewOrder");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Showes the NewOrder dialog");
        menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        Application.this.customDialog.setVisible(true);
			}
		});
        
        
        quoteItem = new JMenuItem("Quote...", KeyEvent.VK_Q);
        quoteItem.setName("Quote");
        quoteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        quoteItem.getAccessibleContext().setAccessibleDescription("Showes the Quota dialog");
        quoteItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        Application.this.quotaDialog.setVisible(true);
			}
		});

        treeItem = new JMenuItem("Tree view...", KeyEvent.VK_T);
        treeItem.setName("TreeVieew");
        treeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        treeItem.getAccessibleContext().setAccessibleDescription("Showes the TreeView dialog");
        treeItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        Application.this.treeViewDialog.setVisible(true);
			}
		});
        
        quitItem = new JMenuItem("Exit");
        quitItem.setName("Exit");
        quitItem.getAccessibleContext().setAccessibleDescription("Exit from application.");
        quitItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        System.exit(0);
			}
		});

        menu.add(menuItem);
        menu.add(quoteItem);
        menu.add(treeItem);
        menu.add(quitItem);

        menuBar.add(toggle);

        return menuBar;
    }
 
    public Container createContentPane() 
    {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
 
        return contentPane;
    }

    public void itemStateChanged(ItemEvent e) 
    {
    }
 
    public static void main(String[] args) 
    {
		System.out.println("Application.main args=" + Arrays.toString(args));

    	int time = 0;
    	try
    	{
    		if (args.length > 0)
    		{
    			time = Integer.parseInt(args[0]);
    		}
    	}
    	catch (Exception e)
    	{ 
    		logger.error(e.getMessage(), e);
    		e.printStackTrace();
    	}
    		
    	final int timeToLoad = time;
    	
    		
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	try 
            	{
					Application demo = new Application(timeToLoad);
				} 
            	catch (InterruptedException e) 
            	{
					e.printStackTrace();
				}
            }
        });
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if ("progress" == evt.getPropertyName())
		{
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			String message = String.format("Completed %d%%.", progress);
			progressMonitor.setNote(message);
			if (progressMonitor.isCanceled() || task.isDone())
			{
				Toolkit.getDefaultToolkit().beep();
				if (progressMonitor.isCanceled())
				{
					task.cancel(true);
				} 
				this.progressMonitor.close();
			}
		}
	}


	private static final Logger logger = Logger.getLogger(Application.class);

	static
	{
		PropertyConfigurator.configure("log.properties");
	}
}	
