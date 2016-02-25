////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.ProxyException;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.common.Document;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.custom.label.CommentsLabel;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public abstract class Common
{
	public static ProgressBar progressBar;
	private static TabPane tabPane;

	public final static String settingsPath = ".settings.xml";
	public static final String breakPointLabel = "breakPoint";

	public final static int PREF_HEIGHT = 23;

	public final static int MIN_HEIGHT = PREF_HEIGHT - 1;
	public final static int MAX_HEIGHT = PREF_HEIGHT + 1;
	/**
	 * this field - pref width label on gui for name action item
	 */
	public final static int PREF_WIDTH_LABEL = 170;

	public static Stage node;

	public static final String intPositiveNumberMatcher = "^\\d+$";
	public static final String intNumberMatcher = "^-?\\d+$";
	public static final String empty = "<none>";
	public static final int BUTTON_SIZE_WITH_ICON = 42;
	public static final javafx.geometry.Insets insetsNode = new javafx.geometry.Insets(2, 5, 2, 5);
	public static final javafx.geometry.Insets insetsGrid = new javafx.geometry.Insets(0);

	private static File baseFile = new File(".");
	public static Popup popup;

	public static final Logger logger = Logger.getLogger(Common.class);

	public static final String FONT_SIZE = "-fx-font-size : 12";

	public static final String DATE_TIME_PATTERN = "HH:mm:ss dd.MM.yyyy";

	public static void setFocused(final TextField field)
	{
		new Thread(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(300);
				Platform.runLater(field::requestFocus);
				return null;
			}
		}).start();
	}

	public static boolean appIsFocused()
	{
		return node != null && node.isFocused();
	}

	public static <T extends ContainingParent> T loadController(URL resource)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(resource);
			Parent parent = loader.load();
			T controller = loader.getController();
			controller.setParent(parent);
			return controller;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Can't load resource: " + resource, e);
		}
	}

	public static CustomTab createTab(final Document model, Settings settings)
	{
		final CustomTab[] closure = new CustomTab[]{null};
		CustomTab tab = new CustomTab(model)
		{
			@Override
			public void onClose() throws Exception
			{
				if (model.canClose())
				{
					model.close(settings);
				}
			}

			@Override
			public void reload() throws Exception
			{
				Platform.runLater(() -> {
					ButtonType desision = DialogsHelper.showFileChangedDialog(model.getName());
					if (desision == ButtonType.OK)
					{
						Common.tryCatch(() -> {
							try (Reader reader = new FileReader(model.getName()))
							{
								model.load(reader);
							}
							model.display();
							model.saved();
						}, "Error on reload");
					}
					closure[0].saved(model.getName());
				});
			}
		};
		closure[0] = tab;

		return tab;
	}

	private static boolean needSelectedTab = false;

	public static boolean isNeedSelectedTab()
	{
		return needSelectedTab;
	}

	public static void setNeedSelectedTab(boolean flag)
	{
		Common.needSelectedTab = flag;
	}

	// TODO move it away
	private static Theme theme;

	public static void setTheme(Theme theme)
	{
		Common.theme = theme;
	}

	public static Theme currentTheme()
	{
		return Common.theme;
	}

	public static CustomTab checkDocument(Document doc)
	{
		Optional<Tab> first = tabPane.getTabs().stream().filter(tab -> ((CustomTab) tab).getDocument().equals(doc)).findFirst();

		if (first.isPresent())
		{
			return (CustomTab) first.get();
		}
		return null;
	}

	public static String getRelativePath(String filePath)
	{
		String result = filePath;
		String base = baseFile.getAbsolutePath().substring(0, baseFile.getAbsolutePath().length() - 1);
		if (filePath.contains(base))
		{
			result = filePath.substring(base.length());
		}
		return result.replace('\\', '/');
	}

	public static String absolutePath(File file)
	{
		return file == null ? null : file.getAbsolutePath();
	}

	public static void sizeLabel(Label label)
	{
		label.setPrefWidth(label.getText().length() * 8 + 20);
		label.setPrefHeight(PREF_HEIGHT);
		label.setMaxHeight(MAX_HEIGHT);
		label.setMinHeight(MIN_HEIGHT);
	}

	public static void sizeTextField(javafx.scene.control.TextField field)
	{
		field.setMinWidth(60);
		field.setPrefWidth(field.getText() == null ? 60 : (field.getText().equals("") ? 60 : (field.getText().length() * 8 + 20)));
	}

	public static void sizeButtons(int px, Button... buttons)
	{
		for (Button button : buttons)
		{
			sizeButton(button, px);
		}
	}

	public static void sizeButton(Button button, int px)
	{
		button.setPrefWidth(px);
		button.setMaxWidth(px);
		button.setMinWidth(px);

		button.setPrefHeight(px);
		button.setMaxHeight(px);
		button.setMinHeight(px);
	}

	public static void sizeHeightComments(CommentsLabel label, int size)
	{
		sizeHeightComments(label, size, size, size);
	}

	public static void sizeHeightComments(CommentsLabel label, int minSize, int prefSize, int maxSize)
	{
		label.setMinHeight(minSize);
		label.setMaxHeight(maxSize);
		label.setPrefHeight(prefSize);
	}

	public static String getShortcutTooltip(Settings settings, String nameShortcut) throws Exception
	{
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, SettingsPanel.SHORTCUTS_NAME, nameShortcut);
		if (value != null)
		{
			return value.getValue().equals(empty) ? "" : "(" + value.getValue() + ")";
		}
		return "";
	}

	private static String getShortcut(Settings settings, String nameShortcut)
	{
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, SettingsPanel.SHORTCUTS_NAME, nameShortcut);
		if (value != null)
		{
			return value.getValue().equals(empty) ? "" : value.getValue();
		}
		return "";
	}

	public static void customizeLabeled(Labeled n, String cssVariable, String icon)
	{
		n.getStyleClass().addAll(cssVariable);
		n.setGraphic(new ImageView(new Image(icon)));
	}

	public static void setProgressBar(ProgressBar bar)
	{
		progressBar = bar;
	}

	public static void setTabPane(TabPane tab)
	{
		tabPane = tab;
	}

	public static TabPane getTabPane()
	{
		return tabPane;
	}

	public static int setHeightComments(String text)
	{
		if (text.trim().length() == 0)
		{
			return 0;
		}
		int length = text.split("\n").length;
		return (length == 1 || length == 0) ? 45 : (length - 1) * 15 + 45;
	}

	public static void progressBarVisible(final boolean flag)
	{
		Platform.runLater(() -> progressBar.setVisible(flag));
	}

	public static String getSimpleTitle(String s)
	{
		if (s == null)
		{
			return null;
		}
		return s.substring(s.lastIndexOf(File.separatorChar) + 1);
	}

	public static LocalDateTime convert(Date date)
	{
		return LocalDateTime.of(DateTime.getYears(date), DateTime.getMonths(date), DateTime.getDays(date), DateTime.getHours(date), DateTime.getMinutes(date), DateTime.getSeconds(date));
	}

	public static Date convert(LocalDateTime dateTime)
	{
		return DateTime.date(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
	}

	// --------------------------------------------------------------------------------------------------------------------------
	// try catch functions
	// --------------------------------------------------------------------------------------------------------------------------
	@FunctionalInterface
	public static interface Function
	{
		void call() throws ProxyException, Exception;
	}

	public static void tryCatch(Function fn, String error)
	{
		try
		{
			fn.call();
		}
		catch (ProxyException e)
		{
			logger.error(e.getFullMessage(), e.getCause());
			DialogsHelper.showError(e.getFullMessage() + " : " + error);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(e.getMessage() + "\n" + error);
		}
	}

	public static void tryCatchThrow(Function fn, String msg) throws Exception
	{
		try
		{
			fn.call();
		}
		catch (Exception e)
		{
			throw new Exception(msg, e);
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------
	// property getter
	// --------------------------------------------------------------------------------------------------------------------------
	@FunctionalInterface
	public static interface PropertyGetter
	{
		Object get(IControl control);
	}

	public static String get(IControl control, String defaultValue, PropertyGetter func)
	{
		if (control == null)
		{
			return defaultValue;
		}
		Object value = func.get(control);
		return value == null ? null : String.valueOf(value);
	}

	public static void copyText(String text)
	{
		StringSelection stringSelection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	public static String stringFromFont(Font font)
	{
		if (font == null)
		{
			font = Font.getDefault();
		}
		return font.getName() + "$" + font.getSize();
	}


	public static Font fontFromString(String fontName)
	{
		if (fontName == null)
		{
			return Font.getDefault();
		}
		String[] parts = fontName.split("\\$");
		if (parts.length >= 2)
		{
			double size = 18;
			try
			{
				size = Double.parseDouble(parts[1]);
			}
			catch (NumberFormatException e)
			{
			}

			return Font.font(parts[0], size);
		}
		return Font.getDefault();
	}

	public static Color stringToColor(String color)
	{
		return Color.web(color);
	}

	public static String colorToString(Color color)
	{
		return String.format("rgba(%s,%s,%s,%s)", ((int) (color.getRed() * 255)), ((int) (color.getGreen() * 255)), ((int) (color.getBlue() * 255)), color.getOpacity());
	}

	public static void saveToClipboard(String text)
	{
		StringSelection stringSelection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	public static String getFromClipboard()
	{
		try
		{
			long t1 = System.currentTimeMillis();
			Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			System.out.println("def toolkit : " + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			Clipboard systemClipboard = defaultToolkit.getSystemClipboard();
			System.out.println("clipboard : " + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			Transferable contents = systemClipboard.getContents(null);
			Object data = systemClipboard.getData(DataFlavor.plainTextFlavor);
			System.out.println("get data" + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			String s = String.valueOf(data);
			System.out.println("value of " + (System.currentTimeMillis() - t1));
			return s;
			//			return String.valueOf(Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
