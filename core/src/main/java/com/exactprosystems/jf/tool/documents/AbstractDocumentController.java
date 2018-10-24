/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.documents;

import com.exactprosystems.jf.common.Settings;
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
		this.model.onSave(doc -> this.save());
		this.customTab = customTab;
		this.customTab.setContent(this.parent);
		this.customTab.setController(this);
		this.model.getNameProperty().setOnChangeListener((o, n) ->
		{
			this.customTab.setTitle(n);
			this.customTab.saved(n);
		});
	}

	protected void restoreSettings(Settings settings)
	{

	}

	protected void save()
	{

	}

	protected void close()
	{
		this.customTab.close();
		this.customTab.getTabPane().getTabs().remove(this.customTab);
	}
}
