package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class LogTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	public Parent parent;
	private SettingsPanel model;

	public ColorPicker cpAll;
	public ColorPicker cpDebug;
	public ColorPicker cpError;
	public ColorPicker cpFatal;
	public ColorPicker cpInfo;
	public ColorPicker cpTrace;
	public ColorPicker cpWarn;
	private Map<String, ColorPicker> colorLogsMap = new HashMap<>();

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.colorLogsMap.put(Settings.FATAL, this.cpFatal);
		this.colorLogsMap.put(Settings.ERROR, this.cpError);
		this.colorLogsMap.put(Settings.WARN, this.cpWarn);
		this.colorLogsMap.put(Settings.INFO, this.cpInfo);
		this.colorLogsMap.put(Settings.DEBUG, this.cpDebug);
		this.colorLogsMap.put(Settings.TRACE, this.cpTrace);
		this.colorLogsMap.put(Settings.ALL, this.cpAll);

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

	public void displayInfo(Map<String, String> res)
	{
		res.forEach((key, value) -> this.colorLogsMap.get(key).setValue(Color.valueOf(value)));
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		return ((GridPane) ((AnchorPane) this.parent).getChildren().get(0)).getHeight();
	}

	public void save()
	{
		this.colorLogsMap.forEach((key, value) -> this.model.updateSettingsValue(key, Settings.LOGS_NAME, value.getValue().toString()));
	}

	@Override
	public void restoreToDefault()
	{
		Settings settings = Settings.defaultSettings();
		this.colorLogsMap.forEach((key, value) -> value.setValue(Color.valueOf(settings.getValue(Settings.GLOBAL_NS, Settings.LOGS_NAME, key).getValue())));
	}

	public void restoreDefaults(ActionEvent actionEvent)
	{
		restoreToDefault();
	}
}