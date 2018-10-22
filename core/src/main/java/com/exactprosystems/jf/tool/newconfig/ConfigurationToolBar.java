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

package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.wizard.WizardButton;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationToolBar extends ToolBar
{
	private ConfigurationFx model;

//	private SplitMenuButton sortButton;
	private WizardButton    wizardButton;
	private Button 			undoBtn;
	private Button 			redoBtn;

	public ConfigurationToolBar(ConfigurationFx model, CompareEnum compareEnum)
	{
		this.model = model;

//		this.sortButton = new SplitMenuButton();
//		this.sortButton.setGraphic(imageByEnum(compareEnum));
		ToggleGroup toggleGroup = new ToggleGroup();

		this.getItems().addAll(
				Arrays.stream(CompareEnum.values())
						.map(e -> create(e, toggleGroup, compareEnum))
						.collect(Collectors.toList())
		);

		this.wizardButton = WizardButton.smallButton();
		this.getItems().addAll(new Separator(Orientation.VERTICAL), this.wizardButton);

		Context context = model.getFactory().createContext();
		WizardManager manager = model.getFactory().getWizardManager();

		this.wizardButton.initButton(context, manager, () -> new Object[]{model}, null);

		toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				Common.tryCatch(() -> this.model.changeSortType((CompareEnum) newValue.getUserData()), "");
			}
		});

		this.redoBtn = new Button();
		this.redoBtn.setId("btnSmallRedo");
		this.redoBtn.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
		this.redoBtn.setOnAction(event -> this.model.redo());

		this.undoBtn = new Button();
		this.undoBtn.setId("btnSmallUndo");
		this.undoBtn.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
		this.undoBtn.setOnAction(event -> this.model.undo());

		this.getItems().addAll(undoBtn, redoBtn);
	}

	//region private methods
	private ToggleButton create(CompareEnum compareEnum, ToggleGroup group, CompareEnum initEnum)
	{
		ToggleButton radioButton = new ToggleButton();
		radioButton.setToggleGroup(group);
		radioButton.setUserData(compareEnum);
		radioButton.setGraphic(imageByEnum(compareEnum));
		radioButton.setTooltip(new Tooltip(String.format(R.CONFIG_TOOL_BAR_SORTING_VIA.get(), compareEnum.getName().toLowerCase())));
		if (compareEnum == initEnum)
		{
			radioButton.setSelected(true);
		}
		return radioButton;
	}

	private ImageView imageByEnum(CompareEnum compareEnum)
	{
		switch (compareEnum)
		{
			case ALPHABET_0_1: return new ImageView(new Image(CssVariables.Icons.NAME_DESCENDING));
			case ALPHABET_1_0: return new ImageView(new Image(CssVariables.Icons.NAME_ASCENDING));
			case DATE_0_1:     return new ImageView(new Image(CssVariables.Icons.DATE_ASCENDING));
			case DATE_1_0:     return new ImageView(new Image(CssVariables.Icons.DATE_DESCENDING));
		}
		return null;
	}
	//endregion

}
