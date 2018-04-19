/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
