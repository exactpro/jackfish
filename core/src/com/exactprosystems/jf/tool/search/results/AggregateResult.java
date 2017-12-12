////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.search.results;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.search.Search;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class AggregateResult extends AbstractResult
{
	private List<SingleResult> list;
	private File file;
	private DocumentKind kind;
	private Search model;

	public AggregateResult(Search model, List<SingleResult> list, File file, DocumentKind kind)
	{
		this.model = model;
		this.kind = kind;
		this.list = list;
		this.file = file;
	}

	public boolean isEmpty()
	{
		return this.list == null || this.list.isEmpty();
	}

	@Override
	public Node toView()
	{
		BorderPane pane = new BorderPane();

		Label text = new Label(Common.getRelativePath(file.getPath()) + " ( " + this.list.size() + " ) ");
		text.setAlignment(Pos.CENTER_LEFT);
		text.setTextAlignment(TextAlignment.LEFT);
		HBox.setHgrow(text, Priority.ALWAYS);
		BorderPane.setAlignment(text, Pos.CENTER_LEFT);

		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_RIGHT);
		Button btnShowInTree = new Button();
		btnShowInTree.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
		btnShowInTree.setId("dictionaryBtnXpathHelper");
		btnShowInTree.setTooltip(new Tooltip(R.AGGREGATE_RESULT_SCROLL.get()));
		btnShowInTree.setOnAction(e -> this.model.scrollFromConfig(file));

		Button btnOpenAsPlainText = new Button();
		btnOpenAsPlainText.setId("btnOpenAsPlainText");
		btnOpenAsPlainText.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
		btnOpenAsPlainText.setTooltip(new Tooltip(R.AGGREGATE_RESULT_OPEN_AS_PLAIN.get()));
		btnOpenAsPlainText.setOnAction(e -> this.model.openAsPlainText(file));

		boolean needAdd = true;
		Consumer<File> consumer = null;

		switch (this.kind)
		{
			case MATRIX:
			case LIBRARY:
				consumer = this.model::openAsMatrix;
				break;
			case GUI_DICTIONARY:
				consumer = this.model::openAsGuiDic;
				break;
			case SYSTEM_VARS:
				consumer = this.model::openAsVars;
				break;
			case REPORTS:
				if (file.getName().endsWith(".html"))
				{
					consumer = this.model::openAsHtml;
				}
				break;
			default:
				needAdd = false;
		}
		if (needAdd)
		{
			Button btnOpenAsDocument = new Button();
			btnOpenAsDocument.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
			btnOpenAsDocument.setId("btnOpenAsDocument");
			btnOpenAsDocument.setTooltip(new Tooltip(R.AGGREGATE_RESULT_OPEN_AS_DOC.get()));
			Consumer<File> finalConsumer = consumer;
			btnOpenAsDocument.setOnAction(e -> finalConsumer.accept(file));
			box.getChildren().addAll(btnOpenAsDocument, Common.createSpacer(Common.SpacerEnum.HorizontalMin));
		}
		box.getChildren().addAll(btnOpenAsPlainText, new Separator(Orientation.VERTICAL), btnShowInTree);

		pane.setCenter(text);
		pane.setRight(box);
		return pane;
	}

	public List<SingleResult> getList()
	{
		return list;
	}
}
