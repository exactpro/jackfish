////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.documents.guidic.*;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.xpath.ImageAndOffset;
import com.exactprosystems.jf.tool.custom.xpath.XpathTreeItem.TreeItemState;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DialogWizard
{
    private static final double     NEAREST_PARENT_FACTOR = 0.5;
    private static final int		MAX_TRIES = 128;
    
    private static ExecutorService  executor = Executors.newFixedThreadPool(1);
    private DictionaryFx            dictionary;
    private Window                  window;
    private IControl                selfControl;

    private DialogWizardController  controller;
    private AppConnection           appConnection;
    private PluginInfo              pluginInfo;
    private WizardSettings          wizardSettings;
    private WizardMatcher           matcher;
    private Document                document;

    private Service<Document>       documentService;
    private Service<ImageAndOffset> imageService;

    private Rectangle               dialogRectangle;
    private int                     xOffset  = 0;
    private int                     yOffset  = 0;

    private Consumer<IWindow>       consumer;

	public DialogWizard(DictionaryFx dictionary, IWindow window, AppConnection appConnection) throws Exception
	{
		this.dictionary = dictionary;
		this.window = (Window)window;
		this.appConnection = appConnection;
		this.pluginInfo = appConnection.getApplication().getFactory().getInfo();
		this.wizardSettings = new WizardSettings(dictionary.getFactory().getSettings());
		this.matcher = new WizardMatcher(this.pluginInfo);

		this.controller = Common.loadController(DialogWizard.class.getResource("DialogWizard.fxml"));
		this.controller.init(this, this.window.getName());
		this.selfControl = this.window.getSelfControl();
		this.controller.displaySelf(selfControl);

		displayElements();
		updateOnButtons();
	}

	//----------------------------------------------------------------------------------------------
    // sophisticated functions
    //----------------------------------------------------------------------------------------------
    public Node findBestIndex(ElementWizardBean bean)
    {
        if (bean == null)
        {
            return null;
        }
        ControlKind kind = bean.getControlKind(); 
        ExtraInfo info = (ExtraInfo)bean.getAbstractControl().getInfo();
        
        Map<Double, Node> candidates = new HashMap<>();
        
        passTree(this.document, node -> candidates.put(similarityFactor(node, kind, info), node));
        Double maxKey = candidates.keySet().stream().max(Double::compare).orElse(Double.MIN_VALUE);
        return maxKey != null && maxKey > this.wizardSettings.getThreshold() ? candidates.get(maxKey) : null;
    }
    
    private void passTree(Node node, Consumer<Node> func)
    {
        func.accept(node);
        IntStream.range(0, node.getChildNodes().getLength())
            .mapToObj(node.getChildNodes()::item)
            .filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
            .forEach(item -> passTree(item, func));
    }
    
    @SuppressWarnings("unchecked")
    private double similarityFactor(Node node, ControlKind kind, ExtraInfo info)
    {
        if (node == null || this.dialogRectangle == null || kind == null || info == null)
        {
            return 0.0;
        }
        
        try
        {
            Rect        actualRectangle     = relativeRect(this.dialogRectangle, (Rectangle)node.getUserData(IRemoteApplication.rectangleName));
            String      actualName          = node.getNodeName();
            String      actualPath          = XpathViewer.fullXpath("", this.document, node, false, null, true);
            List<Attr>  actualAttr          = extractAttributes(node);
            
            Rect        expectedRectangle   = (Rect)info.get(ExtraInfo.rectangleName);
            String      expectedName        = (String) info.get(ExtraInfo.nodeName);
            String      expectedPath        = (String) info.get(ExtraInfo.xpathName);
            List<Attr>  expectedAttr        = (List<Attr>) info.get(ExtraInfo.attrName);
            
            double sum = 0.0;
            // name
            sum += normalize(Str.areEqual(actualName, expectedName) ? 1.0 : 0.0, WizardSettings.Kind.TYPE);
            
            // position
            Point2D actualPos   = actualRectangle.center();
            Point2D expectedPos = expectedRectangle.center();
            double distance =  Math.sqrt( Math.pow(actualPos.getX() - expectedPos.getX(), 2) + Math.pow(actualPos.getY() - expectedPos.getY(), 2));  
            sum += normalize(1 / (1 + Math.abs(distance)), WizardSettings.Kind.POSITION); 
            
            // size
            double different = actualRectangle.square() - expectedRectangle.square();
            sum += normalize(1 / (1 + Math.abs(different)), WizardSettings.Kind.SIZE); 
            
            // path
            String[] actualPathDim = Str.asString(actualPath).split("/");
            String[] expectedPathDim = Str.asString(expectedPath).split("/");
            int count = 0;
            for (int i = expectedPathDim.length - 1; i >= 0; i--)
            {
                int ind = actualPathDim.length - expectedPathDim.length + i;
                if (ind < 0)
                {
                    break;
                }
                count += (Str.areEqual(expectedPathDim[i], actualPathDim[ind]) ? 1 : 0);
            }
            sum += normalize(count / expectedPathDim.length, WizardSettings.Kind.PATH);
            
            // attributes
            double attrFactor = 0.0;
            if (actualAttr != null && expectedAttr != null && expectedAttr.size() > 0)
            {
                Set<Attr> s1 = new HashSet<>(actualAttr);
                Set<Attr> s2 = new HashSet<>(expectedAttr);
                s2.retainAll(s1);
                
                attrFactor = (double)s2.size() / (double)expectedAttr.size();
            }
            sum += normalize(attrFactor, WizardSettings.Kind.PATH);
            
            sum *= wizardSettings.scale();
            
            return sum; 
        }
        catch (Exception e)
        {
            return 0.0;
        }
    }
    
    private Rect relativeRect(Rectangle relative, Rectangle rect)
    {
        if (relative == null || rect == null)
        {
            return new Rect();
        }
        
        if (relative.height == 0 || relative.width == 0)
        {
            return new Rect();
        }
        
        double scaleX = 1 / relative.getWidth();
        double scaleY = 1 / relative.getHeight();
        
        return new Rect(rect.getX() * scaleX, rect.getY() * scaleY, (rect.getX() + rect.getWidth()) * scaleX, (rect.getY() + rect.getHeight()) * scaleY);
    }

    private double normalize(double value, WizardSettings.Kind kind)
    {
        double min = this.wizardSettings.getMin(kind);
        double max = this.wizardSettings.getMax(kind);
        
        return min + (max - min)*value;
    }
    
    //----------------------------------------------------------------------------------------------
	public void arrangeOne(Node node, ElementWizardBean bean, TreeItemState state) throws Exception
	{
		switch (state)
		{
			case ADD:
				if (bean == null)
				{
					Locator locator = compile(composeId(node), composeKind(node), node);
					if (locator != null)
					{
					    AbstractControl control = AbstractControl.create(locator, this.selfControl.getID());
	                    updateExtraInfo(this.window, node, control);
	                	this.window.addControl(SectionKind.Run, control);
					}
				}
				else
				{
					Locator locator = compile(bean.getId(), bean.getControlKind(), node);
					if (locator != null)
					{
					    AbstractControl control = AbstractControl.create(locator, this.selfControl.getID());
	                    updateExtraInfo(this.window, node, bean.getAbstractControl());
	                    Section section = (Section)this.window.getSection(SectionKind.Run);
	                    section.replaceControl(bean.getAbstractControl(), control);
					}
				}
                displayElements();
				break;

			case MARK:
				updateExtraInfo(this.window, node, bean.getAbstractControl());
				break;

			case QUESTION:
				break;
		}
	}

	private void updateExtraInfo(IWindow window, Node node, AbstractControl control) throws Exception
    {
		ExtraInfo info = new ExtraInfo();
		Rectangle rec = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
		Rect rectangle = relativeRect(this.dialogRectangle, rec);

		info.set(ExtraInfo.xpathName, 		XpathViewer.fullXpath("", this.document, node, false, null, true));
		info.set(ExtraInfo.nodeName, 		node.getNodeName());
		info.set(ExtraInfo.rectangleName, 	rectangle);
		List<Attr> attributes = extractAttributes(node);
		if (!attributes.isEmpty())
		{
			info.set(ExtraInfo.attrName, attributes);
		}
		control.set(AbstractControl.infoName, info);
    }

	private List<Attr> extractAttributes(Node node)
	{
		List<Attr> attributes = new ArrayList<>();
		for (int index = 0; index < node.getAttributes().getLength(); index++)
		{
			Node attr = node.getAttributes().item(index);
			attributes.add(new Attr(attr.getNodeName(), attr.getNodeValue()));
		}
		return attributes;
	}

	private Locator compile(String id, ControlKind kind, Node node)
	{
		// try many methods here
		Locator locator = null;

		locator = locatorById(id, kind, node);
		if (locator != null)
		{
			return locator;
		}

		locator = locatorByAttr(id, kind,  node);
		if (locator != null)
		{
			return locator;
		}

		locator = locatorByXpath(id, kind,  node);
		if (locator != null)
		{
			return locator;
		}

		return null; // can't compile the such locator
	}

	private String composeId(Node node)
	{
        String res = null;
		if (node.hasAttributes())
		{
	        res = composeFromAttr(res, node, LocatorFieldKind.UID);
            res = composeFromAttr(res, node, LocatorFieldKind.NAME);
            res = composeFromAttr(res, node, LocatorFieldKind.TITLE);
            res = composeFromAttr(res, node, LocatorFieldKind.ACTION);
		}
		return res;
	}

    private String composeFromAttr(String res, Node node, LocatorFieldKind kind)
    {
        if (res != null)
        {
            return res;
        }
        String attrName = this.pluginInfo.attributeName(kind);
        if (node.hasAttributes())
        {
	        Node attrNode = node.getAttributes().getNamedItem(attrName);
	        if (attrNode == null)
	        {
	        	return null;
	        }
	        		
	        String attr = attrNode.getNodeValue();
	        if (!Str.IsNullOrEmpty(attr) && isStable(attr))
	        {
	        	return attr;
	        }
        }
        return null;
    }

	private ControlKind composeKind(Node node)
	{
		String name = node.getNodeName();
		return this.pluginInfo.controlKindByNode(name);
	}

    private Locator locatorById(String id, ControlKind kind, Node node)
    {
        if (node.hasAttributes())
        {
            String idName = this.pluginInfo.attributeName(LocatorFieldKind.UID);
            Node nodeId = node.getAttributes().getNamedItem(idName);
            if (nodeId != null)
            {
                String uid = nodeId.getNodeValue();
                if (isStable(uid))
                {
                    Locator locator = new Locator().kind(kind).id(id).uid(uid);
                    if (tryLocator(locator, node) == 1)
                    {
                        return locator;
                    }
                }
            }
        }
        return null;
    }

	private Locator locatorByAttr(String id, ControlKind kind, Node node)
	{
        List<Pair> list = new ArrayList<>();
		addAttr(list, node, LocatorFieldKind.UID);
		addAttr(list, node, LocatorFieldKind.CLAZZ);
		addAttr(list, node, LocatorFieldKind.NAME);
		addAttr(list, node, LocatorFieldKind.TITLE);
		addAttr(list, node, LocatorFieldKind.ACTION);
		addAttr(list, node, LocatorFieldKind.TOOLTIP);
		addAttr(list, node, LocatorFieldKind.TEXT);
        
		List<List<Pair>> cases = IntStream.range(1, 1 << list.size())
			.boxed()
			.sorted((a,b) -> Integer.bitCount(a) - Integer.bitCount(b))
			.limit(MAX_TRIES)
			.map(e -> shuffle(e, list))
			.collect(Collectors.toList());
		
		for (List<Pair> caze : cases) 
		{
			Locator locator = new Locator().kind(kind).id(id);
			caze.forEach(p -> locator.set(p.kind, p.value));
			if (tryLocator(locator, node) == 1) 
			{
				return locator;
			}
		}
        
        return null;
	}
	
	private static List<Pair> shuffle(int mask, List<Pair> source)
	{
		List<Pair> res = new ArrayList<>();
		int oneBit = 0;
		while((oneBit = Integer.lowestOneBit(mask)) != 0)
		{
			res.add(source.get(Integer.numberOfTrailingZeros(mask)));
			mask ^= oneBit;
		}
		return res;
	}
	
	
	private static class Pair
	{
		public Pair(LocatorFieldKind kind, String value) 
		{
			this.kind = kind;
			this.value = value;
		}
		
		public LocatorFieldKind kind;
		public String value;
	}
	
	private void addAttr(List<Pair> list, Node node, LocatorFieldKind kind)
	{
		if (!node.hasAttributes())
		{
			return;
		}
		String attrName = this.pluginInfo.attributeName(kind);
		Node attrNode = node.getAttributes().getNamedItem(attrName);
		if (attrNode == null)
		{
			return;
		}
		String value = attrNode.getNodeValue();
		if (isStable(value))
		{
			list.add(new Pair(kind, value));
		}
	}

	private Locator locatorByXpath(String id, ControlKind kind, Node node)
	{
        String xpath = XpathViewer.fullXpath("", this.document, node, false, null, true);
        
        String[] parts = xpath.split("/");
        int stepLimit = Math.max(1, (int)(parts.length * NEAREST_PARENT_FACTOR));

        List<String> parameters = new ArrayList<>();
        parameters.add(this.pluginInfo.attributeName(LocatorFieldKind.UID));

        Node parent = node; 
        for (int level = 0; level < stepLimit; level++)
        {
            parent = parent.getParentNode();
            String relativePath = XpathViewer.fullXpath("", null, parent, false, parameters, false);
            Locator relativeLocator = new Locator().kind(kind).id(id).absoluteXpath(true).xpath(relativePath);
            if (tryLocator(relativeLocator, parent) == 1)
            {
                String finalPath = XpathViewer.fullXpath(relativePath, parent, node, false, null, false);
                Locator finalLocator = new Locator().kind(kind).id(id).absoluteXpath(true).xpath(finalPath);
                if (tryLocator(finalLocator, node) == 1)
                {
                    return finalLocator;
                }
            }
        }
	    
        Locator locator = new Locator().kind(kind).id(id).absoluteXpath(true).xpath(xpath);
        if (tryLocator(locator, node) == 1)
        {
            return locator;
        }

        return null;
	}

	private int tryLocator(Locator locator, Node node)
	{
		if (locator == null)
		{
			return 0;
		}

		try
        {
            List<Node> list = findAll(locator);
            if (list.size() != 1)
            {
            	return list.size();
            }

            if (list.get(0) == node)
            {
            	return 1;
            }
        }
        catch (Exception e)
        {
            // nothing to do
        }
		return 0;
	}

    private List<Node> findAll(Locator locator) throws Exception
    {
        return this.matcher.findAll(this.document, locator);
    }

	private boolean isStable(String identifier)
	{
		if (Str.IsNullOrEmpty(identifier))
		{
			return false;
		}
		else if (identifier.matches(".*\\d+.*"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	//----------------------------------------------------------------------------------------------


	public void show()
	{
		this.controller.show();
	}

	public void setOnAccept(Consumer<IWindow> consumer)
	{
		this.consumer = consumer;
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
						dialogRectangle = rectangle;
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

		this.documentService.setOnSucceeded(event ->
		{
			this.document = (Document) event.getSource().getValue();
			this.controller.displayTree(this.document, xOffset, yOffset);
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
		this.imageService.start();
		this.documentService.start();
	}

	void close(boolean needAccept, List<ElementWizardBean> list)
	{
		if (needAccept)
		{
			Section section = (Section) this.window.getSection(IWindow.SectionKind.Run);
			section.getControls().forEach(section::removeControl);
			list.stream().map(ElementWizardBean::getAbstractControl).forEach(abstractControl -> Common.tryCatch(() -> section.addControl(abstractControl), "Error on add control"));
			Optional.ofNullable(this.consumer).ifPresent(c -> c.accept(this.window));
		}
		this.controller.close();
	}

	void updateId(ElementWizardBean bean, String newId) throws Exception
	{
		AbstractControl abstractControl = bean.getAbstractControl();
		abstractControl.set(AbstractControl.idName, newId);
		updateBean(abstractControl, bean);
		this.controller.displayElement(bean);
	}

	void updateControlKind(ElementWizardBean bean, ControlKind kind) throws Exception
	{
		AbstractControl oldControl = bean.getAbstractControl();
		AbstractControl newControl = AbstractControl.createCopy(oldControl, kind);

		updateBean(newControl, bean);
		updateCountElement(bean);
		this.controller.displayElement(bean);
	}

	void changeElement(ElementWizardBean bean) throws Exception
	{
		AbstractControl newControl = this.controller.editElement(AbstractControl.createCopy(bean.getAbstractControl()));
		if (newControl != null)
		{
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
			this.window.removeControl(bean.getAbstractControl());
			List<ElementWizardBean> remove = this.controller.remove(bean);
			for (int i = 0; i < remove.size(); i++)
			{
				ElementWizardBean bean1 = remove.get(i);
				boolean isNew = bean1.getIsNew();
				updateBean(bean1.getAbstractControl(), bean1);
				bean1.setIsNew(isNew);
				bean1.setNumber(i);
				this.controller.displayElement(bean1);
			}
		}
	}

	void updateRelation(ElementWizardBean bean)
	{
		this.controller.clearAndAddRelation(bean);
	}

	void findElements(List<ElementWizardBean> items) throws Exception
	{
		for (ElementWizardBean item : items)
		{
			updateCountElement(item);
			this.controller.displayElement(item);
		}
	}

	void generateOnOpen() throws Exception
	{
		AbstractControl onOpen = generate(Addition.WaitToAppear);
		Section section = (Section) this.window.getSection(IWindow.SectionKind.OnOpen);
		section.clearSection();
		section.addControl(onOpen);
		updateOnButtons();
	}

	void generateOnClose() throws Exception
	{
		AbstractControl onClose = generate(Addition.WaitToDisappear);
		Section section = (Section) this.window.getSection(IWindow.SectionKind.OnClose);
		section.clearSection();
		section.addControl(onClose);
		updateOnButtons();
	}

	//region private methods
	private void updateOnButtons()
	{
		boolean onOpenEmpty = this.window.getSection(IWindow.SectionKind.OnOpen).getControls().isEmpty();
		boolean onCloseEmpty = this.window.getSection(IWindow.SectionKind.OnClose).getControls().isEmpty();
		this.controller.displayOnButtons(onOpenEmpty, onCloseEmpty);
	}

	private AbstractControl generate(Addition addition) throws Exception
	{
		AbstractControl on = AbstractControl.create(ControlKind.Wait);
		on.set(AbstractControl.refIdName, this.selfControl.getID());
		on.set(AbstractControl.additionName, addition);
		on.set(AbstractControl.timeoutName, 5000);
		on.set(AbstractControl.idName, addition == Addition.WaitToAppear ? "waitOpen" : "waitClose");
		return on;
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void updateCountElement(ElementWizardBean bean) throws Exception
	{
		int count = 0;
		Node found = null;
		AbstractControl abstractControl = bean.getAbstractControl();
		Locator locator = abstractControl.locator();
		List<Node> nodeList;
        try
        {
            nodeList = this.matcher.findAll(this.document, locator);
            count = nodeList.size();
            found = count > 0 ? nodeList.get(0) : null;
        }
        catch (Exception e)
        {
            // nothing to do
        }

		if (count == 1)
		{
			this.controller.foundGreat(found, bean, TreeItemState.MARK);
		}
		else if (count > 1 || count == 0)
		{
			Node bestIndex = findBestIndex(bean);
			this.controller.foundGreat(bestIndex, bean, TreeItemState.QUESTION);
		}
		bean.setCount(count);
	}

	private void displayElements()
	{
		int[] a = new int[]{0};
		this.displayElements(entry -> this.create(a[0]++, entry, false));
	}

	private void displayElements(Function<AbstractControl, ElementWizardBean> mapFunction)
	{
		List<ElementWizardBean> list = this.window.getSection(SectionKind.Run).getControls().stream().map(iC -> ((AbstractControl) iC)).map(mapFunction).collect(Collectors.toList());
		this.controller.displayElements(list);
	}

	private ElementWizardBean create(int number, AbstractControl control, boolean isNew)
	{
		return new ElementWizardBean(control, number, control.getID(), control.getBindedClass(), ((control.getXpath() != null && !control.getXpath().isEmpty()) || control.useAbsoluteXpath()), isNew, 0);
	}

	private void updateBean(AbstractControl control, ElementWizardBean bean)
	{
		bean.setAbstractControl(control);
		bean.setControlKind(control.getBindedClass());
		bean.setId(control.getID());
		bean.setIsNew(true);
		bean.setXpath((control.getXpath() != null && !control.getXpath().isEmpty()));
	}
	//endregion
}
