package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.wizard.WizardButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationToolBar extends ToolBar
{
	private ConfigurationFx model;

	private SplitMenuButton sortButton;
	private WizardButton    wizardButton;

	public ConfigurationToolBar(ConfigurationFx model, CompareEnum compareEnum)
	{
		this.model = model;

		this.sortButton = new SplitMenuButton();
		this.sortButton.setGraphic(imageByEnum(compareEnum));
		ToggleGroup toggleGroup = new ToggleGroup();

		this.sortButton.getItems().addAll(
				Arrays.stream(CompareEnum.values())
						.map(e -> create(e, toggleGroup, compareEnum))
						.collect(Collectors.toList())
		);

		this.getItems().add(this.sortButton);

		this.wizardButton = WizardButton.smallButton();
		this.wizardButton.setVisible(VersionInfo.isDevVersion());
		this.getItems().add(this.wizardButton);

		Context context = model.getFactory().createContext();
		WizardManager manager = model.getFactory().getWizardManager();

		this.wizardButton.initButton(context, manager, () -> new Object[]{model}, null);

		toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				CompareEnum userData = (CompareEnum) newValue.getUserData();
				this.sortButton.setGraphic(imageByEnum(userData));
				Common.tryCatch(() -> this.model.changeSortType(userData), "");
			}
		});
	}

	//region private methods
	private RadioMenuItem create(CompareEnum compareEnum, ToggleGroup group, CompareEnum initEnum)
	{
		RadioMenuItem item = new RadioMenuItem();
		item.setToggleGroup(group);
		item.setUserData(compareEnum);
		item.setGraphic(imageByEnum(compareEnum));
		if (compareEnum == initEnum)
		{
			item.setSelected(true);
		}
		return item;
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
