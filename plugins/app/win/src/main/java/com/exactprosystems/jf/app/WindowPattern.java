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

public enum WindowPattern
{
	InvokePattern(10000),
	SelectionPattern(10001),
	ValuePattern(10002),
	RangeValuePattern(10003),
	ScrollPattern(10004),
	ExpandCollapsePattern(10005),
	GridPattern(10006),
	GridItemPattern(10007), 
	MultipleViewPattern(10008),
	WindowPattern(10009),
	SelectionItemPattern(10010),
	DockPattern(10011), 
	TablePattern(10012),
	TableItemPattern(10013),
	TextPattern(10014),
	TogglePattern(10015),
	TransformPattern(10016),
	ScrollItemPattern(10017),
	
	ItemContainerPattern(10019),
	VirtualizedItemPattern(10020),
	SynchronizedInputPattern(10021),
	;
	
	private WindowPattern(int id)
	{
		this.id = id;
	}
	
    public int getId()
    {
    	return this.id;
    }

    public static WindowPattern byId(int id)
    {
    	for (WindowPattern item : values())
    	{
    		if (item.id == id)
    		{
    			return item;
    		}
    	}
    	
    	return null;
    }
    
	private int id;
}
