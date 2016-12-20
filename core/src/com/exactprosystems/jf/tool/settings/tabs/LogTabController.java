package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
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

public class LogTabController implements Initializable, ContainingParent, ITabHeight
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
		this.colorLogsMap.put(this.cpFatal.getId(), this.cpFatal);
		this.colorLogsMap.put(this.cpError.getId(), this.cpError);
		this.colorLogsMap.put(this.cpWarn.getId(), this.cpWarn);
		this.colorLogsMap.put(this.cpInfo.getId(), this.cpInfo);
		this.colorLogsMap.put(this.cpDebug.getId(), this.cpDebug);
		this.colorLogsMap.put(this.cpTrace.getId(), this.cpTrace);
		this.colorLogsMap.put(this.cpAll.getId(), this.cpAll);
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
		res.entrySet().forEach(entry -> colorLogsMap.get(entry.getKey()).setValue(Color.valueOf(entry.getValue())));
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
		this.colorLogsMap.entrySet().forEach(entry -> this.model.updateSettingsValue(entry.getKey(), Settings.LOGS_NAME, entry.getValue().getValue().toString()));
	}
}