package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	public Parent parent;
	private SettingsPanel model;

	public GridPane gridGit;
	public CustomFieldWithButton cfKnownHost;
	public CustomFieldWithButton cfSSHIdentity;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cfKnownHost = new CustomFieldWithButton();
		this.cfKnownHost.setButtonText("...");
		this.cfKnownHost.setHandler(e -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose known host file", "All files", "*", DialogsHelper.OpenSaveMode.OpenFile);
			Optional.ofNullable(file).map(File::getAbsolutePath).ifPresent(this.cfKnownHost::setText);
		});
		this.gridGit.add(this.cfKnownHost, 1, 0);
		this.cfSSHIdentity = new CustomFieldWithButton();
		this.cfSSHIdentity.setButtonText("...");
		this.cfSSHIdentity.setHandler(e -> {
			File file = DialogsHelper.showOpenSaveDialog("Choose ssh identity file", "All files", "*", DialogsHelper.OpenSaveMode.OpenFile);
			Optional.ofNullable(file).map(File::getAbsolutePath).ifPresent(this.cfSSHIdentity::setText);
		});
		this.gridGit.add(this.cfSSHIdentity, 1, 1);

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
		SettingsPanel.setValue(Settings.GIT_KNOWN_HOST, collect, this.cfKnownHost::setText);
		SettingsPanel.setValue(Settings.GIT_SSH_IDENTITY, collect, this.cfSSHIdentity::setText);
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
		this.model.updateSettingsValue(Settings.GIT_KNOWN_HOST, Settings.GIT, this.cfKnownHost.getText());
		this.model.updateSettingsValue(Settings.GIT_SSH_IDENTITY, Settings.GIT, this.cfSSHIdentity.getText());
	}

	@Override
	public void restoreToDefault()
	{
		Settings settings = Settings.defaultSettings();

		this.cfKnownHost.setText(settings.getValue(Settings.GLOBAL_NS, Settings.GIT, Settings.GIT_KNOWN_HOST).getValue());
		this.cfSSHIdentity.setText(settings.getValue(Settings.GLOBAL_NS, Settings.GIT, Settings.GIT_SSH_IDENTITY).getValue());
	}

	public void restoreDefaults(ActionEvent actionEvent)
	{
		restoreToDefault();
	}
}