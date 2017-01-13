package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;

public class DialogWizard
{
	private DictionaryFx dictionary;
	private IWindow window;

	private DialogWizardController controller;

	public DialogWizard(DictionaryFx dictionary, IWindow window)
	{
		this.dictionary = dictionary;
		this.window = window;

		this.controller = Common.loadController(DialogWizard.class.getResource("DialogWizard.fxml"));
		this.controller.init(this, this.window);
	}

	public void show()
	{
		this.controller.show();
	}
}
