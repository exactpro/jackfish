package com.exactprosystems.jf.tool.custom.wizard;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.Button;

import java.util.function.Supplier;

public class WizardButton extends Button
{
	public WizardButton()
	{
		super();
		this.setId("btnWizard");
		this.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
	}

	public void initButton(IContext context, WizardManager manager, Supplier<Object[]> criteries)
	{
		this.setOnAction(e -> manager.runWizardDefault(context, criteries
					, (suitableWizards) -> DialogsHelper.selectFromList("Choose suitable wizard", null, suitableWizards, manager::nameOf)
					, DialogsHelper::showInfo)
		);
	}
}
