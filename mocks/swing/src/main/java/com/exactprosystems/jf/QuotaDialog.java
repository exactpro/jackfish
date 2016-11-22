package com.exactprosystems.jf;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class QuotaDialog extends JDialog implements ActionListener,
		PropertyChangeListener
{
	private static final long serialVersionUID = -7154652027145729343L;

	private JPanel first;
	private JPanel second;
	private JButton enter;
	private JButton next;
	private JButton cancel;
	private JScrollPane pane;
    private JTextField textSide;
    private JTextField textPrice;
    private JTextField textQuantity;
    private JTextField textClientOrderId;
    private JOptionPane optionPane;

	
	public QuotaDialog(Frame aFrame) 
    {
        super(aFrame, true);
        
        setTitle("Quota");
        
		first = new JPanel(new GridLayout(2, 1));
		second = new JPanel(new GridLayout(2, 1));
		first.setVisible(true);
		second.setVisible(false);

		

        textSide = new JTextField(10);
        textSide.setName("textSide");
        
        
        textPrice = new JTextField(10);
        textPrice.setName("priceEdit");

        textQuantity = new JTextField(10);
        textQuantity.setName("quantityEdit");
        
        textClientOrderId = new JTextField(10);
        textClientOrderId.setName("clrOrdIdEdit");
 

		
		
		next = new JButton(">>");
		next.setVisible(true);
		cancel = new JButton("Cancel");
		cancel.setVisible(true);
		enter = new JButton("Enter");
		enter.setVisible(true);
		next.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				second.setVisible(!second.isVisible());				
			}
		});
	
		cancel.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QuotaDialog.this.dispose();
				
			}
		});
		
		first.add(textSide);
		first.add(textPrice);
		first.add(next);
		
		second.add(textQuantity);
		second.add(textClientOrderId);
		second.add(enter);
		second.add(cancel);
		
		Container content = getContentPane();
		content.setLayout(new GridLayout(2,1));
		
		content.add(first, BorderLayout.WEST);
		content.add(second, BorderLayout.WEST);
		
		pack();
		//setVisible(true);		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	}
}
