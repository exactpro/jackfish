package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.matrix.parser.ScreenshotKind;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MatrixTabController implements Initializable, ContainingParent, ITabHeight
{
	public Parent parent;
	public ComboBox<ScreenshotKind> cbScreenshot;
	private SettingsPanel model;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cbScreenshot.getItems().addAll(ScreenshotKind.values());
		this.cbScreenshot.getSelectionModel().select(ScreenshotKind.Never);
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
		this.cbScreenshot.getSelectionModel().select(ScreenshotKind.valueOf(collect.getOrDefault(Settings.MATRIX_DEFAULT_SCREENSHOT, ScreenshotKind.Never.name())));
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
	}
}