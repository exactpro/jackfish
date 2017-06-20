package com.exactprosystems.jf.tool.custom.wizard;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.exception.ArrayUtils;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import java.util.List;
import java.util.function.Supplier;

public class WizardButton extends Button
{
	public WizardButton()
	{
		super();
        this.setTooltip(new Tooltip("Wizards"));
		this.setId("btnWizard");
		this.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
	}

	public void initButton(IContext context, WizardManager manager, Supplier<Object[]> criteriesSupplier, Supplier<Object[]> parameterSupplier)
	{
	    this.setOnAction(e ->
	    {
	        Object[] criteries = criteriesSupplier.get();
	        List<Class<? extends Wizard>> suitable  = manager.suitableWizards(criteries);
	        if (suitable.isEmpty())
	        {
	            DialogsHelper.showInfo("No one wizard is accesible here");
	            return;
	        }
	        Class<? extends Wizard> wizardClass = DialogsHelper.selectFromList("Choose wizard", null, suitable, wizard ->  manager.nameOf(wizard));
	        Object[] parameters = criteries;
 	        if (parameterSupplier != null)
	        {
 	           parameters = ArrayUtils.concat(criteries, parameterSupplier.get());
	        }
            manager.runWizard(wizardClass, context, parameters);
	    });
	}
}
