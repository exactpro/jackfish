////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.documents.config.Entry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import javafx.concurrent.Service;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEntryNode<T extends Entry> extends TreeNode
{
	private static ExecutorService service = Executors.newSingleThreadExecutor();
	private static Map<Entry, Node> cache = new HashMap<>();

	private ConfigurationFx model;
	private T entry;

	public AbstractEntryNode(ConfigurationFx model, T entry)
	{
		this.model = model;
		this.entry = entry;
	}

	public T getEntry()
	{
		return entry;
	}

	//region TreeNode methods
	@Override
	public Node getView()
	{
		Node view = cache.get(this.entry);
		if (view != null)
		{
			return view;
		}
		Label lbl = new Label();
		Text name = new Text(getEntryName());
		getDescription().filter(desc -> !desc.isEmpty()).map(Tooltip::new).ifPresent(lbl::setTooltip);
		lbl.setGraphic(name);
		Service<List<Node>> entryService = new EntryService(() -> {
			SupportedEntry supportedEntry = getSupportedEntry();
			ArrayList<Node> ret = new ArrayList<>();
			if (supportedEntry != null)
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
				Text exceptionText = new Text(text);
				exceptionText.setOpacity(0.5);
				ret.addAll(Arrays.asList(new ImageView(icon), name, exceptionText));
			}
			return ret;
		});
		entryService.setExecutor(service);
		entryService.setOnSucceeded(event -> {
			List<Node> value = (List<Node>) event.getSource().getValue();
			if (!value.isEmpty())
			{
				HBox box = new HBox();
				box.setSpacing(3);
				box.getChildren().addAll(value);
				lbl.setGraphic(box);
				cache.put(this.entry, lbl);
			}
		});
		entryService.start();
		return lbl;
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.empty();
	}

	@Override
	public void updateParameter(String key, String value)
	{
		Common.tryCatch(() -> {
			this.model.changeEntry(entry, key, value);
			cache.remove(this.entry);
			System.out.println("entry was changed " + entry);
		}, String.format("Error on set value of '%s' to parameter '%s' on class '%s'", value, key, entry.getClass()));
	}
	//endregion

	//region Protected methods
	protected final SupportedEntry getSupportedEntry()
	{
		return this.model.getSupportedEntry(this.entry);
	}

	protected String getEntryName()
	{
		return this.entry.toString();
	}

	protected Optional<String> getDescription()
	{
		return Optional.empty();
	}
	//endregion
}
