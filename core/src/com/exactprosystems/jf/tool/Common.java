////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.error.app.ProxyException;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.custom.label.CommentsLabel;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public abstract class Common
{
	public static ProgressBar progressBar;
	
	// TODO move it to CustomTab
	private static TabPane		tabPane;
	// TODO move it to Main
	public static Stage			node;

	public final static String	SETTINGS_PATH			= ".settings.xml";
	public final static int		PREF_HEIGHT				= 23;
	public final static int		MIN_HEIGHT				= PREF_HEIGHT - 1;
	public final static int		MAX_HEIGHT				= PREF_HEIGHT + 1;
	public final static int		PREF_WIDTH_LABEL		= 170;
	public final static int		BUTTON_SIZE_WITH_ICON	= 32;
	public final static String	UINT_REGEXP				= "^\\d+$";
	public final static String	INT_REGEXP				= "^-?\\d+$";
	public final static String	EMPTY					= "<none>";
	public final static Insets	INSETS_NODE				= new Insets(2, 5, 2, 5);
	public final static Insets	INSETS_GRID				= new Insets(0);
	public final static String	FONT_SIZE				= "-fx-font-size : 12";
	public final static String	DATE_TIME_PATTERN		= "HH:mm:ss dd.MM.yyyy";

	public static final Logger	logger					= Logger.getLogger(Common.class);

	public static void setFocused(final Node node)
	{
		Thread thread = new Thread(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(300);
				Platform.runLater(node::requestFocus);
				return null;
			}
		});
		thread.setName("Focused node : " + node + " , thread id : " + thread.getId());
		thread.start();
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

	// TODO move it to Main
	private static Theme theme;

	public static void setTheme(Theme theme)
	{
		Common.theme = theme;
	}

	public static Theme currentTheme()
	{
		return Common.theme;
	}

	// TODO move it to CustomTab
	public static CustomTab checkDocument(Document doc)
	{
		return tabPane.getTabs()
				.stream()
				.map(t -> (CustomTab) t)
				.filter(tab -> tab.getDocument().equals(doc))
				.findFirst()
				.orElse(null);
	}

	public enum SpacerEnum {
		VerticalMin(CssVariables.VERTICAL_MIN),
		VerticalPref(CssVariables.VERTICAL_MID),
		VerticalMax(CssVariables.VERTICAL_MAX),

		HorizontalMin(CssVariables.HORIZONTAL_MIN),
		HorizontalPref(CssVariables.HORIZONTAL_MID),
		HorizontalMax(CssVariables.HORIZONTAL_MAX);

		private String style;

		SpacerEnum(String style)
		{
			this.style = style;
		}

		public String getStyle()
		{
			return style;
		}
	}

	public static Label createSpacer(SpacerEnum spacerEnum)
	{
		Label lbl = new Label();
		lbl.setId(spacerEnum.getStyle());
		return lbl;
	}

	public static String getRelativePath(String filePath)
	{
		File currentDir = new File(".");
		String result = filePath;
		String base = currentDir.getAbsolutePath().substring(0, currentDir.getAbsolutePath().length() - 1);
		if (filePath.contains(base))
		{
			result = filePath.substring(base.length());
		}
		return result.replace('\\', '/');
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
		int width = 80;
		field.setMinWidth(width);
		field.setPrefWidth(field.getText() == null ? width : (field.getText().equals("") ? width : (field.getText().length() * 8 + 20)));
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
			return value.getValue().equals(EMPTY) ? "" : "(" + value.getValue() + ")";
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
		return s == null ? null : s.substring(s.lastIndexOf(File.separatorChar) + 1);
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

	public interface FunctionWithReturn<T>
	{
		T call() throws ProxyException, Exception;
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

	public static <T> T tryCatch(FunctionWithReturn<T> func, String error, T defaultValue)
	{
		try
		{
			return func.call();
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
		return defaultValue;
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
//			System.out.println("def toolkit : " + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			Clipboard systemClipboard = defaultToolkit.getSystemClipboard();
//			System.out.println("clipboard : " + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			Object data = systemClipboard.getData(DataFlavor.stringFlavor);
//			System.out.println("get data" + (System.currentTimeMillis() - t1));
			t1 = System.currentTimeMillis();
			String s = String.valueOf(data);
//			System.out.println("value of " + (System.currentTimeMillis() - t1));
			return s;
			//			return String.valueOf(Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static List<String> readFile(File file, boolean needRemove) throws Exception
	{
		Path path = Paths.get(file.toURI());
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
		if (needRemove)
		{
			Files.delete(path);
		}
		return lines;
	}

	public static void writeToFile(File file, List<String> lines) throws Exception
	{
		try (PrintWriter writer = new PrintWriter(new FileWriter(file)))
		{
			lines.forEach(writer::println);
		}
	}
}
