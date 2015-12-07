package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.tool.Common;

public class LayoutExpressionBuilder
{
	private LayoutExpressionBuilderController controller;
	
	public LayoutExpressionBuilder()
	{
		// TODO Auto-generated constructor stub
	}
	
	public String show(String initial, String title, String themePath, boolean fullScreen)
	{
		this.controller = Common.loadController(LayoutExpressionBuilder.class.getResource("LayoutExpressionBuilder.fxml"));
		this.controller.init(this);
//		this.controller.displayTree(this.document);
		String result = this.controller.show(title, themePath, fullScreen);
		return result == null ? initial : result;
	}


}
