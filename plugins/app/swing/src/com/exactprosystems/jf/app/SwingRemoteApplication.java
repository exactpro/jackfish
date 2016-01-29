////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.SerializablePair;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.apache.log4j.*;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.ComponentMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ComponentFixture;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class SwingRemoteApplication extends RemoteApplication
{
	private Logger logger = null;
	private Robot currentRobot;
	private HighLighter highLighter = null;

	public SwingOperationExecutor operationExecutor;

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		try
		{
			logger = Logger.getLogger(SwingRemoteApplication.class);

			Layout layout = new PatternLayout(serverLogPattern);
			Appender appender = new FileAppender(layout, logName);
			logger.addAppender(appender);
			logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));

			MatcherSwing.setLogger(logger);
		}
		catch (Exception e)
		{
			logger.error(String.format("createLoggerDerived($s, $s,$s)", logName, serverLogLevel, serverLogPattern));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void connectDerived(Map<String, String> args) throws Exception
	{
		String url = args.get(SwingAppFactory.urlName);

		logger.debug("Connecting to web start application: url=" + url);

		try
		{
			// java -jar netx.jar -verbose -nosecurity  -Xtrustall -Xnofork URL

			JNLPRuntime.setTrustAll(true);
			JNLPRuntime.setAllowRedirect(true);
			JNLPRuntime.setDebug(true);
			JNLPRuntime.setForksAllowed(false);
			JNLPRuntime.setSecurityEnabled(false);
			JNLPRuntime.setVerify(true);
			JNLPRuntime.initialize(true);

			logger.debug("Runtime init has done.");

			Launcher launcher = new Launcher(false);

			try
			{
				ApplicationInstance app = launcher.launch(new URL(url));
				logger.debug("connected to " + app);
			}
			catch (Throwable t)
			{
				logger.error(t.getMessage(), t);
			}

			this.currentRobot = BasicRobot.robotWithCurrentAwtHierarchy();
			this.operationExecutor = new SwingOperationExecutor(this.currentRobot, this.logger);

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					highLighter = new HighLighter();
				}
			});
		}
		catch (Exception e)
		{
			logger.error("connectDerived. keys : " + args.keySet() + " , value : " + args.values());
			logger.error(e.getMessage(), e);
			throw e;
		}

		logger.debug("Application has been connected");
	}

	@Override
	protected void runDerived(Map<String, String> args) throws Exception
	{
		String mainClass = args.get(SwingAppFactory.mainClassName);
		String jar = args.get(SwingAppFactory.jarName);
		String arg = args.get(SwingAppFactory.argsName);

		logger.debug("Launching application: class=" + mainClass + " jar=" + jar + " arg=" + arg);

		try
		{
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jar));

			ClassLoader parent = getClass().getClassLoader();
			@SuppressWarnings("resource") URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[]{}), parent);

			Class<?> applicationType = classLoader.loadClass(mainClass);
			Method mainMethod = applicationType.getMethod("main", String[].class);
			mainMethod.invoke(null, new Object[]{arg == null ? null : new String[]{arg}});

			this.currentRobot = BasicRobot.robotWithCurrentAwtHierarchy();
			this.operationExecutor = new SwingOperationExecutor(this.currentRobot, this.logger);
			this.highLighter = new HighLighter();
		}
		catch (Exception e)
		{
			logger.error("connectDerived. keys : " + args.keySet() + " , value : " + args.values());
			logger.error(e.getMessage(), e);
			throw e;
		}

		logger.debug("Application has been launched");

	}

	@Override
	protected void stopDerived() throws Exception
	{
		try
		{
			if (this.highLighter != null)
			{
				this.highLighter.close();
			}
		}
		catch (Exception e)
		{
			logger.error("stopDerived()");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		// done
	}

	@Override
	protected SerializablePair<String, Boolean> getAlertTextDerived() throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		try
		{
			Collection<String> list = new ArrayList<String>();
			Collection<Component> allDialogs = this.currentRobot.finder().findAll(new ComponentMatcher()
			{
				@Override
				public boolean matches(Component component)
				{
					if (component != null && component.isShowing() && (component instanceof JFrame || component instanceof JDialog))
					{
						return true;
					}
					return false;
				}
			});
			for (Component dialog : allDialogs)
			{
				if (dialog instanceof JFrame)
				{
					list.add(((Frame) dialog).getTitle());
				}
				if (dialog instanceof JDialog)
				{
					list.add(((Dialog) dialog).getTitle());
				}
			}

			return list;
		}
		catch (Exception e)
		{
			logger.error("titlesDerived()");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void resizeDerived(int height, int width, boolean maximize, boolean minimize) throws Exception
	{
		try
		{
			if (maximize)
			{
				Component frame = this.operationExecutor.currentFrame();
				if (frame instanceof JFrame)
				{
					((JFrame) frame).setExtendedState(JFrame.MAXIMIZED_BOTH);
				}
			}
			else if (minimize)
			{
				Component frame = this.operationExecutor.currentFrame();
				if (frame instanceof JFrame)
				{
					((JFrame) frame).setExtendedState(JFrame.ICONIFIED);
				}
			}
			else
			{
				Component frame = this.operationExecutor.currentFrame();
				if (frame instanceof JFrame)
				{
					((JFrame) frame).setSize(width, height);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(String.format("resizeDerived(%s,%s, %s, %s)", height, width, maximize, minimize));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected String switchToDerived(final String title) throws Exception
	{
		try
		{
			Component frame = this.currentRobot.finder().find(new ComponentMatcher()
			{
				@Override
				public boolean matches(Component c)
				{
					return c != null && (c instanceof JFrame && ((JFrame) c).getTitle().equals(title)) || (c instanceof JDialog && ((JDialog) c).getTitle().equals(title));
				}
			});
			this.operationExecutor.setCurrentFrame(frame);
			return title; // done
		}
		catch (Exception e)
		{
			logger.error(String.format("swichToDerived(%s)", title));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected Collection<String> findAllDerived(Locator owner, Locator element) throws Exception
	{
		try
		{
			List<String> res = new ArrayList<String>();
			ComponentFixture<Component> ownerFixture = null;
			if (owner != null)
			{
				ownerFixture = this.operationExecutor.find(null, owner);
			}
			else
			{
				ownerFixture = new AnyComponentlFixture(currentRobot, this.operationExecutor.currentFrame());
			}

			List<ComponentFixture<Component>> components = this.operationExecutor.findAll(element.getControlKind(), ownerFixture, element);
			for (ComponentFixture<Component> component : components)
			{
				res.add(component.toString());
			}
			return res;
		}
		catch (Exception e)
		{
			logger.error(String.format("findAllDerived (%s,%s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected Locator getLocatorDerived(Locator owner, ControlKind controlKind, int x, int y) throws Exception
	{
		try
		{
			Component main = null;
			if (owner != null)
			{
				main = this.operationExecutor.find(null, owner).target;
			}
			else
			{
				main = this.operationExecutor.currentFrame();
			}

			if (main == null)
			{
				throw new Exception("Can't find the main window.");
			}
			Point mainCoords = main.getLocationOnScreen();
			Component component = componentAtPosition(main, x - mainCoords.x, y - mainCoords.y);
			component = parentForKind(component, controlKind);

			if (component == null)
			{
				return null;
			}

			// we have a component and should highlight it
			this.highLighter.start(component.getLocationOnScreen(), component.getSize());

			ControlKind newControlKind = determitateControlKind(component);

			String id = component.getName();
			id = id == null ? newControlKind.name() : id;

			Locator locator = new Locator(null, id, newControlKind);
			locator.clazz(MatcherSwing.getClass(component)).name(MatcherSwing.getName(component)).title(MatcherSwing.getTitle(component)).action(MatcherSwing.getAction(component)).text(MatcherSwing.getText(component)).tooltip(MatcherSwing.getToolTip(component));

			return locator;
		}
		catch (Exception e)
		{
			logger.error(String.format("getLocatorDerived(%s, %s, %d, %d)", owner, controlKind, x, y));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected ImageWrapper getImageDerived(final Locator owner, final Locator element) throws Exception
	{
		try
		{
			final BufferedImage[] images = new BufferedImage[1];
			final Exception[] exceptions = new Exception[1];
			ComponentFixture<Component> component = null;
			if (element != null)
			{
				component = operationExecutor.find(owner, element);
			}
			final ComponentFixture<Component> finalComponent = component;
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Component target;
						if (finalComponent == null)
						{
							target = operationExecutor.currentFrame();
						}
						else
						{
							target = finalComponent.target;
						}
						BufferedImage image = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_RGB);
						target.paint(image.getGraphics()); // alternately use .printAll(..)
						images[0] = image;
					}
					catch (Exception e)
					{
						exceptions[0] = e;
					}
				}
			});
			if (exceptions[0] != null)
			{
				throw exceptions[0];
			}
			return new ImageWrapper(images[0]);
		}
		catch (Exception e)
		{
			logger.error(String.format("getImageDerived(%s,%s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	@Override
	protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
	{
		try
		{
			ComponentFixture<Component> component = this.operationExecutor.find(owner, element);
			return this.operationExecutor.getRectangle(component);
		}
		catch (Exception e)
		{
			logger.error(String.format("getRectangleDerived(%s, %s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception
	{
		try
		{
			return operation.operate(this.operationExecutor, owner, element, rows, header);
		}
		catch (Exception e)
		{
			logger.error(String.format("operateDerived(%s,%s,%s,%s,%s)", owner, element, rows, header, operation));
			logger.error("EXCEPTION : " + e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
	{
		try
		{
			return spec.perform(this.operationExecutor, owner, element);
		}
		catch (Exception e)
		{
			logger.error(String.format("checkLayoutDerived(%s,%s,%s)", owner, element, spec));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void newInstanceDerived(Map<String, String> args) throws Exception
	{
		// done
	}

	@Override
	protected int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception
	{
		try
		{
			List<ComponentFixture<Component>> dialogs = this.operationExecutor.findAll(ControlKind.Any, null, element);

			for (ComponentFixture<Component> dialog : dialogs)
			{
				for (LocatorAndOperation pair : operations)
				{
					Locator locator = pair.getLocator();

					List<ComponentFixture<Component>> components = this.operationExecutor.findAll(locator.getControlKind(), dialog, locator);
					if (components.size() == 1)
					{
						ComponentFixture<Component> component = components.get(0);
						Operation operation = pair.getOperation();
						operation.operate(this.operationExecutor, locator, component);

					}
				}
			}

			return dialogs.size();
		}
		catch (Exception e)
		{
			logger.error(String.format("closeAllDerived(%s,%s)", element, operations));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		return null; // done
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		try
		{
			Component component = null;
			if (owner == null)
			{
				component = this.operationExecutor.currentFrame();
			}
			else
			{
				component = this.operationExecutor.find(null, owner).target;
			}

			return MatcherSwing.createDocument(component, false, true);
		}
		catch (Exception e)
		{
			logger.error(String.format("getTreeDerived(%s)", owner));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
		// done
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
		// done
	}

	@Override
	protected void subscribeDerived(HistogramTransfer histogram) throws Exception
	{

	}

	private Component componentAtPosition(Component component, int x, int y)
	{
		if (component == null)
		{
			return null;
		}

		if (component instanceof Container)
		{
			Container container = (Container) component;
			for (Component comp : container.getComponents())
			{
				if (!comp.isVisible())
				{
					continue;
				}
				if (comp.getBounds().contains(x, y))
				{
					return componentAtPosition(comp, x - comp.getX(), y - comp.getY());
				}
			}
		}

		return component;
	}

	private Component parentForKind(Component component, ControlKind controlKind) throws Exception
	{
		if (component == null || controlKind == null)
		{
			return null;
		}
		Class<? extends Component> found = classToControlKind.get(controlKind);
		if (found == null)
		{
			throw new Exception("Unknown ControlKind: " + controlKind);
		}

		if (found.isAssignableFrom(component.getClass()))
		{
			return component;
		}
		return parentForKind(component.getParent(), controlKind);
	}

	private ControlKind determitateControlKind(Component component)
	{
		for (Entry<ControlKind, Class<? extends Component>> entry : classToControlKind.entrySet())
		{
			if (entry.getValue().isAssignableFrom(component.getClass()))
			{
				return entry.getKey();
			}
		}
		return ControlKind.Any;
	}

	private static Map<ControlKind, Class<? extends Component>> classToControlKind = new LinkedHashMap<>();

	static
	{
		// order is important. see function determitateControlKind
		classToControlKind.put(ControlKind.Button, JButton.class);
		classToControlKind.put(ControlKind.CheckBox, JCheckBox.class);
		classToControlKind.put(ControlKind.ComboBox, JComboBox.class);
		classToControlKind.put(ControlKind.Dialog, JDialog.class);
		classToControlKind.put(ControlKind.Frame, JFrame.class);
		classToControlKind.put(ControlKind.Label, JLabel.class);
		classToControlKind.put(ControlKind.ListView, JList.class);
		classToControlKind.put(ControlKind.Menu, JMenu.class);
		classToControlKind.put(ControlKind.MenuItem, JMenuItem.class);
		classToControlKind.put(ControlKind.Panel, JPanel.class);
		classToControlKind.put(ControlKind.ProgressBar, JProgressBar.class);
		classToControlKind.put(ControlKind.RadioButton, JRadioButton.class);
		classToControlKind.put(ControlKind.ScrollBar, JScrollBar.class);
		classToControlKind.put(ControlKind.Slider, JSlider.class);
		classToControlKind.put(ControlKind.Splitter, JSplitPane.class);
		classToControlKind.put(ControlKind.Table, JTable.class);
		classToControlKind.put(ControlKind.TabPanel, JTabbedPane.class);
		classToControlKind.put(ControlKind.TextBox, JTextField.class);
		classToControlKind.put(ControlKind.ToggleButton, JToggleButton.class);
		classToControlKind.put(ControlKind.Tooltip, JToolTip.class);
		classToControlKind.put(ControlKind.Tree, JTree.class);

		classToControlKind.put(ControlKind.Any, Component.class);
		classToControlKind.put(ControlKind.Wait, Component.class);
		classToControlKind.put(ControlKind.Image, Component.class);
		classToControlKind.put(ControlKind.TreeItem, Component.class);
		classToControlKind.put(ControlKind.Row, Component.class);
	}
}
