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
import org.apache.log4j.Logger;
import org.fest.swing.core.ComponentMatcher;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.MouseClickInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.data.TableCell;
import org.fest.swing.exception.WaitTimedOutError;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.*;
import org.fest.swing.timing.Pause;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

public class SwingOperationExecutor implements OperationExecutor<ComponentFixture<Component>>
{
	private Robot currentRobot;
	private Logger logger;
	private Component currentFrame = null;

	public SwingOperationExecutor(Robot currentRobot, Logger logger)
	{
		this.currentRobot = currentRobot;
		this.logger = logger;
	}

	public void setCurrentFrame(Component frame)
	{
		this.currentFrame = frame;
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
			Container owner = null;
			if (window != null && window.target instanceof Container)
			{
				owner = (Container) window.target;
			}
			else
			{
				owner = (Container) this.currentFrame();
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
			Container container = null;
			if (owner != null)
			{
				ComponentFixture<Component> found = find(null, owner);
				if (found != null && found.target instanceof Container)
				{
					container = (Container) found.target;
				}
			}

			List<ComponentFixture<Component>> res = new ArrayList<>();
			MatcherSwing<Component> matcher = new MatcherSwing<Component>(Component.class, container, element.getControlKind(), element);
			Collection<Component> components = this.currentRobot.finder().findAll(container, matcher);
			for (final Component component : components)
			{
				res.add(getFixture(component));
			}
			return res;
		}
		catch (Throwable e)
		{
			logger.error(String.format("findAll(%s, %s)", owner, element));
			logger.error(e.getMessage(), e);
			throw new Exception("Unable to find component " + element, e);
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
		catch (Throwable e)
		{
			logger.error(String.format("find(%s, %s)", owner, element));
			logger.error(e.getMessage(), e);
			throw new Exception("Unable to find component " + element, e);
		}
	}

	@Override
	public ComponentFixture<Component> lookAtTable(ComponentFixture<Component> component, Locator additional, Locator header, int x, int y) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			//================ WORK variant
			JTableFixture tableFixture = new JTableFixture(this.currentRobot, table);
			JTableCellFixture cell = tableFixture.cell(TableCell.row(y).column(x));

			cell.click();
			cell.doubleClick();
			cell.rightClick();

			cell.enterValue("");
			cell.value();

			cell.select();
			//================

			return new ComponentFixture<Component>(this.currentRobot, cell.editor())
			{
			};
		}
		catch (Throwable e)
		{
			logger.error(String.format("lookAtTable(%s, %s, %s, %d, %d)", component, additional, header, x, y));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean mouse(ComponentFixture<Component> component, int x, int y, MouseAction action) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			toFront();
			Point point = new Point(x, y);
			switch (action)
			{
				case Move:
					if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
					{
						this.currentRobot.moveMouse(component.target);
					}
					else
					{
						this.currentRobot.moveMouse(component.target, point);
					}
					break;

				case LeftClick:
					if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
					{
						this.currentRobot.click(component.target, MouseClickInfo.leftButton().button(), 1);
					}
					else
					{
						this.currentRobot.click(component.target, point, MouseClickInfo.leftButton().button(), 1);
					}
					break;

				case LeftDoubleClick:
					if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
					{
						this.currentRobot.click(component.target, MouseClickInfo.leftButton().button(), 2);
					}
					else
					{
						this.currentRobot.click(component.target, point, MouseClickInfo.leftButton().button(), 2);
					}
					break;

				case RightClick:
					if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
					{
						this.currentRobot.click(component.target, MouseClickInfo.rightButton().button(), 1);
					}
					else
					{
						this.currentRobot.click(component.target, point, MouseClickInfo.rightButton().button(), 1);
					}
					break;

				case RightDoubleClick:
					if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
					{
						this.currentRobot.click(component.target, MouseClickInfo.rightButton().button(), 2);
					}
					else
					{
						this.currentRobot.click(component.target, point, MouseClickInfo.rightButton().button(), 2);
					}
					break;
			}
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
			if (component instanceof KeyboardInputSimulationFixture)
			{
				KeyboardInputSimulationFixture inputSimulationFixture = (KeyboardInputSimulationFixture) component;
				inputSimulationFixture.pressAndReleaseKey(KeyPressInfo.keyCode(getKeyCode(key)));
				return true;
			}
			throw new RemoteException(String.format("Component %s dosen't support press operation", component));
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
			throw new RemoteException(String.format("Component %s dosen't support press operation", component));
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
	public boolean toggle(ComponentFixture<Component> component, boolean value) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			if (currentComponent instanceof JToggleButton)
			{
				JToggleButton toggleButton = component.targetCastedTo(JToggleButton.class);
				toggleButton.setSelected(value);
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
	public boolean select(ComponentFixture<Component> component, String selectedText) throws Exception
	{
		try
		{
			if (!component.target.isEnabled())
			{
				throw new RemoteException("Component " + component + " is disabled.");
			}

			this.currentRobot.waitForIdle();

			if (component.target instanceof JComboBox)
			{
				JComboBox<?> comboBox = component.targetCastedTo(JComboBox.class);
				comboBox.setSelectedItem(selectedText);
			}
			else if (component.target instanceof JTabbedPane)
			{
				JTabbedPane tabPane = component.targetCastedTo(JTabbedPane.class);
				int number = tabPane.getTabCount();
				for (int i = 0; i < number; i++)
				{
					String name = tabPane.getTitleAt(i);
					if (name != null && name.equals(selectedText))
					{
						tabPane.setSelectedIndex(i);
						break;
					}
				}
			}
			else if (component.target instanceof JList)
			{
				JList jList = component.targetCastedTo(JList.class);
				jList.setSelectedValue(selectedText, true);
			}

			waitForIdle();
			return true;
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
					throw new Exception("Path '" + path + "' is not found in the tree.");
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
		catch (Throwable e)
		{
			logger.error(String.format("fold(%s, %b)", component, collaps));
			logger.error(e.getMessage(), e);
			throw e;
		}
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
		throw new RemoteException(String.format("Menu with name '%s' not found in menu '%s'", menuName, parent.getText()));
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
			throw new Exception(String.format("Component %s does not support text entering", component.target));
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
	public boolean wait(Locator locator, int ms, final boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();

		try
		{
			final MatcherSwing<Component> matcher = new MatcherSwing<Component>(Component.class, null, locator.getControlKind(), locator);
			final boolean[] result = {false};

			Pause.pause(new org.fest.swing.timing.Condition("Waiting")
			{
				@Override
				public boolean test()
				{
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
		catch (Throwable e)
		{
			logger.error(String.format("getValue(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
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
		throw new RemoteException(String.format("Component %s don't have value", currentComponent));
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
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean textTableCell(ComponentFixture<Component> component, int column, int row, String text) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture tableFixture = new JTableFixture(this.currentRobot, table);
			tableFixture.enterValue(TableCell.row(row).column(column), text);
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
			toFront();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture tableFixture = new JTableFixture(this.currentRobot, table);
			JTableCellFixture cell = tableFixture.cell(TableCell.row(row).column(column));
			switch (action)
			{
				case Move:
					this.currentRobot.moveMouse(table); // TODO calculate real coordinates and click on it
					break;

				case LeftClick:
					cell.click(MouseClickInfo.leftButton().button());
					break;

				case LeftDoubleClick:
					cell.doubleClick();
					break;

				case RightClick:
					cell.rightClick();
					break;

				case RightDoubleClick:
					cell.rightClick();
					cell.rightClick();
					break;
			}
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
				return getValue(editor);
			}
			throw new RemoteException(String.format("Table %s with row %s and column %s don't have editor", component, row, column));
		}
		catch (Throwable e)
		{
			logger.error(String.format("get(%s, %d, %d)", component, column, row));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRow(ComponentFixture<Component> component, Locator rows, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
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
						throw new RemoteException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
					}
					String value = String.valueOf(table.getModel().getValueAt(Integer.parseInt(rowsIndexes.get(0)), colIndex));

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
		catch (Throwable e)
		{
			logger.error(String.format("getRow(%s, %s, %s, %b, %s, %s)", component, rows, header, useNumericHeader, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> getRowIndexes(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture fixture = new JTableFixture(this.currentRobot, table);
			Map<String, Integer> fieldIndexes = getTableHeaders(table);

			return getIndexes(fixture, fieldIndexes, valueCondition, colorCondition);
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRowByIndex(%s, %s, %s, %b, %s, %s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}


	@Override
	public Map<String, String> getRowByIndex(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);

			Map<String, Integer> fieldIndexes = getTableHeaders(table);

			Map<String, String> ret = new LinkedHashMap<String, String>();

			for (Entry<String, Integer> entry : fieldIndexes.entrySet())
			{
				String name = entry.getKey();
				Integer colIndex = entry.getValue();

				String value = String.valueOf(table.getModel().getValueAt(i, colIndex));
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
	public Map<String, ValueAndColor> getRowWithColor(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture fixture = new JTableFixture(this.currentRobot, table);
			Map<String, Integer> fieldIndexes = getTableHeaders(table);
			Map<String, ValueAndColor> ret = new LinkedHashMap<String, ValueAndColor>();

			for (Entry<String, Integer> entry : fieldIndexes.entrySet())
			{
				String name = entry.getKey();
				Integer colIndex = entry.getValue();

				String value = String.valueOf(table.getModel().getValueAt(i, colIndex));
				String underscopedName = name.replace(' ', '_');

				Color color = fixture.foregroundAt(TableCell.row(i).column(colIndex)).target();
				Color backColor = fixture.backgroundAt(TableCell.row(i).column(colIndex)).target();

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
	public String[][] getTable(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			int rows = table.getRowCount();
			int columns = table.getColumnCount();

			String[][] res = new String[rows + 1][];

			List<String> headers = getHeaders(table);
			res[0] = new String[columns];
			for (int column = 0; column < columns; column++)
			{
				res[0][column] = headers.get(column);
			}

			for (int row = 0; row < rows; row++)
			{
				res[row + 1] = new String[columns];
				for (int column = 0; column < columns; column++)
				{
					Object value = table.getValueAt(row, column);
					res[row + 1][column] = "" + value;
				}
			}

			return res;
		}
		catch (Throwable e)
		{
			logger.error(String.format("getTable(%s, %s, %s, %b)", component, additional, header, useNumericHeader));
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
					ret = (ComponentFixture<T>) new JListFixture(this.currentRobot, (JList) component);
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
					throw new RemoteException("Cannot find the component : " + controlKind);
			}
		}
		this.currentRobot.waitForIdle();

		return ret;
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
			component = this.currentRobot.finder().find(new MatcherSwing<Component>(Component.class, currentFrame(), locator.getControlKind(), locator));
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
					throw new RemoteException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
				}
				Object value = table.getModel().getValueAt(i, index);
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
					throw new RemoteException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
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

	private List<String> getHeaders(JTable table) throws Exception
	{
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < table.getColumnCount(); i++)
		{
			res.add(table.getColumnName(i));
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

	public Component currentFrame()
	{
		if (this.currentFrame == null)
		{
			this.currentFrame = this.currentRobot.finder().find(new ComponentMatcher()
			{
				@Override
				public boolean matches(Component c)
				{
					return c != null && (c instanceof JFrame);
				}
			});
		}
		return this.currentFrame;
	}

	@SuppressWarnings("unchecked")
	private <T extends Component> ComponentFixture<T> getFixture(T component) throws RemoteException
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
			return (ComponentFixture<T>) new JComboBoxFixture(this.currentRobot, (JComboBox) component);
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

	private void toFront()
	{
		Component frame = this.currentFrame();
		if (frame instanceof JFrame)
		{
			((JFrame) frame).setExtendedState(JFrame.ICONIFIED);
			this.currentRobot.waitForIdle();
			((JFrame) frame).setExtendedState(JFrame.NORMAL);
			this.currentRobot.waitForIdle();
		}
	}
}
