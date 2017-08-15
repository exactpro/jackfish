////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents;

import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractDocumentController<T extends Document> implements Initializable, ContainingParent
{
	protected CustomTab customTab;
	protected T         model;
	protected Parent    parent;

	public AbstractDocumentController()
	{
	}

	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	@SuppressWarnings("unchecked")
	protected void init(Document model, CustomTab customTab)
	{
		this.model = (T) model;
		this.customTab = customTab;
		this.customTab.setContent(this.parent);
		this.model.getNameProperty().setOnChangeListener((o, n) ->
		{
			this.customTab.setTitle(n);
			this.customTab.saved(n);
		});
	}

	protected void close()
	{
		this.customTab.close();
		this.customTab.getTabPane().getTabs().remove(this.customTab);
	}
}
