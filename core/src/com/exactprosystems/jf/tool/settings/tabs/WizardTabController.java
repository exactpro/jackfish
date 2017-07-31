package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.wizard.WizardSettings;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class WizardTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	public Parent parent;
	public TextField tfTypeMin;
	public TextField tfTypeMax;
	public TextField tfPathMin;
	public TextField tfPathMax;
	public TextField tfSizeMin;
	public TextField tfSizeMax;
	public TextField tfPositionMin;
	public TextField tfPositionMax;
	public TextField tfAttrMin;
	public TextField tfAttrMax;
	public TextField tfThreshold;

	public GridPane gridWizard;

	private SettingsPanel model;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
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
		if (collect.get(WizardSettings.Kind.TYPE.name() + Settings.MIN) != null) this.tfTypeMin.setText(collect.get(WizardSettings.Kind.TYPE.name() + Settings.MIN));
		if (collect.get(WizardSettings.Kind.TYPE.name() + Settings.MAX) != null) this.tfTypeMax.setText(collect.get(WizardSettings.Kind.TYPE.name() + Settings.MAX));

		if (collect.get(WizardSettings.Kind.PATH.name() + Settings.MIN) != null) this.tfPathMin.setText(collect.get(WizardSettings.Kind.PATH.name() + Settings.MIN));
		if (collect.get(WizardSettings.Kind.PATH.name() + Settings.MAX) != null) this.tfPathMax.setText(collect.get(WizardSettings.Kind.PATH.name() + Settings.MAX));

		if (collect.get(WizardSettings.Kind.SIZE.name() + Settings.MIN) != null) this.tfSizeMin.setText(collect.get(WizardSettings.Kind.SIZE.name() + Settings.MIN));
		if (collect.get(WizardSettings.Kind.SIZE.name() + Settings.MAX) != null) this.tfSizeMax.setText(collect.get(WizardSettings.Kind.SIZE.name() + Settings.MAX));

		if (collect.get(WizardSettings.Kind.POSITION.name() + Settings.MIN) != null) this.tfPositionMin.setText(collect.get(WizardSettings.Kind.POSITION.name() + Settings.MIN));
		if (collect.get(WizardSettings.Kind.POSITION.name() + Settings.MAX) != null) this.tfPositionMax.setText(collect.get(WizardSettings.Kind.POSITION.name() + Settings.MAX));

		if (collect.get(WizardSettings.Kind.ATTR.name() + Settings.MIN) != null) this.tfAttrMin.setText(collect.get(WizardSettings.Kind.ATTR.name() + Settings.MIN));
		if (collect.get(WizardSettings.Kind.ATTR.name() + Settings.MAX) != null) this.tfAttrMax.setText(collect.get(WizardSettings.Kind.ATTR.name() + Settings.MAX));

		if (collect.get(Settings.THRESHOLD) != null) this.tfThreshold.setText(collect.get(Settings.THRESHOLD));
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
		this.model.updateSettingsValue(WizardSettings.Kind.TYPE.name() + Settings.MAX, Settings.WIZARD_NAME, this.tfTypeMax.getText());
		this.model.updateSettingsValue(WizardSettings.Kind.TYPE.name() + Settings.MIN, Settings.WIZARD_NAME, this.tfTypeMin.getText());

		this.model.updateSettingsValue(WizardSettings.Kind.PATH.name() + Settings.MAX, Settings.WIZARD_NAME, this.tfPathMax.getText());
		this.model.updateSettingsValue(WizardSettings.Kind.PATH.name() + Settings.MIN, Settings.WIZARD_NAME, this.tfPathMin.getText());

		this.model.updateSettingsValue(WizardSettings.Kind.SIZE.name() + Settings.MAX, Settings.WIZARD_NAME, this.tfSizeMax.getText());
		this.model.updateSettingsValue(WizardSettings.Kind.SIZE.name() + Settings.MIN, Settings.WIZARD_NAME, this.tfSizeMin.getText());

		this.model.updateSettingsValue(WizardSettings.Kind.POSITION.name() + Settings.MAX, Settings.WIZARD_NAME, this.tfPositionMax.getText());
		this.model.updateSettingsValue(WizardSettings.Kind.POSITION.name() + Settings.MIN, Settings.WIZARD_NAME, this.tfPositionMin.getText());

		this.model.updateSettingsValue(WizardSettings.Kind.ATTR.name() + Settings.MAX, Settings.WIZARD_NAME, this.tfAttrMax.getText());
		this.model.updateSettingsValue(WizardSettings.Kind.ATTR.name() + Settings.MIN, Settings.WIZARD_NAME, this.tfAttrMin.getText());

		this.model.updateSettingsValue(Settings.THRESHOLD, Settings.WIZARD_NAME, this.tfThreshold.getText());
	}

	public void restoreDefaults(ActionEvent event)
	{
		this.restoreToDefault();
	}

	@Override
	public void restoreToDefault()
	{
		Settings defaultSettings = Settings.defaultSettings();
		this.tfTypeMin.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.TYPE.name() + Settings.MIN).getValue());
		this.tfTypeMax.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.TYPE.name() + Settings.MAX).getValue());

		this.tfPathMin.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.PATH.name() + Settings.MIN).getValue());
		this.tfPathMax.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.PATH.name() + Settings.MAX).getValue());

		this.tfSizeMin.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.SIZE.name() + Settings.MIN).getValue());
		this.tfSizeMax.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.SIZE.name() + Settings.MAX).getValue());

		this.tfPositionMin.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.POSITION.name() + Settings.MIN).getValue());
		this.tfPositionMax.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.POSITION.name() + Settings.MAX).getValue());

		this.tfAttrMin.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.ATTR.name() + Settings.MIN).getValue());
		this.tfAttrMax.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, WizardSettings.Kind.ATTR.name() + Settings.MAX).getValue());

		this.tfThreshold.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.WIZARD_NAME, Settings.THRESHOLD).getValue());
	}
}