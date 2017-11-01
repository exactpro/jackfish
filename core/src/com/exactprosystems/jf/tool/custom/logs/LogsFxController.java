////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.logs;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LogsFxController implements Initializable, ContainingParent
{
	@FXML
	private BorderPane     borderPane;
	@FXML
	private ComboBox<File> cbFiles;
	@FXML
	private VBox           topVBox;

	private InlineCssTextArea               consoleArea;
	private LogsFx                          model;
	private Dialog                          dialog;
	private FindPanel<LogsFx.LineWithStyle> findPanel;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.findPanel = new FindPanel<>();
		this.findPanel.getStyleClass().remove(CssVariables.FIND_PANEL);
		this.topVBox.getChildren().add(this.findPanel);

		this.consoleArea = new InlineCssTextArea();
		this.consoleArea.setParagraphGraphicFactory(LineNumberFactory.get(this.consoleArea));
		this.consoleArea.setEditable(false);
		this.borderPane.setCenter(new VirtualizedScrollPane<>(this.consoleArea));
		BorderPane.setMargin(this.consoleArea, new Insets(10, 0, 0, 0));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		Common.addIcons(((Stage) this.dialog.getDialogPane().getScene().getWindow()));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().setPrefWidth(600);
		this.dialog.getDialogPane().setPrefHeight(600);
		this.dialog.setTitle(R.LOGS_LOGS.get());
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.getDialogPane().setContent(parent);
		this.dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		this.cbFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.model.displayLines(newValue);
			}
		});
	}

	void init(LogsFx model)
	{
		this.model = model;
		this.findPanel.setListener(new IFind<LogsFx.LineWithStyle>()
		{
			@Override
			public void find(LogsFx.LineWithStyle line)
			{
				model.find(line);
			}

			@Override
			public List<LogsFx.LineWithStyle> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				return model.findItem(what, matchCase, wholeWord);
			}
		});
	}

	void show()
	{
		this.dialog.show();
	}

	void displayLines(List<LogsFx.LineWithStyle> list)
	{
		for (LogsFx.LineWithStyle line : list)
		{
			int start = this.consoleArea.getLength();
			this.consoleArea.appendText(line.getLine() + "\n");
			this.consoleArea.setStyle(start, this.consoleArea.getLength(), "-fx-fill: " + Common.colorToString(line.getStyle()));
		}
	}

	void clearListView()
	{
		this.consoleArea.clear();
	}

	void clearFiles()
	{
		this.cbFiles.getItems().clear();
	}

	void displayFiles(List<File> files)
	{
		this.cbFiles.getItems().setAll(files);
		this.cbFiles.getSelectionModel().selectFirst();
	}

	@FXML
	private void refresh(ActionEvent actionEvent)
	{
		this.model.refresh();
	}

	void clearAndSelect(int index)
	{
		this.consoleArea.moveTo(index, 0);
		this.consoleArea.setEstimatedScrollY(this.consoleArea.getTotalHeightEstimate() / this.consoleArea.getParagraphs().size() * index);
	}
}
