////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Optional;

public abstract class AbstractEntryNode<T extends Configuration.Entry> extends TreeNode
{
	private ConfigurationFxNew model;
	private T entry;

	public AbstractEntryNode(ConfigurationFxNew model, T entry)
	{
		this.model = model;
		this.entry = entry;
	}

	protected String getEntryName()
	{
		return this.entry.toString();
	}

	protected Optional<String> getDescription()
	{
		return Optional.empty();
	}


	@Override
	public Node getView()
	{
		SupportedEntry supportedEntry = getSupportedEntry();
		Text name = new Text(getEntryName());
		Label lbl = new Label();
		getDescription().filter(desc -> !desc.isEmpty()).ifPresent(tt -> lbl.setTooltip(new Tooltip(tt)));
		if (supportedEntry == null)
		{
			lbl.setGraphic(name);
			return lbl;
		}
		else
		{
			Image icon = new Image(CssVariables.Icons.SUPPORT_ENTRY_ICON);
			String text = "";
			if (!supportedEntry.isSupported())
			{
				icon = new Image(CssVariables.Icons.UNSUPPORT_ENTRY_ICON);
				if (supportedEntry.getRequaredMajorVersion() == -1 && supportedEntry.getRequaredMinorVersion() == -1)
				{
					text = "error on load";
				}
				else
				{
					text = "<required " + supportedEntry.getRequaredMajorVersion() + "." + supportedEntry.getRequaredMinorVersion() + ">";
				}
			}
			HBox box = new HBox();
			box.setSpacing(3);
			Text exceptionText = new Text(text);
			exceptionText.setOpacity(0.5);
			box.getChildren().addAll(new ImageView(icon), name, exceptionText);
			lbl.setGraphic(box);
			return lbl;
		}
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.empty();
	}

	@Override
	public void updateParameter(String key, String value)
	{
		Common.tryCatch(() -> this.model.changeEntry(entry, key, value), String.format("Error on set value of '%s' to parameter '%s' on class '%s'", value, key, entry.getClass()));
	}

	public T getEntry()
	{
		return entry;
	}

	protected abstract SupportedEntry getSupportedEntry();
}
