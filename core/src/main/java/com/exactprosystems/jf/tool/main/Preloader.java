/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.tool.Common;
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

		Text copyrightText = new Text(String.format(R.PRELOADER_COPYRIGHT.get(),DateTime.getYears(DateTime.current())));
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
		this.preloaderStage.setTitle(R.PRELOADER_LOADING.get());
		Common.addIcons(this.preloaderStage);
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
