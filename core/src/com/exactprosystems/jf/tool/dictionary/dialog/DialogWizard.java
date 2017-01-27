package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.xpath.ImageAndOffset;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
public class DialogWizard
{
	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	private DictionaryFx dictionary;
	private IWindow window;
	private IControl selfControl;

	private DialogWizardController controller;
	private AppConnection appConnection;

	private Service<Document> documentService;
	private Service<ImageAndOffset> imageService;

	private int xOffset = 0;
	private int yOffset = 0;

	private Map<Integer, AbstractControl> controlMap;

	public DialogWizard(DictionaryFx dictionary, IWindow window, AppConnection appConnection) throws Exception
	{
		this.dictionary = dictionary;
		this.window = window;
		this.appConnection = appConnection;

		this.controller = Common.loadController(DialogWizard.class.getResource("DialogWizard.fxml"));
		this.controller.init(this, this.window.getName());
		this.selfControl = this.window.getSelfControl();
		this.controller.displaySelf(selfControl);
		this.controlMap = this.window.getSection(IWindow.SectionKind.Run).getControls()
				.stream()
				.filter(c -> c instanceof AbstractControl)
				.map(c ->
				{
					try
					{
						return AbstractControl.createCopy(c);
					}
					catch (Exception e)
					{
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(a -> count++, a -> a));
		displayElements();
	}

	public void show()
	{
		this.controller.show();
	}

	void changeDialogName(String newName)
	{
		this.window.setName(newName);
	}

	void displayImageAndTree()
	{
		if (documentService != null)
		{
			this.documentService.cancel();
		}
		if (imageService != null)
		{
			this.imageService.cancel();
		}
		this.documentService = new Service<Document>()
		{
			@Override
			protected Task<Document> createTask()
			{
				return new Task<Document>()
				{
					@Override
					protected Document call() throws Exception
					{
						return service().getTree(DialogWizard.this.selfControl.locator());
					}
				};
			}
		};

		this.imageService = new Service<ImageAndOffset>()
		{
			@Override
			protected Task<ImageAndOffset> createTask()
			{
				return new Task<ImageAndOffset>()
				{
					@Override
					protected ImageAndOffset call() throws Exception
					{
						int offsetX, offsetY;
						Rectangle rectangle = service().getRectangle(null, DialogWizard.this.selfControl.locator());
						offsetX = rectangle.x;
						offsetY = rectangle.y;
						BufferedImage image = service().getImage(null, DialogWizard.this.selfControl.locator()).getImage();
						return new ImageAndOffset(image, offsetX, offsetY);
					}
				};
			}
		};
		this.documentService.setExecutor(executor);
		this.imageService.setExecutor(executor);

		this.documentService.setOnSucceeded(event -> this.controller.displayTree(((Document) event.getSource().getValue()), xOffset, yOffset));
		this.imageService.setOnSucceeded(event -> {
			ImageAndOffset imageAndOffset = (ImageAndOffset) event.getSource().getValue();
			xOffset = imageAndOffset.offsetX;
			yOffset = imageAndOffset.offsetY;
			this.controller.displayImage(imageAndOffset.image);
		});

		this.imageService.setOnFailed(event -> {
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayImageFailing(message);
		});

		this.documentService.setOnFailed(event -> {
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayDocumentFailing(message);
		});
		this.imageService.start();
		this.documentService.start();
	}

	void updateId(int number, String newId) throws Exception
	{
		this.controlMap.get(number).set(AbstractControl.idName, newId);
		this.controller.displayElement(create(number, this.controlMap.get(number)));
	}

	void updateControlKind(int number, ControlKind kind) throws Exception
	{
		AbstractControl oldControl = this.controlMap.get(number);
		AbstractControl newControl = AbstractControl.createCopy(oldControl, kind);
		this.controlMap.remove(number);
		this.controlMap.put(number, newControl);
		this.controller.displayElement(create(number, this.controlMap.get(number)));
	}

	void close(boolean needAccept)
	{
		if (needAccept)
		{
			Section section = (Section) this.window.getSection(IWindow.SectionKind.Run);
			section.getControls().forEach(section::removeControl);
			this.controlMap.forEach((integer, abstractControl) -> Common.tryCatch(() -> section.addControl(abstractControl), "Error on add control"));
		}
		this.controller.close();
	}

	void changeElement(ElementWizardBean bean) throws Exception
	{
		AbstractControl newControl = this.controller.editElement(AbstractControl.createCopy(this.controlMap.get(bean.getNumber())));
		if (newControl != null)
		{
			this.controlMap.remove(bean.getNumber());
			this.controlMap.put(bean.getNumber(), newControl);

			this.controller.displayElement(create(bean.getNumber(), newControl));
		}
	}

	void removeElement(ElementWizardBean bean)
	{

	}

	void countElement(ElementWizardBean bean)
	{

	}

	//region private methods
	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void displayElements()
	{
		List<ElementWizardBean> list = this.controlMap.entrySet()
				.stream()
				.map(entry -> this.create(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		this.controller.displayElements(list);
	}

	private ElementWizardBean create(int number, AbstractControl control)
	{
		return new ElementWizardBean(
				number,
				control.getID(),
				control.getBindedClass(),
				(control.useAbsoluteXpath() || (control.getXpath() != null && !control.getXpath().isEmpty())),
				true,
				0
		);
	}

	private int count = 0;
	//endregion
}
