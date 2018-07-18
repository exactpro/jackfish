/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.matrix.parser.ScreenshotKind;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MatrixTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	public Parent parent;
	public ComboBox<ScreenshotKind> cbScreenshot;
	public CheckBox cbPopup;
	public CheckBox cbFoldNewItems;
	public CheckBox cbOpenReportAfterFinished;
	private SettingsPanel model;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cbScreenshot.getItems().addAll(ScreenshotKind.values());
		restoreToDefault();
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	public void init(SettingsPanel model)
	{
		this.model = model;
	}

	public void displayInfo(Map<String, String> collect)
	{
		SettingsPanel.setValue(Settings.MATRIX_DEFAULT_SCREENSHOT, collect, str -> this.cbScreenshot.getSelectionModel().select(ScreenshotKind.valueOf(str)));
		SettingsPanel.setValue(Settings.MATRIX_POPUPS, collect, str -> this.cbPopup.setSelected(Boolean.parseBoolean(str)));
		SettingsPanel.setValue(Settings.MATRIX_FOLD_ITEMS, collect, str -> this.cbFoldNewItems.setSelected(Boolean.parseBoolean(str)));
		SettingsPanel.setValue(Settings.MATRIX_OPEN_REPORT_AFTER_FINISHED, collect, str -> this.cbOpenReportAfterFinished.setSelected(Boolean.parseBoolean(str)));
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		Node node = ((AnchorPane) this.parent).getChildren().get(0);
		GridPane gridPane = (GridPane) node;
		return gridPane.getHeight();
	}

	public void save()
	{
		this.model.updateSettingsValue(Settings.MATRIX_DEFAULT_SCREENSHOT, Settings.MATRIX_NAME, this.cbScreenshot.getSelectionModel().getSelectedItem().name());
		this.model.updateSettingsValue(Settings.MATRIX_POPUPS, Settings.MATRIX_NAME, String.valueOf(this.cbPopup.isSelected()));
		this.model.updateSettingsValue(Settings.MATRIX_FOLD_ITEMS, Settings.MATRIX_NAME, String.valueOf(this.cbFoldNewItems.isSelected()));
		this.model.updateSettingsValue(Settings.MATRIX_OPEN_REPORT_AFTER_FINISHED, Settings.MATRIX_NAME, String.valueOf(this.cbOpenReportAfterFinished.isSelected()));
	}

	@Override
	public void restoreToDefault()
	{
		Settings settings = Settings.defaultSettings();

		this.cbScreenshot.getSelectionModel().select(ScreenshotKind.valueOf(settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_DEFAULT_SCREENSHOT).getValue()));
		this.cbPopup.setSelected(Boolean.parseBoolean(settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_POPUPS).getValue()));
		this.cbFoldNewItems.setSelected(Boolean.parseBoolean(settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS).getValue()));
		this.cbOpenReportAfterFinished.setSelected(Boolean.parseBoolean(settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_OPEN_REPORT_AFTER_FINISHED).getValue()));

	}

	public void restoreDefaults(ActionEvent actionEvent)
	{
		restoreToDefault();
	}
}