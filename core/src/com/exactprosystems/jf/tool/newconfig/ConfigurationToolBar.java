package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api2.wizard.WizardManager;
import com.exactprosystems.jf.common.version.VersionInfo;
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
		this.wizardButton.setVisible(VersionInfo.isDevVersion());
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
	}

	//region private methods
	private ToggleButton create(CompareEnum compareEnum, ToggleGroup group, CompareEnum initEnum)
	{
		ToggleButton radioButton = new ToggleButton();
		radioButton.setToggleGroup(group);
		radioButton.setUserData(compareEnum);
		radioButton.setGraphic(imageByEnum(compareEnum));
		radioButton.setTooltip(new Tooltip("Sorting via " + compareEnum.getName().toLowerCase()));
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
