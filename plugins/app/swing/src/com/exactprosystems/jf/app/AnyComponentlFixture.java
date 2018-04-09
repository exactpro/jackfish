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

import java.awt.Component;

import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ComponentFixture;

public class AnyComponentlFixture extends ComponentFixture<Component>
{
	public AnyComponentlFixture(Robot robot, Component target)
	{
		super(robot, target);
	}
}
