package com.exactprosystems.jf.app;

import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ComponentFixture;

import java.awt.*;

public class JTableCellFixture extends ComponentFixture<Component>
{
    public JTableCellFixture(Robot robot, JTableCell target)
    {
        super(robot, target);
    }
}
