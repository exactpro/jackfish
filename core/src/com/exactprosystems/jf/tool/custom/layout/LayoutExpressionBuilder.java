package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.actions.gui.ActionGuiHelper;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class LayoutExpressionBuilder
{
	private LayoutExpressionBuilderController controller;
	private String parameterName;
	private String parameterExpression;
	private AppConnection appConnection;
	private IWindow currentWindow;
	private String windowName;
	private AbstractEvaluator evaluator;

	private double xOffset = 0;
	private double yOffset = 0;

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
		ArrayList<IControl> controls = new ArrayList<>();
		BufferedImage image;
		if (this.appConnection != null)
		{
			IGuiDictionary dictionary = ActionGuiHelper.getGuiDictionary(null, this.appConnection);
			this.currentWindow = dictionary.getWindow(this.windowName);
			this.currentWindow.getControls(IWindow.SectionKind.Self).stream().filter(c -> !this.parameterName.equals(c.getID())).forEach(controls::add);
			this.currentWindow.getControls(IWindow.SectionKind.Run).stream().filter(c -> !this.parameterName.equals(c.getID())).forEach(controls::add);
			Locator selfLocator;
			IControl selfControl = this.currentWindow.getSelfControl();
			if (selfControl == null)
			{
				throw new NullPointerException(String.format("Can't get screenshot, because self section on window %s don't contains controls", this.windowName));
			}
			selfLocator = selfControl.locator();
			image = service().getImage(null, selfLocator).getImage();
			IControl ownerSelf = this.currentWindow.getOwnerControl(selfControl);
			Rectangle selfRectangle = service().getRectangle(ownerSelf == null ? null : ownerSelf.locator(), selfLocator);
			this.xOffset = selfRectangle.getX();
			this.yOffset = selfRectangle.getY();
		}
		else
		{
			DialogsHelper.showError("App connection is null");
			return this.parameterExpression;
		}
		this.controller = Common.loadController(LayoutExpressionBuilder.class.getResource("LayoutExpressionBuilder.fxml"));
		this.controller.init(this, this.evaluator, image);
		displayInitialControl();
		String result = this.controller.show(title, fullScreen, controls);
		return result == null ? parameterExpression : result;
	}

	public void displayControl(IControl control)
	{
		Optional.ofNullable(control).ifPresent(c -> Common.tryCatch(() -> {
			IControl owner = this.currentWindow.getOwnerControl(control);
			Rectangle rectangle = service().getRectangle(owner == null ? null : owner.locator(), control.locator());
			rectangle.setRect(rectangle.getX() - this.xOffset, rectangle.getY() - this.yOffset, rectangle.getWidth(), rectangle.getHeight());
			this.controller.displayControl(rectangle);
			this.controller.displayControlId(control.getID());
		}, String.format("Error on display control %s", c)));

	}

	public void clearCanvas()
	{
		this.controller.clearCanvas();
		this.controller.displayControlId("");
	}

	private void displayInitialControl() throws Exception
	{
		IControl initialControl = this.currentWindow.getControlForName(null, this.parameterName);
		if (initialControl == null)
		{
			throw new NullPointerException(String.format("Control with name %s not found in window with name %s", this.parameterName, this.windowName));
		}

		IControl ownerControl = this.currentWindow.getOwnerControl(initialControl);
		Locator owner = ownerControl == null ? null : ownerControl.locator();
		Rectangle rectangle = service().getRectangle(owner, initialControl.locator());
		rectangle.setRect(rectangle.getX() - this.xOffset, rectangle.getY() - this.yOffset, rectangle.getWidth(), rectangle.getHeight());
		this.controller.displayInitialControl(rectangle);
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	public void addFormula(String parameter, String controlId, Range range, String first, String second)
	{
		Arrays.stream(all).filter(a -> a.name.equals(parameter)).forEach(a -> 
			System.err.println(a.toString(controlId, range, first, second)));
	}
	
	
	static class Assembler
	{	
		public Assembler(String name, boolean needStr, boolean needRange, String format)
		{
			this.name = name;
			this.needStr = needStr;
			this.needRange = needRange;
			this.format = format;
		}

		public String toString(String controlId, Range range, String first, String second) 
		{
			return String.format(this.format, this.name, controlId, range == null ? "" : range.toString(first, second));
		};
		
		String name;
		boolean needStr;
		boolean needRange;
		String format;
	}
	
	static Assembler[] all = new Assembler[]
		{
			// $1 name, $2 controlId, $3 range
			new Assembler("visible", 	false,	false,	".%1$s()"), 
			new Assembler("count", 		false,	true, 	".%1$s(%3$s)"),
			new Assembler("contains", 	true, 	false, 	".%1$s('%2$s')"), 
			new Assembler("left", 		true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("right", 		true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("top", 		true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("bottom", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("inLeft", 	true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("inRight", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("inTop", 		true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("inBottom", 	true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("onLeft", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("onRight", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("onTop", 		true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("onBottom", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("lAlign", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("rAlign", 	true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("tAlign", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("bAlign", 	true, 	true, 	".%1$s('%2$s',%3$s)"), 
			new Assembler("hCenter", 	true, 	true, 	".%1$s('%2$s',%3$s)"),
			new Assembler("vCenter", 	true, 	true, 	".%1$s('%2$s',%3$s)"),		
		};
	
	
	
}
