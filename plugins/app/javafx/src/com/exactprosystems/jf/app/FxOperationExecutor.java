package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import javafx.event.EventTarget;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FxOperationExecutor extends AbstractOperationExecutor<EventTarget>
{
    private Logger logger;

    public FxOperationExecutor(boolean useTrimText, Logger logger)
    {
        super(useTrimText);
        this.logger = logger;
    }

	@Override
	protected String getValueDerived(EventTarget component) throws Exception
	{
		return null;
	}

	@Override
	protected List<String> getListDerived(EventTarget component, boolean onlyVisible) throws Exception
	{
		return null;
	}

	@Override
	protected String getDerived(EventTarget component) throws Exception
	{
		return null;
	}

	@Override
	protected String getAttrDerived(EventTarget component, String name) throws Exception
	{
		return null;
	}

	@Override
	protected String scriptDerived(EventTarget component, String script) throws Exception
	{
		return null;
	}

	@Override
	protected String getValueTableCellDerived(EventTarget component, int column, int row) throws Exception
	{
		return null;
	}

	@Override
	protected Map<String, String> getRowDerived(EventTarget component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return null;
	}

	@Override
	protected Map<String, String> getRowByIndexDerived(EventTarget component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		return null;
	}

	@Override
	protected Map<String, ValueAndColor> getRowWithColorDerived(EventTarget component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		return null;
	}

	@Override
	protected String[][] getTableDerived(EventTarget component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
	{
		return new String[0][];
	}

	@Override
	public Rectangle getRectangle(EventTarget component) throws Exception
	{
		return null;
	}

	@Override
	public Color getColor(EventTarget component, boolean isForeground) throws Exception
	{
		return null;
	}

	@Override
	public List<EventTarget> findAll(ControlKind controlKind, EventTarget window, Locator locator) throws Exception
	{
		return null;
	}

	@Override
	public List<EventTarget> findAll(Locator owner, Locator element) throws Exception
	{
		return null;
	}

	@Override
	public EventTarget find(Locator owner, Locator element) throws Exception
	{
		return null;
	}

	@Override
	public List<EventTarget> findByXpath(EventTarget element, String path) throws Exception
	{
		return null;
	}

	@Override
	public EventTarget lookAtTable(EventTarget table, Locator additional, Locator header, int x, int y) throws Exception
	{
		return null;
	}

	@Override
	public boolean elementIsEnabled(EventTarget component) throws Exception
	{
		return false;
	}

	@Override
	public boolean elementIsVisible(EventTarget component) throws Exception
	{
		return false;
	}

	@Override
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean mouse(EventTarget component, int x, int y, MouseAction action) throws Exception
	{
		return false;
	}

	@Override
	public boolean press(EventTarget component, Keyboard key) throws Exception
	{
		return false;
	}

	@Override
	public boolean upAndDown(EventTarget component, Keyboard key, boolean b) throws Exception
	{
		return false;
	}

	@Override
	public boolean push(EventTarget component) throws Exception
	{
		return false;
	}

	@Override
	public boolean toggle(EventTarget component, boolean value) throws Exception
	{
		return false;
	}

	@Override
	public boolean select(EventTarget component, String selectedText) throws Exception
	{
		return false;
	}

	@Override
	public boolean selectByIndex(EventTarget component, int index) throws Exception
	{
		return false;
	}

	@Override
	public boolean expand(EventTarget component, String path, boolean expandOrCollapse) throws Exception
	{
		return false;
	}

	@Override
	public boolean text(EventTarget component, String text, boolean clear) throws Exception
	{
		return false;
	}

	@Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		return false;
	}

	@Override
	public boolean setValue(EventTarget component, double value) throws Exception
	{
		return false;
	}

	@Override
	public Document getTree(EventTarget component) throws Exception
	{
		return null;
	}

	@Override
	public boolean dragNdrop(EventTarget drag, int x1, int y1, EventTarget drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		return false;
	}

	@Override
	public boolean scrollTo(EventTarget component, int index) throws Exception
	{
		return false;
	}

	@Override
	public boolean mouseTable(EventTarget component, int column, int row, MouseAction action) throws Exception
	{
		return false;
	}

	@Override
	public boolean textTableCell(EventTarget component, int column, int row, String text) throws Exception
	{
		return false;
	}

	@Override
	public List<String> getRowIndexes(EventTarget component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return null;
	}

	@Override
	public int getTableSize(EventTarget component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		return 0;
	}

	@Override
	public Color getColorXY(EventTarget component, int x, int y) throws Exception
	{
		return null;
	}
}
