package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.xpath.ImageAndOffset;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	public DialogWizard(DictionaryFx dictionary, IWindow window, AppConnection appConnection) throws Exception
	{
		this.dictionary = dictionary;
		this.window = window;
		this.appConnection = appConnection;

		this.controller = Common.loadController(DialogWizard.class.getResource("DialogWizard.fxml"));
		if (this.window != null)
		{
			this.controller.init(this, this.window.getName());
			this.selfControl = this.window.getSelfControl();
			this.controller.displaySelf(selfControl);
		}
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
		if (!this.dictionary.isApplicationRun())
		{
			this.controller.displayApplicationNotRun();
		}
		else
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
							return service().getTree(getOwnerLocator());
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
							Rectangle rectangle = service().getRectangle(null, getOwnerLocator());
							offsetX = rectangle.x;
							offsetY = rectangle.y;
							BufferedImage image = service().getImage(null, getOwnerLocator()).getImage();
							return new ImageAndOffset(image, offsetX, offsetY);
						}
					};
				}
			};
			this.documentService.setExecutor(executor);
			this.imageService.setExecutor(executor);

			this.documentService.setOnSucceeded(event -> this.controller.displayTree(((Document) event.getSource().getValue())));
			this.imageService.setOnSucceeded(event -> {
				ImageAndOffset imageAndOffset = (ImageAndOffset) event.getSource().getValue();
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
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private Locator getOwnerLocator()
	{
		return this.selfControl == null ? null : this.selfControl.locator();
	}
}
