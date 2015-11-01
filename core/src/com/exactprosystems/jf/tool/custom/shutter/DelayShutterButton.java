package com.exactprosystems.jf.tool.custom.shutter;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class DelayShutterButton extends BorderPane
{
	private long maxDelay;
	private long begin;

	private ToggleButton button;
	private ProgressBar progressBar;

	private Timer timer;

	private int period = 20;
	private int delay = 20;

	private EventHandler<ActionEvent>	onSwitchonAction;
	private EventHandler<ActionEvent>	onSwitchoffAction;
	private ClickHandler 				onCompleteAction;
	private boolean timerIsCanceled = false;

	public DelayShutterButton(String name, double width, long maxDelay)
	{
		super();
		this.maxDelay = maxDelay;

		
		this.button = new ToggleButton(name);
		this.button.getStyleClass().add(CssVariables.DELAY_SHUTTER);
		this.button.setPrefWidth(width);
		this.button.setMinWidth(width);
		this.button.setMaxWidth(width);

		this.button.setOnAction(a -> {
			if (this.button.isSelected())
			{
				if (this.onSwitchonAction != null)
				{
					this.onSwitchonAction.handle(a);
				}
			}
			else
			{
				if (this.onSwitchoffAction != null)
				{
					this.onSwitchoffAction.handle(a);
				}
			}
		});

		this.progressBar = new ProgressBar();
		this.progressBar.getStyleClass().add(CssVariables.DELAY_SHUTTER);
		this.progressBar.setPrefWidth(this.button.getPrefWidth());
		this.progressBar.setMinWidth(this.button.getMinWidth());
		this.progressBar.setMaxWidth(this.button.getMaxWidth());
		this.progressBar.setPrefHeight(this.button.getPrefHeight());
		this.progressBar.setMinHeight(this.button.getMinHeight());
		this.progressBar.setMaxHeight(this.button.getMaxHeight());

		setCenter(this.button);
	}
	
	public void setToggleGroup(ToggleGroup group)
	{
		this.button.setToggleGroup(group);
	}
	
	public void setSwithconAction(EventHandler<ActionEvent> handler)
	{
		this.onSwitchonAction = handler;
	}

	public void setSwithcoffAction(EventHandler<ActionEvent> handler)
	{
		this.onSwitchoffAction = handler;
	}

	public void setCompleteAction(ClickHandler handler)
	{
		this.onCompleteAction = handler;
	}
	
	public boolean isSelected()
	{
		return this.button.isSelected();
	}
	
	public void start()
	{
		if (!this.button.isSelected())
		{
			return;
		}
		timerIsCanceled = false;
		this.begin = System.currentTimeMillis();
		this.timer = new Timer();
		this.timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				long span = Math.min(System.currentTimeMillis() - begin, maxDelay);
				updateProgressBar(span);
				if (!DelayShutterButton.this.getScene().getWindow().isFocused())
				{
					stop();
				}
			}
		}, delay, period);
	}

	public void stop()
	{
		if (timerIsCanceled)
		{
			return;
		}
		if (this.timer != null)
		{
			this.timer.cancel();
			timerIsCanceled = true;
		}
		if (!this.button.isSelected())
		{
			return;
		}
		
		long span = Math.min(System.currentTimeMillis() - begin, maxDelay);
		this.begin = System.currentTimeMillis();
		Timer backTimer = new Timer();
		backTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (System.currentTimeMillis() - begin > span)
				{
					backTimer.cancel();
					
					if (onCompleteAction != null)
					{
						Point p = MouseInfo.getPointerInfo().getLocation();
						onCompleteAction.click(p.x, p.y);
					}

					updateProgressBar(0);
				}
				else
				{
					updateProgressBar(span - (System.currentTimeMillis() - begin));
				}
			}
		}, delay, period);
	}


	//============================================================
	// private methods
	//============================================================
	private void updateProgressBar(long value)
	{
		double progress = (double)value / (double)maxDelay;
		Platform.runLater(() ->
		{
			progressBar.setProgress(progress);
			if (progress <= 0)
			{
				progressBar.setVisible(false);
				progressBar.toBack();
				setCenter(button);
			}
			else
			{
				progressBar.setVisible(true);
				progressBar.toFront();
				setCenter(progressBar);
			}
		});
	}
}
