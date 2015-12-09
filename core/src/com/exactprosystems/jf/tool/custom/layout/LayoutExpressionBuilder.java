package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.actions.gui.ActionGuiHelper;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutExpressionBuilder
{
	private LayoutExpressionBuilderController controller;
	private String parameterName;
	private String parameterExpression;
	private AppConnection appConnection;
	private IGuiDictionary dictionary;
	private String windowName;
	private AbstractEvaluator evaluator;

	public LayoutExpressionBuilder(String parameterName, String parameterExpression, AppConnection appConnection, String windowName, AbstractEvaluator evaluator)
	{
		this.parameterName = parameterName;
		this.parameterExpression = parameterExpression;
		this.appConnection = appConnection;
		this.windowName = windowName;
		this.evaluator = evaluator;
	}

	public String show(String title, boolean fullScreen) throws Exception
	{
		Map<String, Locator> map = new LinkedHashMap<>();
		ArrayList<IControl> controls = new ArrayList<>();
		if (this.appConnection != null)
		{
			this.dictionary = ActionGuiHelper.getGuiDictionary(null, this.appConnection);
			IWindow window = this.dictionary.getWindow(this.windowName);
			window.getControls(IWindow.SectionKind.Self).stream().filter(c -> !this.parameterName.equals(c.getID())).forEach(controls::add);
			window.getControls(IWindow.SectionKind.Run).stream().filter(c -> !this.parameterName.equals(c.getID())).forEach(controls::add);
		}
		else
		{
			DialogsHelper.showError("App connection is null");
			return this.parameterExpression;
		}
		this.controller = Common.loadController(LayoutExpressionBuilder.class.getResource("LayoutExpressionBuilder.fxml"));
		this.controller.init(this, this.evaluator);
		String result = this.controller.show(title, fullScreen, controls);
		return result == null ? parameterExpression : result;
	}


	public void displayControl(IControl control)
	{

	}
}
