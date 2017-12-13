package com.exactprosystems.jf.app;

import javax.swing.*;

public class JTableCell extends JComponent
{
    private JTable table;
    private int x;
    private int y;

    public JTableCell(JTable table, int x, int y)
    {
        this.table = table;
        this.x = x;
        this.y = y;
    }

    public JTable getTable()
    {
        return table;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }
}
