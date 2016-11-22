package com.exactprosystems.jf;
 
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerListModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.beans.*; //property change stuff
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.awt.*;
import java.awt.event.*;
 
/* 1.4 example used by DialogDemo.java. */
public class NewOrderDialog extends JDialog implements ActionListener, PropertyChangeListener 
{
	private static final long serialVersionUID = -5290563430259637758L;

	private JPanel calendar;
	private JSpinner year;
	private JSpinner month;
	private JTextField textSide;
    private JTextField textPrice;
    private JTextField textQuantity;
    private JTextField textClientOrderId;
    private JCheckBox marketToLimit;
    private JCheckBox  marketOrder;
    private JCheckBox  disabledCheckbox;
    private JOptionPane optionPane;
    private JPanel radioGroup;
    private JPanel qtyButtons;
    private JPanel misc;
    private JComboBox anonymity;
	private JToggleButton expandButton;

    private static final String enterStr = "Enter";
    private static final String cancelStr = "Cancel";
 
    private static class JAddButton extends JButton implements ActionListener
    {
		private static final long serialVersionUID = -4362720271058591175L;

		private int add;
    	private JTextField field;
    	public JAddButton(int add, JTextField field)
    	{
    		if (add == 0)
    		{
    			this.setText("Clear");
    		}
    		else
    		{
    			this.setText(Integer.toString(add));
    		}

    		this.add = add;
    		this.field = field;
    		this.addActionListener(this);
    	}
		@Override
		public void actionPerformed(ActionEvent event)
		{
//			Component parent = getParent().getParent().getParent().getParent();
//			
////			System.out.println("par=" + parent.getLocationOnScreen() + "  w=" + parent.getWidth() + "  h=" + parent.getHeight());
////			System.out.println("com=" + getLocationOnScreen() + "  w=" + getWidth() + "  h=" + getHeight());
//			
//			System.out.println("par=" + parent.getLocationOnScreen());
//			System.out.println("com=" + getLocationOnScreen());
//			System.out.println();
////			System.out.println(MatcherSwing.getPoint(parent, RelativePoint.Base.LeftTop, 10, 10));
////			System.out.println(MatcherSwing.getPoint(parent, RelativePoint.Base.Center, 10, 10));
////			System.out.println(MatcherSwing.getPoint(parent, RelativePoint.Base.RightBottom, 10, 10));
//
//			System.out.println(MatcherSwing.getLocation(parent, RelativePoint.Base.LeftTop, this));
//			System.out.println(MatcherSwing.getLocation(parent, RelativePoint.Base.Center, this));
//			System.out.println(MatcherSwing.getLocation(parent, RelativePoint.Base.RightBottom, this));
//			System.out.println();
//			
//			
			String st = field.getText();
			int val;
			
			try
			{
				val = Integer.parseInt(st);
			}
			catch (NumberFormatException e)
			{
				val = 0;
			}
			
			if (add > 0)
			{
				val += add;
			}
			else
			{
				val = 0;
			}
			
			field.setText(Integer.toString(val));
		}
    }
    
    private static class JQuickPanel extends JPanel
    {
		private static final long serialVersionUID = -2309542686532978738L;

		public JQuickPanel(int rows, int cols, JComponent... items)
    	{
    		this.setLayout(new GridLayout(rows, cols));
    		for (JComponent item : items)
    		{
    			this.add(item);
    		}
    	}
    	
    	public JQuickPanel(JComponent... items)
    	{
    		this(items.length, 1, items);
    	}
    }
    
    /** Creates the reusable dialog. */
    public NewOrderDialog(Frame aFrame) 
    {
        super(aFrame, true);
 
        setTitle("Send new order");
 
        Calendar calendar = new GregorianCalendar();
        
        this.year = new JSpinner();
        this.year.setName("year");
        this.year.setValue(calendar.get(Calendar.YEAR));
        
        
        Calendar c = new GregorianCalendar();
        ArrayList<Object> stringList = new ArrayList<Object>();
        for(int i = 0; i < 12; i++)
        {
        	c.set(Calendar.MONTH, i);
            stringList.add(c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
        }
        this.month = new JSpinner();
        this.month.setName("month");
        this.month.setModel(new SpinnerListModel(stringList));
        this.month.setValue(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
        
        this.calendar = new JPanel();
        this.calendar.setName("calendar");
        this.calendar.setSize(154, 120);
        
        this.textSide = new JTextField(10);
        this.textSide.setName("textSide");
        
        
        this.textPrice = new JTextField(10);
        this.textPrice.setName("priceEdit");
        
        this.textQuantity = new JTextField(10);
        this.textQuantity.setName("quantityEdit");

        // Adding quantity control buttons
        
        this.qtyButtons = new JPanel(new GridLayout(2, 3));
        this.qtyButtons.add(new JAddButton(1,   this.textQuantity));
        this.qtyButtons.add(new JAddButton(5,   this.textQuantity));
        this.qtyButtons.add(new JAddButton(10,  this.textQuantity));
        this.qtyButtons.add(new JAddButton(50,  this.textQuantity));
        this.qtyButtons.add(new JAddButton(0,   this.textQuantity));
        this.qtyButtons.add(new JAddButton(100, this.textQuantity));
        
        this.textClientOrderId = new JTextField(10);
        this.textClientOrderId.setName("clrOrdIdEdit");
        
        // Adding mutually exclusive check-boxes 
        
        ActionListener uncheck = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				JComponent cmp = (JComponent) event.getSource();
				Container cnt = cmp.getParent();
				for (Component child : cnt.getComponents())
				{
					if (child instanceof JCheckBox && !child.equals(cmp))
					{
						((JCheckBox) child).setSelected(false);
					}
				}
			}
		};
		
        this.marketToLimit = new JCheckBox("Market to limit");
        this.marketToLimit.addActionListener(uncheck);

        this.marketOrder = new JCheckBox("Market order");
        this.marketOrder.addActionListener(uncheck);
                
        this.disabledCheckbox = new JCheckBox("Disabled checkbox");
        this.disabledCheckbox.setEnabled(false);
        this.disabledCheckbox.addActionListener(uncheck);
        
        //Adding a radio group. Here, we use a ButtonGroup because we don't need to disable all options 
        
        this.radioGroup = new JPanel(new GridLayout(2, 2));
        this.radioGroup.setBorder(BorderFactory.createTitledBorder("Capacity"));
        
        ButtonGroup group = new ButtonGroup();
        
        group.add(new JRadioButton("O"));
        group.add(new JRadioButton("T"));
        group.add(new JRadioButton("G"));
        group.add(new JRadioButton("P"));
        
        group.getElements().nextElement().setSelected(true);
        
        Enumeration<AbstractButton> elem = group.getElements();
        
        //Put all the buttons onto a panel
        while (elem.hasMoreElements())
        {
        	AbstractButton btn = elem.nextElement();
        	this.radioGroup.add(btn);
        }
        
        //Adding a simple combo box
        this.anonymity = new JComboBox(new String[] {"Anonymous", "Named"});

        this.misc = new JQuickPanel(
				this.marketToLimit, 
				this.marketOrder,
				this.disabledCheckbox,
				this.radioGroup);
        
        final JDialog thatDialog = this;

        this.expandButton = new JToggleButton(">>", false);
		this.expandButton.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				JToggleButton src = (JToggleButton) event.getSource();
				misc.setVisible(src.isSelected());
				thatDialog.pack();
			}
		});
        
        
        //Create an array of the text and components to be displayed.
        Object[] array = { 
        		"Year", this.year,
        		"Month", this.month,
        		"Calendar", this.calendar,
        		new JQuickPanel(new JLabel("Side"), this.textSide), 
        		new JQuickPanel(new JLabel("Price"), this.textPrice), 
        		new JQuickPanel(new JLabel("Quantity"), this.textQuantity),
        		this.qtyButtons,
        		new JQuickPanel(new JLabel("Client order ID"), this.textClientOrderId),
        		new JQuickPanel(new JLabel("Anonymity"), this.anonymity),
        		this.expandButton,
        		this.misc
        		};
        
        this.misc.setVisible(false);
 
        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = { enterStr, cancelStr };
 
        //Create the JOptionPane.
        this.optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    enterStr);
 
        //Make this dialog display it.
        setContentPane(this.optionPane);
 
//        //Handle window closing correctly.
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
 
        //Ensure the text field always gets the first focus.
        addComponentListener(
        		new ComponentAdapter() 
        		{
		            public void componentShown(ComponentEvent ce) 
		            {
		                textSide.requestFocusInWindow();
		            }
        		});
 
        //Register an event handler that puts the text into the option pane.
        this.textSide.addActionListener(this);
 
        //Register an event handler that reacts to option pane state changes.
        this.optionPane.addPropertyChangeListener(this);
    }
 
    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) 
    {
    	
        this.optionPane.setValue(enterStr);
    }
 
    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) 
    {
    	if (e.getNewValue().equals(enterStr))
    	{
    		try
    		{
    			this.optionPane.setValue("closed");
	    		OrderSide side = OrderSide.valueOf(this.textSide.getText().toUpperCase());
	    		double price = Double.parseDouble(this.textPrice.getText());
	    		int qty = Integer.parseInt(this.textQuantity.getText());
	    		String clientOrderId = this.textClientOrderId.getText();
	    		
	    		if (   side != null
					&& price > 0
					&& qty > 0
					&& !clientOrderId.isEmpty() )
	    		{
	    			System.out.println("Send new order: " + side + " " + price + " " + qty + " " + clientOrderId);
	    			
//	    			Tools.sendOrder(side, price, qty, clientOrderId);
	    		}
	    		else
	    		{
	    			JOptionPane.showMessageDialog(this.getLayeredPane(), "Error");
	    		}
	    		this.setVisible(false);
    		}
    		catch (Exception ex)
    		{
    			JOptionPane.showMessageDialog(this.getLayeredPane(), "Error: " + ex.getMessage());
    		}    		
    	}
    	else if (e.getNewValue().equals(cancelStr))
    	{
    		this.optionPane.setValue("closed");
    		this.setVisible(false);
    	}
    }
}