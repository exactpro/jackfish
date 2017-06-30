package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import javafx.scene.layout.HBox;

import org.w3c.dom.Node;

import java.util.*;

public class XmlTreeItem extends XpathItem
{
	private boolean markIsVisible = true;
	private Set<BeanWithMark> set = new HashSet<>();
	private MarkerStyle currentStyle;

	public XmlTreeItem(HBox box, Node node)
	{
		super(box, node);
	}

	public void changeState()
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
