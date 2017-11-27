////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.tabs.*;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsPanelController implements Initializable, ContainingParent
{
	private static final Logger logger = Logger.getLogger(SettingsPanelController.class);

	public Tab mainTab;
	public Tab shortCutsTab;
	public Tab logTab;
	public Tab colorsTab;
	public Tab gitTab;
	public Tab matrixTab;
	public Tab wizardTab;
	public TabPane tabPane;

	private SettingsPanel model;
	private Dialog<ButtonType> dialog;
	private Pane pane;

	private MainTabController mainTabController;
	private ShortcutsTabController shortcutsTabController;
	private LogTabController logTabController;
	private ColorsTabController colorsTabController;
	private GitTabController gitTabController;
	private WizardTabController wizardTabController;
	private MatrixTabController matrixTabController;

	double defaultHeight = -1;
	private final Timeline resizer = new Timeline();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.resizer.setCycleCount(1);
		this.resizer.setDelay(Duration.seconds(0.5));
		this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				Object userData = newValue.getUserData();
				if (userData instanceof ITabHeight)
				{
					double currentHeight = ((ITabHeight) userData).getHeight();
					double dialogHeight = this.dialog.getHeight();
					if (currentHeight == -1 && dialogHeight == this.defaultHeight)
					{
						return;
					}

					//region with animation. Uncomment, if you want change height with animation

					/*SimpleDoubleProperty stageHeightProperty = new SimpleDoubleProperty(dialogHeight);
					stageHeightProperty.addListener((observable1, oldValue1, newValue1) -> this.dialog.setHeight(newValue1.doubleValue()));

					this.resizer.getKeyFrames().clear();

					this.resizer.getKeyFrames().addAll(
							new KeyFrame(Duration.ZERO, new KeyValue(stageHeightProperty, dialogHeight, Interpolator.LINEAR)),
							new KeyFrame(Duration.seconds(0.2), new KeyValue(stageHeightProperty, currentHeight == -1 ? this.defaultHeight : (150 + currentHeight), Interpolator.LINEAR))
					);
					this.resizer.play();*/

					//endregion

					//region without animation
					if (currentHeight == -1)
					{
						this.dialog.setHeight(this.defaultHeight);
					}
					else
					{
						this.dialog.setHeight(190 + currentHeight);
					}
					//endregion
				}
			}
		});
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = (Pane) parent;
	}

	public void init(final SettingsPanel model)
	{
		this.mainTabController = Common.loadController(this.getClass().getResource("tabs/MainTab.fxml"));
		this.mainTabController.init(model);

		this.shortcutsTabController = Common.loadController(this.getClass().getResource("tabs/ShortcutsTab.fxml"));
		this.shortcutsTabController.init(model);

		this.logTabController = Common.loadController(this.getClass().getResource("tabs/LogTab.fxml"));
		this.logTabController.init(model);

		this.colorsTabController = Common.loadController(this.getClass().getResource("tabs/ColorsTab.fxml"));
		this.colorsTabController.init(model);

		this.gitTabController = Common.loadController(this.getClass().getResource("tabs/GitTab.fxml"));
		this.gitTabController.init(model);

		this.wizardTabController = Common.loadController(this.getClass().getResource("tabs/WizardTab.fxml"));
		this.wizardTabController.init(model);

		this.matrixTabController = Common.loadController(this.getClass().getResource("tabs/MatrixTab.fxml"));
		this.matrixTabController.init(model);

		this.model = model;
	}

	//region display methods
	public void displayColors(Map<String, String> collect)
	{
		this.colorsTabController.displayInfo(collect);
		this.colorsTabController.displayInto(this.colorsTab);
	}

	public void displayWizard(Map<String, String> collect)
	{
		this.wizardTabController.displayInfo(collect);
		this.wizardTabController.displayInto(this.wizardTab);
	}

    public void displayGit(Map<String, String> collect)
	{
		this.gitTabController.displayInfo(collect);
		this.gitTabController.displayInto(this.gitTab);
	}

	public void displayMain(Map<String, String> res)
	{
		this.mainTabController.displayInfo(res);
		this.mainTabController.displayInto(this.mainTab);
	}

	public void displayLogs(Map<String, String> res)
	{
		this.logTabController.displayInfo(res);
		this.logTabController.displayInto(this.logTab);
	}

	public void displayShortcuts(Map<String, String> documents, Map<String, String> matrixNavigation, Map<String, String> matrixActions, Map<String, String> other)
	{
		this.shortcutsTabController.displayInfo(documents, matrixNavigation, matrixActions, other);
		this.shortcutsTabController.displayInto(this.shortCutsTab);
	}

	public void displayMatrix(Map<String, String> res)
	{
		this.matrixTabController.displayInfo(res);
		this.matrixTabController.displayInto(this.matrixTab);
	}
	//endregion

	public void display(String title)
	{
		this.dialog = new Dialog<>();
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		this.dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
		ButtonBar.setButtonData(this.dialog.getDialogPane().lookupButton(save), ButtonBar.ButtonData.OTHER);
		this.dialog.getDialogPane().setContent(this.pane);
		this.dialog.setTitle(title);
		this.dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		this.dialog.heightProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue.doubleValue() > this.defaultHeight)
			{
				this.defaultHeight = newValue.doubleValue();
			}
		});
		Optional<ButtonType> optional = this.dialog.showAndWait();
		optional.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(bt ->
		{
			if (save())
			{
				DialogsHelper.showInfo(String.format("Settings saved to file [%s] %n Restart application for all changes apply", Settings.SETTINGS_PATH));
			}
			else
			{
				DialogsHelper.showError("Error to save.\nSee log for details");
			}
		});
	}

	private boolean save()
	{
		try
		{
			this.mainTabController.save();
			this.shortcutsTabController.save();
			this.logTabController.save();
			this.colorsTabController.save();
			this.gitTabController.save();
			this.wizardTabController.save();
			this.matrixTabController.save();
			this.model.save();
			return true;
		}
		catch (Exception e)
		{
			logger.error("error on save");
			logger.error(e.getMessage(), e);
		}
		finally
		{
			this.dialog.hide();
		}
		return false;
	}

	public void restoreAllToDefault(ActionEvent actionEvent)
	{
		this.tabPane.getTabs().stream()
				.map(Tab::getUserData)
				.filter(controller -> controller instanceof ITabRestored)
				.map(controller -> (ITabRestored) controller)
				.forEach(ITabRestored::restoreToDefault);
	}
}