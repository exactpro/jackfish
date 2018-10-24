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
package com.exactprosystems.jf.tool.newconfig.wizard;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class WizardConfigurationController implements Initializable, ContainingParent
{

	public CheckBox cbMatrixFolder;
	public CheckBox cbLibraryFolder;
	public CheckBox cbAppFolder;
	public CheckBox cbClientFolder;
	public CheckBox cbVariablesFolder;
	public CheckBox cbReportFolder;
	public CustomFieldWithButton cfChooseFolder;
	public TextField tfProjectName;

	private WizardConfiguration model;
	private Parent parent;
	private BooleanProperty folderExist = new SimpleBooleanProperty(false);
	private BooleanProperty validFile = new SimpleBooleanProperty(false);

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cfChooseFolder.setButtonText("...");
		this.cfChooseFolder.setHandler(event -> {
			File file = DialogsHelper.showDirChooseDialog(R.WIZARD_CC_CHOOSE_FOLDER.get(), this.cfChooseFolder.getText());
			Optional.ofNullable(file).ifPresent(f -> {
				this.model.setFolderDir(f);
				this.cfChooseFolder.setText(f.getAbsolutePath());
			});
		});
		this.cfChooseFolder.textProperty().addListener((observable, oldValue, newValue) -> {
			this.cfChooseFolder.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
			if (newValue != null && !newValue.isEmpty())
			{
				folderExist.setValue(true);
				if (!new File(newValue).exists())
				{
					this.cfChooseFolder.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					folderExist.setValue(false);
				}
			}
			else
			{
				folderExist.setValue(false);
			}
		});
		this.tfProjectName.textProperty().addListener((observable, oldValue, newValue) -> {
			this.tfProjectName.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
			if (newValue != null && !newValue.isEmpty())
			{
				validFile.setValue(true);
				if (new File(cfChooseFolder.getText() + File.separator + newValue).exists())
				{
					this.tfProjectName.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					validFile.setValue(false);
				}
			}
			else
			{
				validFile.setValue(false);
			}
		});
		this.cbMatrixFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateMatrixDir(newValue));
		this.cbLibraryFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateLibraryDir(newValue));
		this.cbAppFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateAppDicDir(newValue));
		this.cbClientFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateClientDicDir(newValue));
		this.cbVariablesFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateVarsDir(newValue));
		this.cbReportFolder.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.setCreateReportDir(newValue));
	}
	//endregion

	public void init(WizardConfiguration model)
	{
		this.model = model;
	}

	public Boolean display()
	{
		Dialog<Boolean> dialog = new Dialog<>();
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.setTitle(R.WIZARD_CC_DISPLAY_TITLE.get());
//		dialog.getDialogPane().setHeader(new Label());
		dialog.getDialogPane().setContent(this.parent);
		dialog.setResultConverter(param -> {
			this.model.setFolderDir(new File(this.cfChooseFolder.getText()));
			this.model.setNewProjectName(this.tfProjectName.getText());
			return !param.getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE);
		});

		ButtonType buttonCreate = new ButtonType(R.WIZARD_CC_BUTTON_CREATE.get(), ButtonBar.ButtonData.OK_DONE);
		ButtonType buttonClose = new ButtonType(R.WIZARD_CC_BUTTON_CANCEL.get(), ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(buttonCreate, buttonClose);
		Node btnCreate = dialog.getDialogPane().lookupButton(buttonCreate);
		ButtonBar.setButtonData(btnCreate, ButtonBar.ButtonData.OTHER);
		btnCreate.disableProperty().bind(folderExist.not().or(validFile.not()));

		return dialog.showAndWait().orElse(false);
	}
}
