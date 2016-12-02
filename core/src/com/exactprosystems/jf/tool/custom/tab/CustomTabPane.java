package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import javafx.scene.control.TabPane;

public class CustomTabPane extends TabPane
{
	private static CustomTabPane INSTANCE;

	private Settings settings;

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
		return new CustomTab(doc, this.settings);
	}
}
