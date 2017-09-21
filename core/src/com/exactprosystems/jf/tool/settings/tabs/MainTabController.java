package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	private SettingsPanel model;
	private Parent        parent;

	public GridPane         mainGrid;
	public NumberTextField  ntfMaxLastMatrixCount;
	public NumberTextField  ntfTimeNotification;
	public CheckBox         useFullScreen;
	public CheckBox         useExternalReportViewer;
	public ComboBox<Theme>  comboBoxTheme;
	public ComboBox<String> cbFontFamily;
	public ComboBox<Double> cbFontSize;
	public CheckBox         useFullScreenXpath;
	public TextArea         taCopyright;
	public GridPane         numberGrid;

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.ntfMaxLastMatrixCount = new NumberTextField(0);
		this.ntfMaxLastMatrixCount.setId(Settings.MAX_LAST_COUNT);

		this.ntfTimeNotification = new NumberTextField(0);
		this.ntfTimeNotification.setId(Settings.TIME_NOTIFICATION);

		this.numberGrid.add(this.ntfMaxLastMatrixCount, 1, 0, 2, 1);
		this.numberGrid.add(this.ntfTimeNotification, 1, 1);

		this.comboBoxTheme.setItems(FXCollections.observableArrayList(Arrays.stream(Theme.values()).filter(Theme::isVisible).collect(Collectors.toList())));
		initializeFont();

		comboBoxTheme.getSelectionModel().selectedItemProperty().addListener((observableValue, theme, theme2) ->
				Common.runLater(() ->
						this.comboBoxTheme.getScene().getStylesheets().setAll(theme2.getPath())
				)
		);

		restoreToDefault();
	}

	public void init(SettingsPanel model)
	{
		this.model = model;
	}

	public void displayInfo(Map<String, String> res)
	{
		SettingsPanel.setValue(Settings.MAX_LAST_COUNT, res, str -> this.ntfMaxLastMatrixCount.setText(str));
		SettingsPanel.setValue(Settings.TIME_NOTIFICATION, res, str -> this.ntfTimeNotification.setText(str));

		SettingsPanel.setValue(Settings.THEME, res, str -> this.comboBoxTheme.getSelectionModel().select(Theme.valueOf(str)));

		SettingsPanel.setValue(Settings.FONT, res, str -> {
			Font font = Common.fontFromString(str);

			this.cbFontFamily.getSelectionModel().select(font.getFamily());
			this.cbFontSize.getSelectionModel().select(font.getSize());
		});

		SettingsPanel.setValue(Settings.USE_FULL_SCREEN, res, str -> this.useFullScreen.setSelected(Boolean.parseBoolean(str)));
		SettingsPanel.setValue(Settings.USE_EXTERNAL_REPORT_VIEWER, res, str -> this.useExternalReportViewer.setSelected(Boolean.parseBoolean(str)));
		SettingsPanel.setValue(Settings.USE_FULLSCREEN_XPATH, res, str -> this.useFullScreenXpath.setSelected(Boolean.parseBoolean(str)));
		SettingsPanel.setValue(Settings.COPYRIGHT, res, str -> this.taCopyright.setText(str.replaceAll("\\\\n", System.lineSeparator())));
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		return -1;
	}

	public void save()
	{
		this.model.updateSettingsValue(Settings.MAX_LAST_COUNT, Settings.SETTINGS, String.valueOf(this.ntfMaxLastMatrixCount.getValue()));
		this.model.updateSettingsValue(Settings.TIME_NOTIFICATION, Settings.SETTINGS, String.valueOf(this.ntfTimeNotification.getValue()));

		this.model.updateSettingsValue(Settings.THEME, Settings.SETTINGS, this.comboBoxTheme.getSelectionModel().getSelectedItem().toString());
		Font font = Font.font(this.cbFontFamily.getSelectionModel().getSelectedItem(), this.cbFontSize.getSelectionModel().getSelectedItem());
		this.model.updateSettingsValue(Settings.FONT, Settings.SETTINGS, Common.stringFromFont(font));

		this.model.updateSettingsValue(Settings.USE_FULL_SCREEN, Settings.SETTINGS, String.valueOf(this.useFullScreen.isSelected()));
		this.model.updateSettingsValue(Settings.USE_EXTERNAL_REPORT_VIEWER, Settings.SETTINGS, String.valueOf(this.useExternalReportViewer.isSelected()));
		this.model.updateSettingsValue(Settings.USE_FULLSCREEN_XPATH, Settings.SETTINGS, String.valueOf(this.useFullScreenXpath.isSelected()));

		this.model.updateSettingsValue(Settings.COPYRIGHT, Settings.SETTINGS, this.taCopyright.getText().replaceAll(System.lineSeparator(), "\\\\n"));
	}

	@Override
	public void restoreToDefault()
	{
		Settings defaultSettings = Settings.defaultSettings();

		this.ntfMaxLastMatrixCount.setValue(Integer.parseInt(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.MAX_LAST_COUNT).getValue()));
		this.ntfTimeNotification.setValue(Integer.parseInt(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.TIME_NOTIFICATION).getValue()));

		this.comboBoxTheme.getSelectionModel().select(Theme.valueOf(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.THEME).getValue()));

		String fontValue = defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.FONT).getValue();
		Font font = Common.fontFromString(fontValue);
		this.cbFontFamily.getSelectionModel().select(font.getFamily());
		this.cbFontSize.getSelectionModel().select(font.getSize());

		this.useFullScreen.setSelected(Boolean.parseBoolean(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_FULL_SCREEN).getValue()));
		this.useExternalReportViewer.setSelected(Boolean.parseBoolean(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_EXTERNAL_REPORT_VIEWER).getValue()));
		this.useFullScreenXpath.setSelected(Boolean.parseBoolean(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_FULLSCREEN_XPATH).getValue()));

		this.taCopyright.setText(defaultSettings.getValue(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.COPYRIGHT).getValue().replaceAll("\\\\n", System.lineSeparator()));
	}

	public void restoreDefaults(ActionEvent actionEvent)
	{
		restoreToDefault();
	}

	private void initializeFont()
	{
		cbFontFamily.getStyleClass().add("font-menu-button");
		cbFontFamily.setMinWidth(150);
		cbFontFamily.setPrefWidth(150);
		cbFontFamily.setMaxWidth(150);
		cbFontFamily.setFocusTraversable(false);
		cbFontFamily.setCellFactory(param ->
		{
			final ListCell<String> cell = new ListCell<String>()
			{
				@Override
				public void updateItem(String item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty)
					{
						setText(null);
						setGraphic(null);
					}
					else
					{
						setText(item);
						setFont(new Font(item, 12));
					}
				}
			};
			cell.setMinWidth(100);
			cell.setPrefWidth(100);
			cell.setMaxWidth(100);
			return cell;
		});
		Common.runLater(() -> cbFontFamily.setItems(FXCollections.observableArrayList(Font.getFamilies())));
		cbFontSize.getStyleClass().add("font-menu-button");
		cbFontSize.setFocusTraversable(false);
		cbFontSize.getItems().add((double) 8);
		cbFontSize.getItems().add((double) 10);
		cbFontSize.getItems().add((double) 12);
		cbFontSize.getItems().add((double) 13);
		cbFontSize.getItems().add((double) 14);
		cbFontSize.getItems().add((double) 18);
		cbFontSize.getItems().add((double) 24);
		cbFontSize.getItems().add((double) 36);
		cbFontSize.setCellFactory(param -> new ListCell<Double>()
		{
			@Override
			public void updateItem(Double item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setText(null);
					setGraphic(null);
				}
				else
				{
					setText(String.valueOf(item));
					setFont(new Font(cbFontFamily.getValue(), item));
				}
			}
		});
	}
}
