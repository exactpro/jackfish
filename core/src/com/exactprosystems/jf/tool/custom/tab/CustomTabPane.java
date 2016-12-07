package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomTabPane extends TabPane
{
	private static CustomTabPane INSTANCE;

	private Settings settings;

	static final String TAB_DRAG_KEY = "tab";
	private ObjectProperty<Tab> draggingTab = new SimpleObjectProperty<>();

	private Map<CustomTab, Rectangle> tabs = new LinkedHashMap<>();

	private final Tab tempTab = new Tab("");

	public static CustomTabPane getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new CustomTabPane();
		}
		return INSTANCE;
	}

	private CustomTabPane()
	{
		super();
		this.getStyleClass().add(CssVariables.CUSTOM_TAB_PANE);
		this.setOnDragDropped(e -> {
			final Dragboard dragboard = e.getDragboard();
			Tab tab = draggingTab.get();
			if (dragboard.hasString() && TAB_DRAG_KEY.equals(dragboard.getString()) && tab != null)
			{
				int index = this.getTabs().indexOf(tempTab);
				if (index != -1)
				{
					this.getTabs().remove(draggingTab.get());
					this.getTabs().add(index, draggingTab.get());
					this.getTabs().remove(tempTab);
					this.getSelectionModel().select(draggingTab.get());
				}
				e.consume();
			}
		});
		this.setOnDragExited(e -> {
			if (e.getPickResult().getIntersectedNode() != this)
			{
				this.getTabs().remove(tempTab);
			}
		});
		this.setOnDragOver(e -> {
			final Dragboard dragboard = e.getDragboard();
			Tab draggingTab = this.draggingTab.get();
			if (dragboard.hasString() && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab != null)
			{
				//need calculate tab and add borders between tabs
				List<Map.Entry<CustomTab, Rectangle>> linkedList = new LinkedList<>(tabs.entrySet());
				Rectangle last = linkedList.get(linkedList.size() - 1).getValue();
				Rectangle first = linkedList.get(0).getValue();
				boolean isIn = (first.getY() < e.getScreenY()) && ((last.getY() + last.getHeight()) > e.getScreenY());
				if (!isIn)
				{
					this.getTabs().remove(tempTab);
					return;
				}
				Map.Entry<CustomTab, Rectangle> myEntry = null;
				//find current myTab
				for (Map.Entry<CustomTab, Rectangle> entry : linkedList)
				{
					CustomTab key = entry.getKey();
					if (key == draggingTab || key == tempTab)
					{
						continue;
					}
					Rectangle value = entry.getValue();
					double start = value.getX();
					double end = value.getWidth() + start;
					if (start < e.getScreenX() && end > e.getScreenX())
					{
						myEntry = entry;
						break;
					}
				}
				if (myEntry != null)
				{
					Rectangle value = myEntry.getValue();
					double start = value.getX();
					double end = value.getWidth() + start;
					double centre = (start + end) / 2;
					int index = linkedList.indexOf(myEntry);
					int addedIndex = -1;
					this.getTabs().remove(tempTab);
					addedIndex = e.getScreenX() < centre ? index : index + 1;
					if (addedIndex != -1)
					{
						this.getTabs().add(addedIndex, tempTab);
					}
				}
				e.acceptTransferModes(TransferMode.MOVE);
				e.consume();
			}
		});

	}

	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	public void addTab(CustomTab tab)
	{
		this.getTabs().add(tab);
	}

	public void selectTab(CustomTab tab)
	{
		this.getSelectionModel().select(tab);
	}

	public void removeTab(CustomTab tab)
	{
		this.getTabs().remove(tab);
	}

	public CustomTab createTab(Document doc)
	{
		return new CustomTab(doc, this.settings, this);
	}

	//region methods for CustomTab
	Map<CustomTab, Rectangle> getTabsMap()
	{
		return tabs;
	}

	ObjectProperty<Tab> draggingTabProperty()
	{
		return draggingTab;
	}
	//endregion
}
