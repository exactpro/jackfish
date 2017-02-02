package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.xpath.ImageAndOffset;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.DialogEvent;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
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

	private List<AbstractControl> controlList;

	private Document document;

	public DialogWizard(DictionaryFx dictionary, IWindow window, AppConnection appConnection) throws Exception
	{
		this.dictionary = dictionary;
		this.window = window;
		this.appConnection = appConnection;

		this.controller = Common.loadController(DialogWizard.class.getResource("DialogWizard.fxml"));
		this.controller.init(this, this.window.getName());
		this.selfControl = this.window.getSelfControl();
		this.controller.displaySelf(selfControl);
		this.controlList = this.window.getSection(IWindow.SectionKind.Run).getControls()
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
				.collect(Collectors.toList());
		displayElements();
	}

	public void show()
	{
		this.controller.show();
	}

	public void onHiding(Consumer<DialogEvent> consumer)
	{
		this.controller.setOnHiding(consumer);
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

		this.documentService.setOnSucceeded(event -> {
			this.document = (Document) event.getSource().getValue();
			this.controller.displayTree(this.document, xOffset, yOffset);
		});
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

	void close(boolean needAccept)
	{
		if (needAccept)
		{
			Section section = (Section) this.window.getSection(IWindow.SectionKind.Run);
			section.getControls().forEach(section::removeControl);
			this.controlList.forEach(abstractControl -> Common.tryCatch(() -> section.addControl(abstractControl), "Error on add control"));
		}
		this.controller.close();
	}

	void updateId(ElementWizardBean bean, String newId) throws Exception
	{
		AbstractControl abstractControl = this.controlList.get(bean.getNumber());
		abstractControl.set(AbstractControl.idName, newId);
		updateBean(abstractControl, bean);
		this.controller.displayElement(bean);
	}

	void updateControlKind(ElementWizardBean bean, ControlKind kind) throws Exception
	{
		AbstractControl oldControl = this.controlList.get(bean.getNumber());
		AbstractControl newControl = AbstractControl.createCopy(oldControl, kind);

		this.controlList.set(bean.getNumber(), newControl);
		updateBean(newControl, bean);
		updateCountElement(bean);
		this.controller.displayElement(bean);
	}

	void changeElement(ElementWizardBean bean) throws Exception
	{
		AbstractControl newControl = this.controller.editElement(AbstractControl.createCopy(this.controlList.get(bean.getNumber())));
		if (newControl != null)
		{
			this.controlList.set(bean.getNumber(), newControl);
			this.updateBean(newControl, bean);
			this.updateCountElement(bean);
			this.controller.displayElement(bean);
		}
	}

	String showXpathViewer(String xpath)
	{
		return new XpathViewer(this.selfControl.locator(), this.document, service()).show(xpath, "Xpath builder", Common.currentThemesPaths(), false);
	}

	void removeElement(ElementWizardBean bean)
	{
		boolean needRemove = DialogsHelper.showQuestionDialog("Remove element", "Are you sure to remove this element?");
		if (needRemove)
		{
			this.controlList.remove(bean.getNumber());
			List<ElementWizardBean> remove = this.controller.remove(bean);
			for (int i = 0; i < remove.size(); i++)
			{
				ElementWizardBean bean1 = remove.get(i);
				boolean isNew = bean1.getIsNew();
				updateBean(this.controlList.get(i), bean1);
				bean1.setIsNew(isNew);
				bean1.setNumber(i);
				this.controller.displayElement(bean1);
			}
		}
	}

	void findElements(List<ElementWizardBean> items)
	{
		for (ElementWizardBean item : items)
		{
			updateCountElement(item);
			this.controller.displayElement(item);
		}
	}

	//region private methods
	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void updateCountElement(ElementWizardBean bean)
	{
		int count = -1;
		if (bean.isXpath())
		{
			AbstractControl abstractControl = this.controlList.get(bean.getNumber());
			String xpathStr = abstractControl.getXpath();
			XPath xpath = XPathFactory.newInstance().newXPath();
			try
			{
				XPathExpression compile = xpath.compile(xpathStr);
				NodeList nodeList = (NodeList) compile.evaluate(this.document.getDocumentElement(), XPathConstants.NODESET);
				count = nodeList.getLength();
				if (count == 1)
				{
					this.controller.foundGreat(nodeList.item(0), bean);
				}
				else if (count > 1)
				{
					this.controller.foundBad(nodeList, bean);
				}
			}
			catch (XPathExpressionException e)
			{
				DialogsHelper.showError("Xpath wrong. Double check it");
			}
		}
		else
		{
			//TODO need some implementation for this
		}
		bean.setCount(count);
	}

	private void displayElements()
	{
		this.displayElements(entry -> this.create(this.controlList.indexOf(entry), entry, false));
	}

	private void displayElements(Function<AbstractControl, ElementWizardBean> mapFunction)
	{
		List<ElementWizardBean> list = this.controlList
				.stream()
				.map(mapFunction)
				.collect(Collectors.toList());
		this.controller.displayElements(list);
	}

	private ElementWizardBean create(int number, AbstractControl control, boolean isNew)
	{
		return new ElementWizardBean(
				number,
				control.getID(),
				control.getBindedClass(),
				((control.getXpath() != null && !control.getXpath().isEmpty()) || control.useAbsoluteXpath()),
				isNew,
				0
		);
	}

	private void updateBean(AbstractControl control, ElementWizardBean bean)
	{
		bean.setControlKind(control.getBindedClass());
		bean.setId(control.getID());
		bean.setIsNew(true);
		bean.setXpath((control.getXpath() != null && !control.getXpath().isEmpty()) || control.useAbsoluteXpath());
	}
	//endregion
}
