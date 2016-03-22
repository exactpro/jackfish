////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Preloader extends Stage
{
	public Preloader()
	{
		BorderPane borderPane = new BorderPane();
		this.getIcons().add(new Image(CssVariables.Icons.LOADING_ICON));
		borderPane.getStyleClass().addAll(CssVariables.PRELOADER_GRID);
		Scene scene = new Scene(borderPane, 250, 110);
		scene.getStylesheets().add(Theme.PRELOADER.getPath());

		Text textJackFish = new Text("JackFish");
		textJackFish.getStyleClass().addAll(CssVariables.PRELOADER_JASK_FISH_TEXT);

		BorderPane.setAlignment(textJackFish, Pos.CENTER);

		borderPane.setTop(textJackFish);
		ImageView imageView = new ImageView(new Image(CssVariables.Icons.MAIN_ICON));
		BorderPane.setAlignment(imageView, Pos.CENTER);
		BorderPane.setMargin(imageView, new Insets(0, 0, 5, 0));
		borderPane.setCenter(imageView);
		this.initStyle(StageStyle.UNDECORATED);
		this.setTitle("Loading...");
		this.setScene(scene);
		Thread thread = new Thread(new Task<Void>()
		{
			int index = 1;

			@Override
			protected Void call() throws Exception
			{
				Preloader preloader = Preloader.this;
				while (preloader.isShowing())
				{
					try
					{
						Thread.sleep(500);
						String title = title(index++ % 4);
						Platform.runLater(() -> preloader.setTitle(title));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				return null;
			}

			private String title(int index)
			{
				StringBuilder title = new StringBuilder("Loading");
				for (int i = 0; i < index; i++)
				{
					title.append(".");
				}
				return title.toString();
			}
		});
		thread.setName("Preloader thread, id :" + thread.getId());
		thread.start();
	}
}
