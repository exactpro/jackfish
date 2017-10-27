package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import javafx.scene.Node;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FXOperationExecutor extends AbstractOperationExecutor<Node>
{
    private Logger logger;

    public FXOperationExecutor(boolean useTrimText)
    {
        super(useTrimText);
    }

    @Override
    protected String getValueDerived(Node component) throws Exception
    {
        return null;
    }

    @Override
    protected List<String> getListDerived(Node component, boolean onlyVisible) throws Exception
    {
        return null;
    }

    @Override
    protected String getDerived(Node component) throws Exception
    {
        return null;
    }

    @Override
    protected String getAttrDerived(Node component, String name) throws Exception
    {
        return null;
    }

    @Override
    protected String scriptDerived(Node component, String script) throws Exception
    {
        return null;
    }

    @Override
    protected String getValueTableCellDerived(Node component, int column, int row) throws Exception
    {
        return null;
    }

    @Override
    protected Map<String, String> getRowDerived(Node component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
    {
        return null;
    }

    @Override
    protected Map <String, String> getRowByIndexDerived(Node component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
    {
        return null;
    }

    @Override
    protected Map <String, ValueAndColor> getRowWithColorDerived(Node component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
    {
        return null;
    }

    @Override
    protected String[][] getTableDerived(Node component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
    {
        return new String[0][];
    }

    @Override
    public Rectangle getRectangle(Node component) throws Exception
    {
        return null;
    }

    @Override
    public Color getColor(Node component, boolean isForeground) throws Exception
    {
        return null;
    }

    @Override
    public List <Node> findAll(ControlKind controlKind, Node window, Locator locator) throws Exception
    {
        return null;
    }

    @Override
    public List <Node> findAll(Locator owner, Locator element) throws Exception
    {
        return null;
    }

    @Override
    public Node find(Locator owner, Locator element) throws Exception
    {
        return null;
    }

    @Override
    public List <Node> findByXpath(Node element, String path) throws Exception
    {
        return null;
    }

    @Override
    public Node lookAtTable(Node table, Locator additional, Locator header, int x, int y) throws Exception
    {
        return null;
    }

    @Override
    public boolean elementIsEnabled(Node component) throws Exception
    {
        return false;
    }

    @Override
    public boolean elementIsVisible(Node component) throws Exception
    {
        return false;
    }

    @Override
    public boolean tableIsContainer()
    {
        return false;
    }

    @Override
    public boolean mouse(Node component, int x, int y, MouseAction action) throws Exception
    {
        return false;
    }

    @Override
    public boolean press(Node component, Keyboard key) throws Exception
    {
        return false;
    }

    @Override
    public boolean upAndDown(Node component, Keyboard key, boolean b) throws Exception
    {
        return false;
    }

    @Override
    public boolean push(Node component) throws Exception
    {
        return false;
    }

    @Override
    public boolean toggle(Node component, boolean value) throws Exception
    {
        return false;
    }

    @Override
    public boolean select(Node component, String selectedText) throws Exception
    {
        return false;
    }

    @Override
    public boolean selectByIndex(Node component, int index) throws Exception
    {
        return false;
    }

    @Override
    public boolean expand(Node component, String path, boolean expandOrCollapse) throws Exception
    {
        return false;
    }

    @Override
    public boolean text(Node component, String text, boolean clear) throws Exception
    {
        return false;
    }

    @Override
    public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
    {
        return false;
    }

    @Override
    public boolean setValue(Node component, double value) throws Exception
    {
        return false;
    }

    @Override
    public Document getTree(Node component) throws Exception
    {
        return null;
    }

    @Override
    public boolean dragNdrop(Node drag, int x1, int y1, Node drop, int x2, int y2, boolean moveCursor) throws Exception
    {
        return false;
    }

    @Override
    public boolean scrollTo(Node component, int index) throws Exception
    {
        return false;
    }

    @Override
    public boolean mouseTable(Node component, int column, int row, MouseAction action) throws Exception
    {
        return false;
    }

    @Override
    public boolean textTableCell(Node component, int column, int row, String text) throws Exception
    {
        return false;
    }

    @Override
    public List <String> getRowIndexes(Node component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
    {
        return null;
    }

    @Override
    public int getTableSize(Node component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
    {
        return 0;
    }

    @Override
    public Color getColorXY(Node component, int x, int y) throws Exception
    {
        return null;
    }
}
