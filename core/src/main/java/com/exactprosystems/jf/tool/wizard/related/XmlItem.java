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

package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.common.utils.XpathUtils;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.Objects;

public class XmlItem
{
	private Node node;
	private Rectangle rectangle;
    private MarkerStyle style;
    private boolean highlight;
    private boolean visible = true;

	public XmlItem(Node node)
	{
		this.node = node;
		this.rectangle = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
		this.style = null;
	}

	public Node getNode()
	{
		return this.node;
	}
	
	public Rectangle getRectangle()
	{
		return this.rectangle;
	}

    public MarkerStyle getStyle()
    {
        return this.style;
    }

	public String getText()
	{
		return XpathUtils.text(this.node);
	}

	public String findText() {
		return XpathUtils.findText(this.node);
	}
	
    public void setStyle(MarkerStyle style)
    {
        this.style = style;
    }

    public MarkerStyle changeStyle()
    {
        if (this.style == null)
        {
            this.style = MarkerStyle.ADD;
            this.setStyle(MarkerStyle.ADD);
        }
        else if (this.style == MarkerStyle.ADD || style == MarkerStyle.UPDATE)
        {
            this.style = null;
        }
        else
        {
            this.style = MarkerStyle.UPDATE;
        }
        return this.style;
    }

	public void setVisible(boolean flag)
	{
		this.visible = flag;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void highlight(boolean flag)
	{
		this.highlight = flag;
	}

	public boolean isHighlight()
	{
		return this.highlight;
	}
	
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + this.node + ":" + this.rectangle + ":" + this.style + "}";
    }
    
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		XmlItem other = (XmlItem) o;
		
		return Objects.equals(this.node, other.node);
	}

	@Override
	public int hashCode()
	{
	    return Objects.hashCode(this.node);
	}
}
