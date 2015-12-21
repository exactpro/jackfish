////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonBar.ButtonData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayoutExpressionBuilder
{
	private LayoutExpressionBuilderController controller;
	private String parameterName;
	private AppConnection appConnection;
	private IWindow currentWindow;
	private String windowName;
	private AbstractEvaluator evaluator;
	private String parameterExpression;
	private List<FormulaPart> formula;

	private double xOffset = 0;
	private double yOffset = 0;

	private List<IControl> controls;

	private Map<IControl, Rectangle> mapRectangle= new LinkedHashMap<>();
	private Map<IControl, IdWithCoordinates> mapIds = new LinkedHashMap<>();


	public LayoutExpressionBuilder(String parameterName, String parameterExpression, AppConnection appConnection, String windowName, AbstractEvaluator evaluator) throws Exception
	{
		if (appConnection == null)
		{
			throw new Exception("Connection is not established. \nChoose Default app at first and run it.");
		}

		this.parameterExpression = parameterExpression;
		if (Str.IsNullOrEmpty(parameterExpression))
		{
			this.formula = new ArrayList<>();
		}
		else
		{
			this.formula = FormulaParser.parse(parameterExpression);
			if (this.formula == null)
			{
				throw new Exception("Can not parse this expression to split it to parts.");
			}
		}
		
		this.parameterName = parameterName;
		this.appConnection = appConnection;
		this.windowName = windowName;
		this.evaluator = evaluator;
		IGuiDictionary dictionary = appConnection.getDictionary();
		this.currentWindow = dictionary.getWindow(this.windowName);
	}

	public String show(String title, boolean fullScreen) throws Exception
	{
		this.controls = new ArrayList<>();
		this.controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Run));
		this.controls.addAll(this.currentWindow.getControls(IWindow.SectionKind.Self));

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

		this.controller.displayFormula(-1, this.formula);
		
		Task<BufferedImage> prepare = new Task<BufferedImage>()
		{
			@Override
			protected BufferedImage call() throws Exception
			{
				BufferedImage image = service().getImage(null, selfLocator).getImage();
				IControl ownerSelf = currentWindow.getOwnerControl(selfControl);
				Rectangle selfRectangle = service().getRectangle(ownerSelf == null ? null : ownerSelf.locator(), selfLocator);
				xOffset = selfRectangle.getX();
				yOffset = selfRectangle.getY();
				controls.forEach(control -> Common.tryCatch(() -> {
					Locator locator = control.locator();
					IControl ownerControl = currentWindow.getOwnerControl(control);
					Locator ownerLocator = ownerControl == null ? null : ownerControl.locator();
					Rectangle rectangle = service().getRectangle(ownerLocator, locator);
					rectangle.setRect(rectangle.x - xOffset, rectangle.y - yOffset, rectangle.width, rectangle.height);
					mapRectangle.put(control, rectangle);
					IdWithCoordinates idWithCoordinates = new IdWithCoordinates(rectangle.x, rectangle.y, control.getID());
					mapIds.put(control, idWithCoordinates);
				},""));
				return image;
			}
		};
		
		prepare.setOnSucceeded(event -> Common.tryCatch(() ->
		{
			this.controller.displayScreenShot((BufferedImage) event.getSource().getValue());
			displayControl(initialControl, true);
		}, "Error on get screenshot"));

		new Thread(prepare).start();
		
		ButtonData result = this.controller.show(title, fullScreen, this.controls, this.parameterName);
		
		return result == null ? this.parameterExpression : FormulaParser.toFormula(this.formula);
	}

	public void clearCanvas()
	{
		this.controller.clearCanvas();
		this.controller.displayControlId("");
	}

	public void updateFormula(int index, PieceKind kind, String name, Range range, String first, String second)
	{
		this.formula.set(index, new FormulaPart(kind, range, name, first, second));
		this.controller.displayFormula(index, this.formula);
	}
	
	public void addFormula(PieceKind kind, String name, Range range, String first, String second)
	{
		this.formula.add(new FormulaPart(kind, range, name, first, second));
		this.controller.displayFormula(this.formula.size() - 1, this.formula);
	}

	public void removePart(int index)
	{
		this.formula.remove(index);
		this.controller.displayFormula(this.formula.size() - 1, this.formula);
	}

	public void displayPart(int index)
	{
		this.controller.clearControls();
		FormulaPart part = this.formula.get(index);
		this.controller.displayPart(part.getKind(), part.getName(), part.getRange(), part.getFirst(), part.getSecond());
		if (!Str.IsNullOrEmpty(part.getName()))
		{
			this.controller.selectControl(part.getName());
		}
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
		boolean needOutLine = true;
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
					needOutLine = false;
					isWhereSet = true;
					where = ((int) self.getCenterX());
					start = self.y;
					end = self.y + self.height;
					break;

				case WIDTH:
					needOutLine = false;
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
			this.controller.clearArrow();
			if (needOutLine)
			{
				this.controller.displayOutLine(start, end, direction, where);
			}
			this.controller.displayArrow(start, end, where, direction);
		}
	}

	public void displayDistance(IControl otherControl, PieceKind kind)
	{
		if (kind == PieceKind.WIDTH || kind == PieceKind.HEIGHT)
		{
			Common.tryCatch(()->{
				Rectangle selfRectangle = mapRectangle.get(otherControl);
				this.controller.clearArrow();
				int distance = kind.distance(selfRectangle, null);
				this.displayArrow(selfRectangle, new Rectangle(0, 0, 0, 0), kind.arrow());
				this.controller.displayDistance(distance);
			}, "Error on display distance");
		}
		else if (otherControl != null)
		{
			Common.tryCatch(() -> {
				Rectangle selfRectangle = mapRectangle.get(this.currentWindow.getControlForName(null, this.parameterName));
				Rectangle otherRectangle = mapRectangle.get(otherControl);

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
				Rectangle rectangle = this.mapRectangle.get(control);
				this.controller.displayControl(rectangle, self);
				if (!self)
				{
					this.controller.displayControlId(control.getID());
				}
			}, String.format("Error on display control %s", control));
		}
	}

	public void displayIds(boolean flag)
	{
		if (!flag)
		{
			this.controller.hideIds();
			return;
		}
		this.controller.displayIds(this.mapIds.values());
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	static class IdWithCoordinates {
		double x;
		double y;
		String id;

		public IdWithCoordinates(double x, double y, String id)
		{
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
}
