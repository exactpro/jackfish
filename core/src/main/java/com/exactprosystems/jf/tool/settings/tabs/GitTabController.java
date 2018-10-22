/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.api.common.i18n.R;
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
			File file = DialogsHelper.showOpenSaveDialog(R.GIT_TAB_CONTR_CHOOSE_HOST.get(), R.COMMON_ALL_FILES.get(), "*", DialogsHelper.OpenSaveMode.OpenFile);
			Optional.ofNullable(file).map(File::getAbsolutePath).ifPresent(this.cfKnownHost::setText);
		});
		this.gridGit.add(this.cfKnownHost, 1, 0);
		this.cfSSHIdentity = new CustomFieldWithButton();
		this.cfSSHIdentity.setButtonText("...");
		this.cfSSHIdentity.setHandler(e -> {
			File file = DialogsHelper.showOpenSaveDialog(R.GIT_TAB_CONTR_CHOOSE_SSH.get(),  R.COMMON_ALL_FILES.get(), "*", DialogsHelper.OpenSaveMode.OpenFile);
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