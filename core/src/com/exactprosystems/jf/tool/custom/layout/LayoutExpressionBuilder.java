package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import javafx.concurrent.Task;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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

	public LayoutExpressionBuilder(String parameterName, String parameterExpression, AppConnection appConnection, String windowName, AbstractEvaluator evaluator) throws Exception
	{
		if (appConnection == null)
		{
			throw new Exception("Connection is not established. \nChoose Default app at first and run it.");
		}

		this.parameterName = parameterName;
		this.parameterExpression = parameterExpression;
		this.appConnection = appConnection;
		this.windowName = windowName;
		this.evaluator = evaluator;
		IGuiDictionary dictionary = appConnection.getDictionary();
		this.currentWindow = dictionary.getWindow(this.windowName);
	}

	public String show(String title, boolean fullScreen) throws Exception
	{
		ArrayList<IControl> controls = new ArrayList<>();
		controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Self));
		controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Run));

		IControl selfControl = this.currentWindow.getSelfControl();
		if (selfControl == null)
		{
			throw new NullPointerException(String.format("Can't get screenshot, because self section on window %s don't contains controls", this.windowName));
		}

		Locator selfLocator = selfControl.locator();

		this.controller = Common.loadController(LayoutExpressionBuilder.class.getResource("LayoutExpressionBuilder.fxml"));
		this.controller.init(this, this.evaluator);
		this.controller.displayMethods(all);

		IControl initialControl = this.currentWindow.getControlForName(null, this.parameterName);
		if (initialControl == null)
		{
			throw new NullPointerException(String.format("Control with name %s is not found in the window with name %s", this.parameterName, this.windowName));
		}

		Task<BufferedImage> loadImage = new Task<BufferedImage>()
		{
			@Override
			protected BufferedImage call() throws Exception
			{
				BufferedImage image = service().getImage(null, selfLocator).getImage();
				IControl ownerSelf = currentWindow.getOwnerControl(selfControl);
				Rectangle selfRectangle = service().getRectangle(ownerSelf == null ? null : ownerSelf.locator(), selfLocator);
				xOffset = selfRectangle.getX();
				yOffset = selfRectangle.getY();
				return image;
			}
		};
		loadImage.setOnSucceeded(event -> Common.tryCatch(() -> {
			this.controller.displayScreenShot((BufferedImage) event.getSource().getValue());
			displayControl(initialControl, true);
		}, "Error on get screenshot"));

		new Thread(loadImage).start();
		String result = this.controller.show(title, fullScreen, controls);
		return result == null ? this.parameterExpression : result;
	}

	public void displayControl(IControl control, boolean self)
	{
		if (control != null)
		{
			Common.tryCatch(() -> {
				IControl owner = this.currentWindow.getOwnerControl(control);
				Rectangle rectangle = service().getRectangle(owner == null ? null : owner.locator(), control.locator());
				rectangle.setRect(rectangle.getX() - this.xOffset, rectangle.getY() - this.yOffset, rectangle.getWidth(), rectangle.getHeight());
				this.controller.displayControl(rectangle, self);
				if (!self)
				{
					this.controller.displayControlId(control.getID());
				}
			}, String.format("Error on display control %s", control));
		}
	}

	public void clearCanvas()
	{
		this.controller.clearCanvas();
		this.controller.displayControlId("");
	}

	public void addFormula(SpecMethod parameter, String controlId, Range range, String first, String second)
	{
		System.err.println(parameter.toString(controlId, range, first, second));
	}


	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	static class SpecMethod
	{
		public SpecMethod(String name, boolean needStr, boolean needRange, String format)
		{
			this.name = name;
			this.needStr = needStr;
			this.needRange = needRange;
			this.format = format;
		}

		@Override
		public String toString()
		{
			return this.name;
		}

		public String toString(String controlId, Range range, String first, String second)
		{
			return String.format(this.format, this.name, controlId, range == null ? "" : range.toString(first, second));
		}

		String name;
		boolean needStr;
		boolean needRange;
		String format;
	}

	static SpecMethod[] all = new SpecMethod[]{
			// $1 name, $2 controlId, $3 range
			new SpecMethod("visible", false, false, ".%1$s()"), 
			new SpecMethod("count", false, true, ".%1$s(%3$s)"), 
			new SpecMethod("contains", true, false, ".%1$s('%2$s')"), 
			new SpecMethod("left", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("right", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("top", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("bottom", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("inLeft", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("inRight", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("inTop", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("inBottom", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("onLeft", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("onRight", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("onTop", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("onBottom", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("lAlign", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("rAlign", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("tAlign", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("bAlign", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("hCenter", true, true, ".%1$s('%2$s',%3$s)"), 
			new SpecMethod("vCenter", true, true, ".%1$s('%2$s',%3$s)"),};


}
