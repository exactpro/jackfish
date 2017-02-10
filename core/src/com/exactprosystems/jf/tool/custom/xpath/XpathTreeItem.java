package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XpathTreeItem extends XpathItem
{
	public enum TreeItemState
	{
		ADD(CssVariables.Icons.ADD_16_ICON, Color.web("#2687fb")),
		MARK(CssVariables.Icons.MARK_ICON, Color.web("#2a9635")),
		QUESTION(CssVariables.Icons.QUESTION_ICON, Color.web("#f3c738"));

		private String iconPath;
		private Color color;

		TreeItemState(String iconPath, Color color)
		{
			this.iconPath = iconPath;
			this.color = color;
		}

		public String getIconPath()
		{
			return iconPath;
		}

		public Color color()
		{
			return color;
		}
	}

	private boolean markIsVisible = true;
	private List<BeanWithMark> list = new ArrayList<>();
	private TreeItemState currentState;

	public XpathTreeItem(HBox box, Node node)
	{
		super(box, node);
	}

	public void changeState()
	{
		if (currentState == null)
		{
			this.currentState = TreeItemState.ADD;
			this.addRelation(null, TreeItemState.ADD);
		}
		else if (currentState == TreeItemState.ADD)
		{
			this.currentState = null;
			//remove all relation
			this.list.clear();
		}
		else
		{
			this.currentState = TreeItemState.ADD;
			this.list.forEach(b -> b.setState(this.currentState));
		}
	}

	public void addRelation(ElementWizardBean bean, TreeItemState state)
	{
		this.list.add(new BeanWithMark(bean, state));
		this.currentState = state;
	}

	public void clearRelation(ElementWizardBean bean)
	{
		this.list.clear();
		this.currentState = null;
	}

	public boolean contains(ElementWizardBean bean)
	{
		return this.list.stream().map(BeanWithMark::getBean).anyMatch(bean::equals);
	}

	public List<BeanWithMark> getList()
	{
		return list;
	}

	public TreeItemState getState()
	{
		return currentState;
	}

	public boolean isMarkVisible()
	{
		return markIsVisible;
	}

	public void setMarkIsVisible(boolean markIsVisible)
	{
		this.markIsVisible = markIsVisible;
	}

	public static class BeanWithMark
	{
		private ElementWizardBean bean;
		private TreeItemState state;

		public BeanWithMark(ElementWizardBean bean, TreeItemState state)
		{
			this.bean = bean;
			this.state = state;
		}

		public ElementWizardBean getBean()
		{
			return bean;
		}

		public void setBean(ElementWizardBean bean)
		{
			this.bean = bean;
		}

		public TreeItemState getState()
		{
			return state;
		}

		public void setState(TreeItemState state)
		{
			this.state = state;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			BeanWithMark that = (BeanWithMark) o;

			return bean != null ? bean.equals(that.bean) : that.bean == null;
		}

		@Override
		public int hashCode()
		{
			return bean != null ? bean.hashCode() : 0;
		}
	}
}
