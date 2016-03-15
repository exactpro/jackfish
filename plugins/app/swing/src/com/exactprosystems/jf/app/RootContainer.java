////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

public class RootContainer extends Container
{
	private static final long	serialVersionUID	= 7936458589684226139L;

	@Override
	public Component add(Component comp)
	{
		this.components.add(comp);
		return  comp;
	}
	
	@Override
	public int getComponentCount()
	{
		return this.components.size();
	}
	
	@Override
	public Component getComponent(int n)
	{
		return this.components.get(n);
	}
	
	@Override
	public Component[] getComponents()
	{
		return this.components.toArray(new Component[this.components.size()]);
	}
	
	private List<Component> components = new ArrayList<Component>();

}
