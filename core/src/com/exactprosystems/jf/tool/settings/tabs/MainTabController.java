package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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

public class MainTabController implements Initializable, ContainingParent, ITabHeight
{
	private SettingsPanel model;
	private Parent parent;

	public GridPane mainGrid;
	public NumberTextField ntfMaxLastMatrixCount;
	public NumberTextField ntfTimeNotification;
	public CheckBox useFullScreen;
	public ComboBox<Theme> comboBoxTheme;
	public ComboBox<String> cbFontFamily;
	public ComboBox<Double> cbFontSize;
	public CheckBox useFullScreenXpath;
	public TextArea taCopyright;
	public GridPane numberGrid;

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

		this.comboBoxTheme.setId(Settings.THEME);
		this.useFullScreen.setId(Settings.USE_FULL_SCREEN);

		this.comboBoxTheme.setItems(FXCollections.observableArrayList(Arrays.stream(Theme.values()).filter(Theme::isVisible).collect(Collectors.toList())));
		initializeFont();

		comboBoxTheme.getSelectionModel().selectedItemProperty().addListener((observableValue, theme, theme2) -> Platform.runLater(() -> this.useFullScreen.getScene().getStylesheets().setAll(theme2.getPath())));

		this.useFullScreen.setOnAction(actionEvent -> this.model.updateSettingsValue(this.useFullScreen.getId(), Settings.SETTINGS, String.valueOf(useFullScreen.isSelected())));
		this.useFullScreenXpath.setOnAction(actionEvent -> this.model.updateSettingsValue(this.useFullScreenXpath.getId(), Settings.SETTINGS, String.valueOf(useFullScreenXpath.isSelected())));
	}

	public void init(SettingsPanel model)
	{
		this.model = model;
	}

	public void displayInfo(Map<String, String> res)
	{
		Font font = Common.fontFromString(res.get(Settings.FONT));

		this.cbFontFamily.getSelectionModel().select(font.getFamily());
		this.cbFontSize.getSelectionModel().select(font.getSize());
		this.comboBoxTheme.getSelectionModel().select(Theme.valueOf(res.get(comboBoxTheme.getId()) == null ? Theme.WHITE.name() : res.get(comboBoxTheme.getId())));

		this.ntfMaxLastMatrixCount.setText(res.get(ntfMaxLastMatrixCount.getId()) == null ?
				Settings.defaultSettings().getValueOrDefault(Settings.GLOBAL_NS, "Main", Settings.MAX_LAST_COUNT, "").getValue() : res.get(ntfMaxLastMatrixCount.getId()));
		this.ntfTimeNotification.setText(res.get(ntfTimeNotification.getId()) == null ?
				Settings.defaultSettings().getValueOrDefault(Settings.GLOBAL_NS, "Main", Settings.TIME_NOTIFICATION, "").getValue() : res.get(ntfTimeNotification.getId()));
		this.useFullScreen.setSelected(Boolean.valueOf(res.get(useFullScreen.getId()) == null ?
				Settings.defaultSettings().getValueOrDefault(Settings.GLOBAL_NS, "Main", Settings.USE_FULL_SCREEN, "false").getValue() : res.get(useFullScreen.getId())));
		this.useFullScreenXpath.setSelected(Boolean.valueOf(res.get(useFullScreenXpath.getId()) == null ?
				Settings.defaultSettings().getValueOrDefault(Settings.GLOBAL_NS, "Main", Settings.USE_FULLSCREEN_XPATH, "false").getValue() : res.get(useFullScreenXpath.getId())));
		this.taCopyright.setText(res.get(taCopyright.getId()) == null ?
				Settings.defaultSettings().getValueOrDefault(Settings.GLOBAL_NS, "Main", Settings.COPYRIGHT, "").getValue() : res.get(taCopyright.getId()).replaceAll("\\\\n", System.lineSeparator()));
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

	private void initializeFont()
	{
		cbFontFamily.getStyleClass().add("font-menu-button");
		cbFontFamily.setMinWidth(150);
		cbFontFamily.setPrefWidth(150);
		cbFontFamily.setMaxWidth(150);
		cbFontFamily.setFocusTraversable(false);
		cbFontFamily.setCellFactory(param -> {
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
		Platform.runLater(() -> cbFontFamily.setItems(FXCollections.observableArrayList(Font.getFamilies())));
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
		Arrays.asList(cbFontFamily, cbFontSize).stream().forEach(cb -> cb.setOnAction(event -> {
			Font font = Font.font(cbFontFamily.getValue(), cbFontSize.getValue());
			model.updateSettingsValue(Settings.FONT, Settings.SETTINGS, Common.stringFromFont(font));
		}));
	}

	public void save()
	{
		Arrays.asList(this.useFullScreen, this.useFullScreenXpath)
				.forEach(cb -> this.model.updateSettingsValue(cb.getId(), Settings.SETTINGS, String.valueOf(cb.isSelected())));

		this.model.updateSettingsValue(this.comboBoxTheme.getId(), Settings.SETTINGS, this.comboBoxTheme.getSelectionModel().getSelectedItem().toString());

		this.model.updateSettingsValue(this.ntfTimeNotification.getId(), Settings.SETTINGS, String.valueOf(this.ntfTimeNotification.getValue()));
		this.model.updateSettingsValue(this.ntfMaxLastMatrixCount.getId(), Settings.SETTINGS, String.valueOf(this.ntfMaxLastMatrixCount.getValue()));
		this.model.updateSettingsValue(this.taCopyright.getId(), Settings.SETTINGS, this.taCopyright.getText().replaceAll(System.lineSeparator(), "\\\\n"));
	}
}
