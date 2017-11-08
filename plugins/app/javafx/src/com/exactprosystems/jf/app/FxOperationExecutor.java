package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.TooManyElementsException;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;
import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.exactprosystems.jf.app.FxRemoteApplication.tryExecute;

public class FxOperationExecutor extends AbstractOperationExecutor<EventTarget>
{
    private Logger logger;
    private FXRobot currentRobot;

    private boolean isAltDown = false;
    private boolean isShiftDown = false;
    private boolean isControlDown = false;

    private static final FxRemoteApplication.ICheck EMPTY_CHECK = () -> {};

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
		throw new FeatureNotSupportedException("script");
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
		return null;
	}

	@Override
	public List<EventTarget> findAll(Locator owner, Locator element) throws Exception
	{
		return tryExecute(EMPTY_CHECK,
				()->
				{
					logger.debug(String.format("Start found owner : %s", owner));

					Node ownerNode = null;

					if (owner != null)
					{
						List<EventTarget> targets = new MatcherFx(this.info, owner, currentRoot()).findAll();
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
						ownerNode = currentRoot();
					}
					logger.debug(String.format("Found owner : " + MatcherFx.targetToString(ownerNode)));

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
					logger.debug(String.format("find(%s,%s)", owner, element));
					logger.debug(e.getMessage(), e);
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
	    //not checked. can't use robot cause he don't have target for events
		try
		{
            waitForIdle();
			final ArrayList<InputEvent> events = new ArrayList<>();
			events.add(new KeyEvent(null, component, KeyEvent.KEY_PRESSED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
			events.add(new KeyEvent(null, component, KeyEvent.KEY_TYPED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
			events.add(new KeyEvent(null, component, KeyEvent.KEY_RELEASED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
            List<Class<? extends Control>> allControls = new ArrayList<>();
            allControls.add(Button.class); //todo add all classes OR create around 20+ instanceof
            Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
                    /*for (Class<? extends Control> clazz : allControls)
                    {
                        if (component instanceof clazz)
                        {
                            for (InputEvent event : events)
                            {
                                logger.debug("event : " + event);
                                ((clazz) component).fireEvent(event);
                            }
                            break;
                        }
                    }*/
				}
			});
			return true;
		}
		catch (Throwable e)
		{
			logger.error(String.format("press(%s, %s)", component, key));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
            waitForIdle();
		}
	}

	@Override
	public boolean upAndDown(EventTarget component, Keyboard key, boolean b) throws Exception
	{
	    //not checked
        try
        {
            this.currentRobot.waitForIdle();
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
            boolean needPress = (key.equals(Keyboard.CONTROL)) && (key.equals(Keyboard.SHIFT)) && (key.equals(Keyboard.ALT)) && (key.equals(Keyboard.CAPS_LOCK)) ; //todo

            if(b)
            {
                events.add(new KeyEvent(null, component, KeyEvent.KEY_PRESSED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));

                if(needPress)
                {
                    events.add(new KeyEvent(null, component, KeyEvent.KEY_TYPED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
                }
            }
            else
            {
                events.add(new KeyEvent(null, component, KeyEvent.KEY_RELEASED, "", "", KeyCode.getKeyCode(key.name()), this.isShiftDown, this.isControlDown, this.isAltDown, false ));
            }

            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    for (InputEvent event : events)
                    {
                        logger.debug("event : " + event);
                        //todo
                    }
                }
            });
        }
        catch (Throwable e)
        {
            logger.error(String.format("upAndDown(%s, %s, %b)", component, key, b));
            logger.error(e.getMessage(), e);
            throw e;
        }
        return true;
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
		if (component instanceof TabPane)
		{
			TabPane tabPane = (TabPane) component;
			ObservableList<Tab> tabs = tabPane.getTabs();
			for (Tab tab : tabs)
			{
				if (tab.getText().contains(selectedText))
				{
					tabPane.getSelectionModel().select(tab);
					return true;
				}
			}
		}
        return false;
	}

	@Override
	public boolean selectByIndex(EventTarget component, int index) throws Exception
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
		if (component instanceof TreeView)
		{
			TreeView treeView = (TreeView) component;
			treeView.getSelectionModel().select(index);
			return true;
		}
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
		if (component instanceof TextInputControl)
		{
			TextInputControl field = (TextInputControl) component;
			if (clear)
			{
				field.setText(text);
			}else {
				String currentText = field.getText();
				field.setText(currentText + text);
			}

			return true;
		}
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
        if (component instanceof Spinner)
        {
            Spinner spinner = (Spinner) component;
            Object o = spinner.getValueFactory().getConverter().fromString(String.valueOf(value));
            spinner.getValueFactory().setValue(o);
            return true;
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

	private void waitForIdle()
	{
		this.currentRobot.waitForIdle();
	}

	//region methods for work with windows/stages.

	/**
	 * collect roots from all windows
	 */
	Parent currentRoot()
	{
		RootContainer container = new RootContainer();
		Window.impl_getWindows().forEachRemaining(w -> container.addTarget(w.getScene().getRoot()));
		return container;
	}

	Parent currentSceneRoot()
	{
		Iterator<Window> windowIterator = Window.impl_getWindows();
		Parent parent = null;
		//get first window ( and root) from queue
		if (windowIterator.hasNext())
		{
			parent = windowIterator.next().getScene().getRoot();
		}
		return parent;
	}

	Window mainWindow()
	{
		Iterator<Window> windowIterator = Window.impl_getWindows();
		Window mainWindow = null;
		//get first window from queue
		if (windowIterator.hasNext())
		{
			mainWindow = windowIterator.next();
		}
		return mainWindow;
	}

	Stage mainStage()
	{
		ObservableList<Stage> stages = FXRobotHelper.getStages();
		logger.debug(String.format("Found %s stages", stages.size()));
		if (stages.isEmpty())
		{
			return null;
		}
		return stages.stream()
				.peek(s -> logger.debug(String.format("Found stage : %s. MxWindowType : %s", MatcherFx.targetToString(s), s.impl_getMXWindowType())))
				.filter(s -> s.impl_getMXWindowType().equals("PrimaryStage"))
				.findFirst()
				.orElse(stages.get(0));
	}

	Node findOwner(Locator owner) throws Exception
	{
		logger.debug("Start found owner for " + owner);
		if (owner == null)
		{
			Parent root = currentRoot();
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
