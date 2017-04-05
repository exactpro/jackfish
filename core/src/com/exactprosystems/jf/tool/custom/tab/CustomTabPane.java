package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomTabPane extends TabPane
{
	private static CustomTabPane INSTANCE;

	private Settings settings;

	static final String TAB_DRAG_KEY = "tab";

	private ObjectProperty<Tab> draggingTab = new SimpleObjectProperty<>();
	private ObjectProperty<Tab> droppedTab = new SimpleObjectProperty<>();

	private List<Tab> tempTabList = new ArrayList<>();

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
		this.setOnDragDone(e ->
		{
			final Dragboard dragboard = e.getDragboard();
			Tab draggingTab = this.draggingTab.get();
			Tab droppedTab = this.droppedTab.get();
			if (droppedTab == null)
			{
				removeTempTabs();
			}
			else if (dragboard.hasString() && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab != null)
			{
				int index = this.getTabs().indexOf(droppedTab);
				int realIndex = index / 2;

				this.removeTempTabs();
				this.getTabs().remove(draggingTab);
				this.getTabs().add(Math.min(realIndex, this.getTabs().size()), draggingTab);
				this.getSelectionModel().select(draggingTab);

				this.draggingTab.set(null);
				this.droppedTab.set(null);
			}
			e.consume();
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

	ObjectProperty<Tab> draggingTabProperty()
	{
		return draggingTab;
	}

	ObjectProperty<Tab> droppedTabProperty()
	{
		return droppedTab;
	}

	void addTempTabs()
	{
		this.tempTabList.clear();
		int size = this.getTabs().size();
		for (int i = 0; i < size + 1; i++)
		{
			CustomTab.TempCustomTab myTab = new CustomTab.TempCustomTab(null, null, this);
			this.tempTabList.add(myTab);
			this.getTabs().add(i * 2, myTab);
		}

		int index = this.getTabs().indexOf(this.draggingTab.get());
		if (index != -1)
		{
			Tab removeLeft = this.getTabs().remove(Math.max(index - 1, 0));
			Tab removeRight = this.getTabs().remove(Math.min(index, this.getTabs().size()));
			this.tempTabList.removeAll(Arrays.asList(removeLeft, removeRight));
		}
	}

	void removeTempTabs()
	{
		this.getTabs().removeAll(this.tempTabList);
		this.tempTabList.clear();
	}
}
