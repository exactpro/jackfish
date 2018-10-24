/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
