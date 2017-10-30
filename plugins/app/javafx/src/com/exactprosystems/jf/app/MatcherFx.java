package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.LocatorFieldKind;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.rmi.RemoteException;

public class MatcherFx<T extends Node>
{
    public MatcherFx(PluginInfo info, Locator locator, Node owner) throws RemoteException
    {
        if (locator == null)
        {
            throw new NullParameterException("locator");
        }
        this.locator = locator;
        this.info = info;

        String xpath = this.locator.getXpath();
        if (!Str.IsNullOrEmpty(xpath) && owner != null)
        {
            try
            {
                org.w3c.dom.Document document = createDocument(this.info, owner, true, false);
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                org.w3c.dom.Node root = document;
                try
                {
                    XPathExpression compile = xPath.compile("/*");
                    root = (org.w3c.dom.Node) compile.evaluate(document, XPathConstants.NODE);
                }
                catch (XPathExpressionException e)
                {
                }

                this.nodelist = (NodeList) xPath.compile(xpath).evaluate(root, XPathConstants.NODESET);
                logger.debug("Found by xpath : " + nodelist.getLength());
            }
            catch (Exception pe)
            {
                logger.error(pe.getMessage(), pe);
                throw new ElementNotFoundException("Wrong xpath: " + xpath, locator);
            }
        }

        logger.debug("=========================================");
        logger.debug("Matcher locator = " + locator);
    }

    private static org.w3c.dom.Document createDocument(PluginInfo info, Node owner, boolean addItems, boolean addRectangles) throws ParserConfigurationException
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.newDocument();
        buildDom(info, document, document, owner, addItems, addRectangles);

        return document;
    }

    private static void buildDom(PluginInfo info, org.w3c.dom.Document document, org.w3c.dom.Node current, Node component, boolean addItems, boolean addRectangles)
    {
        if (component == null)
        {
            return;
        }

        Element node;
        String simpleName = component.getClass().getSimpleName();
        if (simpleName.isEmpty())
        {
            simpleName = component.getClass().getName();
        }
        String tagName = simpleName.replaceAll("\\$", "_");
        try
        {
            node = document.createElement(tagName);
        }
        catch (DOMException e)
        {
            logger.error("Current component : " + component);
            logger.error("Error on create element with tag : '" + tagName + "'. Component class simple name : '"
                    + simpleName + "'.");
            throw e;
        }
        if (addItems)
        {
            node.setUserData(itemName, component, null);
        }
        if (addRectangles)
        {
            node.setAttribute(IRemoteApplication.rectangleName, Converter.rectangleToString(getRect(component)));
            node.setAttribute(IRemoteApplication.visibleName, "" + component.isVisible());
        }

        String className    = info.attributeName(LocatorFieldKind.CLAZZ);
        String nameName     = info.attributeName(LocatorFieldKind.NAME);
        String titleName    = info.attributeName(LocatorFieldKind.TITLE);
        String actionName   = info.attributeName(LocatorFieldKind.ACTION);
        String tooltipName  = info.attributeName(LocatorFieldKind.TOOLTIP);

        node.setAttribute(actionName,   getAction(component));
        node.setAttribute(titleName,    getTitle(component));
        node.setAttribute(tooltipName,  getToolTip(component));
        node.setAttribute(nameName,     getName(component));
        node.setAttribute(className,    getClass(component));
        String textContent = getText(component);
        if (!Str.IsNullOrEmpty(textContent))
        {
            node.setTextContent(textContent);
        }

        current.appendChild(node);
        if (component instanceof Parent)
        {
            Parent container = (Parent) component;

            for (Node child : container.getChildrenUnmodifiable())
            {
                buildDom(info, document, node, child, addItems, addRectangles);
            }
        }
    }

    public static String getText(Node obj)
    {
        if (obj instanceof TableView)
        {
            StringBuilder sb = new StringBuilder();
            TableView table = (TableView) obj;
            for (int rowNum = 0; rowNum < table.getItems().size(); ++rowNum)
            {
                for (int colNum = 0; colNum < table.getColumns().size(); ++colNum)
                {
                    TableColumn column = ((TableColumn)((TableView)obj).getColumns().get(colNum));
                    String someText = String.valueOf(column.getCellObservableValue(rowNum).getValue());
                    if (someText != null && !someText.isEmpty())
                    {
                        sb.append(someText);
                        sb.append('|');
                    }
                }
                sb.append('\n');
            }
            return sb.toString();
        }
        else if (obj instanceof Text)
        {
            return ((Text) obj).getText();
        }
        else if (obj instanceof Button)
        {
            return ((Button) obj).getText();
        }
        else if (obj instanceof Label)
        {
            return ((Label) obj).getText();
        }
//        else if (obj instanceof Tooltip)
//        {
//            return ((Tooltip) obj).getTipText();
//        }
        else
        {
            return null;
        }
    }

    private static String getAction(Node obj)
    {
        String objAction = null;

        if (obj instanceof ComboBox)
        {
            objAction = ((ComboBox<?>) obj).getOnAction().toString();
        }
        else if (obj instanceof Button)
        {
            objAction = ((Button) obj).getOnAction().toString();
        }
        else if (obj instanceof RadioButton)
        {
            objAction = ((RadioButton) obj).getOnAction().toString();
        }
        else if (obj instanceof ToggleButton)
        {
            objAction = ((ToggleButton) obj).getOnAction().toString();
        }
//        else if (obj instanceof MenuItem)
//        {
//            objAction = ((MenuItem) obj).getActionCommand();
//        }

        return objAction;
    }

    private static String getTitle(Object obj)
    {
        String objTitle = null;

        if (obj instanceof Stage)
        {
            objTitle = ((Stage) obj).getTitle();
        }
        return objTitle;
    }

    private static String getToolTip(Node obj)
    {
        String objText = null;

        if (obj instanceof Control)
        {
            objText = ((Control) obj).getTooltip().getText();
        }

        return objText;
    }

    private static String getName(Node obj)
    {
        String objText = null;

        if (obj != null)
        {
            objText = obj.getId();
        }

        return objText;
    }

    private static String getClass(Node obj)
    {
        String objText = null;

        if (obj != null)
        {
            objText = obj.getClass().getName();
        }

        return objText;
    }

    static Rectangle getRect(Node node)
    {
        logger.debug("getRect is showing ? " + node.isVisible());
        logger.debug("component " + node);
        logger.debug("component hc" + node.hashCode());
        if (node.isVisible())
        {
            Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
            int x = (int) screenBounds.getMinX();
            int y = (int) screenBounds.getMinY();
            int width = (int) screenBounds.getWidth();
            int height = (int) screenBounds.getHeight();
            return new Rectangle(x, y, width, height);
        }
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MatcherFx :").append("\n");
        if (nodelist != null)
        {
            builder.append("found by xpath : ").append(nodelist.getLength()).append("\n");
        }
        if (locator != null)
        {
            builder.append("locator : ").append(locator);
        }
        return builder.toString();
    }

    static void setLogger(Logger logger)
    {
        MatcherFx.logger = logger;
    }

    private static Logger logger   = null;
    private Locator locator;
    static final String itemName = "item";
    private PluginInfo info;
    private NodeList nodelist;
}