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

package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import org.w3c.dom.Node;

public class TableBean
{
	private TableBean option = this;
	private boolean         isNew;
	private int             count;
	private AbstractControl abstractControl;
	private Node            node;
	private String          style;

	public TableBean(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
		this.isNew = false;
		this.count = 0;
	}

	public Node getNode()
	{
		return node;
	}
	public void setNode(Node node)
	{
		this.node = node;
	}

	public String getId()
	{
		return this.abstractControl.getID();
	}

	public boolean getXpath()
	{
		return !Str.IsNullOrEmpty(this.abstractControl.getXpath());
	}

	public boolean getIsNew()
	{
		return isNew;
	}
	public void setIsNew(boolean aNew)
	{
		isNew = aNew;
	}

	public int getCount()
	{
		return count;
	}
	public void setCount(int count)
	{
		this.count = count;
	}

	public AbstractControl getAbstractControl()
	{
		return abstractControl;
	}
	public void setAbstractControl(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
	}

	public String getStyle()
	{
		return style;
	}
	public void setStyle(String style)
	{
		this.style = style;
	}

	public TableBean getOption()
	{
		return option;
	}
	public void setOption(TableBean option)
	{
		this.option = option;
	}

	public ControlKind getControlKind()
	{
		return this.abstractControl.getBindedClass();
	}
}
