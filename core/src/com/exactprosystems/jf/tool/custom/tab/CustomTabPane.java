////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CustomTabPane extends TabPane
{
	private static CustomTabPane INSTANCE;

	static final String TAB_DRAG_KEY = "tab";

	private ObjectProperty<Tab> draggingTab = new SimpleObjectProperty<>();
	private ObjectProperty<Tab> droppedTab = new SimpleObjectProperty<>();

	private List<Tab> tempTabList = new ArrayList<>();

	//TODO remove it
	@Deprecated
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
		this.setOnDragEntered(e -> {
			if (this.draggingTab.get() == null)
			{
				return;
			}
			if (this.tempTabList.isEmpty())
			{
				addTempTabs();
			}
		});
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
				ArrayList<Tab> oldTabs = new ArrayList<>(this.getTabs());
				this.getTabs().clear();
				oldTabs.remove(draggingTab);
				oldTabs.add(Math.min(realIndex, oldTabs.size()), draggingTab);

				this.draggingTab.set(null);
				this.droppedTab.set(null);
				this.getTabs().setAll(oldTabs);
				this.getSelectionModel().select(draggingTab);
			}
			e.consume();
		});
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
		return new CustomTab(doc, this);
	}

	public CustomTab getTabByDoc(Document document)
	{
		return this.getTab(tab -> tab.getDocument().equals(document));
	}

	public CustomTab getTabByFileName(String fileName)
	{
		return this.getTab(tab -> tab.getDocument().getNameProperty().get().equals(fileName));
	}

	public CustomTab getTabByFile(File file)
	{
		return this.getTab(tab -> new File(tab.getDocument().getNameProperty().get()).getAbsolutePath().equals(file.getAbsolutePath()));
	}

	ObjectProperty<Tab> draggingTabProperty()
	{
		return draggingTab;
	}

	ObjectProperty<Tab> droppedTabProperty()
	{
		return droppedTab;
	}

	//region private methods
	private CustomTab getTab(Predicate<CustomTab> predicate)
	{
		return this.getTabs()
				.stream()
				.map(t -> (CustomTab) t)
				.filter(predicate)
				.findFirst()
				.orElse(null);
	}

	private void addTempTabs()
	{
		this.tempTabList.clear();
		int size = this.getTabs().size();
		for (int i = 0; i < size + 1; i++)
		{
			CustomTab.TempCustomTab myTab = new CustomTab.TempCustomTab(null, this);
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

	private void removeTempTabs()
	{
		this.getTabs().removeAll(this.tempTabList);
		this.tempTabList.clear();
	}
	//endregion
}
