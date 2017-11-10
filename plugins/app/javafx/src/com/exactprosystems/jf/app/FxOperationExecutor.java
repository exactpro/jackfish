package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.TooManyElementsException;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
	protected String getValueTableCellDerived(EventTarget component, int column, int row) throws Exception
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

				TableColumn<?,?> tableColumn = tableView.getColumns().get(column);
				ObservableValue<?> cellObservableValue = tableColumn.getCellObservableValue(row);
				return String.valueOf(cellObservableValue.getValue());
			}
			return null;
		}, e->
		{
			logger.error(String.format("getValueTableCellDerived(%s,%s,%s)", component, column, row));
			logger.error(e.getMessage(), e);
		});
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
		return tryExecute(EMPTY_CHECK, () ->
		{
			if (component instanceof TableView)
			{
				TableView<?> tableView = (TableView<?>) component;
				int rowsCount = tableView.getItems().size();
				int columnsCount = tableView.getColumns().size();

				String[][] res = new String[rowsCount + 1][columnsCount];
				ObservableList<? extends TableColumn<?, ?>> columns1 = tableView.getColumns();

				this.fillHeaders(res, tableView, columns);
				for (int i = 0; i < rowsCount; i++)
				{
					for (int j = 0; j < columns1.size(); j++)
					{
						res[i + 1][j] = columns1.get(j).getCellObservableValue(i).getValue().toString();
					}
				}

				return res;
			}
			else
			{
				throw new UnsupportedOperationException("Element is not a table");
			}
		}, e ->
		{
			this.logger.error(String.format("getTable(%s)", component));
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
	public Color getColor(EventTarget component, boolean isForeground) throws Exception
	{
		return null;
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
		FXRobot robot = FXRobotFactory.createRobot(null);
		final int clickCount = this.getClickCount(action);

		robot.mouseMove(x, y);

		switch (action)
		{
			case LeftDoubleClick:
			case LeftClick:
				robot.mouseClick(MouseButton.PRIMARY, clickCount);
				break;
			case RightClick:
			case RightDoubleClick:
				robot.mouseClick(MouseButton.SECONDARY, clickCount);
				break;
			case Press:
				robot.mousePress(MouseButton.PRIMARY);
		}

		return true;
	}

	@Override
	public boolean press(EventTarget component, Keyboard key) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
			() ->
			{
				ArrayList<InputEvent> events = new ArrayList<>();
				events.add(new KeyEvent(null, component, KeyEvent.KEY_PRESSED, "", "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				events.add(new KeyEvent(null, component, KeyEvent.KEY_TYPED, key.name(), "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				events.add(new KeyEvent(null, component, KeyEvent.KEY_RELEASED, "", "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
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

				final ArrayList<InputEvent> events = new ArrayList<>();
				boolean needPress = (!key.equals(Keyboard.CONTROL) && !key.equals(Keyboard.SHIFT) && !key.equals(Keyboard.ALT) && !key.equals(Keyboard.CAPS_LOCK));

				if(b)
				{
					events.add(new KeyEvent(null, component, KeyEvent.KEY_PRESSED, "", "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));

					if(needPress)
					{
						events.add(new KeyEvent(null, component, KeyEvent.KEY_TYPED, key.name(), "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
					}
				}
				else
				{
					events.add(new KeyEvent(null, component, KeyEvent.KEY_RELEASED, "", "", KeyCode.getKeyCode(getKeyName(key)), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
				}

				Platform.runLater(() -> events.stream().peek(event -> logger.debug("Event : " + event)).forEach(event -> Event.fireEvent(component, event)));
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
		if (component instanceof TableView)
		{
			return ((TableView) component).getItems().size();
		}
		else
		{
			throw new UnsupportedOperationException("Element is not a table");
		}
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

	private String getKeyName(Keyboard key)
	{
		switch (key)
		{
			case ESCAPE: return KeyCode.ESCAPE.getName();
			case BACK_SPACE: return KeyCode.BACK_SPACE.getName();
			case INSERT: return KeyCode.INSERT.getName();
			case HOME: return KeyCode.HOME.getName();
			case PAGE_UP: return KeyCode.PAGE_UP.getName();
			case TAB: return KeyCode.TAB.getName();
			case BACK_SLASH: return KeyCode.BACK_SLASH.getName();
			case DELETE: return KeyCode.DELETE.getName();
			case END: return KeyCode.END.getName();
			case PAGE_DOWN: return KeyCode.PAGE_DOWN.getName();
			case CAPS_LOCK: return KeyCode.CAPS.getName();
			case SEMICOLON: return KeyCode.SEMICOLON.getName();
			case QUOTE: return KeyCode.QUOTE.getName();
			case DOUBLE_QUOTE: return KeyCode.QUOTEDBL.getName();
			case ENTER: return KeyCode.ENTER.getName();
			case SHIFT: return KeyCode.SHIFT.getName();
			case UP: return KeyCode.UP.getName();
			case CONTROL: return KeyCode.CONTROL.getName();
			case ALT: return KeyCode.ALT.getName();
			case SPACE: return KeyCode.SPACE.getName();
			case LEFT: return KeyCode.LEFT.getName();
			case DOWN: return KeyCode.DOWN.getName();
			case RIGHT: return KeyCode.RIGHT.getName();
			case PLUS: return KeyCode.PLUS.getName();
			case MINUS: return KeyCode.MINUS.getName();
			case UNDERSCORE: return KeyCode.UNDERSCORE.getName();
			case NUM_LOCK: return KeyCode.NUM_LOCK.getName();
			case NUM_DIVIDE: return KeyCode.DIVIDE.getName();
			case NUM_SEPARATOR: return KeyCode.SEPARATOR.getName();
			case NUM_MULTIPLY: return KeyCode.MULTIPLY.getName();
			case NUM_MINUS: return KeyCode.MINUS.getName();
			case NUM_DOT: return KeyCode.PERIOD.getName();
			case NUM_ENTER: return KeyCode.ENTER.getName();
			default: return key.name();
		}
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
