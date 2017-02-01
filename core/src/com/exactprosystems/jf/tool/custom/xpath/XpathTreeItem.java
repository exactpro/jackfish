package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.w3c.dom.Node;

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

	private static TreeItemState[] states = new TreeItemState[]{TreeItemState.ADD, TreeItemState.MARK, TreeItemState.QUESTION};
	private TreeItemState currentState;
	private int currentIndex = -1;

	public XpathTreeItem(HBox box, Node node)
	{
		super(box, node);
	}

	public void changeState()
	{
		if (currentIndex >= states.length - 1)
		{
			currentState = null;
			currentIndex = -1;
		}
		else
		{
			currentState = states[++currentIndex];
		}
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
}
