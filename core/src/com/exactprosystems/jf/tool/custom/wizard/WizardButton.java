package com.exactprosystems.jf.tool.custom.wizard;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.Button;

import java.util.List;

public class WizardButton extends Button
{
	public WizardButton()
	{
		super();
		this.setId("btnWizard");
		this.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
	}

	public void initButton(IContext context, WizardManager manager, Object... criteries)
	{
		this.setOnAction(e -> {
			List<Class<? extends Wizard>> suitableWizards = manager.suitableWizards(criteries);
			System.err.println("Found suitable wizards : " + suitableWizards.size());
			Class<? extends Wizard> suitableWizard;
			if (suitableWizards.size() > 1)
			{
				suitableWizard = DialogsHelper.selectFromList("Choose suitable wizard", null, suitableWizards, manager::nameOf);
			}
			else if (suitableWizards.size() == 0)
			{
				DialogsHelper.showInfo("Not found suitable wizards");
				return;
			}
			else
			{
				suitableWizard = suitableWizards.get(0);
			}
			System.err.println("Run wizard ... ");
			manager.runWizard(suitableWizard, context, criteries);
		});
	}
}
