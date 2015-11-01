package com.exactprosystems.jf.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class HighLighter implements AutoCloseable
{
	private JWindow top;
	private JWindow bottom;
	private JWindow left;
	private JWindow right;
	
	private static final int thick = 2;
	private static final int period = 500;

	@Override
	public void close() throws Exception
	{
		display(false);	
	}
	
	public HighLighter()
	{
		this.top 		= new JWindow();
		this.bottom		= new JWindow();
		this.left		= new JWindow();
		this.right		= new JWindow();
	}

	public void start(final Point location, final Dimension dimension)
	{
		int x1 = location.x;
		int y1 = location.y;
		int x2 = x1 + dimension.width;
		int y2 = y1 + dimension.height;

		tune(top, 		x1 - thick, y1 - thick, 	dimension.width + 2 * thick, thick);
		tune(bottom, 	x1 - thick, y2, 			dimension.width + 2 * thick, thick);
		tune(left, 		x1 - thick, y1 - thick, 	thick, dimension.height + 2 * thick);
		tune(right, 	x2, y1 - thick, 			thick, dimension.height + 2 * thick);

		display(true);
		try
		{
			Thread.sleep(period);
		}
		catch (InterruptedException e)
		{
			// nothing to do
		}
		display(false);
	}	
	
	private void tune(JWindow window, int x, int y, int width, int height)
	{
		window.setAlwaysOnTop(true);
		window.setLocationRelativeTo(null);
		window.setSize(width, height);
		window.setLocation(x, y);
		
		JPanel panel = new JPanel();
		panel.setForeground(Color.red);
		panel.setBackground(Color.red);
		window.add(panel);
	}

	private void display(boolean value)
	{
		this.top.setVisible(value);
		this.bottom.setVisible(value);
		this.left.setVisible(value);
		this.right.setVisible(value);
	}

}