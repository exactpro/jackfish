////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout.wizard;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class LayoutWizard
{
	private static final Logger logger = Logger.getLogger(LayoutWizard.class);
	private LayoutWizardController controller;
	private IGuiDictionary dictionary;
	private AppConnection appConnection;
	private Table table;
	private DataProvider<String> dataProvider;
	private Task<BufferedImage> task;

	private double xOffset = 0;
	private double yOffset = 0;
	private Map<IControl, Rectangle> mapRectangle= new LinkedHashMap<>();
	private double currentZoom = 1;
	private double initialWidth = 0;
	private double initialHeight = 0;

	public LayoutWizard(Table table, AppConnection appConnection)
	{
		this.table = table;
		this.dataProvider = new TableDataProvider(table);
		this.dictionary = appConnection.getDictionary();
		this.appConnection = appConnection;
		this.controller = Common.loadController(this.getClass().getResource("LayoutWizard.fxml"));
		this.controller.init(this, dataProvider);
		Collection<IWindow> windows = dictionary.getWindows();
		this.controller.displayDialogs(windows);
		String dialogName = this.table.getHeader(0);
		Optional<String> first = windows.stream().map(IWindow::getName).filter(dialogName::equals).findFirst();
		if (!first.isPresent())
		{
			DialogsHelper.showInfo(String.format("Dialog with name '%s' not found", dialogName));
			//TODO mb ask user for remove columns?
		}

	}

	public void show()
	{
		this.controller.show();
	}

	void close()
	{
		this.controller.hide();
	}

	void accept()
	{

	}

	void changeScale(double scale)
	{
		this.currentZoom = scale;
		this.resizeImage();
	}

	void changeDialog(IWindow window) throws Exception
	{
		if (window == null)
		{
			return;
		}
		this.controller.displayControls(window.getControls(IWindow.SectionKind.Run));

		this.controller.displayWindow(window);

		loadImage(window);
	}

	void selectItems(List<IControl> checked)
	{
		System.out.println("Checked : " + checked.toString());
	}

	private void loadImage(IWindow window) throws Exception
	{
		this.controller.clearImage();
		if (task != null)
		{
			task.cancel();
		}
		IControl selfControl = window.getSelfControl();
		if (selfControl == null)
		{
			DialogsHelper.showError(String.format("Can't load a image, because a self control of the window '%s' is null", window.getName()));
			return;
		}
		Locator imageLocator = selfControl.locator();
		List<IControl> controls = new ArrayList<>();
		controls.addAll(window.getControls(IWindow.SectionKind.Run));
		controls.addAll(window.getControls(IWindow.SectionKind.Self));
		this.controller.beforeLoadImage(window.getName());
		this.task = new Task<BufferedImage>()
		{
			@Override
			protected BufferedImage call() throws Exception
			{
				BufferedImage image = service().getImage(null, imageLocator).getImage();
				Rectangle selfRectangle = service().getRectangle(null, imageLocator);
				xOffset = selfRectangle.getX();
				yOffset = selfRectangle.getY();
				controls.forEach(control -> Common.tryCatch(() -> {
					Locator locator = control.locator();
					IControl ownerControl = window.getOwnerControl(control);
					Locator ownerLocator = ownerControl == null ? null : ownerControl.locator();
					Rectangle rectangle = service().getRectangle(ownerLocator, locator);
					rectangle.setRect(rectangle.x - xOffset, rectangle.y - yOffset, rectangle.width, rectangle.height);
					mapRectangle.put(control, rectangle);
				},""));
				return image;
			}
		};
		this.task.setOnSucceeded(event -> Common.tryCatch(() ->
		{
			BufferedImage image = (BufferedImage) event.getSource().getValue();
			this.initialWidth = image.getWidth();
			this.initialHeight = image.getHeight();
			this.controller.displayScreenShot(image);
			this.controller.displayScreenShot(image);
		}, "Error on get screenshot"));
		this.task.setOnFailed(event -> Common.tryCatch(() -> {
			Throwable exception = event.getSource().getException();
			DialogsHelper.showError(exception.getMessage());
			logger.error(exception.getMessage(), exception);
			this.controller.clearImage();
		}, "Error on get screenshot"));

		Thread thread = new Thread(this.task);
		thread.setName("Get screenshot from layout expression builder, thread id : " + thread.getId());
		thread.start();
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void resizeImage()
	{
		this.controller.resizeImage(this.currentZoom * this.initialWidth, this.currentZoom * this.initialHeight);
//		this.displayControl(this.selfControl, true);
//		this.displayControl(this.otherControl, false);
//		if (lastArrow != null)
//		{
//			this.displayArrow(getRect(mapRectangle.get(this.selfControl)), getRect(this.mapRectangle.get(this.otherControl)), this.lastArrow);
//		}
	}
}
