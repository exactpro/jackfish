package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeTableCell;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.MouseInfo;
import java.util.*;
import java.util.stream.IntStream;

public class XmlTreeItem extends XmlItem
{
	private boolean markIsVisible = true;
	private Set<BeanWithMark> set = new HashSet<>();
	private MarkerStyle currentStyle;

    public XmlTreeItem(Node node)
    {
        super(stringNode(node, XpathUtils.text(node)), node);
    }

//    
//	
//	public XmlTreeItem(HBox box, Node node)
//	{
//		super(box, node);
//	}

	public MarkerStyle changeState()
	{
		if (this.currentStyle == null)
		{
			this.currentStyle = MarkerStyle.ADD;
			this.addRelation(null, MarkerStyle.ADD);
			this.set.stream().map(BeanWithMark::getBean).filter(Objects::nonNull).forEach(b -> b.setStyleClass(null));
		}
		else if (this.currentStyle == MarkerStyle.ADD || currentStyle == MarkerStyle.UPDATE)
		{
			this.currentStyle = null;
			this.set.stream().map(BeanWithMark::getBean).filter(Objects::nonNull).forEach(b -> b.setStyleClass(null));
			this.set.clear();
		}
		else
		{
			this.currentStyle = MarkerStyle.UPDATE;
			this.set.stream().map(BeanWithMark::getBean).filter(Objects::nonNull).forEach(b -> b.setStyleClass(CssVariables.COLOR_ADD));
			this.set.forEach(b -> b.setStyle(this.currentStyle));
		}
		return this.currentStyle;
	}

	public void addRelation(ElementWizardBean bean, MarkerStyle state)
	{
		if (bean != null && state != null)
		{
			bean.setStyleClass(state.getCssStyle());
		}
		this.set.add(new BeanWithMark(bean, state));
		this.currentStyle = state;
	}

	public void clearRelation(ElementWizardBean bean)
	{
		this.set.clear();
		this.currentStyle = null;
	}

	public boolean contains(ElementWizardBean bean)
	{
		return this.set.stream().map(BeanWithMark::getBean).anyMatch(bean::equals);
	}

	public List<BeanWithMark> getList()
	{
		return new ArrayList<>(set);
	}

	public MarkerStyle getStyle()
	{
		return this.currentStyle;
	}

	public boolean isMarkVisible()
	{
		return this.markIsVisible;
	}

	public void setMarkIsVisible(boolean markIsVisible)
	{
		this.markIsVisible = markIsVisible;
	}

    private static HBox stringNode(org.w3c.dom.Node node, String text)
    {
        HBox box = new HBox();

        box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE));
        NamedNodeMap attributes = node.getAttributes();
        Optional.ofNullable(attributes).ifPresent(atrs ->
        {
            IntStream.range(0, atrs.getLength())
                    .mapToObj(atrs::item)
                    .forEach(item -> box.getChildren().addAll(
                            createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME)
                            , createText("=", CssVariables.XPATH_TEXT)
                            , createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE)
                    ));
        });
        if (Str.IsNullOrEmpty(text))
        {
            box.getChildren().add(createText("/>", CssVariables.XPATH_NODE));
        }
        else
        {
            box.getChildren().addAll(
                    createText(">", CssVariables.XPATH_NODE)
                    , createText(text, CssVariables.XPATH_TEXT)
                    , createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE)
            );
        }
        return box;
    }

    private static Text createText(String text, String cssClass)
    {
        Text t = new Text(text);
        t.setOnContextMenuRequested(event -> {

            MenuItem item = new MenuItem("Copy " + text);
            item.setOnAction(e -> Common.copyText(text));
            if (t.getParent().getParent() instanceof XmlTreeTableCell)
            {
                XmlTreeTableCell parent = (XmlTreeTableCell) t.getParent().getParent();
                SeparatorMenuItem separator = new SeparatorMenuItem();
                ContextMenu treeMenu = parent.getContextMenu();
                treeMenu.getItems().add(0, item);
                treeMenu.getItems().add(1, separator);
                treeMenu.setOnHidden(e -> treeMenu.getItems().removeAll(item, separator));
            }
            else
            {
                ContextMenu menu = new ContextMenu();
                menu.setAutoHide(true);
                menu.getItems().add(item);
                menu.show(t, MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY());
            }
        });
        t.getStyleClass().add(cssClass);
        return t;
    }
	
	
	
	public static class BeanWithMark
	{
		private ElementWizardBean bean;
		private MarkerStyle style;

		public BeanWithMark(ElementWizardBean bean, MarkerStyle style)
		{
			this.bean = bean;
			this.style = style;
		}

		public ElementWizardBean getBean()
		{
			return bean;
		}

		public void setBean(ElementWizardBean bean)
		{
			this.bean = bean;
		}

		public MarkerStyle getStyle()
		{
			return style;
		}

		public void setStyle(MarkerStyle style)
		{
			this.style = style;
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
