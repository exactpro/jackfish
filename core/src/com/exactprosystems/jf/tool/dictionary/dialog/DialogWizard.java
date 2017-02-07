package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.documents.guidic.Attr;
import com.exactprosystems.jf.documents.guidic.ExtraInfo;
import com.exactprosystems.jf.documents.guidic.Rect;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.xpath.ImageAndOffset;
import com.exactprosystems.jf.tool.custom.xpath.XpathTreeItem;
import com.exactprosystems.jf.tool.custom.xpath.XpathTreeItem.TreeItemState;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.dialog.WizardSettings.Kind;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.DialogEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DialogWizard
{
	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	private DictionaryFx dictionary;
	private IWindow window;
	private IControl selfControl;

	private DialogWizardController controller;
	private AppConnection appConnection;
	private PluginInfo pluginInfo;
	private WizardSettings wizardSettings;

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
		this.pluginInfo = appConnection.getApplication().getFactory().getInfo();
		// TODO replace it to settings from Settings
		this.wizardSettings = new WizardSettings();
		this.wizardSettings.setMax(Kind.TYPE,       1);
        this.wizardSettings.setMin(Kind.TYPE,       -1);
        this.wizardSettings.setMax(Kind.PATH,       1);
        this.wizardSettings.setMin(Kind.PATH,       0);
        this.wizardSettings.setMax(Kind.POSITION,   1);
        this.wizardSettings.setMin(Kind.POSITION,   0);
        this.wizardSettings.setThreshold(0.6);

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
		updateOnButtons();
	}

	//----------------------------------------------------------------------------------------------
    // sophisticated functions
    //----------------------------------------------------------------------------------------------
    // TODO
    public Node findBestIndex(ControlKind kind, ExtraInfo info, Rectangle rect, Document doc)
    {
        if (kind == null || info == null || rect == null || doc == null)
        {
            return null;
        }
        Map<Double, Node> candidates = new HashMap<>();
        
        passTree(doc, node -> candidates.put(similarityFactor(node, rect, kind, info), node));
        Double maxKey = candidates.keySet().stream().max(Double::compare).get();
        return maxKey != null && maxKey > this.wizardSettings.getThreshold() ? candidates.get(maxKey) : null;
    }
    
    private void passTree(Node node, Consumer<Node> func)
    {
        IntStream.range(0, node.getChildNodes().getLength())
            .mapToObj(node.getChildNodes()::item)
            .filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
            .forEach(item -> passTree(item, func));
    }
    
    private double similarityFactor(Node node, Rectangle rect, ControlKind kind, ExtraInfo info)
    {
        if (node == null || rect == null || kind == null || info == null)
        {
            return 0.0;
        }
        
        try
        {
            Rectangle   actualRectangle     = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
            String      actualName          = node.getNodeName();
            String      actualPath          = XpathViewer.fullXpath("", this.document, node, false, null, true);
            List<Attr>  actualAttr          = null;
            
            Rect rec = (Rect)info.get(ExtraInfo.rectangleName);
            Rectangle   expectedRectangle   = new Rectangle(rec.getX1(), rec.getY1(), rec.getX2() - rec.getX1(), rec.getY2() - rec.getY1());
            String      expectedName        = (String) info.get(ExtraInfo.nodeName);
            String      expectedPath        = (String) info.get(ExtraInfo.xpathName);
            List<Attr>  expectedAttr        = (List<Attr>) info.get(ExtraInfo.attrName);
        }
        catch (Exception e)
        {
            return 0.0;
        }
        
        return 0.4;
    }
    
    //----------------------------------------------------------------------------------------------
    public void arrangeAll(List<XpathTreeItem> list) throws Exception
    {
        for (XpathTreeItem mark : list)
        {
            TreeItemState state = mark.getState();
            Node node = mark.getNode();
            if (state != null)
            {
                switch (state)
                {
                    case ADD:
                        String id = composeId(node);
                        ControlKind kind = composeKind(node);
                        Locator locator = compile(id, kind, this.document, node);
                        // put locator into ...
                        updateAllExtraInfo(this.window, mark);
                        break;
                        
                    case MARK:
                        updateAllExtraInfo(this.window, mark);
                        break;
                        
                    case QUESTION:
                        break;
                }
            }
            
        }
    }
    
    private void updateAllExtraInfo(IWindow window, XpathTreeItem mark) throws Exception
    {
        // TODO check the attr 
        for (ElementWizardBean element : mark.getList())
        {
            System.err.println(">> elementId " + element.getId());
            
            AbstractControl control = (AbstractControl)window.getControlForName(SectionKind.Run, element.getId());
            ExtraInfo info = new ExtraInfo();
            Node node = mark.getNode();
            
            Rectangle rec = mark.getRectangle();
            Rect rectangle = new Rect(rec.x, rec.y, rec.x + rec.width, rec.y + rec.height);            
            
            info.set(ExtraInfo.xpathName,       XpathViewer.fullXpath("", this.document, node, false, null, true));
            info.set(ExtraInfo.nodeName,        node.getNodeName());
            info.set(ExtraInfo.rectangleName,   rectangle);
            List<Attr> attributes = new ArrayList<>();
            for (int index = 0; index < node.getAttributes().getLength(); index++)
            {
                Node attr = mark.getNode().getAttributes().item(index);
                attributes.add(new Attr(attr.getNodeName(), attr.getNodeValue()));
            }
            if (!attributes.isEmpty())
            {
                info.set(ExtraInfo.attrName, attributes);
            }
            control.set(AbstractControl.infoName, info);
        }
    }

    public Locator compile(String id, ControlKind kind, Document doc, Node node)
    {
        // try many methods here
        Locator locator = null;
        
        locator = locatorById(id, kind, doc, node);
        if (locator != null)
        {
            return locator;
        }

        locator = locatorByAttr(id, kind, doc, node);
        if (tryLocator(locator, doc, node) == 1)
        {
            return locator.id(id).kind(kind);
        }
        
        locator = locatorByXpath(id, kind, doc, node);
        if (tryLocator(locator, doc, node) == 1)
        {
            return locator.id(id).kind(kind);
        }
        
        return null; // can't compile the such locator
    }
    
    private String composeId(Node node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private ControlKind composeKind(Node node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private Locator locatorById(String id, ControlKind kind, Document doc, Node node)
    {
        if (node.hasAttributes())
        {
            Node nodeId = node.getAttributes().getNamedItem("id");
            String uid = nodeId.getNodeValue();
            if (isStable(uid))
            {
                Locator locator = new Locator().kind(kind).id(id).uid(uid);
                
                if (tryLocator(locator, doc, node) == 1)
                {
                    return locator;
                }
            }
        }
        return null;
    }

    private Locator locatorByAttr(String id, ControlKind kind, Document doc, Node node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private Locator locatorByXpath(String id, ControlKind kind, Document doc, Node node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private int tryLocator(Locator locator, Document doc, Node node)
    {
        if (locator == null)
        {
            return 0;
        }
        
        List<Node> list = findAll(locator, doc);
        if (list.size() != 1)
        {
            return list.size();
        }
        
        if (list.get(0) == node)
        {
            return 1;
        }
        
        return 0;
    }

    private List<Node> findAll(Locator locator, Document doc)
    {
        return Collections.emptyList();
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

	void updateRelation(ElementWizardBean bean)
	{
		System.out.println("<< Add implementation for relation");
	}

	void findElements(List<ElementWizardBean> items)
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

	private void updateCountElement(ElementWizardBean bean)
	{
		int count = 0;
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
