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

	public void displayArrow(Rectangle self, Rectangle other, Arrow arrow)
	{
		int start = 0;
		int end = 0;
		CustomArrow.ArrowDirection direction;
		boolean isWhereSet = false;
		int where = - 1;
		int centerX = (int) ((self.getCenterX() + other.getCenterX()) / 2);
		int centerY = (int) ((self.getCenterY() + other.getCenterY()) / 2);
		if (arrow != null)
		{
			if (arrow == Arrow.LEFT_LEFT || arrow == Arrow.LEFT_RIGHT || arrow == Arrow.RIGHT_LEFT || arrow == Arrow.RIGHT_RIGHT || arrow == Arrow.H_CENTERS || arrow == Arrow.WIDTH)
			{
				direction = CustomArrow.ArrowDirection.HORIZONTAL;
			}
			else
			{
				direction = CustomArrow.ArrowDirection.VERTICAL;
			}
			switch (arrow)
			{
				case LEFT_LEFT:
					start = self.x;
					end = other.x;
					break;

				case LEFT_RIGHT:
					start = self.x;
					end = other.x + other.width;
					break;

				case RIGHT_LEFT:
					start = self.x + self.width;
					end = other.x;
					break;

				case RIGHT_RIGHT:
					start = self.x + self.width;
					end = other.x + other.width;
					break;

				case BOTTOM_TOP:
					start = self.y + self.height;
					end = other.y;
					break;

				case TOP_BOTTOM:
					start = self.y;
					end = other.y + other.height;
					break;

				case TOP_TOP:
					start = self.y;
					end = other.y;
					break;

				case BOTTOM_BOTTOM:
					start = self.y + self.height;
					end = other.y + other.height;
					break;

				case H_CENTERS:
					start = (int) self.getCenterX();
					end = (int) other.getCenterX();
					break;

				case V_CENTERS:
					start = (int) self.getCenterY();
					end = (int) other.getCenterY();
					break;

				case HEIGHT:
					isWhereSet = true;
					where = ((int) self.getCenterX());
					start = self.y;
					end = self.y + self.height;
					break;

				case WIDTH:
					isWhereSet = true;
					where = ((int) self.getCenterY());
					start = self.x;
					end = self.x + self.width;
					break;
			}
			if (!isWhereSet)
			{
				where = direction == CustomArrow.ArrowDirection.VERTICAL ? centerX : centerY;
			}
			System.out.println("#######################");
			System.out.println("start : " + start);
			System.out.println("end   : " + end);
			System.out.println("where : " + where);
			System.out.println("arrow : " + arrow);
			System.out.println("#######################");
			this.controller.displayArrow(start, end, where, direction);
		}
	}

	public void displayDistance(IControl otherControl, PieceKind kind)
	{
		if (kind == PieceKind.WIDTH || kind == PieceKind.HEIGHT)
		{
			Common.tryCatch(()->{
				IControl selfControl = this.currentWindow.getControlForName(null, this.parameterName);
				Locator selfLocator = selfControl.locator();
				IControl selfOwner = this.currentWindow.getOwnerControl(selfControl);
				Rectangle selfRectangle = service().getRectangle(selfOwner == null ? null : selfOwner.locator(), selfLocator);
				this.controller.clearArrow();
				int distance = kind.distance(selfRectangle, null);
				this.displayArrow(selfRectangle, new Rectangle(0, 0, 0, 0), kind.arrow());
				this.controller.displayDistance(distance);
			}, "Error on display distance");
		}
		else if (otherControl != null)
		{
			Common.tryCatch(() -> {
				IControl selfControl = this.currentWindow.getControlForName(null, this.parameterName);
				Locator selfLocator = selfControl.locator();
				IControl selfOwner = this.currentWindow.getOwnerControl(selfControl);
				Rectangle selfRectangle = service().getRectangle(selfOwner == null ? null : selfOwner.locator(), selfLocator);
				selfRectangle.setRect(selfRectangle.x - this.xOffset, selfRectangle.y - this.yOffset, selfRectangle.width, selfRectangle.height);

				Locator otherLocator = otherControl.locator();
				IControl otherOwner = this.currentWindow.getOwnerControl(otherControl);
				Rectangle otherRectangle = service().getRectangle(otherOwner == null ? null : otherOwner.locator(), otherLocator);
				otherRectangle.setRect(otherRectangle.x - this.xOffset, otherRectangle.y - this.yOffset, otherRectangle.width, otherRectangle.height);

				int distance = kind.distance(selfRectangle, otherRectangle);
				this.controller.displayDistance(distance);
				this.controller.clearArrow();
				this.displayArrow(selfRectangle, otherRectangle, kind.arrow());
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
