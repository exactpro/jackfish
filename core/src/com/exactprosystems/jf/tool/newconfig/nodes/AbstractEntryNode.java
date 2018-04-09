/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.Entry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import javafx.concurrent.Service;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
		Node cacheView = cache.get(this.entry);
		if (cacheView != null)
		{
			return cacheView;
		}
		Label viewLabel = new Label();
		Text entryName = new Text(getEntryName());
		getDescription().filter(desc -> !desc.isEmpty()).map(Tooltip::new).ifPresent(viewLabel::setTooltip);
		HBox viewBox = createBox();
		ProgressIndicator indicator = createIndicator();
		if (needSupport())
		{
			viewBox.getChildren().add(indicator);
		}
		viewBox.getChildren().add(entryName);
		viewLabel.setGraphic(viewBox);
		Service<List<Node>> entryService = new EntryService(() -> {
			SupportedEntry supportedEntry = getSupportedEntry();
			ArrayList<Node> ret = new ArrayList<>();
			if (supportedEntry != null)
			{
				Image icon = new Image(CssVariables.Icons.SUPPORT_ENTRY_ICON);
				String text = "";
				if (!supportedEntry.isSupported())
				{
				    text = R.ABSTRACT_ENTRY_NODE_NOT_SUPPORTED.get();
					icon = new Image(CssVariables.Icons.UNSUPPORT_ENTRY_ICON);
				}
				Text exceptionText = new Text(text);
				exceptionText.setOpacity(0.5);
				ret.addAll(Arrays.asList(new ImageView(icon), entryName, exceptionText));
			}
			return ret;
		});
		entryService.setExecutor(service);
		entryService.setOnSucceeded(event -> {
			List<Node> value = (List<Node>) event.getSource().getValue();
			if (!value.isEmpty())
			{
				HBox box = createBox();
				box.getChildren().addAll(value);
				viewLabel.setGraphic(box);
				cache.put(this.entry, viewLabel);
			}
		});
		entryService.start();
		cache.put(this.entry, viewLabel);
		return viewLabel;
	}

	private HBox createBox()
	{
		HBox box = new HBox();
		box.setSpacing(3);
		return box;
	}

	private ProgressIndicator createIndicator()
	{
		ProgressIndicator i = new ProgressIndicator();
		i.setPrefSize(16, 16);
		i.setMinSize(16, 16);
		i.setMaxSize(16, 16);
		i.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		return i;
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
		}, String.format(R.ABSTRACT_ENTRY_NODE_ERROR.get(), value, key, entry.getClass()));
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

	protected boolean needSupport()
	{
		return true;
	}
	//endregion
}
