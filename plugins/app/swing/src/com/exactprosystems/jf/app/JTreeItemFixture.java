package com.exactprosystems.jf.app;

import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ComponentFixture;

import java.awt.*;

public class JTreeItemFixture extends ComponentFixture<Component>
{
    JTreeItemFixture(Robot robot, JTreeItem target)
    {
        super(robot, target);
    }
}
