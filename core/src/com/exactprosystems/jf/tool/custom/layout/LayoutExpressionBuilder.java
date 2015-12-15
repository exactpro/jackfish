package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import javafx.concurrent.Task;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
		controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Run).stream().filter(iControl -> !iControl.getID().equals(this.parameterName)).collect(Collectors.toList()));
		controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Self));

		IControl selfControl = this.currentWindow.getSelfControl();
		if (selfControl == null)
		{
			throw new NullPointerException(String.format("Can't get screenshot, because self section on window %s don't contains controls", this.windowName));
		}

		Locator selfLocator = selfControl.locator();

		this.controller = Common.loadController(LayoutExpressionBuilder.class.getResource("LayoutExpressionBuilder.fxml"));
		this.controller.init(this, this.evaluator);

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

	public void clearCanvas()
	{
		this.controller.clearCanvas();
		this.controller.displayControlId("");
	}

	public void addFormula(PieceKind parameter, String controlId, Range range, String first, String second)
	{
		System.err.println(parameter.toFormula(controlId, range, first, second));
	}

	public void displayArrow(Rectangle init, Rectangle selected, Arrow arrow)
	{
		double start = 0;
		double end = 0;
		CustomArrow.ArrowPosition position = null;
		double where = 0;
		double centerX = (Math.max(init.getCenterX(), selected.getCenterX()) + Math.min(init.getCenterX(), selected.getCenterX())) / 2;
		double centerY = (Math.max(init.getCenterY(), selected.getCenterY()) + Math.min(init.getCenterY(), selected.getCenterY())) / 2;
		if (arrow != null)
		{
			switch (arrow)
			{
				case LEFT_LEFT:
//					position = CustomArrow.ArrowPosition.HORIZONTAL;
//					where = centerY;
//					start =
					break;

				case LEFT_RIGHT:

					break;

				case RIGHT_LEFT:

					break;

				case RIGHT_RIGHT:

					break;

				case BOTTOM_TOP:

					break;

				case TOP_BOTTOM:

					break;

				case TOP_TOP:

					break;

				case BOTTOM_BOTTOM:
					position = CustomArrow.ArrowPosition.VERTICAL;
					where = centerX;
					start = init.getHeight() + init.getY();
					end = selected.getHeight() + selected.getY();
					break;

				case H_CENTERS:

					break;

				case V_CENTERS:

					break;
			}
			this.controller.displayArrow(start, end, where, position);
		}
	}

	public void displayDistance(IControl selectedControl, PieceKind kind)
	{
		if (selectedControl != null)
		{
			Common.tryCatch(() -> {
				IControl initialControl = this.currentWindow.getControlForName(null, this.parameterName);
				Locator initialLocator = initialControl.locator();
				IControl initialOwner = this.currentWindow.getOwnerControl(initialControl);
				Rectangle initialRectangle = service().getRectangle(initialOwner == null ? null : initialOwner.locator(), initialLocator);

				Locator selectedLocator = selectedControl.locator();
				IControl selectedOwner = this.currentWindow.getOwnerControl(selectedControl);
				Rectangle selectedRectangle = service().getRectangle(selectedOwner == null ? null : selectedOwner.locator(), selectedLocator);

				int distance = kind.distance(initialRectangle, selectedRectangle);
				this.controller.displayDistance(distance);
				this.displayArrow(initialRectangle, selectedRectangle, kind.arrow());
			}, "Error on display distance");
		}
		else
		{
			this.controller.clearDistance();
			this.controller.clearArrow();
		}
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

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}
}
