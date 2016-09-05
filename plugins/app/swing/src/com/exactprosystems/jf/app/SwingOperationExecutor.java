////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;
import com.exactprosystems.jf.api.error.app.WrongParameterException;

import org.apache.log4j.Logger;
import org.fest.swing.awt.AWT;
import org.fest.swing.core.ComponentMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.core.Scrolling;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.JTableLocation;
import org.fest.swing.driver.JTreeLocation;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.exception.WaitTimedOutError;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.*;
import org.fest.swing.timing.Pause;
import org.fest.swing.util.Pair;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import static org.fest.swing.driver.ComponentStateValidator.validateIsEnabledAndShowing;
import static org.fest.swing.edt.GuiActionRunner.execute;

public class SwingOperationExecutor implements OperationExecutor<ComponentFixture<Component>>
{
	private Robot currentRobot;
	private Logger logger;

	public SwingOperationExecutor(Robot currentRobot, Logger logger)
	{
		this.currentRobot = currentRobot;
		this.logger = logger;
	}

	public Component currentFrame()
	{
		Collection<Component> list = this.currentRobot.finder().findAll(new ComponentMatcher()
		{
			@Override
			public boolean matches(Component c)
			{
				return c != null && (c instanceof JFrame);
			}
		});
		return (list == null || list.isEmpty()) ? null : list.iterator().next();
	}

	public Component currentRoot()
	{
		Container root = new RootContainer();
		for (Window window : Window.getWindows())
		{
			logger.trace("Find window : " + window);
			if (window.isVisible() && window.isShowing())
			{
				root.add(window);
			}
		}
		return root;
	}


	@Override
	public Rectangle getRectangle(ComponentFixture<Component> component) throws Exception
	{
		try
		{
			Component comp = component.target;
			return MatcherSwing.getRect(comp);
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRectangle(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Color getColor(String color) throws Exception
	{
		try
		{
			if (color == null)
			{
				return null;
			}

			if (color.equalsIgnoreCase("transparent"))
			{
				return new Color(255, 255, 255, 0);
			}
			StringBuilder colorSB = new StringBuilder(color);
			colorSB.delete(0, 5);
			colorSB.deleteCharAt(colorSB.length() - 1);
			String[] colors = colorSB.toString().split(", ");
			return new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
		}
		catch (Throwable e)
		{
			logger.error(String.format("getColor(%s)", color));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<ComponentFixture<Component>> findAll(ControlKind controlKind, ComponentFixture<Component> window, Locator locator) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Container owner = (Container) currentRoot();
			if (window != null && window.target instanceof Container)
			{
				owner = (Container) window.target;
			}

			List<ComponentFixture<Component>> res = new ArrayList<>();
			MatcherSwing<Component> matcher = new MatcherSwing<>(Component.class, owner, controlKind, locator);
			Collection<Component> components = this.currentRobot.finder().findAll(owner, matcher);
			for (final Component component : components)
			{
				res.add(getFixture(component));
			}
			return res;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("findAll(%s, %s, %s)", controlKind, window, locator));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<ComponentFixture<Component>> findAll(Locator owner, Locator element) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Container container = (Container) currentRoot();
			if (owner != null)
			{
				ComponentFixture<Component> found = find(null, owner);
				if (found != null && found.target instanceof Container)
				{
					container = (Container) found.target;
				}
			}

			List<ComponentFixture<Component>> res = new ArrayList<>();
			MatcherSwing<Component> matcher = new MatcherSwing<>(Component.class, container, element.getControlKind(), element);
			Collection<Component> components = this.currentRobot.finder().findAll(container, matcher);
			for (final Component component : components)
			{
				res.add(getFixture(component));
			}
			return res;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("findAll(%s, %s)", owner, element));
			logger.error(e.getMessage(), e);
			throw new ElementNotFoundException("Unable to find component ", element);
		}
	}

	@Override
	public ComponentFixture<Component> find(Locator owner, Locator element) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			return getComponent(owner, element);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("find(%s, %s)", owner, element));
			logger.error(e.getMessage(), e);
			throw new ElementNotFoundException("Unable to find component ", element);
		}
	}

	@Override
	public ComponentFixture<Component> lookAtTable(ComponentFixture<Component> component, Locator additional, Locator header, int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("lookAtTable");
	}

	@Override
	public boolean mouse(ComponentFixture<Component> component, int x, int y, MouseAction action) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			if (component.target instanceof JComponent)
			{
				Scrolling.scrollToVisible(this.currentRobot, ((JComponent) component.target));
			}
			Point point = new Point(x, y);
			if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
			{
				point = AWT.visibleCenterOf(component.target);
			}
			if (component.target instanceof JTree && y != Integer.MIN_VALUE)
			{
				point = scrollToRow(((JTree) component.target), y);
			}
			final ArrayList<MouseEvent> events = new ArrayList<>();

			events.add(new MouseEvent(component.target, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, point.x, point.y, 0, true, MouseEvent.NOBUTTON));
			events.add(new MouseEvent(component.target, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, point.x, point.y, 0, true, MouseEvent.NOBUTTON));

			switch (action)
			{
				case LeftClick:
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					break;

				case LeftDoubleClick:

					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					break;

				case RightClick:
					//TODO check last parameter on these events
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					break;

				case RightDoubleClick:
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					break;
			}
			final Component target = component.target;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					for (MouseEvent event : events)
					{
						logger.debug("event : "  + event.toString());
						target.dispatchEvent(event);
					}
				}
			});
			return true;
		}
		catch (Throwable e)
		{
			logger.error(String.format("mouse(%s, %d, %d, %s)", component, x, y, action));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override
	public boolean press(ComponentFixture<Component> component, Keyboard key) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			final Component target = component.target;
			final ArrayList<InputEvent> events = new ArrayList<>();
			int keyCode = getKeyCode(key);
			events.add(new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, (char) keyCode));
			events.add(new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, (char) keyCode));
			events.add(new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, (char) keyCode));
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					for (InputEvent event : events)
					{
						logger.debug("event : " + event);
						target.dispatchEvent(event);
					}
				}
			});
			return true;
//			if (component instanceof KeyboardInputSimulationFixture)
//			{
//				KeyboardInputSimulationFixture inputSimulationFixture = (KeyboardInputSimulationFixture) component;
//				inputSimulationFixture.pressAndReleaseKey(KeyPressInfo.keyCode(getKeyCode(key)));
//				return true;
//			}
//			throw new RemoteException(String.format("Component %s dosen't support press operation", component));
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
	public boolean upAndDown(ComponentFixture<Component> component, Keyboard key, boolean b) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			if (component instanceof KeyboardInputSimulationFixture)
			{
				KeyboardInputSimulationFixture inputSimulationFixture = (KeyboardInputSimulationFixture) component;
				int keyCode = getKeyCode(key);
				if (b)
				{
					inputSimulationFixture.pressKey(keyCode);
				}
				else
				{
					inputSimulationFixture.releaseKey(keyCode);
				}
				waitForIdle();
				return true;
			}
			throw new OperationNotAllowedException(String.format("Component %s dosen't support press operation", component));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("upAndDown(%s, %s, %b)", component, key, b));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean push(ComponentFixture<Component> component) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			final AbstractButton button = component.targetCastedTo(AbstractButton.class);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					button.doClick();
				}
			});

			return true;
		}
		catch (Throwable e)
		{
			logger.error(String.format("push(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean toggle(ComponentFixture<Component> component, final boolean value) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			if (currentComponent instanceof JToggleButton)
			{
				final JToggleButton toggleButton = component.targetCastedTo(JToggleButton.class);
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						toggleButton.setSelected(value);
					}
				});
			}
			return true;
		}
		catch (Throwable e)
		{
			logger.error(String.format("toggle(%s, %b)", component, value));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override 
	public boolean selectByIndex(ComponentFixture<Component> component, final int index) throws Exception
	{
		try
		{
			if (!component.target.isEnabled())
			{
				throw new OperationNotAllowedException("Component " + component + " is disabled.");
			}

			this.currentRobot.waitForIdle();

			if (component.target instanceof JComboBox)
			{
				final JComboBox<?> comboBox = component.targetCastedTo(JComboBox.class);
				if (index >= 0 && index < comboBox.getItemCount())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							comboBox.setSelectedIndex(index);
						}
					});
					
					return true;
				}
				return false;
			}
			else if (component.target instanceof JTabbedPane)
			{
				final JTabbedPane tabPane = component.targetCastedTo(JTabbedPane.class);
				if (index >= 0 && index < tabPane.getTabCount())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							tabPane.setSelectedIndex(index);
						}
					});
					return true;
				}
				return false;
			}
			else if (component.target instanceof JList)
			{
				JList jList = component.targetCastedTo(JList.class);
				jList.setSelectedIndex(index);
			}

			waitForIdle();
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("selectByIndex(%s, %d)", component, index));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean select(ComponentFixture<Component> component, String selectedText) throws Exception
	{
		try
		{
			if (!component.target.isEnabled())
			{
				throw new OperationNotAllowedException("Component " + component + " is disabled.");
			}

			this.currentRobot.waitForIdle();

			if (component.target instanceof JComboBox)
			{
				final JComboBox<?> comboBox = component.targetCastedTo(JComboBox.class);
				for(int i = 0; i < comboBox.getItemCount(); i++)
				{
					Object value = comboBox.getItemAt(i);
					if (value != null &&  ("" + value).equals(selectedText))
					{
						final int index = i;
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								comboBox.setSelectedIndex(index);
							}
						});
						
						return true;
					}
				}
				return false;
			}
			else if (component.target instanceof JTabbedPane)
			{
				final JTabbedPane tabPane = component.targetCastedTo(JTabbedPane.class);
				int number = tabPane.getTabCount();
				for (int i = 0; i < number; i++)
				{
					String name = tabPane.getTitleAt(i);
					if (name != null && name.contains(selectedText))
					{
						final int index = i;
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								tabPane.setSelectedIndex(index);
							}
						});
						return true;
					}
				}
				return false;
			}
			else if (component.target instanceof JList)
			{
				JList jList = component.targetCastedTo(JList.class);
				jList.setSelectedValue(selectedText, true);
			}

			waitForIdle();
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("select(%s, %s)", component, selectedText));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}


	@Override
	public boolean fold(ComponentFixture<Component> component, String path, boolean collaps) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			String[] split = path.split("/");
			if (currentComponent instanceof JMenu)
			{
				logger.debug("path : " + path);
				JMenu currentMenu = component.targetCastedTo(JMenu.class);
				logger.debug("current menu : " + currentMenu.getText());
				currentMenu.setPopupMenuVisible(true);
				for (String pathItem : split)
				{
					currentMenu = expand(currentMenu, pathItem);
				}
				currentMenu.setSelected(true);
				return true;
			}
			else if (currentComponent instanceof JTree)
			{
				JTree tree = component.targetCastedTo(JTree.class);
				TreeNode node = find((TreeNode) tree.getModel().getRoot(), 0, split);

				if (node == null)
				{
					throw new WrongParameterException("Path '" + path + "' is not found in the tree.");
				}
				TreePath treePath = new TreePath(((DefaultMutableTreeNode) node).getPath());
				if (collaps)
				{
					tree.expandPath(treePath);
				}
				else
				{
					tree.collapsePath(treePath);
				}
			}

			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("fold(%s, %b)", component, collaps));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean text(ComponentFixture<Component> component, String text, boolean clear) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			if (component.target instanceof JTextComponent)
			{
				JTextComponent textComponent = component.targetCastedTo(JTextComponent.class);
				if (!clear)
				{
					String currentText = textComponent.getText();
					textComponent.setText(currentText + text);
				}
				else
				{
					textComponent.setText(text);
				}
				return true;
			}
			throw new OperationNotAllowedException(String.format("Component %s does not support text entering", component.target));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("text(%s, %s, %b)", component, text, clear));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override
	public boolean wait(final Locator locator, int ms, final boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();

		try
		{
			final boolean[] result = {false};

			Pause.pause(new org.fest.swing.timing.Condition("Waiting")
			{
				@Override
				public boolean test()
				{
					MatcherSwing<Component> matcher = null;
					try
					{
						matcher = new MatcherSwing<>(Component.class, currentRoot(), locator.getControlKind(), locator);
					}
					catch (RemoteException e)
					{
						logger.error(e.getMessage(), e);
					}
					Collection<Component> list = currentRobot.finder().findAll(matcher);

					result[0] = !(toAppear ^ list.size() > 0);
					return result[0];
				}
			}, ms);

			return result[0];
		}
		catch (WaitTimedOutError err)
		{
			logger.error("timeout expired.");
			return false;
		}
//		catch (RemoteException e)
//		{
//			throw e;
//		}
		catch (Throwable e)
		{
			logger.error(String.format("wait(%s, %d, %b, %s)", locator, ms, toAppear, atomicLong));
			logger.error(e.getMessage(), e);
			throw e;
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
	public boolean setValue(ComponentFixture<Component> component, double value) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			if (currentComponent instanceof JSlider)
			{
				((JSlider) currentComponent).setValue((int) value);
				return true;
			}
			else if (currentComponent instanceof JScrollBar)
			{
				((JScrollBar) currentComponent).setValue((int) value);
				return true;
			}
			else if (currentComponent instanceof JSplitPane)
			{
				((JSplitPane) currentComponent).setDividerLocation(((int) value));
				return true;
			}
			return false;

		}
		catch (Throwable e)
		{
			logger.error(String.format("setValue(%s, %f)", component, value));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override
	public String getValue(ComponentFixture<Component> component) throws Exception
	{
		try
		{
			Component currentComponent = component.component();
			return getValue(currentComponent);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getValue(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String get(ComponentFixture<Component> component) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			StringBuilder sb = new StringBuilder();
			getAllTexts(component.target, sb);
			return sb.toString();
		}
		catch (Throwable e)
		{
			logger.error(String.format("get(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override
	public String getAttr(ComponentFixture<Component> component, String name) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
			String methodName = "get" + firstLetter + name.substring(1);
			Method[] methods = currentComponent.getClass().getMethods();
			for (Method method : methods)
			{
				if (method.getName().equals(methodName) && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0)
				{
					Object invoke = method.invoke(currentComponent);
					return String.valueOf(invoke);
				}
			}
			return "";
		}
		catch (Throwable e)
		{
			logger.error(String.format("get(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			waitForIdle();
		}
	}

	@Override
	public String script(ComponentFixture<Component> component, String script) throws Exception
	{
		throw new FeatureNotSupportedException("script");
	}

	@Override
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean textTableCell(ComponentFixture<Component> component, final int column, final int row, final String text) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			final JTable table = component.targetCastedTo(JTable.class);
//			JTableFixture tableFixture = new JTableFixture(this.currentRobot, table);
//			tableFixture.enterValue(TableCell.row(row).column(column), text);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					table.setValueAt(text, row, column);
				}
			});
			return true;
		}
		catch (Exception e)
		{
			logger.error(String.format("textTableCell(%s, %d, %d, %s)", component, column, row, text));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean mouseTable(ComponentFixture<Component> component, int column, int row, MouseAction action) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			final JTable table = component.targetCastedTo(JTable.class);
			JTableLocation jTableLocation = new JTableLocation();
			Point point = jTableLocation.pointAt(table, row, column);
			final ArrayList<MouseEvent> events = new ArrayList<>();
			events.add(new MouseEvent(component.target, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, point.x, point.y, 0, true, MouseEvent.NOBUTTON));
			events.add(new MouseEvent(component.target, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, point.x, point.y, 0, true, MouseEvent.NOBUTTON));
			switch (action)
			{
				case LeftClick:
					//TODO when we press left click parameter popupTrigger mb set false? it's right?
					events.add(new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
					break;

				case LeftDoubleClick:
//					cell.doubleClick();
					events.add(new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 2, false, MouseEvent.BUTTON1));
					break;

				case RightClick:
//					cell.rightClick();
					events.add(new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1, true, MouseEvent.BUTTON3));
					break;

				case RightDoubleClick:
//					cell.rightClick();
//					cell.rightClick();
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					events.add(new MouseEvent(component.target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 2, true, MouseEvent.BUTTON3));
					break;
			}
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					for (MouseEvent event : events)
					{
						logger.debug("event : "  + event.toString());
						table.dispatchEvent(event);
					}
				}
			});
			return true;
		}
		catch (Exception e)
		{
			logger.error(String.format("mouseTable(%s, %d, %d, %s)", component, column, row, action));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String getValueTableCell(ComponentFixture<Component> component, int column, int row) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture tableFixture = new JTableFixture(this.currentRobot, table);
			JTableCellFixture cell = tableFixture.cell(TableCell.row(row).column(column));
			Component editor = cell.editor();
			if (editor != null)
			{
				String value = getValue(editor);
				if (Str.IsNullOrEmpty(value))
				{
					try
					{
						Component tableCellRendererComponent = table.getCellRenderer(row, column).getTableCellRendererComponent(table, null, true, true, row, column);
						logger.debug("component : " + tableCellRendererComponent);
						if (tableCellRendererComponent instanceof JLabel)
						{
							value = String.valueOf(((JLabel) tableCellRendererComponent).getIcon());
						}
					}
					catch (Exception e)
					{
						logger.error("Could not get an icon: " + e.getMessage());
					}

				}
				logger.debug("returned value " + value);
				logger.debug("returned value is null " + (value == null));
				return value;
			}
			throw new OperationNotAllowedException(String.format("Table %s with row %s and column %s don't have editor", component, row, column));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("get(%s, %d, %d)", component, column, row));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRow(ComponentFixture<Component> component, Locator rows, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture fixture = new JTableFixture(this.currentRobot, table);
			Map<String, Integer> fieldIndexes = getTableHeaders(table);
			List<String> rowsIndexes = getIndexes(fixture, fieldIndexes, valueCondition, colorCondition);

			if (rowsIndexes.size() == 1)
			{
				Map<String, String> ret = new LinkedHashMap<String, String>();

				for (Entry<String, Integer> entry : fieldIndexes.entrySet())
				{
					String name = entry.getKey();
					Integer colIndex = fieldIndexes.get(name);
					if (colIndex == null)
					{
						throw new WrongParameterException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
					}
					String value = String.valueOf(getValueTableCell(fixture, Integer.parseInt(rowsIndexes.get(0)), colIndex));

					String underscopedName = name.replace(' ', '_');
					ret.put(underscopedName, value);
				}

				return ret;
			}
			else if (rowsIndexes.size() > 1)
			{
				throw new Exception("Too many rows");
			}

			return null;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRow(%s, %s, %s, %b, %s, %s)", component, rows, header, useNumericHeader, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> getRowIndexes(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture fixture = new JTableFixture(this.currentRobot, table);
			Map<String, Integer> fieldIndexes = getTableHeaders(table);

			return getIndexes(fixture, fieldIndexes, valueCondition, colorCondition);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRowByIndex(%s, %s, %s, %b, %s, %s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}


	@Override
	public Map<String, String> getRowByIndex(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);

			Map<String, Integer> fieldIndexes = getTableHeaders(table);

			Map<String, String> ret = new LinkedHashMap<String, String>();
			JTableFixture fixture = (((JTableFixture) (ComponentFixture<? extends Component>) component));
			for (Entry<String, Integer> entry : fieldIndexes.entrySet())
			{
				String name = entry.getKey();
				Integer colIndex = entry.getValue();

				String value = String.valueOf(getValueTableCell(fixture, i, colIndex));
				String underscopedName = name.replace(' ', '_');

				ret.put(underscopedName, value);
			}

			return ret;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRowByIndex(%s, %s, %s, %b, %d)", component, additional, header, useNumericHeader, i));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			Map<String, Integer> fieldIndexes = getTableHeaders(table);
			Map<String, ValueAndColor> ret = new LinkedHashMap<String, ValueAndColor>();
			JTableFixture tableFixture = (((JTableFixture) (ComponentFixture<? extends Component>) component));
			for (Entry<String, Integer> entry : fieldIndexes.entrySet())
			{
				String name = entry.getKey();
				Integer colIndex = entry.getValue();

				String value = String.valueOf(getValueTableCell(tableFixture, i, colIndex));
				String underscopedName = name.replace(' ', '_');

				Color color = tableFixture.foregroundAt(TableCell.row(i).column(colIndex)).target();
				Color backColor = tableFixture.backgroundAt(TableCell.row(i).column(colIndex)).target();

				ret.put(underscopedName, new ValueAndColor(value, color, backColor));
			}

			return ret;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRowWithColor(%s, %s, %s, %b, %d)", component, additional, header, useNumericHeader, i));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String[][] getTable(final ComponentFixture<Component> component, Locator additional, Locator header, final boolean useNumericHeader, String[] columns) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			int rows = table.getRowCount();
			int columnsCount = table.getColumnCount();

			String[][] res = new String[rows + 1][];

			List<String> headers = getHeaders(table, useNumericHeader);
			res[0] = new String[columnsCount];
			for (int column = 0; column < columnsCount; column++)
			{
				res[0][column] = headers.get(column);
			}
			JTableFixture fixture = (((JTableFixture) (ComponentFixture<? extends Component>) component));
			for (int row = 0; row < rows; row++)
			{
				res[row + 1] = new String[columnsCount];
				for (int column = 0; column < columnsCount; column++)
				{
					Object value = getValueTableCell(fixture, row, column);
					if (value == null)
					{
						try
						{
							Component tableCellRendererComponent = table.getCellRenderer(row, column).getTableCellRendererComponent(table, null, true, true, row, column);
							if (tableCellRendererComponent instanceof JLabel)
							{
								value = String.valueOf(((JLabel) tableCellRendererComponent).getIcon());
							}
						}
						catch (Exception e)
						{
							logger.error("Could not get an icon: " + e.getMessage());
						}
					}
					res[row + 1][column] = "" + value;
				}
			}
			return res;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getTable(%s, %s, %s, %b)", component, additional, header, useNumericHeader));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public int getTableSize(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		try
		{
			JTable table = component.targetCastedTo(JTable.class);
			return table.getRowCount();
		}
		catch (Throwable e)
		{
			logger.error(String.format("getTableSize(%s, %s,%s,%b)",component, additional, header, useNumericHeader));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// private methods
	//------------------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private <T extends Component> ComponentFixture<T> getComponent(Locator owner, Locator locator) throws RemoteException
	{
		ComponentFixture<T> ret = null;
		ComponentFixture<?> window = null;
		Component own = null;

		if (owner != null)
		{
			window = getComponent(null, owner);
			own = window.target;
		}
		Component component = null;
		ControlKind controlKind = locator.getControlKind();
		if (controlKind != null)
		{
			switch (controlKind)
			{
				case Any:
					component = getComp(Component.class, window, locator);
					ret = (ComponentFixture<T>) new ComponentFixture<Component>(this.currentRobot, component)
					{
					};
					break;

				case Button:
					component = getComp(JButton.class, window, locator);
					ret = (ComponentFixture<T>) new JButtonFixture(this.currentRobot, (JButton) component);
					break;

				case CheckBox:
					component = getComp(JCheckBox.class, window, locator);
					ret = (ComponentFixture<T>) new JCheckBoxFixture(this.currentRobot, (JCheckBox) component);
					break;

				case ComboBox:
					component = getComp(JComboBox.class, window, locator);
					ret = (ComponentFixture<T>) new JComboBoxFixture(this.currentRobot, (JComboBox<?>) component);
					break;

				case Dialog:
					component = getComp(JDialog.class, window, locator);
					ret = (ComponentFixture<T>) new DialogFixture(this.currentRobot, (JDialog) component);
					DialogFixture jdf = (DialogFixture) ret;
					jdf.target.toFront();
					jdf.focus();
					break;

				case Frame:
					//if need more time, use method withTimeout
					if (own == null)
					{
						own = this.currentRoot();
					}
					ret = (ComponentFixture<T>) WindowFinder.findFrame(new MatcherSwing<Frame>(Frame.class, own, null, locator)).using(currentRobot);
					FrameFixture jff = (FrameFixture) ret;
					jff.target.toFront();
					break;

				case Label:
					component = getComp(JLabel.class, window, locator);
					ret = (ComponentFixture<T>) new JLabelFixture(this.currentRobot, (JLabel) component);
					break;

				case ListView:
					component = getComp(JList.class, window, locator);
					ret = (ComponentFixture<T>) new JListFixture(this.currentRobot, (JList<?>) component);
					break;

				case Menu:
				case MenuItem:
					component = getComp(JMenuItem.class, window, locator);
					ret = (ComponentFixture<T>) new JMenuItemFixture(this.currentRobot, (JMenuItem) component);
					break;

				case Panel:
					component = getComp(JPanel.class, window, locator);
					ret = (ComponentFixture<T>) new JPanelFixture(this.currentRobot, (JPanel) component);
					break;

				case ProgressBar:
					component = getComp(JProgressBar.class, window, locator);
					ret = (ComponentFixture<T>) new JProgressBarFixture(this.currentRobot, (JProgressBar) component);
					break;

				case RadioButton:
					component = getComp(JRadioButton.class, window, locator);
					ret = (ComponentFixture<T>) new JRadioButtonFixture(this.currentRobot, (JRadioButton) component);
					break;

				case Row:
					break;

				case ScrollBar:
					component = getComp(JScrollBar.class, window, locator);
					ret = (ComponentFixture<T>) new JScrollBarFixture(this.currentRobot, (JScrollBar) component);
					break;

				case Slider:
					component = getComp(JSlider.class, window, locator);
					ret = (ComponentFixture<T>) new JSliderFixture(this.currentRobot, (JSlider) component);
					break;

				case Splitter:
					component = getComp(JSplitPane.class, window, locator);
					ret = (ComponentFixture<T>) new JSplitPaneFixture(this.currentRobot, (JSplitPane) component);
					break;

				case Table:
					component = getComp(JTable.class, window, locator);
					ret = (ComponentFixture<T>) new JTableFixture(this.currentRobot, (JTable) component);
					break;

				case TabPanel:
					component = getComp(JTabbedPane.class, window, locator);
					ret = (ComponentFixture<T>) new JTabbedPaneFixture(this.currentRobot, (JTabbedPane) component);
					break;

				case TextBox:
					component = getComp(JTextComponent.class, window, locator);
					ret = (ComponentFixture<T>) new JTextComponentFixture(this.currentRobot, (JTextComponent) component);
					break;

				case ToggleButton:
					component = getComp(JToggleButton.class, window, locator);
					ret = (ComponentFixture<T>) new JToggleButtonFixture(this.currentRobot, (JToggleButton) component);
					break;

				case Tooltip:
					//					TODO can't find JToolTipFixture
					break;

				case Tree:
					component = getComp(JTree.class, window, locator);
					ret = (ComponentFixture<T>) new JTreeFixture(this.currentRobot, (JTree) component);
					break;

				default:
					throw new ElementNotFoundException(locator);
			}
		}
		this.currentRobot.waitForIdle();

		return ret;
	}
	
	private List<String> getHeaders(JTable table, boolean useNumericHeader) throws Exception
	{
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < table.getColumnCount(); i++)
		{
			res.add(useNumericHeader ? String.valueOf(i) : table.getColumnName(i));
		}
		return res;
	}
	
	private Object getValueTableCell(JTableFixture fixture, int row, int column)
	{
		JTable table = fixture.target;
		Object valueAt = table.getValueAt(row, column);
		if (valueAt == null)
		{
			valueAt = fixture.valueAt(TableCell.row(row).column(column));
		}
		return valueAt;
	}
	
	private String getValue(Component currentComponent) throws RemoteException
	{
		if (currentComponent instanceof JToggleButton)
		{
			return String.valueOf(((JToggleButton) currentComponent).isSelected());
		}
		else if (currentComponent instanceof JComboBox)
		{
			return String.valueOf(((JComboBox) currentComponent).getSelectedItem());
		}
		else if (currentComponent instanceof JList)
		{
			return String.valueOf(((JList) currentComponent).getSelectedValue());
		}
		else if (currentComponent instanceof JProgressBar)
		{
			return String.valueOf(((JProgressBar) currentComponent).getValue());
		}
		else if (currentComponent instanceof JScrollBar)
		{
			return String.valueOf(((JScrollBar) currentComponent).getValue());
		}
		else if (currentComponent instanceof JSlider)
		{
			return String.valueOf(((JSlider) currentComponent).getValue());
		}
		else if (currentComponent instanceof JTabbedPane)
		{
			JTabbedPane pane = ((JTabbedPane) currentComponent);
			return pane.getTitleAt(pane.getSelectedIndex());
		}
		else if (currentComponent instanceof JTree)
		{
			return ((JTree) currentComponent).getSelectionPath().toString();
		}
		else if (currentComponent instanceof JLabel)
		{
			return ((JLabel) currentComponent).getText();
		}
		else if (currentComponent instanceof JTextComponent)
		{
			return ((JTextComponent) currentComponent).getText();
		}
		else if (currentComponent instanceof JPanel)
		{
			StringBuilder builder = new StringBuilder();
			getAllTexts(currentComponent, builder);
			return builder.toString();
		}
		else if (currentComponent instanceof JSplitPane)
		{
			JSplitPane splitPane = (JSplitPane) currentComponent;
			return String.valueOf(splitPane.getDividerLocation());
		}
		else if (currentComponent instanceof JToolTip)
		{
			return ((JToolTip) currentComponent).getTipText();
		}
		else if (currentComponent instanceof JButton)
		{
			return ((JButton) currentComponent).getText();
		}
		throw new OperationNotAllowedException(String.format("Component %s don't have value", currentComponent));
	}
	
	private JMenu expand(JMenu parent, String menuName) throws RemoteException
	{
		for (int i = 0; i < parent.getItemCount(); i++)
		{
			Component menuComponent = parent.getMenuComponent(i);
			if (menuComponent instanceof JMenu)
			{
				JMenu returnMenu = (JMenu) menuComponent;
				logger.debug("found menu : " + returnMenu.getText());
				returnMenu.setPopupMenuVisible(true);
				logger.debug("Menu visible? : " + returnMenu.isPopupMenuVisible());
				return returnMenu;
			}
		}
		throw new WrongParameterException(String.format("Menu with name '%s' not found in menu '%s'", menuName, parent.getText()));
	}

	@SuppressWarnings("unchecked")
	private <T extends Component> T getComp(Class<T> type, ComponentFixture<?> window, Locator locator) throws RemoteException
	{
		Component component = null;
		if (window != null)
		{
			component = this.currentRobot.finder().find((Container) window.target, new MatcherSwing<T>(type, window.target, locator.getControlKind(), locator));
		}
		else
		{
			component = this.currentRobot.finder().find(new MatcherSwing<Component>(Component.class, currentRoot(), locator.getControlKind(), locator));
		}

		return (T) component;
	}

	private static Map<String, Integer> getTableHeaders(final JTable table)
	{
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
		{
			String realName = table.getColumnModel().getColumn(i).getHeaderValue().toString();
			String underscopedName = realName.replace(' ', '_');
			result.put(underscopedName, i);
		}

		return result;
	}

	private List<String> getIndexes(JTableFixture fixture, Map<String, Integer> fieldIndexes, ICondition valueCondition, ICondition colorCondition) throws RemoteException
	{
		JTable table = fixture.targetCastedTo(JTable.class);
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++)
		{
			boolean found = true;

			if (valueCondition != null)
			{
				String name = valueCondition.getName();
				Integer index = fieldIndexes.get(name);
				if (index == null)
				{
					throw new WrongParameterException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
				}
				Object value = getValueTableCell(fixture, i, index);
				if (!valueCondition.isMatched(name, value))
				{
					found = false;
				}
			}

			if (found && colorCondition != null)
			{
				String name = colorCondition.getName();
				Integer index = fieldIndexes.get(name);
				if (index == null)
				{
					throw new WrongParameterException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
				}
				Color color = fixture.foregroundAt(TableCell.row(i).column(index)).target();
				if (!colorCondition.isMatched(name, color))
				{
					found = false;
				}
			}

			if (found)
			{
				res.add(String.valueOf(i));
			}
		}

		return res;
	}

	private String humanReadableHeaders(Map<String, Integer> fieldIndexes)
	{
		StringBuilder sb = new StringBuilder("[");
		String comma = "";
		for (String name : fieldIndexes.keySet())
		{
			sb.append(comma);

			for (char ch : name.toCharArray())
			{
				if (ch > 127 || ch < 32) // beyond ASCII
				{
					sb.append(String.format("\\u%04X", (int) ch));
				}
				else
				{
					sb.append(ch);
				}
			}
			comma = ", ";
		}

		sb.append("]");


		return sb.toString();
	}

	private void getAllTexts(Component parent, StringBuilder ret)
	{
		String text = MatcherSwing.getText(parent);
		if (text != null)
		{
			ret.append(text);
		}

		if (parent instanceof Container)
		{
			for (Component component : ((Container) parent).getComponents())
			{
				if (component instanceof Container)
				{
					getAllTexts((Container) component, ret);
				}
				else
				{
					text = MatcherSwing.getText(component);

					if (text != null)
					{
						ret.append(text);
					}
				}
			}
		}
	}

	private void waitForIdle()
	{
		this.currentRobot.waitForIdle();
	}

	private int getKeyCode(Keyboard key)
	{
		int id = 0;
		switch (key)
		{
			case ESCAPE:
				id = KeyEvent.VK_ESCAPE;
				break;
			case F1:
				id = KeyEvent.VK_F1;
				break;
			case F2:
				id = KeyEvent.VK_F2;
				break;
			case F3:
				id = KeyEvent.VK_F3;
				break;
			case F4:
				id = KeyEvent.VK_F4;
				break;
			case F5:
				id = KeyEvent.VK_F5;
				break;
			case F6:
				id = KeyEvent.VK_F6;
				break;
			case F7:
				id = KeyEvent.VK_F7;
				break;
			case F8:
				id = KeyEvent.VK_F8;
				break;
			case F9:
				id = KeyEvent.VK_F9;
				break;
			case F10:
				id = KeyEvent.VK_F10;
				break;
			case F11:
				id = KeyEvent.VK_F11;
				break;
			case F12:
				id = KeyEvent.VK_F12;
				break;

			case DIG1:
				id = KeyEvent.VK_1;
				break;
			case DIG2:
				id = KeyEvent.VK_2;
				break;
			case DIG3:
				id = KeyEvent.VK_3;
				break;
			case DIG4:
				id = KeyEvent.VK_4;
				break;
			case DIG5:
				id = KeyEvent.VK_5;
				break;
			case DIG6:
				id = KeyEvent.VK_6;
				break;
			case DIG7:
				id = KeyEvent.VK_7;
				break;
			case DIG8:
				id = KeyEvent.VK_8;
				break;
			case DIG9:
				id = KeyEvent.VK_9;
				break;
			case DIG0:
				id = KeyEvent.VK_0;
				break;
			case BACK_SPACE:
				id = KeyEvent.VK_BACK_SPACE;
				break;
			case INSERT:
				id = KeyEvent.VK_INSERT;
				break;
			case HOME:
				id = KeyEvent.VK_HOME;
				break;
			case PAGE_UP:
				id = KeyEvent.VK_PAGE_UP;
				break;

			case TAB:
				id = KeyEvent.VK_TAB;
				break;
			case Q:
				id = KeyEvent.VK_Q;
				break;
			case W:
				id = KeyEvent.VK_W;
				break;
			case E:
				id = KeyEvent.VK_E;
				break;
			case R:
				id = KeyEvent.VK_R;
				break;
			case T:
				id = KeyEvent.VK_T;
				break;
			case Y:
				id = KeyEvent.VK_Y;
				break;
			case U:
				id = KeyEvent.VK_U;
				break;
			case I:
				id = KeyEvent.VK_I;
				break;
			case O:
				id = KeyEvent.VK_O;
				break;
			case P:
				id = KeyEvent.VK_P;
				break;
			case SLASH:
				id = KeyEvent.VK_SLASH;
				break;
			case BACK_SLASH:
				id = KeyEvent.VK_BACK_SLASH;
				break;
			case DELETE:
				id = KeyEvent.VK_DELETE;
				break;
			case END:
				id = KeyEvent.VK_END;
				break;
			case PAGE_DOWN:
				id = KeyEvent.VK_PAGE_DOWN;
				break;

			case CAPS_LOCK:
				id = KeyEvent.VK_CAPS_LOCK;
				break;
			case A:
				id = KeyEvent.VK_A;
				break;
			case S:
				id = KeyEvent.VK_S;
				break;
			case D:
				id = KeyEvent.VK_D;
				break;
			case F:
				id = KeyEvent.VK_F;
				break;
			case G:
				id = KeyEvent.VK_G;
				break;
			case H:
				id = KeyEvent.VK_H;
				break;
			case J:
				id = KeyEvent.VK_J;
				break;
			case K:
				id = KeyEvent.VK_K;
				break;
			case L:
				id = KeyEvent.VK_L;
				break;
			case SEMICOLON:
				id = KeyEvent.VK_SEMICOLON;
				break;
			case QUOTE:
				id = KeyEvent.VK_QUOTE;
				break;
			case DOUBLE_QUOTE:
				id = KeyEvent.VK_QUOTEDBL;
				break;
			case ENTER:
				id = KeyEvent.VK_ENTER;
				break;

			case SHIFT:
				id = KeyEvent.VK_SHIFT;
				break;
			case Z:
				id = KeyEvent.VK_Z;
				break;
			case X:
				id = KeyEvent.VK_X;
				break;
			case C:
				id = KeyEvent.VK_C;
				break;
			case V:
				id = KeyEvent.VK_V;
				break;
			case B:
				id = KeyEvent.VK_B;
				break;
			case N:
				id = KeyEvent.VK_N;
				break;
			case M:
				id = KeyEvent.VK_M;
				break;
			case UP:
				id = KeyEvent.VK_UP;
				break;

			case CONTROL:
				id = KeyEvent.VK_CONTROL;
				break;
			case ALT:
				id = KeyEvent.VK_ALT;
				break;
			case SPACE:
				id = KeyEvent.VK_SPACE;
				break;
			case LEFT:
				id = KeyEvent.VK_LEFT;
				break;
			case DOWN:
				id = KeyEvent.VK_DOWN;
				break;

			case RIGHT:
				id = KeyEvent.VK_RIGHT;
				break;

			case PLUS:
				id = KeyEvent.VK_PLUS;
				break;
			case MINUS:
				id = KeyEvent.VK_MINUS;
				break;

			case UNDERSCORE:
				id = KeyEvent.VK_UNDERSCORE;
				break;
		}
		return id;
	}

	private TreeNode find(TreeNode node, int level, String[] path)
	{
		if (level >= path.length)
		{
			return null;
		}
		String value = path[level];

		if (node.toString().equals(value))
		{
			if (level == path.length - 1)
			{
				return node;
			}
			else
			{
				Enumeration<TreeNode> children = node.children();
				while (children.hasMoreElements())
				{
					TreeNode found = find(children.nextElement(), level + 1, path);
					if (found != null)
					{
						return found;
					}
				}
			}
		}

		return null;
	}

	/*
	 *	this code from org.fest.swing.driver.JTreeDriver.java
	 */
	private Point scrollToRow(JTree tree, int row)
	{
		Point p = scrollToRow(tree, row, new JTreeLocation()).ii;
		waitForIdle();
		return p;
	}

	private static Pair<Boolean, Point> scrollToRow(final JTree tree, final int row, final JTreeLocation location)
	{
		return execute(new GuiQuery<Pair<Boolean, Point>>()
		{
			protected Pair<Boolean, Point> executeInEDT()
			{
				validateIsEnabledAndShowing(tree);
				Point p = scrollToVisible(tree, row, location);
				boolean selected = tree.getSelectionCount() == 1 && tree.isRowSelected(row);
				return new Pair<Boolean, Point>(selected, p);
			}
		});
	}

	private static Point scrollToVisible(JTree tree, int row, JTreeLocation location)
	{
		Pair<Rectangle, Point> boundsAndCoordinates = location.rowBoundsAndCoordinates(tree, row);
		tree.scrollRectToVisible(boundsAndCoordinates.i);
		return boundsAndCoordinates.ii;
	}

	@SuppressWarnings("unchecked")
	<T extends Component> ComponentFixture<T> getFixture(T component) throws RemoteException
	{
		if (component instanceof JButton)
		{
			return (ComponentFixture<T>) new JButtonFixture(this.currentRobot, (JButton) component);
		}
		else if (component instanceof JCheckBox)
		{
			return (ComponentFixture<T>) new JCheckBoxFixture(this.currentRobot, (JCheckBox) component);
		}
		else if (component instanceof JComboBox)
		{
			return (ComponentFixture<T>) new JComboBoxFixture(this.currentRobot, (JComboBox<?>) component);
		}
		else if (component instanceof JDialog)
		{
			DialogFixture fixture = new DialogFixture(this.currentRobot, (JDialog) component);
			fixture.target.toFront();
			fixture.focus();
			return (ComponentFixture<T>) fixture;
		}
		else if (component instanceof JFrame)
		{
			FrameFixture frameFixture = new FrameFixture(this.currentRobot, (Frame) component);
			frameFixture.target.toFront();
			return (ComponentFixture<T>) frameFixture;
		}
		else if (component instanceof JLabel)
		{
			return (ComponentFixture<T>) new JLabelFixture(this.currentRobot, (JLabel) component);
		}
		else if (component instanceof JList)
		{
			return (ComponentFixture<T>) new JListFixture(this.currentRobot, ((JList) component));
		}
		else if (component instanceof JMenuItem)
		{
			return (ComponentFixture<T>) new JMenuItemFixture(this.currentRobot, ((JMenuItem) component));
		}
		else if (component instanceof JPanel)
		{
			return (ComponentFixture<T>) new JPanelFixture(this.currentRobot, ((JPanel) component));
		}
		else if (component instanceof JProgressBar)
		{
			return (ComponentFixture<T>) new JProgressBarFixture(this.currentRobot, ((JProgressBar) component));
		}
		else if (component instanceof JScrollBar)
		{
			return (ComponentFixture<T>) new JScrollBarFixture(this.currentRobot, (JScrollBar) component);
		}
		else if (component instanceof JSlider)
		{
			return (ComponentFixture<T>) new JSliderFixture(this.currentRobot, (JSlider) component);
		}
		else if (component instanceof JSpinner)
		{
			return (ComponentFixture<T>) new JSpinnerFixture(this.currentRobot, (JSpinner) component);
		}
		else if (component instanceof JTable)
		{
			return (ComponentFixture<T>) new JTableFixture(this.currentRobot, (JTable) component);
		}
		else if (component instanceof JTabbedPane)
		{
			return (ComponentFixture<T>) new JTabbedPaneFixture(this.currentRobot, (JTabbedPane) component);
		}
		else if (component instanceof JTextComponent)
		{
			return (ComponentFixture<T>) new JTextComponentFixture(this.currentRobot, (JTextComponent) component);
		}
		else if (component instanceof JToggleButton)
		{
			return (ComponentFixture<T>) new JToggleButtonFixture(this.currentRobot, (JToggleButton) component);
		}
		else if (component instanceof JTree)
		{
			return (ComponentFixture<T>) new JTreeFixture(this.currentRobot, (JTree) component);
		}
		else if (component instanceof JSplitPane)
		{
			return (ComponentFixture<T>) new JSplitPaneFixture(this.currentRobot, ((JSplitPane) component));
		}
		return new ComponentFixture<T>(this.currentRobot, component)
		{
		};
	}
}
