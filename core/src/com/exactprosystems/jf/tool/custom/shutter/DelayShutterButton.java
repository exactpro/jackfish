package com.exactprosystems.jf.tool.custom.shutter;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

@Deprecated
public class DelayShutterButton extends BorderPane
{
	private long maxDelay;
	private long begin;

	private ToggleButton button;
	private ProgressIndicator progress;

	private Timer timer;

	private int period = 20;
	private int delay = 20;

	private EventHandler<ActionEvent>	onSwitchonAction;
	private EventHandler<ActionEvent>	onSwitchoffAction;
	private ClickHandler 				onCompleteAction;
	private boolean timerIsCanceled = false;

	public DelayShutterButton(String tooltip, String icon, long maxDelay)
	{
		super();
		this.maxDelay = maxDelay;
		this.button = new ToggleButton("", new ImageView(new Image(icon)));
		this.button.setTooltip(new Tooltip(tooltip));
		this.button.getStyleClass().add(CssVariables.DELAY_SHUTTER);
		this.button.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
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

		this.progress = new ProgressBar();
		this.progress.getStyleClass().add(CssVariables.DELAY_SHUTTER);
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
			this.progress.setProgress(progress);
			if (progress <= 0)
			{
				this.progress.setVisible(false);
				this.progress.toBack();
				setCenter(button);
			}
			else
			{
				this.progress.setPrefWidth(this.button.getWidth());
				this.progress.setMinWidth(this.button.getWidth());
				this.progress.setMaxWidth(this.button.getWidth());
				this.progress.setPrefHeight(this.button.getHeight());
				this.progress.setMinHeight(this.button.getHeight());
				this.progress.setMaxHeight(this.button.getHeight());
				this.progress.setVisible(true);
				this.progress.toFront();
				setCenter(this.progress);
			}
		});
	}
}
