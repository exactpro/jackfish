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
    private MarkerStyle currentStyle;

	public XmlItem(Node node)
	{
		this.node = node;
		this.rectangle = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
		this.currentStyle = null;
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
        return this.currentStyle;
    }

	public String getText()
	{
		return XpathUtils.text(this.node);
	}
	
    public MarkerStyle changeState()
    {
        if (this.currentStyle == null)
        {
            this.currentStyle = MarkerStyle.ADD;
            this.addRelation(MarkerStyle.ADD);
        }
        else if (this.currentStyle == MarkerStyle.ADD || currentStyle == MarkerStyle.UPDATE)
        {
            this.currentStyle = null;
        }
        else
        {
            this.currentStyle = MarkerStyle.UPDATE;
        }
        return this.currentStyle;
    }
	
    public void addRelation(MarkerStyle state)
    {
        this.currentStyle = state;
    }

    public void clearRelation(ElementWizardBean bean)
    {
        this.currentStyle = null;
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
