package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.ServiceLambdaBean;
import com.exactprosystems.jf.tool.wizard.related.ImageAndOffset;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class XpathViewer
{
	private Document                     document;
	private XpathViewerContentController controller;
	private Node                         currentNode;

	private String relativeXpath;

	private Common.SupplierWithException<Document> documentSupplier;
	private ServiceLambdaBean                      serviceLambdaBean;
	private Locator                                ownerLocator;


	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	private Service<Document>       documentService;
	private Service<ImageAndOffset> imageService;
	private int xOffset = 0;
	private int yOffset = 0;

	public XpathViewer(Locator ownerLocator, Common.SupplierWithException<Document> documentSupplier, ServiceLambdaBean serviceLambdaBean)
	{
		this.ownerLocator = ownerLocator;
		this.serviceLambdaBean = serviceLambdaBean;
		this.documentSupplier = documentSupplier;

		this.controller = Common.loadController(XpathViewer.class.getResource("XpathViewerContent.fxml"));
	}


	public String show(String initial, String title, List<String> themePaths, boolean fullScreen)
	{
		this.controller.init(this, initial);
		String result = this.controller.show(title, themePaths, fullScreen);
		return result == null ? initial : result;
	}

	//region xpath methods
	void setRelativeXpath(String xpath)
	{
		this.relativeXpath = xpath;
	}

	void applyXpath(String xpath)
	{
		Node rootNode = this.document;
		if (this.ownerLocator != null)
		{
			rootNode = XpathUtils.getFirst(this.document, "/*");
		}

		this.controller.deselectItems();
		this.controller.displayResults(XpathUtils.evaluate(rootNode, xpath));
	}

	void updateNode(Node node)
	{
		this.currentNode = node;

		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(attr -> IntStream.range(0, attr.getLength()).mapToObj(attr::item).map(Node::getNodeName).forEach(params::add));
		this.controller.displayParams(params);
	}

	void createXpaths(boolean useText, List<String> parameters)
	{
		String relativePath = null;
		Node relativeNode = null;
		Node rootNode = this.document;

		if (this.ownerLocator != null)
		{
			relativePath = ".";
			relativeNode = XpathUtils.getFirst(this.document, "/*");
			rootNode = relativeNode;
		}

		if (!Str.IsNullOrEmpty(this.relativeXpath))
		{
			relativePath = this.relativeXpath;
			relativeNode = XpathUtils.getFirst(this.document, this.relativeXpath);
		}

		String xpath1 = XpathUtils.fullXpath(relativePath, relativeNode, currentNode, false, null, true);
		String xpath2 = XpathUtils.fullXpath(relativePath, relativeNode, currentNode, useText, parameters, true);
		String xpath3 = XpathUtils.fullXpath(relativePath, relativeNode, currentNode, false, null, false);
		String xpath4 = XpathUtils.fullXpath(relativePath, relativeNode, currentNode, useText, parameters, false);

		this.controller.displayXpaths(xpath1, xpath2, xpath3, xpath4);
		this.controller.displayCounters(XpathUtils.evaluate(rootNode, xpath1), XpathUtils.evaluate(rootNode, xpath2), XpathUtils.evaluate(rootNode, xpath3), XpathUtils.evaluate(rootNode, xpath4));

		if (this.currentNode != null)
		{
			Rectangle rectangle = (Rectangle) this.currentNode.getUserData(IRemoteApplication.rectangleName);
			this.controller.displayRectangle(rectangle);
		}
	}
	//endregion

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
						return documentSupplier.get();
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
						Rectangle rectangle = serviceLambdaBean.rectangleSupplier().get();
						offsetX = rectangle.x;
						offsetY = rectangle.y;
						BufferedImage image = serviceLambdaBean.bufferedImageSupplier().get();
						return new ImageAndOffset(image, offsetX, offsetY);
					}
				};
			}
		};
		this.documentService.setExecutor(executor);
		this.imageService.setExecutor(executor);

		this.documentService.setOnSucceeded(event ->
		{
			this.document = (Document) event.getSource().getValue();
			this.currentNode = XpathUtils.getFirst(this.document, "/*");
			this.controller.displayDocument(this.document, xOffset, yOffset);
		});
		this.imageService.setOnSucceeded(event ->
		{
			ImageAndOffset imageAndOffset = (ImageAndOffset) event.getSource().getValue();
			xOffset = imageAndOffset.offsetX;
			yOffset = imageAndOffset.offsetY;
			this.controller.displayImage(imageAndOffset.image);
		});

		this.imageService.setOnFailed(event ->
		{
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayImageFailing(message);
		});

		this.documentService.setOnFailed(event ->
		{
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayDocumentFailing(message);
		});
		if (this.serviceLambdaBean != null)
		{
			this.imageService.start();
		}
		else
		{
			this.controller.displayImage(null);
		}
		this.documentService.start();
	}

}