////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout.wizard;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.ServerException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LayoutWizard
{
	private static final Logger logger = Logger.getLogger(LayoutWizard.class);
	private LayoutWizardController controller;
	private IGuiDictionary dictionary;
	private AppConnection appConnection;
	private Table table;
	private Table oldTable;
	private Task<BufferedImage> task;

	private double xOffset = 0;
	private double yOffset = 0;
	private Map<IControl, Rectangle> mapRectangle= new LinkedHashMap<>();
	private double currentZoom = 1;
	private double initialWidth = 0;
	private double initialHeight = 0;

	private List<IControl> oldCheckedList = new ArrayList<>();

	public LayoutWizard(Table table, AppConnection appConnection, AbstractEvaluator evaluator)
	{
		this.oldTable = table;
		this.table = new Table(table, evaluator);
		this.dictionary = appConnection.getDictionary();
		this.appConnection = appConnection;
		if (!tableIsValid())
		{
			return;
		}
		this.controller = Common.loadController(this.getClass().getResource("LayoutWizard.fxml"));
		this.controller.init(this, evaluator);
		this.controller.displayDialogs(this.dictionary.getWindows().stream()
				.filter(w -> Common.tryCatch(w::getSelfControl, "nothing", null) != null)
				.collect(Collectors.toList())
		);
		String header = this.table.getHeaderSize() > 0 ? this.table.getHeader(0) : null;
		Optional<IWindow> window = this.dictionary.getWindows().stream().filter(w -> w.getName().equals(header)).findFirst();
		List<IControl> controlsList = new ArrayList<>();
		window.ifPresent(w ->
		{
			Collection<IControl> controls = w.getControls(IWindow.SectionKind.Run);
			IntStream.range(0, this.table.getHeaderSize())
					.mapToObj(i -> this.table.getHeader(i))
					.forEach(h -> controls.stream()
							.filter(c -> c.getID().equals(h))
							.findFirst()
							.ifPresent(controlsList::add)
					);
		});
		this.controller.displayWindow(window.orElse(null), controlsList);
		this.selectItems(controlsList);
	}

	public void show()
	{
		if (this.controller != null)
		{
			this.controller.show();
		}
	}

	void close()
	{
		this.controller.hide();
	}

	void accept()
	{
		this.oldTable.fillFromTable(this.table);
		close();
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
		loadImage(window);
	}

	void selectItems(List<IControl> checked)
	{
		checkControls(checked);
		this.controller.displaySelectedControls(checked);
	}

	void changeItem(IControl horizontal, IControl vertical)
	{

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
				BufferedImage image = null;
				try
				{
					image = service().getImage(null, imageLocator).getImage();
				}
				//TODO need catch ElementNotFoundException, not ServerException
				catch (ServerException e)
				{
					return null;
				}
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
			if (image == null)
			{
				this.controller.loadImageFailed();
			}
			else
			{
				this.initialWidth = image.getWidth();
				this.initialHeight = image.getHeight();
				this.controller.displayScreenShot(image);
			}
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

	private boolean tableIsValid()
	{
		if (this.table.isEmptyTable())
		{
			return true;
		}
		boolean[] hasError = {false};
		StringBuilder sbError = new StringBuilder("The table is not valid. See errors below :\n");
		int[] errorCount = {0};
		String dialogName = this.table.getHeader(0);
		Optional<String> first = dictionary.getWindows().stream().map(IWindow::getName).filter(dialogName::equals).findFirst();
		if (!first.isPresent())
		{
			sbError.append(errorCount[0]++).append(": ");
			sbError.append(String.format("In the header with number 0 need be the name of dialog. Dialog with name '%s' not found", dialogName));
			sbError.append("\n");
			hasError[0] = true;
		}
		if (this.table.getHeaderSize() - 1 != this.table.size())
		{
			sbError.append(errorCount[0]++).append(": ");
			sbError.append("The table must be square");
			sbError.append("\n");
			hasError[0] = true;
		}
		int l = Math.min(this.table.getHeaderSize() - 1, this.table.size());
		IntStream.range(0, l).forEach(i ->
		{
			String header = this.table.getHeader(i + 1);
			String cell = String.valueOf(this.table.get(i).get(dialogName));
			if (!header.equals(cell))
			{
				sbError.append(errorCount[0]++).append(": ");
				sbError.append(String.format("The row with index %d ( %s ) not equals the header with index %d ( %s) ", i, cell, i + 1, header));
				sbError.append("\n");
				hasError[0] = true;
			}
		});
		boolean result = !hasError[0] || DialogsHelper.showYesNoDialog(sbError.toString(), "Would you like continue process with empty table?");
		if (result && hasError[0])
		{
			this.table.clear();
			List<String> headers = new ArrayList<>();
			for (int i = 0; i < this.table.getHeaderSize(); i++)
			{
				headers.add(table.getHeader(i));
			}
			this.table.removeColumns(headers.toArray(new String[headers.size()]));
		}
		return result;
	}

	private void checkControls(List<IControl> newControls)
	{
		//TODO implement this methods
		List<IControl> list = oldCheckedList.stream().filter(c -> !newControls.contains(c)).collect(Collectors.toList());


		this.oldCheckedList.clear();
		this.oldCheckedList.addAll(newControls);
	}
}
