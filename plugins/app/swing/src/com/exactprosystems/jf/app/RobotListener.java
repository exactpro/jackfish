////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.HistogramMetric;
import com.exactprosystems.jf.api.app.MetricsCounter;
import org.fest.swing.core.*;
import org.fest.swing.core.Robot;
import org.fest.swing.hierarchy.ComponentHierarchy;

import javax.swing.*;
import java.awt.*;

public class RobotListener implements Robot
{
	private Robot robot;
	private MetricsCounter metricsCounter;

	public RobotListener(Robot robot, MetricsCounter metricsCounter)
	{
		this.robot = robot;
		this.metricsCounter = metricsCounter;
	}

	@Override
	public ComponentHierarchy hierarchy()
	{
		return this.robot.hierarchy();
	}

	@Override
	public ComponentFinder finder()
	{
		return this.robot.finder();
	}

	@Override
	public ComponentPrinter printer()
	{
		return this.robot.printer();
	}

	@Override
	public void showWindow(Window w)
	{
		this.robot.showWindow(w);
	}

	@Override
	public void showWindow(Window w, Dimension size)
	{
		this.robot.showWindow(w, size);
	}

	@Override
	public void showWindow(Window w, Dimension size, boolean pack)
	{
		this.robot.showWindow(w, size, pack);
	}

	@Override
	public void close(Window w)
	{
		this.robot.close(w);
	}

	@Override
	public void focus(Component c)
	{
		this.robot.focus(c);
	}

	@Override
	public void focusAndWaitForFocusGain(Component c)
	{
		this.robot.focusAndWaitForFocusGain(c);
	}

	@Override
	public void cleanUp()
	{
		this.robot.cleanUp();
	}

	@Override
	public void cleanUpWithoutDisposingWindows()
	{
		this.robot.cleanUpWithoutDisposingWindows();
	}

	@Override
	public void click(Component c)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(c);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void rightClick(Component c)
	{
		this.robot.rightClick(c);
	}

	@Override
	public void click(Component c, MouseButton button)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(c, button);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void doubleClick(Component c)
	{
		this.robot.doubleClick(c);
	}

	@Override
	public void click(Component c, MouseButton button, int times)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(c, button, times);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void click(Component c, Point where)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(c, where);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void click(Component c, Point where, MouseButton button, int times)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(c, where, button, times);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void click(Point where, MouseButton button, int times)
	{
		this.metricsCounter.before(HistogramMetric.Click);
		this.robot.click(where, button, times);
		this.metricsCounter.after(HistogramMetric.Click);
	}

	@Override
	public void pressMouse(MouseButton button)
	{
		this.robot.pressMouse(button);
	}

	@Override
	public void pressMouse(Component c, Point where)
	{
		this.robot.pressMouse(c, where);
	}

	@Override
	public void pressMouse(Component c, Point where, MouseButton button)
	{
		this.robot.pressMouse(c, where, button);
	}

	@Override
	public void pressMouse(Point where, MouseButton button)
	{
		this.robot.pressMouse(where, button);
	}

	@Override
	public void moveMouse(Component c)
	{
		this.robot.moveMouse(c);
	}

	@Override
	public void moveMouse(Component c, Point p)
	{
		this.robot.moveMouse(c, p);
	}

	@Override
	public void moveMouse(Component c, int x, int y)
	{
		this.robot.moveMouse(c, x, y);
	}

	@Override
	public void moveMouse(Point p)
	{
		this.robot.moveMouse(p);
	}

	@Override
	public void moveMouse(int x, int y)
	{
		this.robot.moveMouse(x, y);
	}

	@Override
	public void releaseMouse(MouseButton button)
	{
		this.robot.releaseMouse(button);
	}

	@Override
	public void releaseMouseButtons()
	{
		this.robot.releaseMouseButtons();
	}

	@Override
	public void rotateMouseWheel(Component c, int amount)
	{
		this.robot.rotateMouseWheel(c, amount);
	}

	@Override
	public void rotateMouseWheel(int amount)
	{
		this.robot.rotateMouseWheel(amount);
	}

	@Override
	public void jitter(Component c)
	{
		this.robot.jitter(c);
	}

	@Override
	public void jitter(Component c, Point where)
	{
		this.robot.jitter(c, where);
	}

	@Override
	public void enterText(String text)
	{
		this.robot.enterText(text);
	}

	@Override
	public void type(char character)
	{
		this.robot.type(character);
	}

	@Override
	public void pressAndReleaseKey(int keyCode, int... modifiers)
	{
		this.robot.pressAndReleaseKey(keyCode, modifiers);
	}

	@Override
	public void pressAndReleaseKeys(int... keyCodes)
	{
		this.robot.pressAndReleaseKeys(keyCodes);
	}

	@Override
	public void pressKey(int keyCode)
	{
		this.robot.pressKey(keyCode);
	}

	@Override
	public void releaseKey(int keyCode)
	{
		this.robot.releaseKey(keyCode);
	}

	@Override
	public void pressModifiers(int modifierMask)
	{
		this.robot.pressModifiers(modifierMask);
	}

	@Override
	public void releaseModifiers(int modifierMask)
	{
		this.robot.releaseModifiers(modifierMask);
	}

	@Override
	public void waitForIdle()
	{
		this.robot.waitForIdle();
	}

	@Override
	public boolean isDragging()
	{
		return this.robot.isDragging();
	}

	@Override
	public boolean isReadyForInput(Component c)
	{
		return this.robot.isReadyForInput(c);
	}

	@Override
	public JPopupMenu showPopupMenu(Component invoker)
	{
		return this.robot.showPopupMenu(invoker);
	}

	@Override
	public JPopupMenu showPopupMenu(Component invoker, Point location)
	{
		return this.robot.showPopupMenu(invoker, location);
	}

	@Override
	public JPopupMenu findActivePopupMenu()
	{
		return this.robot.findActivePopupMenu();
	}

	@Override
	public void requireNoJOptionPaneIsShowing()
	{
		this.robot.requireNoJOptionPaneIsShowing();
	}

	@Override
	public Settings settings()
	{
		return this.robot.settings();
	}

	@Override
	public boolean isActive()
	{
		return this.robot.isActive();
	}
}
