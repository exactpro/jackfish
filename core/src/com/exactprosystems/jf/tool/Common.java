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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.ProxyException;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.custom.label.CommentsLabel;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.awt.Toolkit;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public abstract class Common
{
	public static ProgressBar progressBar;
	
	// TODO move it to Main
	public static Stage			node;

	private static Consumer<String> browserListener;

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

	//region focused methods
	/**
	 * set focused the node after 300ms delay
	 */
	public static void setFocused(final Node node)
	{
		setFocused(node, 300);
	}

	/**
	 * set focused the node after 15ms delay
	 */
	public static void setFocusedFast(final Node node)
	{
		setFocused(node, 15);
	}

	/**
	 * set focused the node after the delay
	 */
	public static void setFocused(final Node node, int delay)
	{
		Task<Void> task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(delay);
				return null;
			}
		};
		task.setOnSucceeded(e -> node.requestFocus());
		Thread thread = new Thread(task);
		thread.setName("Focused node with delay : " + node + " , thread id : " + thread.getId());
		thread.start();

	/*	Thread thread = new Thread(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(delay);
				Common.runLater(node::requestFocus);
				return null;
			}
		});
		thread.setName("Focused node with delay : " + node + " , thread id : " + thread.getId());
		thread.start();*/
	}

	public static boolean appIsFocused()
	{
		return node != null && node.isFocused();
	}

	//endregion

	public static void reportListener(Consumer<String> listener)
	{
		Common.browserListener = listener;
	}

	public static void openDefaultBrowser(String url)
	{
		Common.browserListener.accept(url);
	}

	/**
	 * use this method instead of Platform.runLater()
	 */
	public static void runLater(Runnable runnable)
	{
		if (Platform.isFxApplicationThread())
		{
			runnable.run();
		}
		else
		{
			Platform.runLater(runnable);
		}
	}

	@Deprecated
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

	// TODO move it to CustomTab
	public static CustomTab checkDocument(Document doc)
	{
		return CustomTabPane.getInstance().getTabs()
				.stream()
				.map(t -> (CustomTab) t)
				.filter(tab -> tab.getDocument().equals(doc))
				.findFirst()
				.orElse(null);
	}

	public static CustomTab checkDocument(String fileName)
	{
		return CustomTabPane.getInstance().getTabs()
				.stream()
				.map(t -> (CustomTab) t)
				.filter(tab -> tab.getDocument().getNameProperty().get().equals(fileName))
				.findFirst()
				.orElse(null);
	}

	public static CustomTab checkDocument(File file)
	{
		return CustomTabPane.getInstance().getTabs()
				.stream()
				.map(t -> (CustomTab) t)
				.filter(tab -> new File(tab.getDocument().getNameProperty().get()).getAbsolutePath().equals(file.getAbsolutePath()))
				.findFirst()
				.orElse(null);
	}

	public enum SpacerEnum {
		VerticalMin(CssVariables.VERTICAL_MIN),
		VerticalMid(CssVariables.VERTICAL_MID),
		VerticalMax(CssVariables.VERTICAL_MAX),

		HorizontalMin(CssVariables.HORIZONTAL_MIN),
		HorizontalMid(CssVariables.HORIZONTAL_MID),
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
		label.setPrefWidth(computeTextWidth(label.getFont(), label.getText(), 0.0D) + 20);
		label.setPrefHeight(PREF_HEIGHT);
		label.setMaxHeight(MAX_HEIGHT);
		label.setMinHeight(MIN_HEIGHT);
	}

	public static void sizeTextField(javafx.scene.control.TextField field)
	{
		int width = 80;
		field.setMinWidth(width);
		field.setPrefWidth(computeTextWidth(field.getFont(), field.getText(), 0.0D) + 40);
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
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, Settings.SHORTCUTS_NAME, nameShortcut);
		if (value != null)
		{
			return value.getValue().equals(EMPTY) ? "" : "(" + value.getValue() + ")";
		}
		return "";
	}

	public static KeyCombination getShortcut(Settings settings, String shortcutName)
	{
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, Settings.SHORTCUTS_NAME, shortcutName);
		if (value != null && !value.getValue().equals(EMPTY))
		{
			return KeyCombination.valueOf(value.getValue());
		}
		return null;
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
		Common.runLater(() -> progressBar.setVisible(flag));
	}

	public static String getSimpleTitle(String s)
	{
		return s == null ? null : s.substring(s.lastIndexOf(File.separatorChar) + 1);
	}

	public static LocalDateTime convert(Date date)
	{
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static Date convert(LocalDateTime dateTime)
	{
		if (dateTime == null)
		{
			return null;
		}
		Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	// --------------------------------------------------------------------------------------------------------------------------
	// try catch functions
	// --------------------------------------------------------------------------------------------------------------------------
	@FunctionalInterface
	public interface Function
	{
		void call() throws ProxyException, Exception;
	}

    @FunctionalInterface
	public interface SupplierWithException<T>
	{
		T get() throws Exception;
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

	public static <T> T tryCatch(SupplierWithException<T> func, String error, T defaultValue)
	{
		try
		{
			return func.get();
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

	public static void addIcons(Stage stage)
	{
		stage.getIcons().addAll(new Image(CssVariables.Icons.MAIN_ICON));
	}

	public static String createLiteral(Object value, AbstractEvaluator evaluator)
	{
		if (value instanceof String)
		{
			return evaluator.createString((String) value);
		}
		if (value instanceof Number)
		{
			return String.valueOf(value);
		}
		if (value instanceof Date)
		{
			DateTime date = new DateTime((Date)value);
			return String.format("DateTime.date(%d,%d,%d,%d,%d,%d)", date.years(), date.months(), date.days(), date.hours(), date.minutes(), date.seconds());
		}
		if (value instanceof File)
		{
			return evaluator.createString(((File) value).getPath());
		}
		return Str.asString(value);
	}

	public static StyleSpans<Collection<String>> convertFromList(List<StyleWithRange> list)
	{
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		list.forEach(styleWithRange ->
		{
			String style = styleWithRange.getStyle();
			spansBuilder.add(Collections.singleton(style == null ? "default" : style), styleWithRange.getRange());
		});
		return spansBuilder.create();
	}

	static final Text helper;
	static final double DEFAULT_WRAPPING_WIDTH;
	static final double DEFAULT_LINE_SPACING;
	static final String DEFAULT_TEXT;
	static final TextBoundsType DEFAULT_BOUNDS_TYPE;

	static
	{
		helper = new Text();
		DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
		DEFAULT_LINE_SPACING = helper.getLineSpacing();
		DEFAULT_TEXT = helper.getText();
		DEFAULT_BOUNDS_TYPE = helper.getBoundsType();
	}

	public static double computeTextWidth(Font font, String text, double help0)
	{
		helper.setText(text);
		helper.setFont(font);

		helper.setWrappingWidth(0.0D);
		helper.setLineSpacing(0.0D);
		double d = Math.min(helper.prefWidth(-1.0D), help0);
		helper.setWrappingWidth((int) Math.ceil(d));
		d = Math.ceil(helper.getLayoutBounds().getWidth());

		helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
		helper.setLineSpacing(DEFAULT_LINE_SPACING);
		helper.setText(DEFAULT_TEXT);
		return d;
	}

	public static boolean confirmFileDelete(String name)
	{
		return DialogsHelper.showQuestionDialog("Removing", String.format("Are you sure you want "
				+"to remove %s?\nYou can't undo this action.", name));
	}
}
