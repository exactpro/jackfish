package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.wizard.WizardButton;

import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationToolBar extends ToolBar
{
	private ConfigurationFx model;

	private SplitMenuButton sortButton;
    private WizardButton wizardButton;

	public ConfigurationToolBar(ConfigurationFx model, CompareEnum compareEnum){
		this.model = model;

		this.sortButton = new SplitMenuButton();
		this.sortButton.setText("Sort by");

		ToggleGroup toggleGroup = new ToggleGroup();

		this.sortButton.getItems().addAll(
				Arrays.stream(CompareEnum.values())
						.map(e -> create(e, toggleGroup, compareEnum))
						.collect(Collectors.toList())
		);

		this.getItems().add(this.sortButton);

        this.wizardButton = new WizardButton();
        this.wizardButton.setVisible(VersionInfo.isDevVersion());
        this.getItems().add(this.wizardButton);
        
        Context context = model.getFactory().createContext();
        WizardManager manager = model.getFactory().getWizardManager();
        
        this.wizardButton.initButton(context, manager, () -> new Object[] { model } );
		
		
		toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				Common.tryCatch(() -> this.model.changeSortType(((CompareEnum) newValue.getUserData())), "");
			}
		});
	}

	//region private methods
	private RadioMenuItem create(CompareEnum compareEnum, ToggleGroup group, CompareEnum initEnum)
	{
		RadioMenuItem item = new RadioMenuItem(compareEnum.getName());
		item.setToggleGroup(group);
		item.setUserData(compareEnum);
		if (compareEnum == initEnum)
		{
			item.setSelected(true);
		}
		return item;
	}
	//endregion

}
