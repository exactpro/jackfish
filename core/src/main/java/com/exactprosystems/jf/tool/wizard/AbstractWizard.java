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

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.api.wizard.WizardResult;
import com.exactprosystems.jf.documents.config.Context;

import javafx.scene.layout.BorderPane;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractWizard implements Wizard
{
	private WizardDialog dialog;

	protected Context      context;
	protected WizardManager wizardManager;

	@Override
	public void init(Context context, WizardManager wizardManager, Object... parameters)
	{
		this.context = context;
		this.wizardManager = wizardManager;
	}

	@Override
	public WizardResult run()
	{
	    this.dialog = new WizardDialog(this, this.context);
	    initDialog(this.dialog.getPane());
		this.dialog.expandTitle(getTitle());
	    boolean succeed = this.dialog.showAndWait().orElse(false);

	    if (succeed)
	    {
	        Supplier<List<WizardCommand> > resultSupplier = getCommands();
	        return WizardResult.submit(resultSupplier.get());
	    }
	    onRefused();

		return  WizardResult.deny();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public WizardManager manager()
	{
		return this.wizardManager;
	}

	@SuppressWarnings("unchecked")
	protected <T> T get(Class<T> clazz, Object[] parameters)
	{
		if (parameters == null)
		{
			return null;
		}

		Object res = Arrays.stream(parameters).filter(p -> p != null && clazz.isAssignableFrom(p.getClass())).findFirst().orElse(null);

		return (T) res;
	}
	
    protected abstract void initDialog(BorderPane borderPane);

    protected String getTitle() 
    {
        return "";
    }

    protected abstract Supplier<List<WizardCommand> > getCommands();

	protected void onRefused() {}

	protected final void closeDialog()
	{
		if (this.dialog != null)
		{
			this.dialog.setResult(false);
			this.dialog.close();
		}
	}
}

