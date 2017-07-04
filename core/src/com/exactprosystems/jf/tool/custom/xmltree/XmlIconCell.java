package com.exactprosystems.jf.tool.custom.xmltree;

import java.util.List;
import java.util.stream.Collectors;

import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem.BeanWithMark;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class XmlIconCell extends TreeTableCell<XmlTreeItem, XmlTreeItem>
{
	private ImageView imageView = new ImageView();

	@Override
	protected void updateItem(XmlTreeItem item, boolean empty)
	{
		super.updateItem(item, empty);
		setTooltip(null);
		if (item != null && !empty)
		{
			MarkerStyle icon = item.getStyle();
			List<XmlTreeItem.BeanWithMark> list = item.getList();
			if (!list.isEmpty())
			{
				String tooltip = list.stream()
						.filter(beanWithMark -> beanWithMark.getBean() != null)
						.map(bean -> bean.getBean().getId() + " ["+bean.getBean().getControlKind().name() + "]")
						.collect(Collectors.joining("\n"));
				this.setTooltip(new Tooltip(tooltip));
			}
			this.imageView.setImage(icon == null ? null : new Image(icon.getIconPath()));
			this.imageView.setOpacity(item.isMarkVisible() ? 1.0 : 0.4);
			setGraphic(this.imageView);
		}
		else
		{
			setGraphic(null);
		}
	}
}