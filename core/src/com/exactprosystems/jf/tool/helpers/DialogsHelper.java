////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.helpers;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.*;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.Notifications;
import com.exactprosystems.jf.tool.custom.UserEditTableDialog;
import com.exactprosystems.jf.tool.custom.UserInputDialog;
import com.exactprosystems.jf.tool.custom.browser.ReportBrowser;
import com.exactprosystems.jf.tool.custom.date.DateTimePicker;
import com.exactprosystems.jf.tool.custom.date.DateTimePickerSkin;
import com.exactprosystems.jf.tool.custom.helper.HelperFx;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public abstract class DialogsHelper
{
	private static int timeNotification = 10;

	@FunctionalInterface
	public interface ListViewPanel<T>
	{
		void select(T t);
	}

	public enum OpenSaveMode
	{
		OpenFile,
		SaveFile
	}

	public static ButtonType showParametersDialog(String title, final Map<String, String> parameters, AbstractEvaluator evaluator, Function<String, ListProvider> function)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefHeight(500);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setTitle("Parameters");
		ButtonType btnYes = new ButtonType(title, ButtonBar.ButtonData.YES);
		dialog.getDialogPane().getButtonTypes().addAll(btnYes);
		ListView<ExpressionFieldsPane> listView = new ListView<>();
		dialog.getDialogPane().setContent(listView);
		parameters.forEach((key, value) -> listView.getItems().addAll(new ExpressionFieldsPane(key, value, evaluator, function.apply(key))));
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			parameters.clear();
			ObservableList<ExpressionFieldsPane> items = listView.getItems();
			for (ExpressionFieldsPane item : items){
				parameters.put(item.getKey().getText(), Str.IsNullOrEmpty(item.getValue().getText()) ? null : item.getValue().getText());
			}
			return buttonType.get();
		}
		return ButtonType.CANCEL;
	}

	public static ButtonType showFileChangedDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle("Warning");
		dialog.getDialogPane().setHeaderText("File " + fileName + " was changed by another process");
		dialog.getDialogPane().setContentText("Reload it?");
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		return buttonType.orElse(ButtonType.CANCEL);
	}

	public static Date showDateTimePicker(Date initialValue)
	{
		DateTimePicker picker = new DateTimePicker(initialValue);
		DateTimePickerSkin skin = new DateTimePickerSkin(picker);
		Node popupContent = skin.getPopupContent();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
		alert.getDialogPane().setContent(popupContent);
		alert.setTitle("Select date");
		alert.setHeaderText("Choose date");
		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = alert.showAndWait();
		Optional<ButtonType> btnOk = buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE));
		return btnOk.map(buttonType1 -> picker.getDate()).orElse(initialValue);
	}


	public static boolean showQuestionDialog(String header, String body)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle("Warning");
		dialog.getDialogPane().setHeaderText(header);
		dialog.getDialogPane().setContentText(body);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		return buttonType.map(buttonType1 -> buttonType1.getButtonData() == ButtonBar.ButtonData.OK_DONE).orElse(false);
	}

	public static ButtonType showSaveFileDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle("Save");
		dialog.getDialogPane().setHeaderText("File " + fileName + " was changed.");
		dialog.getDialogPane().setContentText("Do you want to save?");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> res = dialog.showAndWait();
		if (res.isPresent())
		{
			return res.get();
		}
		return ButtonType.CANCEL;
	}

	public static <T> void showFindListView(final List<T> list, String title, final ListViewPanel<T> listener)
	{
		if (list == null || list.isEmpty())
		{
			showInfo("Nothing to show");
			return;
		}
		if (list.size() == 1)
		{
			listener.select(list.get(0));
			return;
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		BorderPane.setAlignment(listView, Pos.CENTER);
		TextField tf = new TextField();
		Platform.runLater(tf::requestFocus);
		pane.setTop(tf);
		BorderPane.setAlignment(tf, Pos.CENTER);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();
		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				listener.select(selectedItem);
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					listener.select(t);
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(listView.getItems()::add);
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				listener.select(listView.getItems().get(0));
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> optional = dialog.showAndWait();
		optional.ifPresent(o ->
		{
			if (o.getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(listener::select);
			}
		});
	}

	public static <T> T selectFromList(String title, T initValue, final List<T> list)
	{
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[]{initValue};

		if (list == null || list.isEmpty())
		{
			showInfo("Nothing to show");
			return result[0];
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		TextField tf = new TextField();
		pane.setTop(tf);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();

		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				result[0] = selectedItem;
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					result[0] = t;
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(t -> listView.getItems().add(t));
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				result[0] = listView.getItems().get(0);
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		Platform.runLater(tf::requestFocus);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> result[0] = t);
			}
			return result[0];
		}
		return result[0];
	}

	public static <T> T selectFromList(String title, T initValue, final List<T> list, Function<T,String> converter)
	{
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[]{initValue};

		if (list == null || list.isEmpty())
		{
			showInfo("Nothing to show");
			return result[0];
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		listView.setCellFactory(p -> new ListCell<T>(){
			@Override
			protected void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(converter.apply(item));
				}
				else
				{
					setText(null);
				}
			}
		});
		pane.setCenter(listView);
		TextField tf = new TextField();
		pane.setTop(tf);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();

		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				result[0] = selectedItem;
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					result[0] = t;
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> converter.apply(t).toUpperCase().contains(t1.toUpperCase())).forEach(t -> listView.getItems().add(t));
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				result[0] = listView.getItems().get(0);
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		Platform.runLater(tf::requestFocus);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> result[0] = t);
			}
			return result[0];
		}
		return result[0];
	}

	public static File showOpenSaveDialog(String title, String filter, String extension, OpenSaveMode mode)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		switch (mode)
		{
			case SaveFile:
				return fileChooser.showSaveDialog(Common.node);

			case OpenFile:
				return fileChooser.showOpenDialog(Common.node);

			default:
				throw new RuntimeException("Unsupported mode" + mode);
		}
	}

	public static List<File> showMultipleDialog(String title, String filter, String extension)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		return fileChooser.showOpenMultipleDialog(Common.node);
	}

	public static File showDirChooseDialog(String title)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(getCurrentDir()));
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showDirChooseDialog(String title, String initialDirectory)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		File value = new File(initialDirectory);
		if (!value.exists())
		{
			value = new File(getCurrentDir());
		}
		chooser.setInitialDirectory(value);
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showSaveAsDialog(Document doc) throws Exception
	{
		Class<?> docClass = doc.getClass();
		DocumentInfo annotation;
		while ((annotation = docClass.getAnnotation(DocumentInfo.class)) == null && docClass.getSuperclass() != null)
		{
			docClass = docClass.getSuperclass();
		}
		if (annotation == null)
		{
			throw new Exception("Unknown type of document: " + docClass);
		}
		String title = "Save " + docClass.getSimpleName().toLowerCase();
		String filter = annotation.extentioin() + " files(*." + annotation.extentioin() + ")";
		String extension = "*." + annotation.extentioin();
		String ext = "." + annotation.extentioin();
		File file = showOpenSaveDialog(title, filter, extension, OpenSaveMode.SaveFile);
		if (file != null)
		{
			String path = file.getPath();
			if (!path.endsWith(ext))
			{
				file = new File(path + ext);
			}
		}
		return file;
	}

    public static void showHelpDialog(Context context, String name, ReportBuilder report, MatrixItem help) throws Exception
    {
        report.reportStarted(null, "");
        help.execute(context, context.getMatrixListener(), context.getEvaluator(), report);
        report.reportFinished(0, 0, null, null);

        WebView browser = new WebView();
        WebEngine engine = browser.getEngine();
        String str = report.getContent();
        engine.loadContent(str);

        Dialog<?> dialog = new Alert(Alert.AlertType.INFORMATION);
        Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
        dialog.getDialogPane().setContent(browser);
        dialog.getDialogPane().setPrefWidth(1124);
        dialog.getDialogPane().setPrefHeight(768);
        dialog.setResizable(true);
        dialog.getDialogPane().setHeader(new Label());
        dialog.setTitle("Help for " + name);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
        dialog.show();
    }

	
	public static String showHelperDialog(String title, AbstractEvaluator evaluator, String value, Matrix matrix)
	{
		try
		{
			HelperFx helper = new HelperFx(title, evaluator, matrix);
			return helper.showAndWait(value);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			showError(e.getMessage());
		}
		return value;
	}

    public static Boolean showUserTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns)
    {
        Task<Boolean> task = new Task<Boolean>()
        {
            @Override
            protected Boolean call() throws Exception
            {
                UserEditTableDialog dialog = new UserEditTableDialog(title, table, columns);
                dialog.setTitle(title);
                dialog.getDialogPane().setHeader(null);
                Optional<Boolean> s = dialog.showAndWait();
                return s.orElse(false);
            }
        };

        final Boolean[] res = { Boolean.FALSE };
        task.setOnSucceeded(e -> res[0] = ((Boolean) e.getSource().getValue()));
        Platform.runLater(task);
        try
        {
            res[0] = task.get();
        }
        catch (Exception e)
        {
            task.cancel();
        }
        return res[0];
    }
	
	public static Object showUserInput(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, 
	        List<ReadableValue> dataSource, int timeout)
	{
		Task<Object> task = new Task<Object>()
		{
			@Override
			protected Object call() throws Exception
			{
				UserInputDialog dialog = new UserInputDialog(defaultValue, evaluator, helpKind, dataSource, timeout);
				dialog.setTitle(title);
				dialog.getDialogPane().setHeader(null);
				Optional<Object> s = dialog.showAndWait();
				return s.isPresent() ? s.get() : null;
			}
		};

		final Object[] res = { defaultValue };
		task.setOnSucceeded(e -> res[0] = e.getSource().getValue());
		Platform.runLater(task);
		try
		{
			res[0] = task.get();
		}
		catch (Exception e)
		{
			task.cancel();
		}
		return res[0];
	}

	public static void setTimeNotification(int timeNotification)
	{
		DialogsHelper.timeNotification = timeNotification;
	}

	public static void showError(final String message)
	{
		showNotifier(message, Notifier.Error);
	}

	public static void showSuccess(final String message)
	{
		showNotifier(message, Notifier.Success);
	}

	public static void showInfo(final String message)
	{
		showNotifier(message, Notifier.Info);
	}

	public static void showAboutProgram()
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle("About program");
		dialog.getDialogPane().setPrefWidth(600);
		dialog.getDialogPane().setPrefHeight(250);
		GridPane grid = new GridPane();
		grid.setVgap(0);
		grid.setHgap(8);
		Image img = new Image(CssVariables.Icons.LOGO_FISH);
		dialog.setResizable(true);
		String version = String.format("Version : %25s", VersionInfo.getVersion());
		String name = "JackFish";
		Text nameText = new Text(name);
		nameText.setFont(javafx.scene.text.Font.font(30));
		String copyright = version + "\n\n\nThis is unpublished, licensed software, confidential and proprietary information which is the\nproperty of Exactpro Systems, LLC or its licensors.\nQuality Assurance & Related Development for Innovative Trading Systems.\n\nCopyright (c) 2009-2017, Exactpro Systems, LLC. All rights reserved.";
		Text copyrightTxt = new Text(copyright);
		dialog.getDialogPane().setHeader(new Label());
		ImageView logo = new ImageView(img);
		grid.add(logo, 0, 0,1,2);
		grid.add(nameText, 1, 0);
		grid.add(copyrightTxt,1,1);
		dialog.getDialogPane().setContent(grid);
		GridPane.setValignment(logo, VPos.TOP);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.show();
	}

	public static void newShowActionsHelp(DocumentFactory factory)
	{
		try
		{
			Context context = factory.createContext();
			NewHelpBuilder report = (NewHelpBuilder) new NewHelpBuilderFactory().createReportBuilder(null, null, new Date());
			MatrixItem help = DocumentationBuilder.createHelp(report, context);
			report.reportStarted(null, VersionInfo.getVersion());
			help.execute(context, context.getMatrixListener(), context.getEvaluator(), report);
			report.reportFinished(0, 0, null, null);
			//report.printOut();
			displayHelp(report.getContent());
		}
		catch (Exception e)
		{
			String message = "Error on .\n" + e.getMessage();
			logger.error(message);
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(message);
		}
	}

	public static void displayReport(File file, String matrixName, DocumentFactory factory)
	{
		Settings.SettingsValue value = factory.getSettings().getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_EXTERNAL_REPORT_VIEWER, "false");
		boolean useExternalReportViewer = Boolean.parseBoolean(value.getValue());
		if (useExternalReportViewer)
		{
			if (Desktop.isDesktopSupported())
			{
				Common.openDefaultBrowser(file.getAbsolutePath());
				return;
			}
			else
			{
				DialogsHelper.showError("Desktop is not supported. Used internal report browser");
			}
		}
		Platform.runLater(() ->
		{
			final String[] matrName = {matrixName};
			tryCatch(() ->
			{
				Configuration configuration = factory.getConfiguration();
				boolean addButton = configuration != null;
				ReportBrowser reportBrowser = new ReportBrowser(factory.createContext(), file);
				Dialog<ButtonType> dialog = new Dialog<>();
				Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
				if (addButton)
				{
					dialog.getDialogPane().getButtonTypes().add(new ButtonType("Open", ButtonBar.ButtonData.OTHER));
				}
				dialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));
				dialog.setResizable(true);
				dialog.getDialogPane().setPrefWidth(1024);
				dialog.getDialogPane().setPrefHeight(768);
				dialog.getDialogPane().setContent(reportBrowser);
				dialog.initModality(Modality.NONE);
				dialog.setTitle("Report");
				if (matrName[0] == null)
				{
					Matcher matcher = Pattern.compile("\\d+_\\d+_(.+?)_(FAILED|PASSED|RUNNING)\\.html").matcher(file.getAbsolutePath());
					if (matcher.find())
					{
						matrName[0] = matcher.group(1);
					}
					else
					{
						matrName[0] = "Unknown matrix";
					}
				}
				dialog.setHeaderText("Report for " + matrName[0]);
				dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
				Optional<ButtonType> buttonType = dialog.showAndWait();
				buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OTHER)).ifPresent(type -> Common.tryCatch(() ->
				{
					String name = reportBrowser.getMatrix();
					if (name != null && !name.isEmpty())
					{
                        Context context = factory.createContext();
                        MatrixRunner runner = context.createRunner(matrName[0], null, new Date(), null);
						Matrix matrix = factory.createMatrix(matrName[0], runner);
						matrix.load(new StringReader(name));
						matrix.display();
					}
				}, "Error on open matrix from report"));
			}, "Error on show report");
		});
	}

	public static void showNotifier(final String message, final Notifier notifier)
	{
		Platform.runLater(() -> Notifications.create().msg(message).hideAfter(Duration.seconds(timeNotification)).state(notifier).title(notifier.name()).show());
	}

	public static Alert createGitDialog(String title, Parent parent)
	{
		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(Common.node);
		dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.setTitle(title);
		Label header = new Label();
		header.setMinHeight(0.0);
		header.setPrefHeight(0.0);
		header.setMaxHeight(0.0);
		dialog.getDialogPane().setHeader(header);
		dialog.getDialogPane().setContent(parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) dialog.getDialogPane().lookupButton(buttonCreate);
		Control p = ((Control) dialog.getDialogPane().lookup(".button-bar"));
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
		p.setPrefHeight(0.0);
		p.setMaxHeight(0.0);
		p.setMinHeight(0.0);
		p.setVisible(false);
		return dialog;
	}

	public static boolean showYesNoDialog(String message, String question)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setHeaderText(question);
		dialog.getDialogPane().setPrefWidth(1000);
		dialog.setContentText(message);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
		((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
		return dialog.showAndWait().filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).isPresent();
	}

	private static void displayHelp(String content)
	{
		WebView browser = new WebView();
		WebEngine engine = browser.getEngine();
		engine.loadContent(content);
		browser.setContextMenuEnabled(false);
		createCtrlCHandler(browser);
		createContextMenu(browser);

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(browser);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setHeader(new Label());
		dialog.setHeaderText("Help");
		GridPane grid = (GridPane) dialog.getDialogPane().lookup(".header-panel");
		grid.setStyle("-fx-font-size: 30;");
		dialog.setTitle("Actions help");
		dialog.setResizable(true);
		dialog.getDialogPane().setContent(borderPane);
		dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
		dialog.getDialogPane().setPrefWidth(1024);
		dialog.getDialogPane().setPrefHeight(768);
		dialog.getDialogPane().setPadding(new Insets(-28,-10,-59,-10));
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.initModality(Modality.NONE);
		dialog.show();
	}

	private static class Modifiers
	{
		private boolean ctrlC = false;
	}

	private static void createCtrlCHandler(WebView webView)
	{
		Modifiers modifiers = new Modifiers();
		webView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN).match(event))
			{
				modifiers.ctrlC = true;
			}
		});
		webView.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode().equals(KeyCode.CONTROL) && modifiers.ctrlC)
			{
				modifiers.ctrlC = false;
				Sys.copyToClipboard((String) webView.getEngine().executeScript("window.getSelection().toString()"));
			}
		});
	}

	private static void createContextMenu(WebView webView)
	{
		ContextMenu contextMenu = new ContextMenu();
		MenuItem copy = new MenuItem("Copy");
		copy.setOnAction(e -> Sys.copyToClipboard((String) webView.getEngine().executeScript("window.getSelection().toString()")));

		contextMenu.getItems().addAll(copy);

		webView.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				contextMenu.show(webView, e.getScreenX(), e.getScreenY());
			} else {
				contextMenu.hide();
			}
		});
	}

	private static String getCurrentDir()
	{
		return Paths.get("").toAbsolutePath().toString();
	}


	private static final Logger logger = Logger.getLogger(DialogsHelper.class);
}
