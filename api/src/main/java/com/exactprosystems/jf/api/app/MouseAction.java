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

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public enum MouseAction  implements Serializable
{
	Move(0),
	LeftClick(1),
	RightClick(2),
	LeftDoubleClick(3),
	RightDoubleClick(4),
	DragNDrop(5),
	Press(6),
	Drop(7),
	Focus(8),
	Enter(9),
	Activated(10);
	
	private MouseAction(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	private int id;

	private static final long	serialVersionUID	= -3586047076930611493L;
}
