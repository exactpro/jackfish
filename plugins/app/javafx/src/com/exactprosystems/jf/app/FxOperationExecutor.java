package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.TooManyElementsException;
import com.exactprosystems.jf.api.error.app.WrongParameterException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.exactprosystems.jf.app.UtilsFx.tryExecute;

public class FxOperationExecutor extends AbstractOperationExecutor<EventTarget>
{
    private Logger logger;

    private boolean isAltDown = false;
    private boolean isShiftDown = false;
    private boolean isControlDown = false;

    private static final UtilsFx.ICheck EMPTY_CHECK = () -> {};

    public FxOperationExecutor(boolean useTrimText, Logger logger)
    {
        super(useTrimText);
        this.logger = logger;
    }

	@Override
	protected String getValueDerived(EventTarget component) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof Button)
			{
				return ((Button) component).getText();
			}
			if (component instanceof CheckBox)
			{
				return String.valueOf(((CheckBox) component).isSelected());
			}
			if (component instanceof ComboBox)
			{
				return String.valueOf(((ComboBox) component).getSelectionModel().getSelectedItem());
			}
			if (component instanceof Label)
			{
				return ((Label) component).getText();
			}
			if (component instanceof ListView)
			{
				return String.valueOf(((ListView) component).getSelectionModel().getSelectedItem());
			}
			if (component instanceof Pane)
			{
				StringBuilder sb = new StringBuilder();
				MatcherFx.collectAllText(((Pane) component), sb);
				return sb.toString();
			}
			if (component instanceof ProgressBar)
			{
				return String.valueOf(((ProgressBar) component).getProgress());
			}
			if (component instanceof Slider)
			{
				return String.valueOf(((Slider) component).getValue());
			}
			if (component instanceof SplitPane)
			{
				return String.valueOf(((SplitPane) component).getDividerPositions()[0]);
			}
			if (component instanceof Spinner)
			{
				return String.valueOf(((Spinner) component).getValue());
			}
			if (component instanceof TextInputControl)
			{
				return ((TextInputControl) component).getText();
			}
			if (component instanceof ToggleButton)
			{
				return String.valueOf(((ToggleButton) component).isSelected());
			}
			if (component instanceof Tooltip)
			{
				return ((Tooltip) component).getText();
			}
			return null;
		}, e->
		{
			logger.error(String.format("getValueDerived(%s)", component));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected List<String> getListDerived(EventTarget component, boolean onlyVisible) throws Exception
	{
		return tryExecute(EMPTY_CHECK, ()->
		{
			if (component instanceof ComboBox)
			{
				ComboBox comboBox = (ComboBox) component;
				StringConverter converter = comboBox.getConverter();
				List<String> list = new ArrayList<>();
				for (Object item : comboBox.getItems())
				{
					list.add(converter.toString(item));
				}
				return list;
			}
			if (component instanceof TabPane)
			{
				return ((TabPane) component).getTabs()
						.stream()
						.map(Tab::getText)
						.collect(Collectors.toList());
			}
			if (component instanceof ListView)
			{
				return ((ListView<?>) component).getItems()
						.stream()
						.map(Object::toString)
						.collect(Collectors.toList());
			}
			throw new Exception("Target element does not has items");
		}, e->
		{
			logger.error(String.format("getListDerived(%s,%s)", component, onlyVisible));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected String getDerived(EventTarget component) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof Pane)
			{
				StringBuilder sb = new StringBuilder();
				MatcherFx.collectAllText((Pane) component, sb);
				return sb.toString();
			}
			return MatcherFx.getText(component);
		}, e ->
		{
			logger.error(String.format("getDerived(%s)", component));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected String getAttrDerived(EventTarget component, String name) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
			String methodName = "get" + firstLetter + name.substring(1);
			Method[] methods = component.getClass().getMethods();
			for (Method method : methods)
			{
				if (method.getName().equals(methodName) && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0)
				{
					Object invoke = method.invoke(component);
					return String.valueOf(invoke);
				}
			}
			return "";
		}, e ->
		{
			logger.error(String.format("getAttrDerived(%s,%s)", component, name));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected String scriptDerived(EventTarget component, String script) throws Exception
	{
		throw new FeatureNotSupportedException("script");
	}

	@Override
	protected String getValueTableCellDerived(EventTarget target, int column, int row) throws Exception
	{
		return tryExecute(EMPTY_CHECK, ()->
		{
			if (target instanceof TableView)
			{
				TableView<?> tableView = (TableView) target;
				if (tableView.getItems().size() < row)
				{
					throw new Exception(String.format("Can't get value for row %s because size of rows is %s", row, tableView.getItems().size()));
				}

				if (tableView.getColumns().size() < column)
				{
					throw new Exception(String.format("Can't get value for column %s because size of columns is %s", column, tableView.getColumns().size()));
				}

				TableColumn<?,?> tableColumn = tableView.getColumns().get(column);
				ObservableValue<?> cellObservableValue = tableColumn.getCellObservableValue(row);
				return String.valueOf(cellObservableValue.getValue());
			}
			throw tableException(target);
		}, e->
		{
			logger.error(String.format("getValueTableCellDerived(%s,%s,%s)", target, column, row));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected Map<String, String> getRowDerived(EventTarget target, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			List<Map<String, Object>> listOfRows = new ArrayList<>();
			if (target instanceof TableView)
			{
				TableView<?> tableView = (TableView<?>) target;
				List<String> tableHeaders = getTableHeaders(tableView, columns);
				for (int i = 0; i < tableView.getItems().size(); i++)
				{
					Map<String, Object> row = new LinkedHashMap<>();
					for (int j = 0; j < tableHeaders.size(); j++)
					{
						TableColumn<?,?> tableColumn = tableView.getColumns().get(j);
						ObservableValue<?> cellObservableValue = tableColumn.getCellObservableValue(i);
						row.put(tableHeaders.get(j), cellObservableValue.getValue());
					}

					if (Objects.nonNull(valueCondition) && valueCondition.isMatched(row))
					{
						listOfRows.add(row);
					}
				}
				if (listOfRows.size() > 1)
				{
					throw new Exception("Too many rows");
				}
				if (listOfRows.isEmpty())
				{
					throw new Exception("No one row was found");
				}
				Map<String, String> map = new LinkedHashMap<>();
				listOfRows.get(0).forEach((s, o) -> map.put(s, String.valueOf(o)));
				return map;
			}
			throw tableException(target);
		}, e->
		{
			logger.error(String.format("getRowDerived(%s,%s,%s,%s,%s,%s,%s)", target, additional, header, useNumericHeader, Arrays.toString(columns), valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected Map<String, String> getRowByIndexDerived(EventTarget target, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			Map<String, String> result = new LinkedHashMap<>();
			if (target instanceof TableView)
			{
				TableView<?> tableView = (TableView<?>) target;
				List<String> tableHeaders = getTableHeaders(tableView, columns);
				for (int j = 0; j < tableHeaders.size(); j++)
				{
					String cellValue = getValueTableCellDerived(tableView, j, i);
					result.put(tableHeaders.get(j), cellValue);
				}
				return result;
			}
			throw tableException(target);
		}, e->
		{
			logger.error(String.format("getRowByIndex(%s,%s,%s,%s,%s,%s)", target, additional, header, useNumericHeader, Arrays.toString(columns), i));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected Map<String, ValueAndColor> getRowWithColorDerived(EventTarget target, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			Map<String, ValueAndColor> result = new LinkedHashMap<>();
			if (target instanceof TableView)
			{
				TableView tableView = (TableView<?>) target;
				List<String> tableHeaders = getTableHeaders(tableView, columns);
				for (int j = 0; j < tableHeaders.size(); j++)
				{
					String headerName = tableHeaders.get(j);
					String cellValue = getValueTableCellDerived(tableView, j, i);

//					TODO think how we can get color from row?
//					TableCell call = (TableCell) ((TableColumn) tableView.getColumns().get(0)).getCellFactory().call(tableView.getColumns().get(0));
//					TableRow tableRow = call.getTableRow();
//					Paint fill = tableRow.getBackground().getFills().get(0).getFill();

					result.put(headerName, new ValueAndColor(cellValue, Color.BLACK, Color.WHITE));
				}
				return result;
			}
			throw tableException(target);
		}, e->
		{
			logger.error(String.format("getRowWithColorDerived(%s,%s,%s,%s,%s,%s)", target, additional, header, useNumericHeader, Arrays.toString(columns), i));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	protected String[][] getTableDerived(EventTarget target, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (target instanceof TableView)
			{
				TableView<?> tableView = (TableView<?>) target;
				int rowsCount = tableView.getItems().size();
				int columnsCount = tableView.getColumns().size();

				String[][] res = new String[rowsCount + 1][columnsCount];
				ObservableList<? extends TableColumn<?, ?>> tableColumns = tableView.getColumns();

				this.fillHeaders(res, tableView, columns);
				IntStream.range(0, rowsCount)
						.forEach(i -> IntStream.range(0, tableColumns.size())
								.forEach(j -> res[i + 1][j] = Str.asString(tableColumns.get(j).getCellObservableValue(i).getValue()))
						);
				return res;
			}
			throw tableException(target);
		}, e ->
		{
			this.logger.error(String.format("getTable(%s,%s,%s,%s,%s)", target, additional, header, useNumericHeader, Arrays.toString(columns)));
			this.logger.error(e.getMessage(), e);
		});
	}

	@Override
	public Rectangle getRectangle(EventTarget target) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				() -> MatcherFx.getRect(target),
				e->
				{
					this.logger.error(String.format("getRectangle(%s)", target));
					this.logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public Color getColor(EventTarget target, boolean isForeground) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			Rectangle rectangle = getRectangle(target);
			double xOffset = rectangle.width * 0.1;
			double yOffset = rectangle.height * 0.1;
			Point point = this.getPointLocation(target, (int) xOffset, (int) yOffset);
			return new Robot().getPixelColor(point.x, point.y);
		}, e ->
		{
			this.logger.error(String.format("getColorXY(%s)", target));
			this.logger.error(e.getMessage(), e);
		});
	}

	@Override
	public List<EventTarget> findAll(ControlKind controlKind, EventTarget window, Locator locator) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				() ->
				{
					if (window instanceof Node)
					{
						return new MatcherFx(this.info, locator, (Node) window).findAll();
					}
					throw new Exception("Window is not a node");
				},
				e->
				{
					logger.error(String.format("findAll(%s,%s,%s)", controlKind, window, locator));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public List<EventTarget> findAll(Locator owner, Locator element) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					logger.debug(String.format("Start found owner : %s", owner));

					Node ownerNode;

					if (owner != null)
					{
						List<EventTarget> targets = new MatcherFx(this.info, owner, UtilsFx.currentRoot()).findAll();
						logger.debug(String.format("Found owners size %s", targets.size()));

						if (targets.isEmpty())
						{
							throw new ElementNotFoundException("owner", owner);
						}

						if (targets.size() > 1)
						{
							targets.stream().map(MatcherFx::targetToString).forEach(s -> logger.debug(String.format("Found %s", s)));
							throw new TooManyElementsException(Integer.toString(targets.size()), owner);
						}

						EventTarget target = targets.get(0);
						if (!(target instanceof Node))
						{
							logger.debug(String.format("Owner not instance of Node. Owner : %s", MatcherFx.targetToString(target)));
							throw new ElementNotFoundException("Found owner not a node");
						}
						ownerNode = (Node) target;
					}
					else
					{
						ownerNode = UtilsFx.currentRoot();
					}
					logger.debug(String.format("Found owner : %s", MatcherFx.targetToString(ownerNode)));

					return new MatcherFx(this.info, element, ownerNode).findAll();
				},
				e->
				{
					logger.error(String.format("findAll(%s,%s)", owner, element));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public EventTarget find(Locator owner, Locator element) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					logger.debug(String.format("Start find for owner = %s and element = %s", owner, element));
					List<EventTarget> targets = this.findAll(owner, element);
					logger.debug(String.format("Found %s elements", targets.size()));
					if (targets.isEmpty())
					{
						throw new ElementNotFoundException(element);
					}
					if (targets.size() > 1)
					{
						targets.stream().map(MatcherFx::targetToString).forEach(s -> logger.debug(String.format("Found %s", s)));
						throw new TooManyElementsException(Integer.toString(targets.size()), element);
					}
					return targets.get(0);
				},
				e ->
				{
					logger.error(String.format("find(%s,%s)", owner, element));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public List<EventTarget> findByXpath(EventTarget element, String path) throws Exception
	{
		//TODO implement
		return null;
	}

	@Override
	public EventTarget lookAtTable(EventTarget table, Locator additional, Locator header, int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("lookAtTable");
	}

	@Override
	public boolean elementIsEnabled(EventTarget component) throws Exception
	{
		return component instanceof Node && !((Node) component).isDisable();
	}

	@Override
	public boolean elementIsVisible(EventTarget component) throws Exception
	{
		return component instanceof Node && !((Node) component).isVisible();
	}

	@Override
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean mouse(EventTarget component, int x, int y, MouseAction action) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof Node)
			{
				Node node = (Node) component;
				Point point = this.checkCoords(node, x, y);

				List<Event> eventList = createMouseEventsList(action, component, point.x, point.y);
				executeEventList(node, eventList);

				return true;
			}

			return false;
		}, e ->{
			logger.error(String.format("click(%s)", component));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public boolean press(EventTarget component, Keyboard key) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
			() ->
			{
				KeyCode keyCode = getKeyCode(key);
				ArrayList<InputEvent> events = new ArrayList<>();
				events.add(new KeyEvent(null, component, KeyEvent.KEY_PRESSED, "", "", keyCode, this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				if(needType(keyCode))
				{
					events.add(new KeyEvent(null, component, KeyEvent.KEY_TYPED, getTypedValue(key), "", keyCode, this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				}
				events.add(new KeyEvent(null, component, KeyEvent.KEY_RELEASED, "", "", keyCode, this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				Platform.runLater(() -> events.stream().peek(e -> logger.debug("Event : " + e)).forEach(e -> Event.fireEvent(component, e)));
				return true;
			},
			e->
			{
				logger.error(String.format("press(%s, %s)", component, key));
				logger.error(e.getMessage(), e);
				logger.error(e.getCause(), e);
			}
		);
	}

	@Override
	public boolean upAndDown(EventTarget component, Keyboard key, boolean b) throws Exception
	{

		return tryExecute(EMPTY_CHECK,
			() ->
			{
				switch (key)
				{
					case SHIFT:
						this.isShiftDown = b;
						break;
					case ALT:
						this.isAltDown = b;
						break;
					case CONTROL:
						this.isControlDown = b;
						break;
					default:
						break;
				}
				return true;
			},
			e->
			{
				logger.error(String.format("upAndDown(%s, %s, %b)", component, key, b));
				logger.error(e.getMessage(), e);
				logger.error(e.getCause(), e);
			}
		);
	}

	@Override
	public boolean push(EventTarget target) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
			()->
			{
				if (target instanceof ButtonBase)
				{
					Platform.runLater(((ButtonBase) target)::fire);
					return true;
				}
				return false;
			},
			e ->
			{
				logger.error(String.format("push(%s)", target));
				logger.error(e.getMessage(), e);
			}
	);
	}

	@Override
	public boolean toggle(EventTarget target, boolean value) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					if (target instanceof ToggleButton)
					{
						Platform.runLater(() -> ((ToggleButton) target).setSelected(value));
						return true;
					}
					else if (target instanceof CheckBox)
					{
						Platform.runLater(() -> ((CheckBox) target).setSelected(value));
						return true;
					}
					return false;
				},
				e ->
				{
					logger.error(String.format("toggle(%s,%s)", target, value));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public boolean select(EventTarget component, String selectedText) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					if (component instanceof TabPane)
					{
						TabPane tabPane = (TabPane) component;
						ObservableList<Tab> tabs = tabPane.getTabs();
						tabs.stream()
								.filter(tab -> tab.getText().contains(selectedText))
								.findFirst()
								.ifPresent(tabPane.getSelectionModel()::select);
						return true;
					}
					if (component instanceof ComboBox)
					{
						ComboBox comboBox = (ComboBox) component;
						StringConverter converter = comboBox.getConverter();
						comboBox.getItems().stream()
								.filter(s -> converter.toString(s).equals(selectedText))
								.findFirst()
								.ifPresent(comboBox.getSelectionModel()::select);
						return true;
					}
					if (component instanceof ListView)
					{
						ListView listView = (ListView) component;
						listView.getItems().stream()
								.filter(s -> s.toString().equals(selectedText))
								.findFirst()
								.ifPresent(listView.getSelectionModel()::select);
						return true;
					}
					return false;
				},
				e->
				{
					logger.error(String.format("select(%s,%s)", component, selectedText));
					logger.error(e.getMessage(), e);
				}
		);

	}

	@Override
	public boolean selectByIndex(EventTarget component, int index) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				() ->
				{
					if (component instanceof ComboBox)
					{
						ComboBox comboBox = (ComboBox) component;
						comboBox.getSelectionModel().select(index);
						return true;
					}
					if (component instanceof TabPane)
					{
						TabPane tabPane = (TabPane) component;
						tabPane.getSelectionModel().select(index);
						return true;
					}
					if (component instanceof ListView)
					{
						ListView listView = (ListView) component;
						listView.getSelectionModel().select(index);
						return true;
					}
					return false;
				},
				e->
				{
					logger.error(String.format("selectByIndex(%s,%s)", component, index));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public boolean expand(EventTarget component, String path, boolean expandOrCollapse) throws Exception
	{
		//TODO implement
		return false;
	}

	@Override
	public boolean text(EventTarget component, String text, boolean clear) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					if (component instanceof TextInputControl)
					{
						TextInputControl field = (TextInputControl) component;
						if (clear)
						{
							field.setText(text);
						}
						else
						{
							String currentText = field.getText();
							field.setText(currentText + text);
						}

						return true;
					}
					return false;
				},
				e ->
				{
					logger.error(String.format("text(%s, %s,%s)", component, text, clear));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();
		try
		{
			logger.debug("Wait to " + (toAppear ? "" : "Dis") + "appear for " + locator + " on time " + ms);
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() < time + ms)
			{
				try
				{
					List<EventTarget> targets = this.findAll(null, locator);
					if (toAppear)
					{
						if (!targets.isEmpty())
						{
							return true;
						}
					}
					else
					{
						if (targets.isEmpty())
						{
							return true;
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Error on waiting");
					logger.error(e.getMessage(), e);
				}
			}
			return false;
		}
		finally
		{
			if (atomicLong != null)
			{
				atomicLong.set(System.currentTimeMillis() - begin);
			}
		}
	}

	@Override
	public boolean setValue(EventTarget component, double value) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				() ->
				{
					if (component instanceof Spinner)
					{
						Spinner spinner = (Spinner) component;
						Object val = spinner.getValue();
						if (val instanceof Double)
						{
							spinner.getValueFactory().setValue(value);
							return true;
						}
						else if (val instanceof Integer)
						{
							spinner.getValueFactory().setValue((int)value);
							return true;
						}
					}
					if (component instanceof ScrollBar)
					{
						((ScrollBar) component).setValue(value);
						return true;
					}
					if (component instanceof Slider)
					{
						((Slider) component).setValue(value);
						return true;
					}
					if (component instanceof SplitPane)
					{
						((SplitPane) component).getDividers().get(0).setPosition(value);//todo splitPane has multi dividers, think what to do with it
						return true;
					}
					return false;
				},
				e ->
				{
					this.logger.error(String.format("setValue(%s)", component));
					this.logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	public Document getTree(EventTarget component) throws Exception
	{
		//TODO implement
		return null;
	}

	@Override
	public boolean dragNdrop(EventTarget drag, int x1, int y1, EventTarget drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			//TODO implement
			return false;
		}, e ->
		{
			logger.error(String.format("dragNdrop(%s,%s,%s,%s,%s,%s,%s)", drag, x1, y1, drop, x2, y2, moveCursor));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public boolean scrollTo(EventTarget component, int index) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof ListView)
			{
				ListView listView = (ListView) component;
				listView.scrollTo(index);
				return true;
			}
			if (component instanceof TreeView)
			{
				TreeView treeView = (TreeView) component;
				treeView.scrollTo(index);
				return true;
			}
			return false;
		}, e ->
		{
			this.logger.error(String.format("scrollTo(%s)", component));
			this.logger.error(e.getMessage(), e);
		});

	}

	@Override
	public boolean mouseTable(EventTarget component, int column, int row, MouseAction action) throws Exception
	{
		return tryExecute(EMPTY_CHECK, ()->
		{
			if (component instanceof TableView)
			{
				TableView<?> tableView = (TableView) component;
				if (tableView.getItems().size() < row)
				{
					throw new Exception(String.format("Can't get value for row %s because size of rows is %s", row, tableView.getItems().size()));
				}

				if (tableView.getColumns().size() < column)
				{
					throw new Exception(String.format("Can't get value for column %s because size of columns is %s", column, tableView.getColumns().size()));
				}

				Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
					try
					{
						logger.debug(String.format("Start scroll to column %s", column));
						tableView.scrollToColumnIndex(column);
						logger.debug(String.format("Start scroll to row %s", row));
						tableView.scrollTo(row);
						logger.debug(String.format("Start getting cell"));
						return (Node) tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, row, column);
					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
					}
					return null;
				});
				logger.debug("Found cell : " + cell);
				if (cell != null)
				{
					if (cell instanceof TableCell<?, ?> && ((TableCell) cell).getGraphic() != null)
					{
						cell = ((TableCell) cell).getGraphic();
					}
					Point point = this.checkCoords(cell, Integer.MIN_VALUE, Integer.MIN_VALUE);

					List<Event> eventList = createMouseEventsList(action, cell, point.x, point.y);
					executeEventList(cell, eventList);

					return true;
				}
				return false;
			}
			throw tableException(component);
		}, e->
		{
			logger.error(String.format("mouseTable(%s,%s,%s,%s)", component, column, row, action));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public boolean textTableCell(EventTarget component, int column, int row, String text) throws Exception
	{
		return tryExecute(EMPTY_CHECK, ()->
		{
			if (component instanceof TableView)
			{
				TableView<?> tableView = (TableView) component;
				if (tableView.getItems().size() < row)
				{
					throw new Exception(String.format("Can't get value for row %s because size of rows is %s", row, tableView.getItems().size()));
				}

				if (tableView.getColumns().size() < column)
				{
					throw new Exception(String.format("Can't get value for column %s because size of columns is %s", column, tableView.getColumns().size()));
				}

				Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
					try
					{
						logger.debug(String.format("Start scroll to column %s", column));
						tableView.scrollToColumnIndex(column);
						logger.debug(String.format("Start scroll to row %s", row));
						tableView.scrollTo(row);
						logger.debug(String.format("Start getting cell"));
						return (Node) tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, row, column);
					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
					}
					return null;
				});
				logger.debug("Found cell : " + cell);
				if (cell != null)
				{
					if (cell instanceof TableCell<?, ?> && ((TableCell) cell).getGraphic() != null)
					{
						cell = ((TableCell) cell).getGraphic();
					}
					if (!(cell instanceof TextInputControl))
					{
						Locator locator = new Locator();
						locator.kind(ControlKind.TextBox);
						List<EventTarget> all = new MatcherFx(this.info, locator, cell).findAll();
						if (all.isEmpty())
						{
							throw new Exception("Cant set text to not text element");
						}
						TextInputControl cellBox = (TextInputControl) all.get(0);
						cellBox.setText(text);
					}
					else
					{
						((TextInputControl) cell).setText(text);
					}
					return true;
				}
				return false;
			}
			throw tableException(component);
		}, e->
		{
			logger.error(String.format("mouseTable(%s,%s,%s,%s)", component, column, row, text));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public List<String> getRowIndexes(EventTarget target, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			List<String> rowNumbers = new ArrayList<>();
			if (target instanceof TableView)
			{
				TableView<?> tableView = (TableView<?>) target;
				List<String> tableHeaders = getTableHeaders(tableView, columns);
				for (int i = 0; i < tableView.getItems().size(); i++)
				{
					Map<String, Object> row = new LinkedHashMap<>();
					for (int j = 0; j < tableHeaders.size(); j++)
					{
						String cellValue = getValueTableCellDerived(tableView, j, i);
						row.put(tableHeaders.get(j), cellValue);
					}

					if (Objects.nonNull(valueCondition) && valueCondition.isMatched(row))
					{
						rowNumbers.add(String.valueOf(i));
					}
				}
				return rowNumbers;
			}
			throw tableException(target);
		}, e->
		{
			logger.error(String.format("getRowDerived(%s,%s,%s,%s,%s,%s,%s)", target, additional, header, useNumericHeader, Arrays.toString(columns), valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public int getTableSize(EventTarget component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof TableView)
			{
				return ((TableView) component).getItems().size();
			}
			throw tableException(component);
		}, e ->
		{
			logger.error(String.format("getTableSize(%s,%s,%s,%s)", component, additional, header, useNumericHeader));
			logger.error(e.getMessage(), e);
		});
	}

	@Override
	public Color getColorXY(EventTarget component, int x, int y) throws Exception
	{
		return tryExecute(EMPTY_CHECK, () ->
		{
			Point point = this.getPointLocation(component, x, y);
			return new Robot().getPixelColor(point.x, point.y);
		}, e ->
		{
			this.logger.error(String.format("getColorXY(%s)", component));
			this.logger.error(e.getMessage(), e);
		});
	}

	//region private methods
	private WrongParameterException tableException(EventTarget target)
	{
		return new WrongParameterException(String.format("Target not instance of Table. Target : %s", target));
	}

	private List<String> getTableHeaders(TableView<?> tableView, String[] columns)
	{
		List<String> list = new ArrayList<>();
		ObservableList<? extends TableColumn<?, ?>> tableColumns = tableView.getColumns();

		for (int i = 0; i < tableColumns.size(); i++)
		{
			String columnName;
			if (columns == null)
			{
				columnName = tableColumns.get(i).getText();
			}
			else
			{
				columnName = i < columns.length ? columns[i] : String.valueOf(i);
			}
			list.add(columnName.replace(' ', '_'));
		}
		return Converter.convertColumns(list);
	}

	private KeyCode getKeyCode(Keyboard key)
	{
		switch (key)
		{
			case ESCAPE : return KeyCode.ESCAPE;
			case F1 : return KeyCode.F1;
			case F2 : return KeyCode.F2;
			case F3 : return KeyCode.F3;
			case F4 : return KeyCode.F4;
			case F5 : return KeyCode.F5;
			case F6 : return KeyCode.F6;
			case F7 : return KeyCode.F7;
			case F8 : return KeyCode.F8;
			case F9 : return KeyCode.F9;
			case F10 : return KeyCode.F10;
			case F11 : return KeyCode.F11;
			case F12 : return KeyCode.F12;

			case DIG1 : return KeyCode.DIGIT1;
			case DIG2 : return KeyCode.DIGIT2;
			case DIG3 : return KeyCode.DIGIT3;
			case DIG4 : return KeyCode.DIGIT4;
			case DIG5 : return KeyCode.DIGIT5;
			case DIG6 : return KeyCode.DIGIT6;
			case DIG7 : return KeyCode.DIGIT7;
			case DIG8 : return KeyCode.DIGIT8;
			case DIG9 : return KeyCode.DIGIT9;
			case DIG0 : return KeyCode.DIGIT0;
			case BACK_SPACE : return KeyCode.BACK_SPACE;
			case INSERT : return KeyCode.INSERT;
			case HOME : return KeyCode.HOME;
			case PAGE_UP : return KeyCode.PAGE_UP;

			case TAB : return KeyCode.TAB;
			case Q : return KeyCode.Q;
			case W : return KeyCode.W;
			case E : return KeyCode.E;
			case R : return KeyCode.R;
			case T : return KeyCode.T;
			case Y : return KeyCode.Y;
			case U : return KeyCode.U;
			case I : return KeyCode.I;
			case O : return KeyCode.O;
			case P : return KeyCode.P;
			case SLASH : return KeyCode.SLASH;
			case BACK_SLASH : return KeyCode.BACK_SLASH;
			case DELETE : return KeyCode.DELETE;
			case END : return KeyCode.END;
			case PAGE_DOWN : return KeyCode.PAGE_DOWN;

			case CAPS_LOCK : return KeyCode.CAPS;
			case A : return KeyCode.A;
			case S : return KeyCode.S;
			case D : return KeyCode.D;
			case F : return KeyCode.F;
			case G : return KeyCode.G;
			case H : return KeyCode.H;
			case J : return KeyCode.J;
			case K : return KeyCode.K;
			case L : return KeyCode.L;
			case SEMICOLON : return KeyCode.SEMICOLON;
			case QUOTE : return KeyCode.QUOTE;
			case DOUBLE_QUOTE : return KeyCode.QUOTEDBL;
			case ENTER : return KeyCode.ENTER;

			case SHIFT : return KeyCode.SHIFT;
			case Z : return KeyCode.Z;
			case X : return KeyCode.X;
			case C : return KeyCode.C;
			case V : return KeyCode.V;
			case B : return KeyCode.B;
			case N : return KeyCode.N;
			case M : return KeyCode.M;
			case DOT : return KeyCode.PERIOD;
			case UP : return KeyCode.UP;

			case CONTROL : return KeyCode.CONTROL;
			case ALT : return KeyCode.ALT;
			case SPACE : return KeyCode.SPACE;
			case LEFT : return KeyCode.LEFT;
			case DOWN : return KeyCode.DOWN;

			case RIGHT : return KeyCode.RIGHT;

			case PLUS : return KeyCode.PLUS;
			case MINUS : return KeyCode.MINUS;

			case UNDERSCORE : return KeyCode.UNDERSCORE;

			case NUM_LOCK: return KeyCode.NUM_LOCK;
			case NUM_DIVIDE : return KeyCode.DIVIDE;
			case NUM_SEPARATOR : return KeyCode.SEPARATOR;
			case NUM_MULTIPLY : return KeyCode.MULTIPLY;
			case NUM_MINUS : return KeyCode.MINUS;
			case NUM_DIG7 : return KeyCode.NUMPAD7;
			case NUM_DIG8 : return KeyCode.NUMPAD8;
			case NUM_DIG9 : return KeyCode.NUMPAD9;
			case NUM_PLUS : return KeyCode.PLUS;
			case NUM_DIG4 : return KeyCode.NUMPAD4;
			case NUM_DIG5 : return KeyCode.NUMPAD5;
			case NUM_DIG6 : return KeyCode.NUMPAD6;
			case NUM_DIG1 : return KeyCode.NUMPAD1;
			case NUM_DIG2 : return KeyCode.NUMPAD2;
			case NUM_DIG3 : return KeyCode.NUMPAD3;
			case NUM_DIG0 : return KeyCode.NUMPAD0;
			case NUM_DOT : return KeyCode.PERIOD;
			case NUM_ENTER : return KeyCode.ENTER;
			default: return KeyCode.UNDEFINED;
		}
	}

	private String getTypedValue(Keyboard key)
	{
		return this.isShiftDown ? key.getChar().toUpperCase() : key.getChar().toLowerCase();
	}

	private boolean needType(KeyCode keyCode)
	{
		return !keyCode.isFunctionKey() && !keyCode.isNavigationKey()
				&& !keyCode.isArrowKey() && !keyCode.isModifierKey()
				&& !keyCode.isKeypadKey() && !keyCode.isMediaKey()
				&& !keyCode.equals(KeyCode.ENTER) && !keyCode.equals(KeyCode.TAB); //todo think about it
	}

	private int getClickCount(MouseAction action)
	{
		switch (action)
		{
			case LeftDoubleClick:
			case RightDoubleClick:
				return 2;
			default:
				return 1;
		}
	}

	private void fillHeaders(String[][] array, TableView<?> table, String[] customHeaders)
	{
		ObservableList<? extends TableColumn<?, ?>> columns = table.getColumns();

		if (customHeaders != null)
		{
			for (int i = 0; i < columns.size(); i++)
			{
				if (i < customHeaders.length)
				{
					array[0][i] = customHeaders[i];

				}
				else
				{
					array[0][i] = String.valueOf(i);
				}
			}
			return;
		}
		for (int i = 0; i < columns.size(); i++)
		{
			array[0][i] = columns.get(i).getText();
		}
	}

	private List<Event> createMouseEventsList(MouseAction action, EventTarget target, int x, int y) throws Exception
	{
		MouseButton mb;
		List<Event> result = new ArrayList<>();
		Rectangle rectangle = this.getRectangle(target);
		Point respectScene = getPointRespectScene(target);
		int clickCount = getClickCount(action);

		result.add(new MouseEvent(MouseEvent.MOUSE_ENTERED_TARGET, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, MouseButton.NONE,
				clickCount, isShiftDown, isControlDown, isAltDown, false, false,
				false, false, true, false, false, null));
		result.add(new MouseEvent(MouseEvent.MOUSE_MOVED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, MouseButton.NONE,
				clickCount, isShiftDown, isControlDown, isAltDown, false, false,
				false, false, true, false, false, null));
		switch (action)
		{
			case LeftClick:
			case LeftDoubleClick:
				mb = MouseButton.PRIMARY;
				result.add(new MouseEvent(MouseEvent.MOUSE_PRESSED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				result.add(new MouseEvent(MouseEvent.MOUSE_RELEASED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				result.add(new MouseEvent(MouseEvent.MOUSE_CLICKED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				return result;
			case RightClick:
			case RightDoubleClick:
				mb = MouseButton.SECONDARY;
				result.add(new MouseEvent(MouseEvent.MOUSE_PRESSED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, false,
						false, true, true, true, true, null));
				result.add(new MouseEvent(MouseEvent.MOUSE_RELEASED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, false,
						false, true, true, true, true, null));
				result.add(new MouseEvent(MouseEvent.MOUSE_CLICKED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, false,
						false, true, true, true, true, null));
				return result;
			case Press:
				mb = MouseButton.PRIMARY;
				result.add(new MouseEvent(MouseEvent.MOUSE_PRESSED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				return result;
			case Drop:
				mb = MouseButton.PRIMARY;
				result.add(new MouseEvent(MouseEvent.MOUSE_DRAGGED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				result.add(new MouseEvent(MouseEvent.MOUSE_RELEASED, respectScene.x + x, respectScene.y + y, rectangle.x + x, rectangle.y + y, mb,
						clickCount, isShiftDown, isControlDown, isAltDown, false, true,
						false, false, true, false, false, null));
				return result;
			default:
				return result;
		}
	}

	private Point getPointRespectScene(EventTarget target) throws Exception
	{
		if (target instanceof Node)
		{
			Node node = (Node) target;
			if (node.isVisible())
			{
				Bounds screenBounds = node.localToScene(node.getBoundsInLocal());
				int x = (int) screenBounds.getMinX();
				int y = (int) screenBounds.getMinY();

				return new Point(x, y);
			}else
			{
				throw new UnsupportedOperationException(String.format("Element %s is not visible", node));
			}

		}else
		{
			throw new UnsupportedOperationException(String.format("Element %s is not a node", target));
		}
	}

	private Point checkCoords(Node node, int x, int y) throws Exception
	{
		Point res;
		Rectangle rectangle = this.getRectangle(node);
		if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
		{
			res = new Point(rectangle.width / 2, rectangle.height / 2);
		}
		else
		{
			res = new Point(x, y);
		}

		return res;
	}

	private void executeEventList(Node node, List<Event> events)
	{
		Platform.runLater(() -> {
			for (Event event : events)
			{
				node.fireEvent(event);
			}
		});
	}

	private Point getPointLocation(EventTarget target, int x, int y) throws Exception
	{
		Rectangle rectangle = this.getRectangle(target);
		return new Point(rectangle.x + x, rectangle.y + y);
	}

	Node findOwner(Locator owner) throws Exception
	{
		logger.debug("Start found owner for " + owner);
		if (owner == null)
		{
			Parent root = UtilsFx.currentRoot();
			logger.debug("Found root of main window : " + root);
			return root;
		}
		else
		{
			logger.debug("Try to find owner");
			EventTarget target = this.find(null, owner);
			logger.debug("Found eventTarget : " + target);
			if (target instanceof Node)
			{
				return (Node) target;
			}
		}
		return null;
	}
	//endregion
}
