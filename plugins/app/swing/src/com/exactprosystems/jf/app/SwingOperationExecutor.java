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
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.conditions.StringCondition;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;
import com.exactprosystems.jf.api.error.app.WrongParameterException;
import org.apache.log4j.Logger;
import org.fest.swing.awt.AWT;
import org.fest.swing.core.ComponentMatcher;
import org.fest.swing.core.MouseButton;
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
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.event.FocusEvent;
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

public class SwingOperationExecutor extends AbstractOperationExecutor<ComponentFixture<Component>>
{
	private Robot currentRobot;
	private Logger logger;

	private boolean isAltDown = false;
	private boolean isShiftDown = false;
	private boolean isControlDown = false;

	public SwingOperationExecutor(Robot currentRobot, Logger logger, boolean useTrimText)
	{
		super(useTrimText);
		this.currentRobot = currentRobot;
		this.logger = logger;
	}

	public Component fromOwner(Locator owner)  throws RemoteException
	{
        try
        {
            Component component = null;
            logger.debug("owner : " + owner);
            if (owner == null)
            {
                component = currentRoot();
            }
            else
            {
                    component = find(null, owner).target;
            }
            return component;
        }
        catch (Exception e)
        {
            throw new RemoteException(e.getMessage(), e);
        }
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

	//region OperationExecutor methods

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
	public Color getColor(ComponentFixture<Component> component, boolean isForeground) throws Exception
	{
		try
		{
			if (isForeground)
			{
				return component.target.getForeground();
			}
			else
			{
				return component.target.getBackground();
			}
		}
		catch (Throwable e)
		{
			logger.error(String.format("getColor(%s, %s)", component, isForeground));
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
			Container owner = (Container)fromOwner(null);
			if (window != null && window.target instanceof Container)
			{
				owner = (Container) window.target;
			}

			List<ComponentFixture<Component>> res = new ArrayList<>();
			MatcherSwing<Component> matcher = new MatcherSwing<>(this.info, Component.class, owner, controlKind, locator);
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
			Container container = (Container)fromOwner(owner);  
			List<ComponentFixture<Component>> res = new ArrayList<>();
			MatcherSwing<Component> matcher = new MatcherSwing<>(this.info, Component.class, container, element.getControlKind(), element);
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
    public List<ComponentFixture<Component>> findByXpath(ComponentFixture<Component> component, String path) throws Exception
	{
		JTree tree = component.targetCastedTo(JTree.class);
		NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(tree), path);
		if(nodes.getLength() != 0)
		{
			List<ComponentFixture<Component>> list = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				JTreeItem treeItem = new JTreeItem(tree, (TreePath) nodes.item(i).getUserData("path"));
				if(treeItem.isVisible())
				{
					list.add(new JTreeItemFixture(this.currentRobot, treeItem));
				}
			}
			return list;
		}
		else
		{
			return Collections.emptyList();
		}
    }

    @Override
	public ComponentFixture<Component> lookAtTable(ComponentFixture<Component> component, Locator additional, Locator header, int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("lookAtTable");
	}

	@Override
	public boolean elementIsEnabled(ComponentFixture<Component> component)
	{
		return component.target.isEnabled();
	}

    @Override
    public boolean elementIsVisible(ComponentFixture<Component> component)
    {
        return component.target.isVisible();
    }

	@Override
	public boolean mouse(ComponentFixture<Component> component, int x, int y, MouseAction action) throws Exception
	{
		try
		{
			Component target = component.target;
			if (target instanceof JComponent)
			{
				Scrolling.scrollToVisible(this.currentRobot, ((JComponent) target));
			}
			Point point = new Point(x, y);
			if (target instanceof JTreeItem)
			{
				JTreeItem treeItem = (JTreeItem) target;
				Pair<Rectangle, Point> pointPair = new JTreeLocation().pathBoundsAndCoordinates(treeItem.getTree(), treeItem.getPath());
				treeItem.getTree().scrollPathToVisible(treeItem.getPath());
				if(isCoordsDidNotIntroduce(x,y))
				{
					executeAction(action, treeItem.getTree(), pointPair.ii.x, pointPair.ii.y);
				}
				else
				{
					executeAction(action, treeItem.getTree(), pointPair.ii.x - pointPair.i.width/2 + point.x, pointPair.ii.y - pointPair.i.height/2 + point.y);
				}
				return true;
			}

			if (isCoordsDidNotIntroduce(x,y))
			{
				point = AWT.visibleCenterOf(target);
			}

			executeAction(action, target, point.x, point.y);
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
			int modifiers = getModifierKeysArePressed();
			events.add(new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, (char) keyCode));
			if (this.isControlDown){
				events.add(new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, (char) getKeyCodeWithControl(key)));
			} else {
				events.add(new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, (char) keyCode));
			}
			events.add(new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, (char) keyCode));
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

			final Component target = component.target;
			final ArrayList<InputEvent> events = new ArrayList<>();
			int keyCode = getKeyCode(key);
			int modifiers = getModifierKeysArePressed();
			boolean needPress = (keyCode != getKeyCode(Keyboard.CONTROL) && keyCode != getKeyCode(Keyboard.SHIFT) && keyCode != getKeyCode(Keyboard.ALT) && keyCode != getKeyCode(Keyboard.CAPS_LOCK));

			if(b)
			{
				events.add(new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, (char) keyCode));

				if(needPress)
				{
					if (this.isControlDown){
						events.add(new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, (char) getKeyCodeWithControl(key)));
					} else {
						events.add(new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, KeyEvent.VK_UNDEFINED, (char) keyCode));
					}
				}
			}
			else
			{
				events.add(new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, (char) keyCode));
			}

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
				JList<?> jList = component.targetCastedTo(JList.class);
				jList.setSelectedIndex(index);
			}
			else if (component.target instanceof JTree)
			{
				JTree jTree = component.targetCastedTo(JTree.class);
				jTree.setSelectionRow(index);
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
				JList<?> jList = component.targetCastedTo(JList.class);
				jList.setSelectedValue(selectedText, true);
			}
			else if (component.target instanceof JTree)
			{
				JTree tree = component.targetCastedTo(JTree.class);
				NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(tree), selectedText);
				TreePath[] rows = new TreePath[nodes.getLength()];
				for (int i = 0; i < nodes.getLength(); i++) {
					rows[i] = (TreePath) nodes.item(i).getUserData("path");
					tree.expandPath(rows[i]);
				}
				tree.setSelectionPaths(rows);
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
	public boolean expand(ComponentFixture<Component> component, String path, boolean expandOrCollapse) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Component currentComponent = component.component();
			if (currentComponent instanceof JMenu)
			{
				ArrayList<String> items = new ArrayList<>();
				items.addAll(Arrays.asList(path.split("/")));

				JMenu menu = (JMenu) currentComponent;
				if(items.size() <= 1)
				{
					if(expandOrCollapse)
					{
						menu.doClick();
					}
					else
					{
						MenuSelectionManager.defaultManager().clearSelectedPath();
					}
					return true;
				}

				items.remove(0);
				if(!expandOrCollapse)
				{
					items.remove(items.size() - 1);
				}

				menu = findMenu(menu, items);

				if(menu != null)
				{
					menu.doClick();
				}
				else
				{
					throw new WrongParameterException("The menu element was not found in path '" + path + "'");
				}
			}
			else if (currentComponent instanceof JTree)
			{
				JTree tree = component.targetCastedTo(JTree.class);
				NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(tree), path);
				if (nodes.getLength() == 0)
				{
					throw new WrongParameterException("Path '" + path + "' is not found in the tree.");
				}
				TreePath[] rows = new TreePath[nodes.getLength()];
				for (int i = nodes.getLength() - 1; i >= 0; i--)
				{
					rows[i] = (TreePath) nodes.item(i).getUserData("path");
					if (expandOrCollapse)
					{
						tree.expandPath(rows[i]);
					}
					else
					{
						tree.collapsePath(rows[i]);
					}
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
			logger.error(String.format("fold(%s, %b)", component, expandOrCollapse));
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
			else if (component.target instanceof JComboBox)
			{
				JComboBox jComboBox = component.targetCastedTo(JComboBox.class);
				if (!jComboBox.isEditable())
				{
					throw new Exception("ComboBox is not editable");
				}
				Component editorComponent = jComboBox.getEditor().getEditorComponent();
				if (editorComponent instanceof JTextComponent)
				{
					JTextComponent textComponent = (JTextComponent) editorComponent;
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
			}
			return true;
			//throw new OperationNotAllowedException(String.format("Component %s does not support text entering", component.target));
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
						matcher = new MatcherSwing<>(SwingOperationExecutor.this.info, Component.class, currentRoot(), locator.getControlKind(), locator);
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
				((JSplitPane) currentComponent).setDividerLocation((int) value);
				return true;
			}
			else if(currentComponent instanceof JSpinner)
			{
				((JSpinner) currentComponent).setValue((int) value);
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
	public String getValueDerived(ComponentFixture<Component> component) throws Exception
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
	public List<String> getListDerived(ComponentFixture<Component> fixture, boolean onlyVisible) throws Exception
	{
		ListModel<?> model = getListModelFromComponentOrError(fixture.target);
		return getListOfNamesFromListItems(model);
	}

	@Override
	public Document getTree(ComponentFixture<Component> component) throws Exception
	{
		return convertTreeToXMLDoc((JTree)component.target);
	}

	@Override
	public String getDerived(ComponentFixture<Component> component) throws Exception
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
	public String getAttrDerived(ComponentFixture<Component> component, String name) throws Exception
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
	public String scriptDerived(ComponentFixture<Component> component, String script) throws FeatureNotSupportedException
	{
		throw new FeatureNotSupportedException("script");
	}

	@Override
	public boolean dragNdrop(ComponentFixture<Component> drag, int x1, int y1, ComponentFixture<Component> drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		try
		{
			if(moveCursor)
			{
				dragNdropThrowRobot(drag, x1, y1, drop, x2, y2);
			}
			else
			{
				dragNdropThrowEvents(drag, x1, y1, drop, x2, y2);
			}
			return true;
		}
		catch (Exception e)
		{
            this.logger.error(String.format("dragNdrop(%s,%d,%d,%s,%d,%d)", drag.target.getName(), x1, y1, drag.target.getName(), x2, y2));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean scrollTo(ComponentFixture<Component> component, int index) throws Exception
	{
		try
		{
			if (component.target instanceof JList)
			{
				JList<?> jList = component.targetCastedTo(JList.class);
				jList.ensureIndexIsVisible(index);
			}
			else if (component.target instanceof JTree)
			{
				JTree jTree = component.targetCastedTo(JTree.class);
				jTree.scrollRowToVisible(index);
			}
			waitForIdle();
			return true;
		}
		catch (Exception e)
		{
			logger.error(String.format("scrollTo(%s,%d)", component, index));
			logger.error(e.getMessage(), e);
			throw e;
		}
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
			final JTable table = component.targetCastedTo(JTable.class);
			Point point = new JTableLocation().pointAt(table, row, column);
			executeAction(action, table, point.x, point.y);

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
	public String getValueTableCellDerived(ComponentFixture<Component> component, int column, int row) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			JTableFixture fixture = new JTableFixture(this.currentRobot, table);
			return String.valueOf(getValueTableCell(fixture, row, column));
		}
		catch (Throwable e)
		{
			logger.error(String.format("get(%s, %d, %d)", component, column, row));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRowDerived(ComponentFixture<Component> component, Locator rows, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try {
			this.currentRobot.waitForIdle();
			Map<String, String> ret = new LinkedHashMap<>();
			if (component.target instanceof JTable) {
				JTable table = component.targetCastedTo(JTable.class);
				JTableFixture fixture = new JTableFixture(this.currentRobot, table);
				Map<String, Integer> fieldIndexes = getTableHeaders(table, columns);
				List<String> rowsIndexes = getIndexes(fixture, fieldIndexes, valueCondition, colorCondition);

				if (rowsIndexes.size() == 1) {
					for (Entry<String, Integer> entry : fieldIndexes.entrySet()) {
						String name = entry.getKey();
						Integer colIndex = fieldIndexes.get(name);
						if (colIndex == null) {
							throw new WrongParameterException("The column '" + name + "' is not found. Possible values are: " + humanReadableHeaders(fieldIndexes));
						}
						String value = String.valueOf(getValueTableCell(fixture, Integer.parseInt(rowsIndexes.get(0)), colIndex));

						String underscopedName = name.replace(' ', '_');
						ret.put(underscopedName, value);
					}
					return ret;
				} else if (rowsIndexes.size() > 1) {
					throw new Exception("Too many rows");
				}
			}
			//TODO remove this branch. We can't call Do.getRow with condition on tree (see ControlKind.java)
			if (component.target instanceof JTree) {
				String path = ((StringCondition) valueCondition).getValue();
				JTree tree = component.targetCastedTo(JTree.class);
				int index = findIndex(tree, path);
				if (index == -1) {
					throw new WrongParameterException("Path '" + path + "' is not found in the tree.");
				}
				ret.put("value", convertJTreeToPathsList((JTree)component.target).get(index));
				return ret;
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
			if (component.target instanceof JTable)
			{
				JTable table = component.targetCastedTo(JTable.class);
				JTableFixture fixture = new JTableFixture(this.currentRobot, table);
				Map<String, Integer> fieldIndexes = getTableHeaders(table, columns);
				return getIndexes(fixture, fieldIndexes, valueCondition, colorCondition);
			}
			if (component.target instanceof JTree)
			{
				String path = ((StringCondition) valueCondition).getValue();
				JTree tree = component.targetCastedTo(JTree.class);
				int index = findIndex(tree, path);
				if (index == -1) {
					return Collections.emptyList();
				}
				List<String> res = new ArrayList<>();
				res.add(String.valueOf(index));
				return res;
			}
			return Collections.emptyList();
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
	public Map<String, String> getRowByIndexDerived(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			Map<String, String> ret = new LinkedHashMap<>();
			if(component.target instanceof JTable) {
				JTable table = component.targetCastedTo(JTable.class);
				Map<String, Integer> fieldIndexes = getTableHeaders(table, columns);
				JTableFixture fixture = (JTableFixture) (ComponentFixture<? extends Component>) component;
				for (Entry<String, Integer> entry : fieldIndexes.entrySet()) {
					String name = entry.getKey();
					Integer colIndex = entry.getValue();

					String value = String.valueOf(getValueTableCell(fixture, i, colIndex));
					String underscopedName = name.replace(' ', '_');

					ret.put(underscopedName, value);
				}
			}
			//TODO remove this branch. We can't call Do.getRow with condition on tree (see ControlKind.java)
			if(component.target instanceof JTree)
			{
				ret.put("value", convertJTreeToPathsList((JTree)component.target).get(i));
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
	public Map<String, ValueAndColor> getRowWithColorDerived(ComponentFixture<Component> component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			Map<String, Integer> fieldIndexes = getTableHeaders(table, columns);
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
	public String[][] getTableDerived(final ComponentFixture<Component> component, Locator additional, Locator header, final boolean useNumericHeader, String[] columns) throws Exception
	{
		try
		{
			this.currentRobot.waitForIdle();
			JTable table = component.targetCastedTo(JTable.class);
			int rows = table.getRowCount();
			int columnsCount = table.getColumnCount();

			String[][] res = new String[rows + 1][];
			res[0] = new String[columnsCount];
			Map<String, Integer> headers = getTableHeaders(table, columns);
			for (Entry<String, Integer> entry : headers.entrySet())
			{
				String columnCaption = entry.getKey();
				Integer columnNumber = entry.getValue();

				res[0][columnNumber] = (useNumericHeader) ? String.valueOf(columnNumber) : columnCaption;
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
							value = getValue(tableCellRendererComponent);
//							if (tableCellRendererComponent instanceof JLabel)
//							{
//								Icon icon = ((JLabel) tableCellRendererComponent).getIcon();
//								//if we don't have icon, put empty value
//								value = icon != null ? String.valueOf(icon) : "";
//							}
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
		} catch (Throwable e)
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

    @Override
    public Color getColorXY(ComponentFixture<Component> component, int x, int y) throws Exception
    {
        Point point = getPointLocation(component.target, x, y);
        return new java.awt.Robot().getPixelColor(point.x, point.y);
    }

	
	//endregion

	//region private methods
	@SuppressWarnings("unchecked")
	private <T extends Component> ComponentFixture<T> getComponent(Locator owner, Locator locator) throws RemoteException
	{
		ComponentFixture<T> ret = null;
        Component own = fromOwner(owner);
		ComponentFixture<?> window = getFixture(own);
		
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
					break;

				case Frame:
					//if need more time, use method withTimeout
					if (own == null)
					{
						own = this.currentRoot();
					}
					ret = (ComponentFixture<T>) WindowFinder.findFrame(new MatcherSwing<Frame>(this.info, Frame.class, own, null, locator)).using(currentRobot);
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

				case Spinner:
					component = getComp(JSpinner.class, window, locator);
					ret = (ComponentFixture<T>) new JSpinnerFixture(this.currentRobot, (JSpinner) component);
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

	private Map<String, Integer> getTableHeaders(final JTable table, String[] columns)
	{
		List<String> headers = new ArrayList<>();
		Map<String, Integer> result = new LinkedHashMap<>();
		String realName = "";
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
		{
			if(columns == null)
			{
				realName = table.getColumnModel().getColumn(i).getHeaderValue().toString();
				if(realName.equals("-"))
				{
					realName = "";
				}
			}
			else
			{
				realName = (i < columns.length) ? columns[i] : String.valueOf(i);
			}

			String underscopedName = realName.replace(' ', '_');
			headers.add(underscopedName);
		}
		List<String> newColumns = Converter.convertColumns(headers);
		int count = 0;
		for (String newColumn : newColumns)
		{
			result.put(newColumn, count++);
		}
		return result;
	}

	private Object getValueTableCell(JTableFixture fixture, int row, int column) throws IllegalAccessException
	{
		JTable table = fixture.target;
		Object valueAt = table.getValueAt(row, column);

		if(valueAt != null)
		{
			if (valueAt instanceof Boolean)
			{
				return valueAt.toString();
			}

			Component rendererComponent = table.getCellRenderer(row, column).getTableCellRendererComponent(table, valueAt, true, true, row, column);
			if (rendererComponent instanceof JLabel)
			{
				JLabel label = (JLabel) rendererComponent;
				StringBuffer result = new StringBuffer();
				result.append(label.getText());
				if (!label.getText().isEmpty() && label.getIcon() != null)
				{
					result.append(" | ");
				}
				if (label.getIcon() != null)
				{
					result.append(label.getIcon());
				}
				return result;
			}
		}

		valueAt = fixture.valueAt(TableCell.row(row).column(column));
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
			return String.valueOf(((JComboBox<?>) currentComponent).getSelectedItem());
		}
		else if (currentComponent instanceof JList)
		{
			return String.valueOf(((JList<?>) currentComponent).getSelectedValue());
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
			Icon icon = ((JLabel) currentComponent).getIcon();
			if (icon != null)
			{
				return String.valueOf(icon);
			}
			String text = ((JLabel) currentComponent).getText();
			if (text != null)
			{
				return text;
			}
			return "";
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
		else if(currentComponent instanceof JSpinner)
		{
			return String.valueOf(((JSpinner) currentComponent).getValue());
		}

		throw new OperationNotAllowedException(String.format("Component %s don't have value", currentComponent));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Component> T getComp(Class<T> type, ComponentFixture<?> window, Locator locator) throws RemoteException
	{
		Component component = null;
		if (window != null)
		{
			component = this.currentRobot.finder().find((Container) window.target, new MatcherSwing<T>(this.info, type, window.target, locator.getControlKind(), locator));
		}
		else
		{
			component = this.currentRobot.finder().find(new MatcherSwing<Component>(this.info, Component.class, currentRoot(), locator.getControlKind(), locator));
		}

		return (T) component;
	}

	private List<String> getIndexes(JTableFixture fixture, Map<String, Integer> fieldIndexes, ICondition valueCondition, ICondition colorCondition) throws RemoteException, IllegalAccessException
	{
		JTable table = fixture.targetCastedTo(JTable.class);
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++)
		{
			boolean found = true;

			if (valueCondition != null)
			{
                Map<String, Object> values = new HashMap<>();
                for (int j = 0; j < table.getColumnCount(); j++)
                {
                    String name = table.getColumnName(j);
                    Object value = getValueTableCell(fixture, i, j);
                    values.put(name, value);
                }
                if (!valueCondition.isMatched(values))
                {
                    found = false;
                }
			}

			if (found && colorCondition != null)
			{
                Map<String, Object> colors = new HashMap<>();
                for (int j = 0; j < table.getColumnCount(); j++)
                {
                    String name = table.getColumnName(j);
                    Color color = fixture.foregroundAt(TableCell.row(i).column(j)).target();
                    colors.put(name, color);
                }
				if (!colorCondition.isMatched(colors))
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
		switch (key)
		{
			case ESCAPE : return KeyEvent.VK_ESCAPE;
			case F1 : return KeyEvent.VK_F1;
			case F2 : return KeyEvent.VK_F2;
			case F3 : return KeyEvent.VK_F3;
			case F4 : return KeyEvent.VK_F4;
			case F5 : return KeyEvent.VK_F5;
			case F6 : return KeyEvent.VK_F6;
			case F7 : return KeyEvent.VK_F7;
			case F8 : return KeyEvent.VK_F8;
			case F9 : return KeyEvent.VK_F9;
			case F10 : return KeyEvent.VK_F10;
			case F11 : return KeyEvent.VK_F11;
			case F12 : return KeyEvent.VK_F12;

			case DIG1 : return KeyEvent.VK_1;
			case DIG2 : return KeyEvent.VK_2;
			case DIG3 : return KeyEvent.VK_3;
			case DIG4 : return KeyEvent.VK_4;
			case DIG5 : return KeyEvent.VK_5;
			case DIG6 : return KeyEvent.VK_6;
			case DIG7 : return KeyEvent.VK_7;
			case DIG8 : return KeyEvent.VK_8;
			case DIG9 : return KeyEvent.VK_9;
			case DIG0 : return KeyEvent.VK_0;
			case BACK_SPACE : return KeyEvent.VK_BACK_SPACE;
			case INSERT : return KeyEvent.VK_INSERT;
			case HOME : return KeyEvent.VK_HOME;
			case PAGE_UP : return KeyEvent.VK_PAGE_UP;

			case TAB : return KeyEvent.VK_TAB;
			case Q : return KeyEvent.VK_Q;
			case W : return KeyEvent.VK_W;
			case E : return KeyEvent.VK_E;
			case R : return KeyEvent.VK_R;
			case T : return KeyEvent.VK_T;
			case Y : return KeyEvent.VK_Y;
			case U : return KeyEvent.VK_U;
			case I : return KeyEvent.VK_I;
			case O : return KeyEvent.VK_O;
			case P : return KeyEvent.VK_P;
			case SLASH : return KeyEvent.VK_SLASH;
			case BACK_SLASH : return KeyEvent.VK_BACK_SLASH;
			case DELETE : return KeyEvent.VK_DELETE;
			case END : return KeyEvent.VK_END;
			case PAGE_DOWN : return KeyEvent.VK_PAGE_DOWN;

			case CAPS_LOCK : return KeyEvent.VK_CAPS_LOCK;
			case A : return KeyEvent.VK_A;
			case S : return KeyEvent.VK_S;
			case D : return KeyEvent.VK_D;
			case F : return KeyEvent.VK_F;
			case G : return KeyEvent.VK_G;
			case H : return KeyEvent.VK_H;
			case J : return KeyEvent.VK_J;
			case K : return KeyEvent.VK_K;
			case L : return KeyEvent.VK_L;
			case SEMICOLON : return KeyEvent.VK_SEMICOLON;
			case QUOTE : return KeyEvent.VK_QUOTE;
			case DOUBLE_QUOTE : return KeyEvent.VK_QUOTEDBL;
			case ENTER : return KeyEvent.VK_ENTER;

			case SHIFT : return KeyEvent.VK_SHIFT;
			case Z : return KeyEvent.VK_Z;
			case X : return KeyEvent.VK_X;
			case C : return KeyEvent.VK_C;
			case V : return KeyEvent.VK_V;
			case B : return KeyEvent.VK_B;
			case N : return KeyEvent.VK_N;
			case M : return KeyEvent.VK_M;
			case DOT : return KeyEvent.VK_PERIOD;
			case UP : return KeyEvent.VK_UP;

			case CONTROL : return KeyEvent.VK_CONTROL;
			case ALT : return KeyEvent.VK_ALT;
			case SPACE : return KeyEvent.VK_SPACE;
			case LEFT : return KeyEvent.VK_LEFT;
			case DOWN : return KeyEvent.VK_DOWN;

			case RIGHT : return KeyEvent.VK_RIGHT;

			case PLUS : return KeyEvent.VK_PLUS;
			case MINUS : return KeyEvent.VK_MINUS;

			case UNDERSCORE : return KeyEvent.VK_UNDERSCORE;

			case NUM_LOCK: return KeyEvent.VK_NUM_LOCK;
			case NUM_DIVIDE : return KeyEvent.VK_DIVIDE;
			case NUM_SEPARATOR : return KeyEvent.VK_SEPARATOR;
			case NUM_MULTIPLY : return KeyEvent.VK_MULTIPLY;
			case NUM_MINUS : return KeyEvent.VK_SUBTRACT;
			case NUM_DIG7 : return KeyEvent.VK_NUMPAD7;
			case NUM_DIG8 : return KeyEvent.VK_NUMPAD8;
			case NUM_DIG9 : return KeyEvent.VK_NUMPAD9;
			case NUM_PLUS : return KeyEvent.VK_ADD;
			case NUM_DIG4 : return KeyEvent.VK_NUMPAD4;
			case NUM_DIG5 : return KeyEvent.VK_NUMPAD5;
			case NUM_DIG6 : return KeyEvent.VK_NUMPAD6;
			case NUM_DIG1 : return KeyEvent.VK_NUMPAD1;
			case NUM_DIG2 : return KeyEvent.VK_NUMPAD2;
			case NUM_DIG3 : return KeyEvent.VK_NUMPAD3;
			case NUM_DIG0 : return KeyEvent.VK_NUMPAD0;
			case NUM_DOT : return KeyEvent.VK_DECIMAL;
			case NUM_ENTER : return KeyEvent.VK_ENTER;
			default: return 0;
		}
	}

	private int getModifierKeysArePressed()
	{
		int modifiers = 0;
		if (this.isAltDown)
		{
			modifiers |= InputEvent.ALT_DOWN_MASK;
		}
		if (this.isControlDown)
		{
			modifiers |= InputEvent.CTRL_DOWN_MASK;
		}
		if (this.isShiftDown)
		{
			modifiers |= InputEvent.SHIFT_DOWN_MASK;
		}
		this.logger.debug("Press modifiers : " + InputEvent.getModifiersExText(modifiers));
		return modifiers;
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
				@SuppressWarnings("unchecked")
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

	List<String> convertJTreeToPathsList(JTree tree)
	{
		int i = 0;
		List<String> result = new ArrayList<>();
		StringBuilder pathBuilder = new StringBuilder();
		while (tree.getPathForRow(i) != null) {
			if (tree.getPathForRow(i).getParentPath() != null) {
				pathBuilder
						.append(tree.getPathForRow(i).getParentPath().toString())
						.append('[')
						.append(tree.getPathForRow(i).getLastPathComponent().toString())
						.append(']');
			} else {
				pathBuilder
						.append('[')
						.append(tree.getPathForRow(i).getLastPathComponent().toString())
						.append(']');
			}
			result.add(pathBuilder.toString());
			pathBuilder.delete(0, pathBuilder.length());
			i++;
		}
		return result;
	}

	private int findIndex(JTree tree, String path)
	{
		List<String> rows = convertJTreeToPathsList(tree);
		for (int i = 0; i < rows.size(); i++) {
			if(path.equals(rows.get(i)))
			{
				return i;
			}
		}
		return -1;
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

	private class JTreeItemFixture extends ComponentFixture<Component>
	{
		JTreeItemFixture(Robot robot, JTreeItem target)
		{
			super(robot, target);
		}
	}

	private class JTreeItem extends JComponent
	{
		JTree tree;
		TreePath path;

		JTreeItem(JTree tree, TreePath path)
		{
			this.tree = tree;
			this.path = path;
		}

		JTree getTree()
		{
			return tree;
		}

		TreePath getPath()
		{
			return path;
		}

		@Override
		public boolean isVisible()
		{
			return this.tree.isVisible(this.path);
		}
	}

	private Document convertTreeToXMLDoc(JTree tree) throws ParserConfigurationException, XPathExpressionException
	{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		TreeNode treeNode = (TreeNode) tree.getModel().getRoot();
		Node root = doc.getDocumentElement();
		createDom(doc, treeNode, root);
		return doc;
	}

	private void createDom(Document doc, TreeNode treeNode, Node current)
	{
		Element node = doc.createElement("item");
		node.setAttribute("name", treeNode.toString());
		node.setUserData("path", new TreePath(((DefaultMutableTreeNode) treeNode).getPath()), null);
		node.setUserData("node", treeNode, null);

		if(current != null)
		{
			current.appendChild(node);
		}
		else
		{
			doc.appendChild(node);
		}

		Enumeration kiddies = treeNode.children();
		while (kiddies.hasMoreElements())
		{
			createDom(doc, (TreeNode) kiddies.nextElement(), node);
		}
	}

	private NodeList findNodesInTreeByXpath(Document document, String selectedText) throws XPathExpressionException
	{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(selectedText);

		Object result = expr.evaluate(document, XPathConstants.NODESET);
		return (NodeList) result;
	}

	private JMenu findMenu(JMenu menu, List<String> items)
	{
		if(items.isEmpty())
		{
			return menu;
		}

		boolean found = false;
		for(String menuName : items)
		{
			found = false;
			for(Component menuComponent : menu.getMenuComponents())
			{
				if (menuComponent instanceof JMenu && ((JMenu) menuComponent).getText().equals(menuName))
				{
					menu = (JMenu) menuComponent;
					found = true;
					break;
				}
			}
		}
		return found ? menu : null;
	}

	private List<String> getListOfNamesFromListItems(ListModel<?> model)
	{
		ArrayList<String> resultList = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++)
		{
			resultList.add(model.getElementAt(i).toString());
		}
		return resultList;
	}

	private ListModel<?> getListModelFromComponentOrError(Component component)
	{
		switch (component.getClass().getSimpleName())
		{
			case "JComboBox":	return ((JComboBox<?>) component).getModel();
			case "JList":		return ((JList<?>) component).getModel();
			case "JTabbedPane":	return new TabbedPaneModel((JTabbedPane) component);
			default:			throw new Error("Element " + component.getName() + " does not have list model. Please try another element.");
		}
	}

	private boolean isCoordsDidNotIntroduce(int x, int y)
	{
		return (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE);
	}

	private void waitUntilAnotherActionsCompleted()
	{
		this.currentRobot.waitForIdle();
	}

	private ArrayList<AWTEvent> createEventsList(MouseAction action, Component component, int x, int y)
	{
		final int NO_CLICK = 0;
		final int ONE_CLICK = 1;
		final int TWO_CLICK = 2;
		final boolean SHOW_POPUP = true;
		final boolean NOT_SHOW_POPUP = false;

		final ArrayList<AWTEvent> events = new ArrayList<>();
		final int ALT_CTRL_SHIFT = getModifierKeysArePressed();

		events.add(new FocusEvent(component, FocusEvent.FOCUS_GAINED));
		events.add(new MouseEvent(component, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, NO_CLICK, NOT_SHOW_POPUP, MouseEvent.NOBUTTON));
		events.add(new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, NO_CLICK, NOT_SHOW_POPUP, MouseEvent.NOBUTTON));
		switch (action)
		{
			case LeftClick:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				break;

			case LeftDoubleClick:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				break;

			case RightClick:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				break;

			case RightDoubleClick:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, TWO_CLICK, SHOW_POPUP, MouseEvent.BUTTON3));
				break;

			case Press:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				break;

			case Drop:
				events.add(new MouseEvent(component, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, NO_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				events.add(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), ALT_CTRL_SHIFT, x, y, ONE_CLICK, NOT_SHOW_POPUP, MouseEvent.BUTTON1));
				break;

			case DragNDrop:
			case Focus:
			case Enter:
			case Move:
			case Activated:
				break;
		}
		return events;
	}

	private void executeEventsList(final Component component, final ArrayList<AWTEvent> events)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (AWTEvent event : events)
				{
					logger.debug("event : "  + event.toString());
					component.dispatchEvent(event);
				}
			}
		});
	}

	private void executeAction(MouseAction action, Component component, int x, int y)
	{
		waitUntilAnotherActionsCompleted();
		ArrayList<AWTEvent> events = createEventsList(action, component, x, y);
		executeEventsList(component, events);
	}

	private Point getPointLocation(Component component, int x, int y)
	{
		Point point = AWT.locationOnScreenOf(component);
		x += point.getX();
		y += point.getY();
		return new Point(x,y);
	}

	private boolean dragNdropThrowEvents(ComponentFixture<Component> drag, int x1, int y1, ComponentFixture<Component> drop, int x2, int y2)
	{
		Component dragComp = drag.target;
		if (isCoordsDidNotIntroduce(x1,y1))
		{
			Point point = AWT.visibleCenterOf(dragComp);
			x1 = point.x;
			y1 = point.y;
		}

		if(drop == null)
		{
			executeAction(MouseAction.Press, dragComp, x1, y1);
			executeAction(MouseAction.Drop, dragComp, x2, y2);
		}
		else
		{
			Component dropComp = drop.target;

			Point pointOne = getPointLocation(dragComp, x1, y1);
			Point pointTwo = getPointLocation(dropComp, x2, y2);
			int x3 = x1 + (pointTwo.x - pointOne.x);
			int y3 = y1 + (pointTwo.y - pointOne.y);

			executeAction(MouseAction.Press, dragComp, x1, y1);
			executeAction(MouseAction.Drop, dragComp, x3, y3);
		}
		return true;
	}

	private boolean dragNdropThrowRobot(ComponentFixture<Component> drag, int x1, int y1, ComponentFixture<Component> drop, int x2, int y2) throws InterruptedException
	{
		Component dragComp = drag.target;
		if (isCoordsDidNotIntroduce(x1,y1))
		{
			Point point = AWT.visibleCenterOf(dragComp);
			x1 = point.x;
			y1 = point.y;
		}

		if(drop == null)
		{
			this.currentRobot.pressMouse(dragComp, new Point(x1, y1), MouseButton.LEFT_BUTTON);
			Thread.sleep(100);
			this.currentRobot.moveMouse(dragComp, new Point(x2, y2));
			Thread.sleep(100);
			this.currentRobot.releaseMouse(MouseButton.LEFT_BUTTON);
			Thread.sleep(100);
		}
		else
		{
			Component dropComp = drop.target;
			Point pointOne = getPointLocation(dragComp, x1, y1);
			Point pointTwo = getPointLocation(dropComp, x2, y2);
			int x3 = x1 + (pointTwo.x - pointOne.x);
			int y3 = y1 + (pointTwo.y - pointOne.y);

			this.currentRobot.pressMouse(dragComp, new Point(x1, y1), MouseButton.LEFT_BUTTON);
			Thread.sleep(100);
			this.currentRobot.moveMouse(dragComp, new Point(x3, y3));
			Thread.sleep(100);
			this.currentRobot.releaseMouse(MouseButton.LEFT_BUTTON);
			Thread.sleep(100);
		}

		return true;
	}
	//endregion

	void clearModifiers()
	{
		this.isAltDown = false;
		this.isShiftDown = false;
		this.isControlDown = false;
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
		}
		else if (component instanceof JFrame)
		{
			FrameFixture frameFixture = new FrameFixture(this.currentRobot, (Frame) component);
		}
		else if (component instanceof JLabel)
		{
			return (ComponentFixture<T>) new JLabelFixture(this.currentRobot, (JLabel) component);
		}
		else if (component instanceof JList)
		{
			return (ComponentFixture<T>) new JListFixture(this.currentRobot, ((JList<?>) component));
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
		else if (component instanceof JTreeItem)
		{
			return (ComponentFixture<T>) new JTreeItemFixture(this.currentRobot, (JTreeItem) component);
		}
		else if (component instanceof JSplitPane)
		{
			return (ComponentFixture<T>) new JSplitPaneFixture(this.currentRobot, ((JSplitPane) component));
		}
		return new ComponentFixture<T>(this.currentRobot, component)
		{
		};
	}

	//http://www.aivosto.com/vbtips/control-characters.html#list_C0
	private int getKeyCodeWithControl(Keyboard key){
		switch (key)
		{
			case A: return 1;
			case B: return 2;
			case C: return 3;
			case D: return 4;
			case E: return 5;
			case F: return 6;
			case G: return 7;
			case H: return 8;
			case I: return 9;
			case J: return 10;
			case K: return 11;
			case L: return 12;
			case M: return 13;
			case N: return 14;
			case O: return 15;
			case P: return 16;
			case Q: return 17;
			case R: return 18;
			case S: return 19;
			case T: return 20;
			case U: return 21;
			case V: return 22;
			case W: return 23;
			case X: return 24;
			case Y: return 25;
			case Z: return 26;
			case UNDERSCORE: return 31;
			case SPACE: return 32;
			case DELETE: return 127;
			default:
				return getKeyCode(key);
		}
	}

}
