////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Preloader extends javafx.application.Preloader
{
	private Stage preloaderStage;

	private final double GRID_SIZE = 8.0;
	private final ProgressBar progressBar;

	public Preloader()
	{
		this.progressBar = new ProgressBar();
	}

	private Scene getScene()
	{
		this.progressBar.setPrefHeight(GRID_SIZE * 2);
		this.progressBar.setMaxHeight(GRID_SIZE * 2);
		this.progressBar.setMinHeight(GRID_SIZE * 2);
		this.progressBar.getStyleClass().add(CssVariables.PRELOADER_PROGRESS_BAR);
		this.progressBar.setProgress(0.0);
		AnchorPane anchorPane = new AnchorPane();

		anchorPane.setId("preloaderPane");

		Text copyrightText = new Text("@ 2009-" + DateTime.getYears(DateTime.current()) + " Exactpro Systems. All rights reserved.");
		copyrightText.setId("textLogo");

		AnchorPane.setBottomAnchor(copyrightText, GRID_SIZE * 2);
		AnchorPane.setLeftAnchor(copyrightText, GRID_SIZE * 2);
		anchorPane.getChildren().add(copyrightText);

		AnchorPane.setBottomAnchor(this.progressBar, GRID_SIZE * 6);
		AnchorPane.setRightAnchor(this.progressBar, GRID_SIZE * 2);
		AnchorPane.setLeftAnchor(this.progressBar, GRID_SIZE * 2);
		anchorPane.getChildren().add(this.progressBar);

		ImageView exLogo = new ImageView();
		exLogo.setId("exLogo");
		AnchorPane.setRightAnchor(exLogo, GRID_SIZE * 2);
		AnchorPane.setBottomAnchor(exLogo, GRID_SIZE * 0.5);
		anchorPane.getChildren().add(exLogo);

		ImageView jfLogo = new ImageView();
		jfLogo.setId("jfLogo");
		AnchorPane.setTopAnchor(jfLogo, GRID_SIZE * 6);
		AnchorPane.setLeftAnchor(jfLogo, GRID_SIZE * 16);
		anchorPane.getChildren().add(jfLogo);

		Text currentVersion = new Text(VersionInfo.getVersion());
		currentVersion.setId("textLogo");
		AnchorPane.setLeftAnchor(currentVersion, GRID_SIZE * 16);
		AnchorPane.setBottomAnchor(currentVersion, GRID_SIZE * 12);
		anchorPane.getChildren().add(currentVersion);

		Scene scene = new Scene(anchorPane, 512, 256);
		scene.getStylesheets().add(Theme.GENERAL.getPath());
		return scene;
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		this.preloaderStage = primaryStage;
		this.preloaderStage.setScene(getScene());
		this.preloaderStage.initStyle(StageStyle.UNDECORATED);
		this.preloaderStage.setTitle("Loading...");
		this.preloaderStage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification info)
	{
		if (info instanceof ProgressNotification) {
			ProgressNotification progressNotification = (ProgressNotification) info;
			double v = progressNotification.getProgress() / 100;
			this.progressBar.setProgress(v);
		} else if (info instanceof StateChangeNotification) {
			preloaderStage.hide();
		}
	}
}
