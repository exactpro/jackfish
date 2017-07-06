package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;

import org.w3c.dom.Node;

import java.awt.*;
import java.util.Objects;

public class XmlItem
{
	private Node node;
	private Rectangle rectangle;
    private MarkerStyle style;

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
