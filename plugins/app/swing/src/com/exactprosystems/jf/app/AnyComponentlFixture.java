////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
